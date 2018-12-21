import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import org.jnativehook.GlobalScreen
import org.jnativehook.NativeHookException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * https://github.com/kwhat/jnativehook
 *
 * com.1stleg:jnativehook:2.1.02
 */

fun main(args: Array<String>) {
    // disable logging
    val logger = Logger.getLogger(GlobalScreen::class.java.getPackage().name)
    logger.level = Level.OFF // Level.ALL shows everything, .WARNING shows only warnings
    logger.useParentHandlers = false

    // enable key listening
    GlobalScreen.registerNativeHook()
    GlobalScreen.addNativeKeyListener(GlobalKeyListenerExample())

    // test custom key presses that aren't supported by java.awt.Robot
    mediaNext()
    mediaPrev()
    mediaPlay()
    mediaStop()
    mediaMute()
    volUp()
    volDown()
    rightAlt()
    rightCtrl()
    rightShift()

}

// id, modifiers, rawCode, keyCode, keyLocation
fun mediaNext() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 176, 57369, NativeKeyEvent.CHAR_UNDEFINED))
}
fun mediaPrev() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 177, 57360, NativeKeyEvent.CHAR_UNDEFINED))
}
fun mediaPlay() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 179, 57378, NativeKeyEvent.CHAR_UNDEFINED))
}
fun mediaStop() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 178, 57380, NativeKeyEvent.CHAR_UNDEFINED))
}
fun mediaMute() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 173, 57376, NativeKeyEvent.CHAR_UNDEFINED))
}
fun volUp() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 175, 57392, NativeKeyEvent.CHAR_UNDEFINED))
}
fun volDown() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 174, 57390, NativeKeyEvent.CHAR_UNDEFINED))
}

// These don't work
fun rightAlt() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 165, 56, NativeKeyEvent.CHAR_UNDEFINED))
}
fun rightCtrl() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 163, 29, NativeKeyEvent.CHAR_UNDEFINED))
}
fun rightShift() {
    GlobalScreen.postNativeEvent(NativeKeyEvent(2401, 0, 161, 3638, NativeKeyEvent.CHAR_UNDEFINED))
}


class GlobalKeyListenerExample : NativeKeyListener {
    override fun nativeKeyPressed(e: NativeKeyEvent) {
        val keyCode = e.keyCode
        println("ID: ${e.id} | KeyCode: $keyCode | RawCode: ${e.rawCode} | Key: ${NativeKeyEvent.getKeyText(keyCode)}")
        if (e.keyCode == NativeKeyEvent.VC_ESCAPE) {
            try {
                GlobalScreen.unregisterNativeHook()
            } catch (e1: NativeHookException) {
                e1.printStackTrace()
            }
        }
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
//        println("Key Released: " + NativeKeyEvent.getKeyText(e.keyCode))
    }

    override fun nativeKeyTyped(e: NativeKeyEvent) {
//        println("Key Typed: " + NativeKeyEvent.getKeyText(e.keyCode))
    }
}