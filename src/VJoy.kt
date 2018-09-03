
import com.sun.jna.*
import com.sun.jna.ptr.*
import com.sun.jna.platform.win32.*

class VJoy(path: String) {
    private val vj = Native.loadLibrary(path, vjdll::class.java)

    fun axisEnum(axis: String): Int {
        val lowerAxis = axis.toLowerCase()
        return when (lowerAxis) {
            "x", "hid_usage_x" -> 0x30
            "y", "hid_usage_y" -> 0x31
            "z", "hid_usage_z" -> 0x32
            "rx", "hid_usage_rx" -> 0x33
            "ry", "hid_usage_ry" -> 0x34
            "rz", "hid_usage_rz" -> 0x35
            "sl0", "hid_usage_sl0" -> 0x36
            "sl1", "hid_usage_sl1" -> 0x37
            "whl", "hid_usage_whl" -> 0x38
            "pov", "hid_usage_pov" -> 0x39
            else -> 0
        }
    }

    fun vJoyEnabled(): Boolean {
        return vj.vJoyEnabled()
    }

    fun getvJoyManufacturerString(): String {
        return vj.GetvJoyManufacturerString().toString()
    }

    fun getvJoyProductString(): String {
        return vj.GetvJoyProductString().toString()
    }

    fun getvJoySerialNumberString(): String {
        return vj.GetvJoySerialNumberString().toString()
    }

    fun getvJoyVersion(): Short {
        return vj.GetvJoyVersion()
    }

    fun getvJoyMaxDevices(): Int {
        val maxDevs = IntByReference()
        return if (vj.GetvJoyMaxDevices(maxDevs)) maxDevs.value
        else 0
    }

    fun getNumberExistingVJD(): Int {
        val existingDevs = IntByReference()
        return if (vj.GetNumberExistingVJD(existingDevs)) existingDevs.value
        else 0
    }

    fun driverMatch(dllVer: Short?, driverVer: Short?): Boolean {
        return vj.DriverMatch(dllVer, driverVer)
    }

    fun getVJDButtonNumber(rID: Int): Int {
        return vj.GetVJDButtonNumber(rID)
    }

    fun getVJDDiscPovNumber(rID: Int): Int {
        return vj.GetVJDDiscPovNumber(rID)
    }

    fun getVJDContPovNumber(rID: Int): Int {
        return vj.GetVJDContPovNumber(rID)
    }

    fun getVJDAxisExist(rID: Int, axis: String): Boolean {
        return vj.GetVJDAxisExist(rID, axisEnum(axis))
    }

    fun getVJDAxisMax(rID: Int, axis: String): Long {
        val max = LongByReference()
        vj.GetVJDAxisMax(rID, axisEnum(axis), max)
        return max.value
    }

    fun getVJDAxisMin(rID: Int, axis: String): Long {
        val min = LongByReference()
        vj.GetVJDAxisMin(rID, axisEnum(axis), min)
        return min.value
    }

    fun getVJDStatus(rID: Int): String {
        val status = vj.GetVJDStatus(rID)
        when (status) {
            0 -> return "VJD_STAT_OWN"
            1 -> return "VJD_STAT_FREE"
            2 -> return "VJD_STAT_BUSY"
            3 -> return "VJD_STAT_MISS"
            4 -> return "VJD_STAT_UNKN"
            else -> return "VJD_STAT_ERR"
        }
    }

    fun isVJDExists(rID: Int): Boolean {
        return vj.isVJDExists(rID)
    }

    fun getOwnerPid(rID: Int): Int {
        return vj.GetOwnerPid(rID)
    }

    fun acquireVJD(rID: Int): Boolean {
        return vj.AcquireVJD(rID)
    }

    fun relinquishVJD(rID: Int) {
        vj.RelinquishVJD(rID)
    }

//    fun vjdUpdate(rID: Int, pData: Int): Boolean {
//        return vj.UpdateVJD(rID)
//    }

    fun setAxis(value: Long, rID: Int, axis: String): Boolean {
        return vj.SetAxis(value, rID, axisEnum(axis))
    }

    // nBtn can be 1-128
    fun setBtn(value: Boolean, rID: Int, nBtn: Short): Boolean {
        return vj.SetBtn(value, rID, nBtn)
    }

