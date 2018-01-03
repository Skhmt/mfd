/**
 * Multi-Function Display
 *
 * This creates a SystemTray and a series of webservers.
 * Port 80 will be the management page.
 * Port 6331, 6332, 6333, and 6334 will be for setting up 4 separate MFDs
 *
 */

import org.json.JSONArray
import spark.kotlin.*

import java.awt.*
import java.awt.event.InputEvent
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.Inet4Address
import java.net.URI
import java.util.*
import javax.imageio.ImageIO
import org.json.JSONObject
import java.io.FileFilter

fun main(args: Array<String>) {
    val version = "0.3.0"
    val mfd = MFD(version, 80)
    mfd.tray()
    mfd.spark()
}

class MFD(ver: String, prt: Int) {
    private val robot: Robot = Robot()
    private val crypto: MFDCrypto = MFDCrypto()

    private val version: String = ver
    private var port = prt

    private var users = mutableMapOf<String, Long>()

    private var plainPassword = ""

    init {
        robot.isAutoWaitForIdle = true

        setCrypto(crypto.randomPassword(9))
    }

    private fun setCrypto(password: String) {
        crypto.salt = crypto.generateSalt()
        crypto.iterations = 10000 // default value is 10000
        crypto.key = crypto.generateKey(password)
        plainPassword = password
    }

    private fun getCrypto(): String {
        val salt = crypto.byteArrayToHex(crypto.salt)
        val iterations = crypto.iterations
        val json = JSONObject()
        json.put("salt", salt)
        json.put("iterations", iterations)
        return json.toString()
    }

