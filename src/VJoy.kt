import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference

class VJoy(path: String) {
    private val vj = Native.loadLibrary(path, vjdll::class.java)

    // constants
    val HID_USAGE_X = "30".toInt(16)
    val HID_USAGE_Y = "31".toInt(16)
    val HID_USAGE_Z = "32".toInt(16)
    val HID_USAGE_RX = "33".toInt(16)
    val HID_USAGE_RY = "34".toInt(16)
    val HID_USAGE_RZ = "35".toInt(16)
    val HID_USAGE_SL0 = "36".toInt(16)
    val HID_USAGE_SL1 = "37".toInt(16)
    val HID_USAGE_WHL = "38".toInt(16)
    val HID_USAGE_POV = "39".toInt(16)

    var enabled: Boolean = vj.vJoyEnabled()
    var manufacturer: String = vj.GetvJoyManufacturerString().toString()
    var product: String = vj.GetvJoyProductString().toString()
    var serialNumber: String = vj.GetvJoySerialNumberString().toString()
    var version: Short = vj.GetvJoyVersion()
    var maxDevices: Int = getVJMaxDevices()
    var existingDevices: Int = getNumberExistingVJD()

    fun driverMatch(driver1: Short?, driver2: Short?): Boolean {
        return vj.DriverMatch(driver1, driver2)
    }

    fun vjdButtonNumber(rID: Int): Int {
        return vj.GetVJDButtonNumber(rID)
    }

    fun vjdDiscPovNumber(rID: Int): Int {
        return vj.GetVJDDiscPovNumber(rID)
    }

    fun vjdContPovNumber(rID: Int): Int {
        return vj.GetVJDContPovNumber(rID)
    }

    fun vjdHasAxis(rID: Int, axis: Int): Boolean {
        return vj.GetVJDAxisExist(rID, axis)
    }

    fun vjdAxisMax(rID: Int, axis: Int): Long {
        val max = LongByReference()
        vj.GetVJDAxisMax(rID, axis, max)
        return max.value
    }

    fun vjdAxisMin(rID: Int, axis: Int): Long {
        val min = LongByReference()
        vj.GetVJDAxisMin(rID, axis, min)
        return min.value
    }

    fun vjdStatus(rID: Int): String {
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

    fun vjdExists(rID: Int): Boolean {
        return vj.isVJDExists(rID)
    }

    fun vjdOwner(rID: Int): Int {
        return vj.GetOwnerPid(rID)
    }

    fun vjdAcquire(rID: Int): Boolean {
        return vj.AcquireVJD(rID)
    }

    fun vjdRelinquish(rID: Int) {
        vj.RelinquishVJD(rID)
    }

//    fun vjdUpdate(rID: Int, pData: Int): Boolean {
//        return vj.UpdateVJD(rID)
//    }

    fun vjdSetAxis(value: Long, rID: Int, axis: Int): Boolean {
        return vj.SetAxis(value, rID, axis)
    }

    // nBtn can be 1-128
    fun vjdSetBtn(value: Boolean, rID: Int, nBtn: Short): Boolean {
        return vj.SetBtn(value, rID, nBtn)
    }

    // value can be 0: north/forwards, 1: east/right, 2: south/back, 3: west/left, -1 neutral
    // nPov can be 1-4
    fun vjdDiscPov(value: Int, rID: Int, nPov: Short): Boolean {
        return vj.SetDiscPov(value, rID, nPov)
    }

    // value can be -1 to 35999 in units of 1/100th of a degree
    // nPov can be 1-4
    fun vjdContPov(value: Int, rID: Int, nPov: Short): Boolean {
        return vj.SetContPov(value, rID, nPov)
    }

    fun vjdReset(rID: Int): Boolean {
        return vj.ResetVJD(rID)
    }

    fun vjdResetBtns(rID: Int): Boolean {
        return vj.ResetButtons(rID)
    }

    fun vjdResetPovs(rID: Int): Boolean {
        return vj.ResetPovs(rID)
    }

    fun resetAll() {
        vj.ResetAll()
    }

    // Internal use helper functions

    private fun getVJMaxDevices(): Int {
        val maxDevs = IntByReference()
        return if (vj.GetvJoyMaxDevices(maxDevs)) maxDevs.value
        else 0
    }

    private fun getNumberExistingVJD(): Int {
        val existingDevs = IntByReference()
        return if (vj.GetNumberExistingVJD(existingDevs)) existingDevs.value
        else 0
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