    // value can be 0: north/forwards, 1: east/right, 2: south/back, 3: west/left, -1 neutral
    // nPov can be 1-4
    fun setDiscPov(value: Int, rID: Int, nPov: Short): Boolean {
        return vj.SetDiscPov(value, rID, nPov)
    }

    // value can be -1 to 35999 in units of 1/100th of a degree
    // nPov can be 1-4
    fun setContPov(value: Int, rID: Int, nPov: Short): Boolean {
        return vj.SetContPov(value, rID, nPov)
    }

    fun resetVJD(rID: Int): Boolean {
        return vj.ResetVJD(rID)
    }

    fun resetButtons(rID: Int): Boolean {
        return vj.ResetButtons(rID)
    }

    fun resetPovs(rID: Int): Boolean {
        return vj.ResetPovs(rID)
    }

    fun resetAll() {
        vj.ResetAll()
    }

    // the dll

    interface vjdll: Library {
        fun vJoyEnabled(): Boolean
        fun GetvJoyVersion(): Short
        fun GetvJoyManufacturerString(): WString
        fun GetvJoyProductString(): WString
        fun GetvJoySerialNumberString(): WString
        fun DriverMatch(version1: Short?, version2: Short?): Boolean

        // What is the maximum possible number of vJoy devices
        fun GetvJoyMaxDevices(n: IntByReference): Boolean

        // What is the number of vJoy devices currently enabled
        fun GetNumberExistingVJD(n: IntByReference): Boolean

        // Get the number of buttons defined in the specified VDJ
        fun GetVJDButtonNumber(rID: Int): Int

        // Get the number of descrete-type POV hats defined in the specified VDJ
        fun GetVJDDiscPovNumber(rID: Int): Int

        // Get the number of descrete-type POV hats defined in the specified VDJ (same as the above?)
        fun GetVJDContPovNumber(rID: Int): Int

        // Test if given axis defined in the specified VDJ
        fun GetVJDAxisExist(rID: Int, axis: Int): Boolean
        fun GetVJDAxisMax(rID: Int, axis: Int, max: LongByReference): Boolean
        fun GetVJDAxisMin(rID: Int, axis: Int, min: LongByReference): Boolean

        // Get the status of the specified vJoy Device.
        fun GetVJDStatus(rID: Int): Int

        // TRUE if the specified vJoy Device exists
        fun isVJDExists(rID: Int): Boolean

        // Reurn owner's Process ID if the specified vJoy Device exists
        fun GetOwnerPid(rID: Int): Int

        // Acquire the specified vJoy Device.
        fun AcquireVJD(rID: Int): Boolean

        // Relinquish the specified vJoy Device.
        fun RelinquishVJD(rID: Int)

        // Update the position data of the specified vJoy Device.
//        fun UpdateVJD(rID: Int): Boolean

        // Write Value to a given axis defined in the specified VDJ
        fun SetAxis(value: Long, rID: Int, Axis: Int): Boolean

        // Write Value to a given button defined in the specified VDJ
        fun SetBtn(value: Boolean, rID: Int, nBtn: Short): Boolean

        // Write Value to a given descrete POV defined in the specified VDJ
        fun SetDiscPov(value: Int, rID: Int, nPov: Short): Boolean

        // Write Value to a given continuous POV defined in the specified VDJ
        fun SetContPov(value: Int, rID: Int, nPov: Short): Boolean

        // Reset all controls to predefined values in the specified VDJ
        fun ResetVJD(rID: Int): Boolean

        // Reset all controls to predefined values in all VDJ
        fun ResetAll()

        // Reset all buttons (To 0) in the specified VDJ
        fun ResetButtons(rID: Int): Boolean

        // Reset all POV Switches (To -1) in the specified VDJ
        fun ResetPovs(rID: Int): Boolean
    }

    init {
        // ...
    }

}

