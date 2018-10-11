import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.platform.win32.User32
import javax.script.ScriptEngineManager
import com.sun.jna.Pointer
import com.sun.jna.platform.unix.X11
import com.sun.jna.platform.win32.Psapi
import com.sun.jna.win32.StdCallLibrary

// http://java-native-access.github.io/jna/4.0/javadoc/


class getDisplay {

    fun getTitle(): String {
        return when {
            Platform.isWindows() -> getWindowsFocus()
            Platform.isLinux() -> getLinuxFocus()
            Platform.isMac() -> getMacFocus()
            else -> ""
        }
    }

    // https://stackoverflow.com/a/26024224
    private fun getWindowsFocus(): String {
        val max_title_length = 1024

        val buffer = CharArray(max_title_length * 2)
        val hwnd = User32.INSTANCE.GetForegroundWindow()
        User32.INSTANCE.GetWindowText(hwnd, buffer, max_title_length)
        return Native.toString(buffer)
//    val rect = RECT()
//    User32.INSTANCE.GetWindowRect(hwnd, rect)
//    println("rect = $rect")
    }

    // https://stackoverflow.com/a/18275492
    interface XLib : StdCallLibrary {

        fun xGetInputFocus(display: X11.Display, focus_return: X11.Window, revert_to_return: Pointer): Int

        companion object {
            val INSTANCE = Native.loadLibrary("XLib", Psapi::class.java) as XLib
        }
    }
    private fun getLinuxFocus(): String {
        val x11 = X11.INSTANCE
        val xlib = XLib.INSTANCE
        val display = x11.XOpenDisplay(null)
        val window = X11.Window()
        xlib.xGetInputFocus(display, window, Pointer.NULL)
        val name = X11.XTextProperty()
        x11.XGetWMName(display, window, name)
        return name.toString()
    }

    // https://stackoverflow.com/a/18275492
    private fun getMacFocus(): String {
        val script = "tell application \"System Events\"\n\tname of application processes whose frontmost is tru\nend"
        val appleScript = ScriptEngineManager().getEngineByName("AppleScript")
        return appleScript.eval(script) as String
    }
}