    fun spark() {
        val http: Http = ignite()

        http.port(port)

        val displayFile = File("./displays/")
        if (!displayFile.exists()) displayFile.mkdirs()
        http.staticFiles.externalLocation("displays")

        http.get("/mfd") {
            val json = JSONObject()
            json.put("version", version)
            json.put("crypto", "/mfd/crypto")
            json.put("displays", "/mfd/displays")
            json.put("api", "/mfd/api")
            json.toString()
        }

        http.get("/mfd/crypto") {
            getCrypto()
        }

        http.get("/mfd/displays") {
            val directoryFiles = displayFile.listFiles(FileFilter { it.isDirectory })
            val directoryNames = directoryFiles.map{ it.name }
            val json = JSONObject()
            val jsonArr = JSONArray(directoryNames)
            json.put("displays", jsonArr)
            json.toString()
        }

        http.get("/mfd/api") {
            "See documentation. The API uses HTTP POST requests."
        }

        ///////////////////////////////////////////////////////////////////
        //                                                               //
        //                              API                              //
        //                                                               //
        ///////////////////////////////////////////////////////////////////

        //fetch("/mfd/app", {method: "POST", body: "..."})
        http.post("/mfd/api") {
            if (request.body().isEmpty()) {
                status(400)
                return@post "<strong>Error 400</strong> - Bad request"
            }

            val body = request.body().toString()
            val bodyArray = body.split(":")
            val jsonObj: JSONObject
            val ivHex = bodyArray[0]
            try {
                val iv = crypto.hexToByteArray(ivHex)
                val cipherText = crypto.hexToByteArray(bodyArray[1])
                val plainText = crypto.decrypt(iv, cipherText)

                jsonObj = JSONObject(plainText)
            }
            catch (e: javax.crypto.AEADBadTagException) {
                status(401)
                return@post "<strong>Error 401</strong> - Unauthorized"
            }

            val user: String = ivHex.substring(0,8)
            val time: Long = java.lang.Long.parseLong(ivHex.substring(8), 16)

            if ( users.containsKey(user) && time < users[user] as Long ) {
                println("replayed")
                status(401)
                return@post "<strong>Error 401</strong> - Unauthorized"
            }
            else { // new user or "time" is more recent than the latest time for that user
                users[user] = time
            }

            val action = jsonObj.get("action") as String
            val data = jsonObj.get("data") as String

            // handle action and data
            try {
                when (action) {
                    "keyon" -> {
                        val key: Int = k2e(data)
                        robot.keyPress(key)
                        status(204)
                    }
                    "keyoff" -> {
                        val key: Int = k2e(data)
                        robot.keyRelease(key)
                        status(204)
                    }
                    "typestring" -> {
                        val keyArray = data.toCharArray()
                        for (c in keyArray) {
                            if ("!@#$%^&*()_+{}|:\"<>?~".contains(c)) {
                                robot.keyPress(k2e("shift"))
                                val key: Int;
                                when (c) {
                                    '!' -> key = k2e("1")
                                    '@' -> key = k2e("2")
                                    '#' -> key = k2e("3")
                                    '$' -> key = k2e("4")
                                    '%' -> key = k2e("5")
                                    '^' -> key = k2e("6")
                                    '&' -> key = k2e("7")
                                    '*' -> key = k2e("8")
                                    '(' -> key = k2e("9")
                                    ')' -> key = k2e("0")
                                    '_' -> key = k2e("-")
                                    '+' -> key = k2e("=")
                                    '{' -> key = k2e("[")
                                    '}' -> key = k2e("]")
                                    '|' -> key = k2e("\\")
                                    ':' -> key = k2e(";")
                                    '"' -> key = k2e("'")
                                    '<' -> key = k2e(",")
                                    '>' -> key = k2e(".")
                                    '?' -> key = k2e("/")
                                    '~' -> key = k2e("`")
                                    else -> key = k2e(" ")
                                }
                                robot.keyPress(key)
                                robot.keyRelease(key)
                                robot.keyRelease(k2e("shift"))
                            }
                            else {
                                if (c.isUpperCase()) robot.keyPress(k2e("shift"))
                                val key = k2e(c.toLowerCase().toString())
                                robot.keyPress(key)
                                robot.keyRelease(key)
                                if (c.isUpperCase()) robot.keyRelease(k2e("shift"))
                            }
                        }
                        status(204)
                    }
                    "exec" -> {
                        Runtime.getRuntime().exec(data)
                        status(204)
                    }
                    "url" -> {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(URI(data))
                            status(204)
                        }
                        else {
                            println("Error: 'Desktop' isn't supported in your version of Java - can't open \"$data\"")
                            status(500)
                            "<strong>Error 500</strong> - Internal Server Error"
                        }
                    }
                    "mousemove" -> {
                        val pointList = data.split(',')
                        val x = pointList[0].toInt()
                        val y = pointList[1].toInt()
                        robot.mouseMove(x, y)
                        status(204)
                    }
                    "mouseon" -> {
                        val button: Int
                        when (data) {
                            "center" -> button = InputEvent.BUTTON2_DOWN_MASK
                            "right" -> button = InputEvent.BUTTON3_DOWN_MASK
                            else -> button = InputEvent.BUTTON1_DOWN_MASK
                        }
                        robot.mousePress(button)
                        status(204)
                    }
                    "mouseoff" -> {
                        val button: Int
                        when (data) {
                            "center" -> button = InputEvent.BUTTON2_DOWN_MASK
                            "right" -> button = InputEvent.BUTTON3_DOWN_MASK
                            else -> button = InputEvent.BUTTON1_DOWN_MASK
                        }

                        robot.mouseRelease(button)
                        status(204)

                    }
                    "mousewheel" -> {
                        val amt = data.toInt()
                        robot.mouseWheel(amt)
                        status(204)
                    }
                    "capture" -> {
                        val dataList = data.split(',')
                        val x = dataList[0].toInt()
                        val y = dataList[1].toInt()
                        val width = dataList[2].toInt()
                        val height = dataList[3].toInt()

                        val rect = Rectangle(x, y, width, height)
                        val bufferedImage = robot.createScreenCapture(rect)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        ImageIO.write(bufferedImage, "png", byteArrayOutputStream)
                        byteArrayOutputStream.flush()
                        response.type("image/png")
                        byteArrayOutputStream.toByteArray()
                    }
                    "pixel" -> {
                        val pointList = data.split(',')
                        val x = pointList[0].toInt()
                        val y = pointList[1].toInt()
                        val color = robot.getPixelColor(x, y)
                        "{ \"red\": ${color.red}, \"green\": ${color.green}, \"blue\": ${color.blue} }"
                    }
                    "mousepointer" -> {
                        val p = MouseInfo.getPointerInfo().location
                        val json = JSONObject()
                        json.put("x", p.x)
                        json.put("y", p.y)
                        json.toString()
                    }
                    "test" -> {
                        val json = JSONObject()
                        json.put("test", "OK")
                        json.toString()
                    }
                    "changepass" -> {
                        println("Password changed to $data")
                        setCrypto(data)
                        return@post getCrypto()
                    }
                    "version" -> {
                        val json = JSONObject()
                        json.put("version", version)
                        json.toString()
                    }
                    else -> {
                        status(404)
                        "<strong>Error 404</strong> - Endpoint not found"
                    }
                } // close when
            } catch (e: IllegalArgumentException) {
                println(e.printStackTrace())
                status(400)
                "<strong>Error 400</strong> - Bad request"
            }
        }
    }

    fun tray() {
        if (!SystemTray.isSupported()) {
            println("SystemTray not supported")
            return
        }

        // Image reading
        var icon: BufferedImage? = null
        try {
            val fileIcon = this.javaClass.getResource("icon/mfd.png")

            icon = ImageIO.read(fileIcon);
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Creating the vals we need
        val popup = PopupMenu()
        val trayIcon = TrayIcon(icon, "MFD $version")
        val tray = SystemTray.getSystemTray()

        // Create pop-up menu components
        val configItem = MenuItem("Config")
        val redditItem = MenuItem("/r/mfd")
        val exitItem = MenuItem("Exit")

        // Add components to pop-up menu
        popup.add(configItem)
        popup.add(redditItem)
        popup.addSeparator()
        popup.add(exitItem)

        trayIcon.setPopupMenu(popup)
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon)
        } catch (e: AWTException) {
            println("TrayIcon could not be added")
        }

        // Listeners
        redditItem.addActionListener {
            if (Desktop.isDesktopSupported()) {
                val uri = "https://reddit.com/r/mfd"
                Desktop.getDesktop().browse(URI(uri))
            }
        }
        configItem.addActionListener {
            if (Desktop.isDesktopSupported()) {
                val ip: ByteArray = Inet4Address.getLocalHost().address
                val oct1: Int = if (ip[0].toInt() < 0) ip[0].toInt() + 256 else ip[0].toInt()
                val oct2: Int = if (ip[1].toInt() < 0) ip[1].toInt() + 256 else ip[1].toInt()
                val oct3: Int = if (ip[2].toInt() < 0) ip[2].toInt() + 256 else ip[2].toInt()
                val oct4: Int = if (ip[3].toInt() < 0) ip[3].toInt() + 256 else ip[3].toInt()
                var urlQuery = "$oct1.$oct2.$oct3.$oct4"
                if (port != 80) urlQuery += ":$port"
                val uri = "http://localhost:$port/?ip=$urlQuery#$plainPassword"
                Desktop.getDesktop().browse(URI(uri))
            }
        }
        exitItem.addActionListener {
            tray.remove(trayIcon)
            System.exit(0)
        }

//        trayIcon.displayMessage("Multi-Function Display", version, TrayIcon.MessageType.NONE)
    }
}