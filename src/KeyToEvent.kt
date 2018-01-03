import java.awt.event.KeyEvent

// key to event
fun k2e(input: String): Int {
    val lowerCaseInput = input.toLowerCase()
    when(lowerCaseInput) {
        // english alphabet
        "a" -> return KeyEvent.VK_A
        "b" -> return KeyEvent.VK_B
        "c" -> return KeyEvent.VK_C
        "d" -> return KeyEvent.VK_D
        "e" -> return KeyEvent.VK_E
        "f" -> return KeyEvent.VK_F
        "g" -> return KeyEvent.VK_G
        "h" -> return KeyEvent.VK_H
        "i" -> return KeyEvent.VK_I
        "j" -> return KeyEvent.VK_J
        "k" -> return KeyEvent.VK_K
        "l" -> return KeyEvent.VK_L
        "m" -> return KeyEvent.VK_M
        "n" -> return KeyEvent.VK_N
        "o" -> return KeyEvent.VK_O
        "p" -> return KeyEvent.VK_P
        "q" -> return KeyEvent.VK_Q
        "r" -> return KeyEvent.VK_R
        "s" -> return KeyEvent.VK_S
        "t" -> return KeyEvent.VK_T
        "u" -> return KeyEvent.VK_U
        "v" -> return KeyEvent.VK_V
        "w" -> return KeyEvent.VK_W
        "x" -> return KeyEvent.VK_X
        "y" -> return KeyEvent.VK_Y
        "z" -> return KeyEvent.VK_Z

        // arabic numerals
        "0" -> return KeyEvent.VK_0
        "1" -> return KeyEvent.VK_1
        "2" -> return KeyEvent.VK_2
        "3" -> return KeyEvent.VK_3
        "4" -> return KeyEvent.VK_4
        "5" -> return KeyEvent.VK_5
        "6" -> return KeyEvent.VK_6
        "7" -> return KeyEvent.VK_7
        "8" -> return KeyEvent.VK_8
        "9" -> return KeyEvent.VK_9

        // numpad arabic numerals
        "num0" -> return KeyEvent.VK_NUMPAD0
        "num1" -> return KeyEvent.VK_NUMPAD1
        "num2" -> return KeyEvent.VK_NUMPAD2
        "num3" -> return KeyEvent.VK_NUMPAD3
        "num4" -> return KeyEvent.VK_NUMPAD4
        "num5" -> return KeyEvent.VK_NUMPAD5
        "num6" -> return KeyEvent.VK_NUMPAD6
        "num7" -> return KeyEvent.VK_NUMPAD7
        "num8" -> return KeyEvent.VK_NUMPAD8
        "num9" -> return KeyEvent.VK_NUMPAD9

        // numpad characters
        "*" -> return KeyEvent.VK_ASTERISK
        "-" -> return KeyEvent.VK_MINUS
        "+" -> return KeyEvent.VK_PLUS
        "numseparator" -> return KeyEvent.VK_SEPARATOR

        // shift-numbers on US keyboard
        // these don't work with java.awt.Robot
//        "!" -> return KeyEvent.VK_EXCLAMATION_MARK
//        "@" -> return KeyEvent.VK_AT
//        "#" -> return KeyEvent.VK_NUMBER_SIGN
//        "$" -> return KeyEvent.VK_DOLLAR
//        "^" -> return KeyEvent.VK_CIRCUMFLEX
//        "&" -> return KeyEvent.VK_AMPERSAND
//        "(" -> return KeyEvent.VK_LEFT_PARENTHESIS
//        ")" -> return KeyEvent.VK_RIGHT_PARENTHESIS
//        "_" -> return KeyEvent.VK_UNDERSCORE

        // "special" characters
        "[" -> return KeyEvent.VK_OPEN_BRACKET
        "]" -> return KeyEvent.VK_CLOSE_BRACKET
        "\\" -> return KeyEvent.VK_BACK_SLASH
        "/" -> return KeyEvent.VK_SLASH
        "," -> return KeyEvent.VK_COMMA
        "." -> return KeyEvent.VK_PERIOD
        ";" -> return KeyEvent.VK_SEMICOLON
        ":" -> return KeyEvent.VK_COLON
        "'" -> return KeyEvent.VK_QUOTE
        "`" -> return KeyEvent.VK_BACK_QUOTE
        "=" -> return KeyEvent.VK_EQUALS

        "numlock" -> return KeyEvent.VK_NUM_LOCK
        "capslock" -> return KeyEvent.VK_CAPS_LOCK
        "scrolllock" -> return KeyEvent.VK_SCROLL_LOCK
        "prtscn" -> return KeyEvent.VK_PRINTSCREEN
        "pause" -> return KeyEvent.VK_PAUSE

        "f1" -> return KeyEvent.VK_F1
        "f2" -> return KeyEvent.VK_F2
        "f3" -> return KeyEvent.VK_F3
        "f4" -> return KeyEvent.VK_F4
        "f5" -> return KeyEvent.VK_F5
        "f6" -> return KeyEvent.VK_F6
        "f7" -> return KeyEvent.VK_F7
        "f8" -> return KeyEvent.VK_F8
        "f9" -> return KeyEvent.VK_F9
        "f10" -> return KeyEvent.VK_F10
        "f11" -> return KeyEvent.VK_F11
        "f12" -> return KeyEvent.VK_F12
        "f13" -> return KeyEvent.VK_F13
        "f14" -> return KeyEvent.VK_F14
        "f15" -> return KeyEvent.VK_F15
        "f16" -> return KeyEvent.VK_F16
        "f17" -> return KeyEvent.VK_F17
        "f18" -> return KeyEvent.VK_F18
        "f19" -> return KeyEvent.VK_F19
        "f20" -> return KeyEvent.VK_F20
        "f21" -> return KeyEvent.VK_F21
        "f22" -> return KeyEvent.VK_F22
        "f23" -> return KeyEvent.VK_F23
        "f24" -> return KeyEvent.VK_F24

        "up" -> return KeyEvent.VK_UP
        "down" -> return KeyEvent.VK_DOWN
        "left" -> return KeyEvent.VK_LEFT
        "right" -> return KeyEvent.VK_RIGHT

        "insert" -> return KeyEvent.VK_INSERT
        "home" -> return KeyEvent.VK_HOME
        "pgup" -> return KeyEvent.VK_PAGE_UP
        "pgdn" -> return KeyEvent.VK_PAGE_DOWN
        "end" -> return KeyEvent.VK_END
        "del" -> return KeyEvent.VK_DELETE

        "tab" -> return KeyEvent.VK_TAB
        "enter" -> return KeyEvent.VK_ENTER
        "backspace" -> return KeyEvent.VK_BACK_SPACE
        "space", " " -> return KeyEvent.VK_SPACE
        "windows" -> return KeyEvent.VK_WINDOWS
        "shift" -> return KeyEvent.VK_SHIFT
        "ctrl" -> return KeyEvent.VK_CONTROL
        "alt" -> return KeyEvent.VK_ALT
        "ralt" -> return KeyEvent.VK_ALT_GRAPH

    //        "" -> return KeyEvent.VK_

        else -> return KeyEvent.VK_UNDEFINED
     }
}