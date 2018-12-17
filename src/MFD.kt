/**
 * Multi-Function Display
 *
 * This creates a SystemTray and a series of webservers.
 * Port 80 will be the management page.
 *
 */

import com.sun.jna.Platform
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import javazoom.jl.player.Player
import org.json.JSONArray
import java.awt.*
import java.awt.event.InputEvent
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.Inet4Address
import java.net.URI
import javax.imageio.ImageIO
import org.json.JSONObject
import java.awt.event.KeyEvent
import java.io.FileFilter
import spark.Spark.*


fun main(args: Array<String>) {

    val version = "1.6.0"

    var port = 80
    var verbose = false
    var key = ""
    var safeMode = false
    var setIP = ""

    for (i in 0 until args.size) {
        when (args[i]) {
            "-p" -> {
                if (args.size > i + 1) port = args[i+1].toInt()
            }
            "-v" -> {
                verbose = true
            }
            "-k" -> {
                if (args.size > i+1) key = args[i+1]
            }
            "-s" -> {
                safeMode = true
            }
            "-ip" -> {
                if (args.size > i+1) setIP = args[i+1]
            }
            else -> {}
        }
    }

    val mfd = MFD(version, port, verbose, key, safeMode, setIP)
    if (verbose) println("> Creating tray icon")
    mfd.tray()
    if (verbose) println("> Creating spark server")
    mfd.spark()
    if (verbose) println("> --- MFD initialized")
}

