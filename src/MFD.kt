/**
 * Multi-Function Display
 *
 * This creates a SystemTray and a series of webservers.
 * Port 80 will be the management page.
 * Port 6331, 6332, 6333, and 6334 will be for setting up 4 separate MFDs
 *
 */

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

fun main(args: Array<String>) {
    val mfd = MFD()
    mfd.tray()
    mfd.spark()
}

class MFD {

    val robot: Robot
    val crypto: MFDCrypto

    var port: Int
    var delay: Int // in milliseconds

    val plainTextPassword: String

    init {
        port = 80
        delay = 17

        robot = Robot()
        robot.isAutoWaitForIdle = true

        crypto = MFDCrypto()
        crypto.salt = crypto.generateSalt()
        crypto.iterations = 10000 // default value is 10000
        plainTextPassword = crypto.randomPassword(9)
        crypto.key = crypto.generateKey(plainTextPassword)
//        crypto.key = crypto.generateKey("password") // for testing
    }

    fun spark() {
        val http: Http = ignite()

        http.port(port)

        val displayFile = File("./displays/")
        if (!displayFile.exists()) displayFile.mkdirs()
        http.staticFiles.externalLocation("displays")

        // queryString: [32 char iv][n caracter cipherText]
        fun isValid(queryString: String?): Boolean {
            try {
                if (queryString == null) return false
                val queryArray = queryString.split(":")
                val iv = crypto.hexToByteArray(queryArray[0])
                val cipherText = crypto.hexToByteArray(queryArray[1])
                val plainText = crypto.decrypt(iv, cipherText).toLong()
                val currentTime = Date().time
                return (currentTime - plainText in -500..500)
            }
            catch (e: javax.crypto.AEADBadTagException) {
                return false
            }
        }

        http.get("/mfd") {
            """
                {
                    "crypto": "/mfd/crypto",
                    "commands": "/mfd/cmd"
                }
            """.trimIndent()
        }

        http.get("/mfd/crypto") {
            val salt = crypto.byteArrayToHex(crypto.salt)
            val iterations = crypto.iterations
            """{"salt":"$salt","iterations":$iterations}"""
        }

        http.get("/mfd/test") {
            if (isValid(request.queryParams("token"))) {
                status(204)
            }
            else {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
        }

        ///////////////////////////////////////////////////////////////////
        //                                                               //
        //                              API                              //
        //                                                               //
        ///////////////////////////////////////////////////////////////////

        //fetch("/mfd/cmd", {method: "POST", body: ""})
        http.post("/mfd/cmd") {
            val body = request.body().toString()
            if (body == "") {
                status(400)
                return@post "<strong>Error 400</strong> - Bad request"
            }

            val bodyArray = body.split(":")
            val jsonObj: JSONObject

            try {
                val iv = crypto.hexToByteArray(bodyArray[0])
                val cipherText = crypto.hexToByteArray(bodyArray[1])
                val plainText = crypto.decrypt(iv, cipherText)

                jsonObj = JSONObject(plainText)
            }
            catch (e: javax.crypto.AEADBadTagException) {
                status(401)
                return@post "<strong>Error 401</strong> - Unauthorized"
            }

            val time = jsonObj.get("time") as Long
            val action = jsonObj.get("action") as String
            val data = jsonObj.get("data") as String

            val currentTime = Date().time
            if ( !(currentTime - time in -500..500) ) {
                status(401)
                return@post "<strong>Error 401</strong> - Unauthorized"
            }

            // handle action and data

            status(204)
        }

        // fetch("/mfd/keytap", {method: "POST", body: "a"})
        http.post("/mfd/keytap") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val key = k2e(request.body().toString())
                try {
                    robot.delay(delay)
                    robot.keyPress(key)
                    robot.delay(delay)
                    robot.keyRelease(key)
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        // fetch("/mfd/keyon", {method: "POST", body: "shift"})
        http.post("/mfd/keyon") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val key: Int = k2e(request.body().toString())
                try {
                    robot.keyPress(key)
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        // fetch("/mfd/keyoff", {method: "POST", body: "shift"})
        http.post("/mfd/keyoff") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val key: Int = k2e(request.body().toString())
                try {
                    robot.keyRelease(key)
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        // fetch("/mfd/typestring", {method: "POST", body: "hello world"})
        http.post("/mfd/typestring") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val keyArray = request.body().toCharArray()
                try {
                    for (c in keyArray) {
                        val key = k2e(c.toString())
                        robot.delay(delay)
                        robot.keyPress(key)
                        robot.delay(delay)
                        robot.keyRelease(key)
                    }
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        // fetch("/mfd/delay", {method: "POST", body: "5"})
        http.post("/mfd/delay/:delay") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                delay = request.params("delay").toInt()
                status(204)
            }
        }

        http.get("/mfd/delay/") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                "{ \"delay\": $delay }"
            }
        }

        // fetch("/mfd/exec", {method: "POST", body: "notepad.exe"})
        http.post("/mfd/exec") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                try {
                    Runtime.getRuntime().exec( request.body().toString() )
                    status(204)
                } catch (e: Exception) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        http.post("/mfd/url") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                try {
                    val uri = request.body().toString()
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(uri))
                    else println("Error: Desktop isn't supported - can't open \"$uri\"")
                } catch (e: Exception) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        // fetch("/mfd/pixel/50/100")
        http.get("/mfd/pixel/:x/:y") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                try {
                    val color = robot.getPixelColor(request.params("x").toInt(), request.params("y").toInt())
                    "{ \"red\": ${color.red}, \"green\": ${color.green}, \"blue\": ${color.blue} }"
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        // fetch("/mfd/move/50/100", {method: "POST"})
        http.post("/mfd/move/:x/:y") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                try {
                    robot.mouseMove(request.params("x").toInt(), request.params("y").toInt())
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        http.post("/mfd/click/:button") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val button: Int
                when (request.params("button")) {
                    "center" -> button = InputEvent.BUTTON2_DOWN_MASK
                    "right" -> button = InputEvent.BUTTON3_DOWN_MASK
                    else -> button = InputEvent.BUTTON1_DOWN_MASK
                }
                try {
                    robot.delay(delay)
                    robot.mousePress(button)
                    robot.delay(delay)
                    robot.mouseRelease(button)
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        http.post("/mfd/mouseon/:button") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val button: Int
                when (request.params("button")) {
                    "center" -> button = InputEvent.BUTTON2_DOWN_MASK
                    "right" -> button = InputEvent.BUTTON3_DOWN_MASK
                    else -> button = InputEvent.BUTTON1_DOWN_MASK
                }
                try {
                    robot.mousePress(button)
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        http.post("/mfd/mouseoff/:button") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val button: Int
                when (request.params("button")) {
                    "center" -> button = InputEvent.BUTTON2_DOWN_MASK
                    "right" -> button = InputEvent.BUTTON3_DOWN_MASK
                    else -> button = InputEvent.BUTTON1_DOWN_MASK
                }
                try {
                    robot.mouseRelease(button)
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        // fetch("/mfd/wheel", {method: "POST", body: "100"}) negative is up/away from user, positive is down/toward user
        http.post("/mfd/wheel/:clicks") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val amt = request.params("clicks").toInt()
                try {
                    robot.mouseWheel(amt)
                    status(204)
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
            }
        }

        http.get("/mfd/capture/:x/:y/:width/:height") {
            if (!isValid(request.queryParams("token"))) {
                status(401)
                "<strong>Error 401</strong> - Unauthorized"
            }
            else {
                val x = request.params("x").toInt()
                val y = request.params("y").toInt()
                val width = request.params("width").toInt()
                val height = request.params("height").toInt()
                try {
                    val rect = Rectangle(x, y, width, height)
                    val bufferedImage = robot.createScreenCapture(rect)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    ImageIO.write(bufferedImage, "png", byteArrayOutputStream)
                    byteArrayOutputStream.flush()
                    response.type("image/png")
                    byteArrayOutputStream.toByteArray()
                } catch (e: IllegalArgumentException) {
                    status(400)
                    "<strong>Error 400</strong> - Bad request"
                }
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
        val trayIcon = TrayIcon(icon, "MFD")
        val tray = SystemTray.getSystemTray()

        // Create pop-up menu components
        val configItem = MenuItem("Config")
        val aboutItem = MenuItem("Help")
        val exitItem = MenuItem("Exit")

        // Add components to pop-up menu
        popup.add(configItem)
        popup.add(aboutItem)
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
        aboutItem.addActionListener {
            if (Desktop.isDesktopSupported()) {
                val uri = "https://github.com"
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
                val uri = "http://localhost:$port/?ip=$urlQuery&pass=$plainTextPassword"
                Desktop.getDesktop().browse(URI(uri))
            }
        }
//        trayIcon.displayMessage("caption", "error", TrayIcon.MessageType.ERROR)
//        trayIcon.displayMessage("caption", "warn", TrayIcon.MessageType.WARNING)
//        trayIcon.displayMessage("caption", "info", TrayIcon.MessageType.INFO)
//        trayIcon.displayMessage("caption", "none", TrayIcon.MessageType.NONE)
        exitItem.addActionListener {
            tray.remove(trayIcon)
            System.exit(0)
        }

    }
}