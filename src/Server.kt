
import com.profesorfalken.jsensors.JSensors
import com.profesorfalken.jsensors.model.components.Cpu
import com.profesorfalken.jsensors.model.components.Gpu
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.json.JSONArray
import org.json.JSONObject
import spark.Spark
import spark.Spark.post
import java.awt.Desktop
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.io.*
import java.lang.UnsupportedOperationException
import java.net.BindException
import java.net.URI
import java.util.logging.Level
import java.util.logging.Logger
import javax.crypto.AEADBadTagException
import javax.imageio.ImageIO

fun server(state: MFDState) {

    val robot = Robot()
    val mediaPlayer = MP3Clip() // so the mp3 Player doesn't get GC'd and stop after 5 seconds of playing
    val users = mutableMapOf<String, Long>()

    // disable logging for GlobalScreen - used for media keys
    val logger = Logger.getLogger(GlobalScreen::class.java.getPackage().name)
    logger.level = Level.OFF // Level.ALL shows everything, Level.WARNING shows only warnings
    logger.useParentHandlers = false

    if (state.verbose) println("> OS detected as ${state.os}")

    robot.isAutoWaitForIdle = true
    if (state.key == "") {
        if (state.verbose) println("> Randomizing password")
        state.key = state.crypto.randomPassword(9)
    } else {
        if (state.verbose) println("> Setting password to: ${state.key}")
    }
    setCrypto(state)

    var runningString = "MFD ${state.version} running locally at http://localhost"
    if (state.port != 80) runningString += ":${state.port}"

    println(runningString)
    println("Password: ${state.key}")
    print("IP Address for your secondary device to connect to: http://${getIPv4()}")
    if (state.port != 80) println(":${state.port}") else println("")


    if (state.verbose) println("> Setting the port")
    Spark.port(state.port)
    Spark.initExceptionHandler {
        if (it is BindException) println("Error, port ${state.port} already in use.")
        else println(it)
        System.exit(0)
    }

    if (state.verbose) println("> Setting static files location")
    val displayFile = File("./displays/")
    if (!displayFile.exists()) displayFile.mkdirs()
    Spark.staticFiles.externalLocation("displays")

    if (state.verbose) print("> Checking for vJoy... ")
    var vj: VJoy? = null
    if (state.os == "Windows") {
        val dllName = "vJoyInterface.dll"
        var dllPath: String? = null
        try {
            dllPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{8E31F76F-74C3-47F1-9550-E041EEDC5FBB}_is1",
                    "DllX64Location")
            if (state.verbose) println("Found 64-bit vJoy")
        } catch (e: com.sun.jna.platform.win32.Win32Exception) {
            try {
                dllPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                        "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{8E31F76F-74C3-47F1-9550-E041EEDC5FBB}_is1",
                        "DllX86Location")
                if (state.verbose) println("Found 32-bit vJoy")
            } catch (f: com.sun.jna.platform.win32.Win32Exception) {
                if (state.verbose) println("Not found")
            }
        }
        if (dllPath != null) vj = VJoy("$dllPath\\$dllName")
    } else {
        if (state.verbose) println("Not found - vJoy is Windows exclusive")
    }

    if (state.verbose) println("> Creating endpoints")

    Spark.get("/mfd") { req, res ->
        val json = JSONObject()
        json.put("version", state.version)
        json.put("crypto", "/mfd/crypto")
        json.put("displays", "/mfd/displays")
        json.put("api", "/mfd/api")

        println("${req.ip()}> Sending endpoint list")
        res.status(200)
        res.type("application/json")
        json.toString()
    }

    Spark.get("/mfd/crypto") { req, res ->
        println("${req.ip()}> Sending crypto information")

        res.status(200)
        res.type("application/json")
        getCrypto(state)
    }

    Spark.get("/mfd/displays") { req, res ->
        if (state.verbose) println("> Getting display list")
        val directoryFiles = displayFile.listFiles(FileFilter { it.isDirectory })
        val directoryNames = directoryFiles.map { it.name }
        val json = JSONObject()
        val jsonArr = JSONArray(directoryNames)
        json.put("displays", jsonArr)

        println("${req.ip()}> Sending display list")
        res.status(200)
        res.type("application/json")
        json.toString()
    }

    Spark.get("/mfd/api") { _, res ->
        res.status(200)
        res.type("text/plain")
        "See documentation. The API uses HTTP POST requests."
    }

    ///////////////////////////////////////////////////////////////////
    //                                                               //
    //                              API                              //
    //                                                               //
    ///////////////////////////////////////////////////////////////////

    //fetch("/mfd/api", {method: "POST", body: "..."})
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
            if (state.verbose) print("${req.ip()}> Checking crypto... ")
            val iv = state.crypto.hexToByteArray(ivHex)
            val cipherText = state.crypto.hexToByteArray(bodyArray[1])
            val plainText = state.crypto.decrypt(iv, cipherText)

            jsonObj = JSONObject(plainText)
            if (state.verbose) println("OK")
        } catch (e: AEADBadTagException) {
            if (state.verbose) println("BAD")
            else println("${req.ip()}> Bad or unrecognized crypto")

            res.status(401)
            res.type("text/html")
            return@post "<strong>Error 401</strong> - Bad or unrecognized crypto"
        }

        val user: String = ivHex.substring(0, 8)
        val time: Long = java.lang.Long.parseLong(ivHex.substring(8), 16)

        if (users.containsKey(user) && time <= users[user] as Long) {
            println("${req.ip()}> Replayed or timestamp expired data")
            res.status(401)
            res.type("text/html")
            return@post "<strong>Error 401</strong> - Timestamp expired"
        } else { // new user or "time" is more recent than the latest time for that user
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
                        if (state.safeMode && (key == KeyEvent.VK_WINDOWS || key == KeyEvent.VK_META)) {
                            println("${req.ip()}> Blocked by SafeMode: keyon:$data")
                            res.status(403)
                        } else {
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
                        } else {
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
                "media" -> {
                    when (data) {
                        "next" -> {
                            GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 176, 57369, NativeKeyEvent.CHAR_UNDEFINED))
                        }
                        "prev" -> {
                            GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 177, 57360, NativeKeyEvent.CHAR_UNDEFINED))
                        }
                        "play" -> {
                            GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 179, 57378, NativeKeyEvent.CHAR_UNDEFINED))
                        }
                        "stop" -> {
                            GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 178, 57380, NativeKeyEvent.CHAR_UNDEFINED))
                        }
                        "mute" -> {
                            GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 173, 57376, NativeKeyEvent.CHAR_UNDEFINED))
                        }
                        "up" -> {
                            GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 175, 57392, NativeKeyEvent.CHAR_UNDEFINED))
                        }
                        "down" -> {
                            GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 174, 57390, NativeKeyEvent.CHAR_UNDEFINED))
                        }
                        else -> {
                            res.status(400)
                            return@post "<strong>Error 400</strong> - Unknown command: $data"
                        }
                    }
                    println("${req.ip()}> media:$data")
                    res.status(204)
                }
                "exec" -> {
                    try {
                        if (!state.safeMode) {
                            Runtime.getRuntime().exec(data)
                            println("${req.ip()}> exec:$data")
                            res.status(204)
                        } else {
                            println("${req.ip()}> Blocked by SafeMode: exec:$data")
                            res.status(403)
                        }
                    } catch (e: IOException) {
                        println(e.message)
                        res.status(400)
                        res.type("text/html")
                        return@post "<strong>Error 400</strong> - Exec: ${e.message}"
                    }
                }
                "url" -> {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            if (!state.safeMode) {
                                Desktop.getDesktop().browse(URI(data))
                                println("${req.ip()}> url:$data")
                                res.status(204)
                            } else {
                                println("${req.ip()}> Blocked by SafeMode: url:$data")
                                res.status(403)
                            }
                        } catch (e: UnsupportedOperationException) {
                            println("${req.ip()}> Failed to visit $data. This is a known problem on Linux.")
                            res.status(400)
                            res.type("text/html")
                            return@post "<strong>Error 400</strong> - URL: ${e.message}"
                        } catch (e: IOException) {
                            println("""${req.ip()}> Failed to visit $data. This could be caused by omitting "http:\\" or "https:\\" on macOS.""")
                            res.status(400)
                            res.type("text/html")
                            return@post "<strong>Error 400</strong> - URL: ${e.message}"
                        }
                    } else {
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
                    if (!state.safeMode) {
                        state.key = data
                        setCrypto(state)

                        println("${req.ip()}> Password change:$data")
                        res.status(200)
                        res.type("application/json")
                        return@post getCrypto(state)
                    } else {
                        println("${req.ip()}> Blocked by SafeMode: Password change:$data")
                        res.status(403)
                    }
                }
                "version" -> {
                    val json = JSONObject()
                    json.put("version", state.version)

                    println("${req.ip()}> version:${state.version}")
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
                    val file = File(data)
                    if (file.exists()) {
                        Thread {
                            mediaPlayer.play(file)
                        }.start()
                        println("${req.ip()}> playmp3:$data")
                        res.status(204)
                    } else {
                        println("${req.ip()}> playmp3 not found:$data")
                        res.status(400)
                    }
                }
                "stopmp3" -> {
                    mediaPlayer.stop()
                    println("${req.ip()}> stopmp3")
                    res.status(204)
                }
                "volmp3" -> {
                    mediaPlayer.vol = data.toInt()
                    println("${req.ip()}> volmp3:$data")
                    res.status(204)
                }
                "sensors" -> {
                    val json = JSONObject()
                    val components = JSensors.get.components()
                    val cpus: List<Cpu>? = components.cpus
                    val gpus: List<Gpu>? = components.gpus

                    // CPU
                    val cpuArray = JSONArray()
                    if (cpus != null) for (cpu in cpus) {
                        if (cpu.sensors != null) {
                            val cpuJson = JSONObject()
                            cpuJson.put("name", cpu.name)

                            // temperatures
                            val temps = cpu.sensors.temperatures
                            for (temp in temps) {
                                if (temp.name == "Temp CPU Package")
                                    cpuJson.put("temp", temp.value)
                            }

                            // loads
                            val loads = cpu.sensors.loads
                            for (load in loads) {
                                if (load.name == "Load CPU Total")
                                    cpuJson.put("load", load.value)
                                else if (load.name == "Load Memory")
                                    cpuJson.put("mem", load.value)
                            }

                            cpuArray.put(cpuJson)
                        }
                    }
                    json.put("cpus", cpuArray)

                    // GPU
                    val gpuArray = JSONArray()
                    if (gpus != null) for (gpu in gpus) {
                        if (gpu.sensors != null) {
                            val gpuJson = JSONObject()
                            gpuJson.put("name", gpu.name)

                            // temperatures
                            val temps = gpu.sensors.temperatures
                            for (temp in temps) {
                                if (temp.name == "Temp GPU Core")
                                    gpuJson.put("temp", temp.value)
                            }

                            gpuArray.put(gpuJson)
                        }
                    }
                    json.put("gpus", gpuArray)

                    println("${req.ip()}> sensors")
                    res.status(200)
                    res.type("application/json")
                    json.toString()
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
                    arrayOf("x", "y", "z", "rx", "ry", "rz", "sl0", "sl1", "whl", "pov").forEach {
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