class MFD(
        private val version: String,
        private val port: Int,
        private val verbose: Boolean,
        private var plainPassword: String,
        private val safeMode: Boolean,
        private val setIP: String) {

    private val robot: Robot = Robot()
    private val crypto: MFDCrypto = MFDCrypto()
    private val os = getOS()

    private lateinit var mediaPlayer: Player // so the Player doesn't get GC'd and stop after 5 seconds of playing

    private var users = mutableMapOf<String, Long>()

    init {
        if (verbose) println("> OS detected as $os")

        robot.isAutoWaitForIdle = true
        if (plainPassword == "") {
            if (verbose) println("> Randomizing password")
            setCrypto(crypto.randomPassword(9))
        } else {
            if (verbose) println("> Setting password to: $plainPassword")
            setCrypto(plainPassword)
        }

        var runningString = "MFD $version running locally at http://localhost"
        if (port != 80) runningString += ":$port"

        println(runningString)
        println("Password: $plainPassword")
        print("IP Address for your secondary device to connect to: http://${getIPv4()}")
        if (port != 80) println(":$port") else println("")
    }

    private fun setCrypto(password: String) {
        if (verbose) println("> Generating salt")
        crypto.salt = crypto.generateSalt()
        crypto.iterations = 10000 // default value is 10000

        if (verbose) println("> Generating key")
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

    private fun getOS(): String {
        if (verbose) println("> os.name: $os")
        return when {
            Platform.isWindows() -> "Windows"
            Platform.isMac() -> "macOS"
            Platform.isLinux() -> "*nix"
            else -> "unknown"
        }
    }

    fun spark() {

        if (verbose) println("> Setting the port")
        port(port)
        initExceptionHandler{
            if (it is java.net.BindException) println("Error, port $port already in use.")
            else println(it)
            System.exit(0)
        }

        if (verbose) println("> Setting static files location")
        val displayFile = File("./displays/")
        if (!displayFile.exists()) displayFile.mkdirs()
        staticFiles.externalLocation("displays")

        if (verbose) print("> Checking for vJoy... ")
        var vj: VJoy? = null
        if (os == "Windows") {
            val dllName = "vJoyInterface.dll"
            var dllPath: String? = null
            try {
                dllPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                        "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{8E31F76F-74C3-47F1-9550-E041EEDC5FBB}_is1",
                        "DllX64Location")
                if (verbose) println("Found 64-bit vJoy")
            } catch(e: com.sun.jna.platform.win32.Win32Exception) {
                try {
                    dllPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                            "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{8E31F76F-74C3-47F1-9550-E041EEDC5FBB}_is1",
                            "DllX86Location")
                    if (verbose) println("Found 32-bit vJoy")
                } catch(f: com.sun.jna.platform.win32.Win32Exception) {
                    if (verbose) println("Not found")
                }
            }
            if (dllPath != null) vj = VJoy("$dllPath\\$dllName")
        } else {
            if (verbose) println("Not found - vJoy is Windows exclusive")
        }

        if (verbose) println("> Creating endpoints")

        get("/mfd") { req, res ->
            val json = JSONObject()
            json.put("version", version)
            json.put("crypto", "/mfd/crypto")
            json.put("displays", "/mfd/displays")
            json.put("api", "/mfd/api")

            println("${req.ip()}> Sending endpoint list")
            res.status(200)
            res.type("application/json")
            json.toString()
        }

        get("/mfd/crypto") { req, res ->
            println("${req.ip()}> Sending crypto information")

            res.status(200)
            res.type("application/json")
            getCrypto()
        }

        get("/mfd/displays") { req, res ->
            if (verbose) println("> Getting display list")
            val directoryFiles = displayFile.listFiles(FileFilter { it.isDirectory })
            val directoryNames = directoryFiles.map{ it.name }
            val json = JSONObject()
            val jsonArr = JSONArray(directoryNames)
            json.put("displays", jsonArr)

            println("${req.ip()}> Sending display list")
            res.status(200)
            res.type("application/json")
            json.toString()
        }

        get("/mfd/api") { _, res ->
            res.status(200)
            res.type("text/plain")
            "See documentation. The API uses HTTP POST requests."
        }

        ///////////////////////////////////////////////////////////////////
        //                                                               //
        //                              API                              //
        //                                                               //
        ///////////////////////////////////////////////////////////////////

        //fetch("/mfd/app", {method: "POST", body: "..."})
        post("/mfd/api") { req, res ->
            if (req.body().isEmpty()) {
                println("${req.ip()}> Request body empty")
                res.status(400)
                res.type("text/html")
                return@post "<strong>Error 400</strong> - Body empty"
            }

            val body = req.body().toString()
            val bodyArray = body.split(":")
            val jsonObj: JSONObject
            val ivHex = bodyArray[0]
            try {
                if (verbose) print("${req.ip()}> Checking crypto... ")
                val iv = crypto.hexToByteArray(ivHex)
                val cipherText = crypto.hexToByteArray(bodyArray[1])
                val plainText = crypto.decrypt(iv, cipherText)

                jsonObj = JSONObject(plainText)
                if (verbose) println("OK")
            }
            catch (e: javax.crypto.AEADBadTagException) {
                if (verbose) println("BAD")
                else println("${req.ip()}> Bad or unrecognized crypto")

                res.status(401)
                res.type("text/html")
                return@post "<strong>Error 401</strong> - Bad or unrecognized crypto"
            }

            val user: String = ivHex.substring(0,8)
            val time: Long = java.lang.Long.parseLong(ivHex.substring(8), 16)

            if ( users.containsKey(user) && time < users[user] as Long ) {
                println("${req.ip()}> Replayed or timestamp expired data")
                res.status(401)
                res.type("text/html")
                return@post "<strong>Error 401</strong> - Timestamp expired"
            }
            else { // new user or "time" is more recent than the latest time for that user
                users[user] = time
            }

            val action = jsonObj.get("action") as String
            val data = jsonObj.get("data") as String

            // error handling if vjoy not enabled
            fun vjError(): String {
                println("${req.ip()}> vJoy not enabled")
                res.status(501)
                res.type("text/html")
                return "<strong>Error 501</strong> - vJoy not enabled on this server"
            }

            // handle action and data
            try {
                when (action) {
                    "keyon" -> {
                        try {
                            val key: Int = k2e(data)
                            if (safeMode && (key == KeyEvent.VK_WINDOWS || key == KeyEvent.VK_META)) {
                                println("${req.ip()}> Blocked by SafeMode: keyon:$data")
                                res.status(403)
                            }
                            else {
                                robot.keyPress(key)
                                println("${req.ip()}> keyon:$data")
                                res.status(204)
                            }

                        } catch (e: IllegalArgumentException) {
                            
                        }
                    }
                    "keyoff" -> {
                        val key: Int = k2e(data)
                        robot.keyRelease(key)
                        println("${req.ip()}> keyoff:$data")
                        res.status(204)
                    }
                    "typestring" -> {
                        val keyArray = data.toCharArray()
                        for (c in keyArray) {
                            if ("!@#$%^&*()_+{}|:\"<>?~".contains(c)) {
                                robot.keyPress(k2e("shift"))
                                val key: Int
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
                        println("${req.ip()}> typestring:$data")
                        res.status(204)
                    }
                    "exec" -> {
                        try {
                            if (!safeMode) {
                                Runtime.getRuntime().exec(data)
                                println("${req.ip()}> exec:$data")
                                res.status(204)
                            }
                            else {
                                println("${req.ip()}> Blocked by SafeMode: exec:$data")
                                res.status(403)
                            }
                        }
                        catch (e: java.io.IOException) {
                            println(e.message)
                            res.status(400)
                            res.type("text/html")
                            return@post "<strong>Error 400</strong> - Exec: " + e.message
                        }
                    }
                    "url" -> {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                if (!safeMode) {
                                    Desktop.getDesktop().browse(URI(data))
                                    println("${req.ip()}> url:$data")
                                    res.status(204)
                                }
                                else {
                                    println("${req.ip()}> Blocked by SafeMode: url:$data")
                                    res.status(403)
                                }
                            } catch (e: java.lang.UnsupportedOperationException) {
                                println("${req.ip()}> Failed to visit $data. This is a known problem on Linux.")
                                res.status(400)
                                res.type("text/html")
                                return@post "<strong>Error 400</strong> - URL: " + e.message
                            } catch (e: java.io.IOException) {
                                println("""${req.ip()}> Failed to visit $data. This could be caused by omitting "http:\\" or "https:\\" on macOS.""")
                                res.status(400)
                                res.type("text/html")
                                return@post "<strong>Error 400</strong> - URL: " + e.message
                            }
                        }
                        else {
                            println("${req.ip()}> Error: 'Desktop' isn't supported in your version of Java - can't open \"$data\"")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> - 'Desktop' isn't supported in your version of Java"
                        }
                    }
                    "mousemove" -> {
                        val pointList = data.split(',')
                        val x = pointList[0].toInt()
                        val y = pointList[1].toInt()
                        robot.mouseMove(x, y)
                        println("${req.ip()}> mousemove:$x,$y")
                        res.status(204)
                    }
                    "mouseon" -> {
                        val button: Int = when (data) {
                            "center" -> InputEvent.BUTTON2_DOWN_MASK
                            "right" -> InputEvent.BUTTON3_DOWN_MASK
                            else -> InputEvent.BUTTON1_DOWN_MASK
                        }
                        robot.mousePress(button)
                        println("${req.ip()}> mouseon:$data")
                        res.status(204)
                    }
                    "mouseoff" -> {
                        val button: Int = when (data) {
                            "center" -> InputEvent.BUTTON2_DOWN_MASK
                            "right" -> InputEvent.BUTTON3_DOWN_MASK
                            else -> InputEvent.BUTTON1_DOWN_MASK
                        }

                        robot.mouseRelease(button)
                        println("${req.ip()}> mouseoff:$data")
                        res.status(204)

                    }
                    "mousewheel" -> {
                        val amt = data.toInt()
                        robot.mouseWheel(amt)
                        println("${req.ip()}> mousewheel:$data")
                        res.status(204)
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

                        println("${req.ip()}> screenshot:$x,$y,$width,$height")
                        res.status(200)
                        res.type("image/png")
                        byteArrayOutputStream.toByteArray()
                    }
                    "pixel" -> {
                        val pointList = data.split(',')
                        val x = pointList[0].toInt()
                        val y = pointList[1].toInt()
                        val color = robot.getPixelColor(x, y)

                        val json = JSONObject()
                        json.put("red", color.red)
                        json.put("green", color.green)
                        json.put("blue", color.blue)

                        println("${req.ip()}> pixel:$x,$y color:${color.red},${color.green},${color.blue}")
                        res.status(200)
                        res.type("application/json")
                        json.toString()
                    }
                    "mousepointer" -> {
                        val p = MouseInfo.getPointerInfo().location
                        val json = JSONObject()
                        json.put("x", p.x)
                        json.put("y", p.y)

                        println("${req.ip()}> mousepointer:${p.x},${p.y}")
                        res.status(200)
                        res.type("application/json")
                        json.toString()
                    }
                    "test" -> {
                        val json = JSONObject()
                        json.put("test", "OK")

                        res.status(200)
                        res.type("application/json")
                        json.toString()
                    }
                    "changepass" -> {
                        if (!safeMode) {
                            setCrypto(data)

                            println("${req.ip()}> Password change:$data")
                            res.status(200)
                            res.type("application/json")
                            return@post getCrypto()
                        }
                        else {
                            println("${req.ip()}> Blocked by SafeMode: Password change:$data")
                            res.status(403)
                        }
                    }
                    "version" -> {
                        val json = JSONObject()
                        json.put("version", version)

                        println("${req.ip()}> version:$version")
                        res.status(200)
                        res.type("application/json")
                        json.toString()
                    }
                    "focusedwindowtitle" -> {
                        val display = getDisplay()
                        val title = display.getTitle()

                        val json = JSONObject()
                        json.put("focusedWindowTitle", title)

                        println("${req.ip()}> focusedWindowTitle:$title")
                        res.status(200)
                        res.type("application/json")
                        json.toString()
                    }
                    "playmp3" -> {
                        try {
                            mediaPlayer.close()
                        } catch (e: Exception) {
                        }

                        Thread{
                            val fileStream = File(data).inputStream()
                            mediaPlayer = Player(fileStream)
                            mediaPlayer.play()
                        }.start()

                        println("${req.ip()}> playmp3:$data")
                        res.status(204)

                    }
                    "stopmp3" -> {
                        try {
                            mediaPlayer.close()
                        } catch (e: Exception) {
                        }
                        println("${req.ip()}> stopmp3")
                        res.status(204)
                    }
                    "vj_info" -> {
                        if (vj == null) return@post vjError()

                        val json = JSONObject()
                        json.put("enabled", vj.vJoyEnabled())
                        json.put("manufacturer", vj.getvJoyManufacturerString())
                        json.put("product", vj.getvJoyProductString())
                        json.put("serialnumber", vj.getvJoySerialNumberString())
                        json.put("version", vj.getvJoyVersion())
                        json.put("maxDevices", vj.getvJoyMaxDevices())
                        json.put("existingDevices", vj.getNumberExistingVJD())

                        println("${req.ip()}> vj_manufacturer")
                        res.status(200)
                        res.type("application/json")
                        json.toString()
                    }
//                    "vj_" -> {
//                        if (vj == null) return@post vjError(request, response)
//
//                        println("${req.ip()}> vj_")
//                        status(204)
//                    }
                    "vj_vjd_info" -> {
                        if (vj == null) return@post vjError()

                        val rID = data.toInt()
                        if (!vj.isVJDExists(rID)) {
                            println("${req.ip()}> requested non-existent VJD: $rID")
                            res.status(404)
                            res.type("text/html")
                            return@post "<strong>Error 404</strong> Requested VJD does not exist"
                        }

                        val json = JSONObject()
                        json.put("status", vj.getVJDStatus(rID))
                        json.put("btnNumber", vj.getVJDButtonNumber(rID))
                        json.put("discPovNumber", vj.getVJDDiscPovNumber(rID))
                        json.put("contPovNumber", vj.getVJDContPovNumber(rID))
                        json.put("ownerPid", vj.getOwnerPid(rID))
                        arrayOf("x", "y", "z", "rx", "ry", "rz", "sl0", "sl1", "whl", "pov").forEach{
                            if (vj.getVJDAxisExist(rID, it)) {
                                val max = vj.getVJDAxisMax(rID, it)
                                json.put("${it}_max", max)
                            }
                        }

                        println("${req.ip()}> vj_vjd_info: $rID")
                        res.status(200)
                        res.type("application/json")
                        json.toString()
                    }
                    "vj_vjd_acquire" -> {
                        if (vj == null) return@post vjError()

                        val rID = data.toInt()
                        if (!vj.isVJDExists(rID)) {
                            println("${req.ip()}> requested non-existent vjd: $rID")
                            res.status(404)
                            res.type("text/html")
                            return@post "<strong>Error 404</strong> Requested VJD does not exist"
                        }

                        val status = vj.getVJDStatus(rID)
                        if (status == "VJD_STAT_OWN" || status == "VJD_STAT_FREE") {
                            if (vj.acquireVJD(rID)) {
                                println("${req.ip()}> vj_vjd_acquire: $rID")
                                res.status(204)
                            } else {
                                println("${req.ip()}> failed to acquire VJD: $rID")
                                res.status(500)
                                res.type("text/html")
                                return@post "<strong>Error 500</strong> Failed to acquire VJD"
                            }
                        } else {
                            println("${req.ip()}> requested unavailable VJD: $rID")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> Requested unavailable VJD"
                        }
                    }
                    "vj_vjd_relinquish" -> {
                        if (vj == null) return@post vjError()

                        val rID = data.toInt()
                        vj.relinquishVJD(rID)

                        println("${req.ip()}> vj_vjd_relinquish: $rID")
                        res.status(204)
                    }
                    "vj_vjd_setaxis" -> {
                        if (vj == null) return@post vjError()

                        val params = data.split(',')
                        val rID = params[0].toInt()
                        val axis = params[1]
                        val value = params[2].toLong()

                        val status = vj.getVJDStatus(rID)
                        if (status == "VJD_STAT_OWN") {
                            if (vj.setAxis(value, rID, axis)) {
                                println("${req.ip()}> vj_vjd_setaxis: $rID, $axis, $value")
                                res.status(204)
                            } else {
                                println("${req.ip()}> failed to set axis: $rID, $axis, $value")
                                res.status(500)
                                res.type("text/html")
                                return@post "<strong>Error 500</strong> Failed to set axis"
                            }
                        } else {
                            println("${req.ip()}> requested unavailable VJD: $rID")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> Requested unavailable VJD"
                        }
                    }
                    "vj_vjd_setbtn" -> {
                        if (vj == null) return@post vjError()

                        val params = data.split(',')
                        val rID = params[0].toInt()
                        val btn = params[1].toShort()
                        val value = params[2].toBoolean()

                        val status = vj.getVJDStatus(rID)
                        if (status == "VJD_STAT_OWN") {
                            if (vj.setBtn(value, rID, btn)) {
                                println("${req.ip()}> vj_vjd_setbtn: $rID, $btn, $value")
                                res.status(204)
                            } else {
                                println("${req.ip()}> failed to set btn: $rID, $btn, $value")
                                res.status(500)
                                res.type("text/html")
                                return@post "<strong>Error 500</strong> Failed to set btn"
                            }
                        } else {
                            println("${req.ip()}> requested unavailable VJD: $rID")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> Requested unavailable VJD"
                        }
                    }
                    "vj_vjd_setdiscpov" -> {
                        if (vj == null) return@post vjError()

                        val params = data.split(',')
                        val rID = params[0].toInt()
                        val nPov = params[1].toShort()
                        val value = params[2].toInt()

                        val status = vj.getVJDStatus(rID)
                        if (status == "VJD_STAT_OWN") {
                            if (vj.setDiscPov(value, rID, nPov)) {
                                println("${req.ip()}> vj_vjd_setdiscpov: $rID, $nPov, $value")
                                res.status(204)
                            } else {
                                println("${req.ip()}> failed to set discrete pov: $rID, $nPov, $value")
                                res.status(500)
                                res.type("text/html")
                                return@post "<strong>Error 500</strong> Failed to set discrete pov"
                            }
                        } else {
                            println("${req.ip()}> requested unavailable VJD: $rID")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> Requested unavailable VJD"
                        }
                    }
                    "vj_vjd_setcontpov" -> {
                        if (vj == null) return@post vjError()

                        val params = data.split(',')
                        val rID = params[0].toInt()
                        val nPov = params[1].toShort()
                        val value = params[2].toInt()

                        val status = vj.getVJDStatus(rID)
                        if (status == "VJD_STAT_OWN") {
                            if (vj.setContPov(value, rID, nPov)) {
                                println("${req.ip()}> vj_vjd_setcontpov: $rID, $nPov, $value")
                                res.status(204)
                            } else {
                                println("${req.ip()}> failed to set continuous pov: $rID, $nPov, $value")
                                res.status(500)
                                res.type("text/html")
                                return@post "<strong>Error 500</strong> Failed to set continuous pov"
                            }
                        } else {
                            println("${req.ip()}> requested unavailable VJD: $rID")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> Requested unavailable VJD"
                        }
                    }
                    "vj_vjd_reset" -> {
                        if (vj == null) return@post vjError()

                        val rID = data.toInt()
                        if (vj.resetVJD(rID)) {
                            println("${req.ip()}> vj_vjd_relinquish: $rID")
                            res.status(204)
                        } else {
                            println("${req.ip()}> failed to reset VJD: $rID")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> Failed to reset VJD"
                        }
                    }
                    "vj_vjd_resetbtns" -> {
                        if (vj == null) return@post vjError()

                        val rID = data.toInt()
                        if (vj.resetButtons(rID)) {
                            println("${req.ip()}> vj_vjd_resetbtns: $rID")
                            res.status(204)
                        } else {
                            println("${req.ip()}> failed to reset VJD buttons: $rID")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> Failed to reset VJD buttons"
                        }
                    }
                    "vj_vjd_resetpovs" -> {
                        if (vj == null) return@post vjError()

                        val rID = data.toInt()
                        if (vj.resetPovs(rID)) {
                            println("${req.ip()}> vj_vjd_resetpovs: $rID")
                            res.status(204)
                        } else {
                            println("${req.ip()}> failed to reset VJD povs: $rID")
                            res.status(500)
                            res.type("text/html")
                            return@post "<strong>Error 500</strong> Failed to reset VJD"
                        }
                    }
                    "vj_resetall" -> {
                        if (vj == null) return@post vjError()

                        vj.resetAll()
                        println("${req.ip()}> vj_resetall")
                        res.status(204)
                    }
                    else -> {
                        println("${req.ip()}> Endpoint not found")
                        res.status(404)
                        res.type("text/html")
                        "<strong>Error 404</strong> - Endpoint not found"
                    }
                } // close when
            } catch (e: IllegalArgumentException) {
                println(e.printStackTrace())
                res.status(400)
                res.type("text/html")
                return@post "<strong>Error 400</strong> - Bad request"
            }
        }

    }

    fun tray() {
        if (!SystemTray.isSupported()) {
            println("SystemTray not supported")
            return
        }

        if (verbose) println("> Reading mfd.png")
        // Image reading
        var icon: BufferedImage? = null
        try {
            val fileIcon = this.javaClass.getResource("icon/mfd.png")

            icon = ImageIO.read(fileIcon)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (verbose) println("> Creating popup menu")
        // Creating the values we need
        val popup = PopupMenu()
        val trayIcon = TrayIcon(icon, "MFD $version")
        val tray = SystemTray.getSystemTray()

        if (verbose) println("> Creating menuItems")
        // Create pop-up menu components
        val configItem = MenuItem("Config")
        val redditItem = MenuItem("/r/mfd")
        val githubItem = MenuItem("GitHub repo")
        val exitItem = MenuItem("Exit")

        if (verbose) println("> Adding components to pop-up menu")
        // Add components to pop-up menu
        popup.add(configItem)
        popup.addSeparator()
        popup.add(redditItem)
        popup.add(githubItem)
        popup.addSeparator()
        popup.add(exitItem)

        trayIcon.popupMenu = popup
        trayIcon.isImageAutoSize = true
        try {
            tray.add(trayIcon)
        } catch (e: AWTException) {
            println("TrayIcon could not be added")
        }

        // Listeners
        redditItem.addActionListener {
            if (Desktop.isDesktopSupported()) {
                val uri = "https://reddit.com/r/mfd"
                try {
                    Desktop.getDesktop().browse(URI(uri))
                } catch (e: java.lang.UnsupportedOperationException) {
                    println("Please visit the following URL:")
                    println(uri)
                }
            }
        }
        githubItem.addActionListener {
            if (Desktop.isDesktopSupported()) {
                val uri = "https://github.com/Skhmt/mfd"
                try {
                    Desktop.getDesktop().browse(URI(uri))
                } catch (e: java.lang.UnsupportedOperationException) {
                    println("Please visit the following URL:")
                    println(uri)
                }
            }
        }
        configItem.addActionListener {
            if (Desktop.isDesktopSupported()) {
                var urlQuery: String
                if (setIP == "") {
                    urlQuery = getIPv4()
                    if (port != 80) urlQuery += ":$port"
                }
                else {
                    urlQuery = setIP
                }

                val uri = "http://localhost:$port/?ip=$urlQuery#.$plainPassword"

                println("Please visit the following URL:")
                println(uri)
                try {
                    Desktop.getDesktop().browse(URI(uri))
                } catch (e: java.lang.UnsupportedOperationException) {
                    // Computer doesn't support the BROWSE operation - likely on Linux
                }
            }
        }
        exitItem.addActionListener {
            tray.remove(trayIcon)
            System.exit(0)
        }

        if (verbose) println("> Displaying version message")
        trayIcon.displayMessage("Multi-Function Display", "MFD $version is running", TrayIcon.MessageType.NONE)
    }

    private fun getIPv4(): String {
        val ip: ByteArray = Inet4Address.getLocalHost().address
        val oct1: Int = if (ip[0].toInt() < 0) ip[0].toInt() + 256 else ip[0].toInt()
        val oct2: Int = if (ip[1].toInt() < 0) ip[1].toInt() + 256 else ip[1].toInt()
        val oct3: Int = if (ip[2].toInt() < 0) ip[2].toInt() + 256 else ip[2].toInt()
        val oct4: Int = if (ip[3].toInt() < 0) ip[3].toInt() + 256 else ip[3].toInt()
        return "$oct1.$oct2.$oct3.$oct4"
    }
}