// example output
fun main (args: Array<String>) {
    val dllName = "vJoyInterface.dll"
    var dllPath: String
    var vj: VJoy
    try {
        dllPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{8E31F76F-74C3-47F1-9550-E041EEDC5FBB}_is1",
                "DllX64Location")
        println("vJoy x64 dll location: $dllPath")
    } catch(e: com.sun.jna.platform.win32.Win32Exception) {
        try {
            dllPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{8E31F76F-74C3-47F1-9550-E041EEDC5FBB}_is1",
                    "DllX86Location")
            println("vJoy x86 dll location: $dllPath")
        } catch(f: com.sun.jna.platform.win32.Win32Exception) {
            println("vJoy not installed properly")
            return
        }
    }

    vj = VJoy("$dllPath\\$dllName")

    if (vj.vJoyEnabled()) {
        println("Vendor: ${vj.getvJoyManufacturerString()}")
        println("Product: ${vj.getvJoyProductString()}")
        println("Version: ${vj.getvJoySerialNumberString()}")
        println("vJoy version: ${vj.getvJoyVersion()}")
        println("Driver match: ${vj.driverMatch(null, null)}") // normally you would put the dll and driver versions here

        println("Maximum vJoy Devices: ${vj.getvJoyMaxDevices()}")
        println("Existing vJoy Devices: ${vj.getNumberExistingVJD()}")

        for (i in 1..vj.getNumberExistingVJD()) {
            var status = vj.getVJDStatus(i)
            println("---- Device rID: $i ----")
            println("Device Exists: ${vj.isVJDExists(i)}")
            println("Device Owner's PID: ${vj.getOwnerPid(i)} (-13 is free, -12 is missing, -11 is an error)")
            println("Device Status: $status")
            println("Buttons: ${vj.getVJDButtonNumber(i)}")
            println("POV (Disc) hats: ${vj.getVJDDiscPovNumber(i)}")
            println("POV (Cont) hats: ${vj.getVJDContPovNumber(i)}")

            // Acquire device
            if (status == "VJD_STAT_OWN" || status == "VJD_STAT_FREE") {
                if (vj.acquireVJD(i)) {
                    println("Acquired vJoy Device.")
                } else {
                    println("Failed to acquire vJoy Device.")
                }
            }
            status = vj.getVJDStatus(i)

            // print axis information and test it
            arrayOf("x", "y", "z", "rx", "ry", "rz", "sl0", "sl1", "whl", "pov").forEach{
                if (vj.getVJDAxisExist(i, it)) {
                    println("$it: ${vj.getVJDAxisMin(i, it)} to ${vj.getVJDAxisMax(i, it)}")
                    if (status == "VJD_STAT_OWN") {
                        vj.setAxis(vj.getVJDAxisMax(i, it), i, it)
                        Thread.sleep(250)
                        vj.setAxis(0, i, it)
                        Thread.sleep(250)
                    }
                }
            }

            if (status == "VJD_STAT_OWN") {
                // Test buttons
                for (b in 1..vj.getVJDButtonNumber(i)) {
                    vj.setBtn(true, i, b.toShort())
                    Thread.sleep(250)
                    vj.setBtn(false, i, b.toShort())
                }

                // Test disc povs
                for (d in 1..vj.getVJDDiscPovNumber(i)) {
                    vj.setDiscPov(0, i, d.toShort()) // north
                    Thread.sleep(250)
                    vj.setDiscPov(1, i, d.toShort()) // east
                    Thread.sleep(250)
                    vj.setDiscPov(2, i, d.toShort()) // south
                    Thread.sleep(250)
                    vj.setDiscPov(3, i, d.toShort()) // west
                    Thread.sleep(250)
                    vj.setDiscPov(-1, i, d.toShort()) // neutral
                }

                // Test cont povs
                for (c in 1..vj.getVJDContPovNumber(i)) {
                    val max = vj.getVJDAxisMax(i, "pov").toInt()
                    vj.setContPov(max, i, c.toShort()) // north
                    Thread.sleep(160)
                    vj.setContPov(max*1/8, i, c.toShort()) // northeast
                    Thread.sleep(160)
                    vj.setContPov(max*1/4, i, c.toShort()) // east
                    Thread.sleep(160)
                    vj.setContPov(max*3/8, i, c.toShort()) // southeast
                    Thread.sleep(160)
                    vj.setContPov(max*2/4, i, c.toShort()) // south
                    Thread.sleep(160)
                    vj.setContPov(max*5/8, i, c.toShort()) // southwest
                    Thread.sleep(160)
                    vj.setContPov(max*3/4, i, c.toShort()) // west
                    Thread.sleep(160)
                    vj.setContPov(max*7/8, i, c.toShort()) // northwest
                    Thread.sleep(160)
                    vj.setContPov(-1, i, c.toShort()) // neutral
                }
            }

            // Reset all
            vj.resetAll()

        }

    }
}