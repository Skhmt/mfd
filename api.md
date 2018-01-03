# MFD javascript API
I don't recommend connecting directly with the MFD server API. Use the included javascript API instead, it's much more user friendly, fault tolerant,
and actually has some derived features that the server API itself doesn't implement. 

## Actions
These are the functions that you'll call to control the server computer.
### `mfd.keyTap(_key_[, _modifiers_])` or `mfd.kT(...)`
Taps (and releases) a key. Examples: `mfd.keyTap('f4', ['alt'])`, `mfd.keyTap('a')`
### `mfd.keyOn(_key_)` or `mfd.kD(...)`
Presses a key, does not release it.
### `mfd.keyOff(_key_)` or `mfd.kU(...)`
Releases a key.
### `mfd.typeString(_string_)` or `mfd.ts(...)`
Types a full text string as written. 
### `mfd.mouseMove(_x_, _y_)` or `mfd.mM(...)`
Moves the cursor/mouse to the given coordinates. The `0,0` point is the top left corner of the screen.
### `mfd.mouseClick(_button_)` or `mfd.mC(...)`
Clicks (and releases) a mouse button.
### `mfd.mouseOn(_button_)` or `mfd.mD(...)`
Clicks and holds a mouse button.
### `mfd.mouseOff(_button_)` or `mfd.mU(...)`
Releases a mouse button.
### `mfd.mouseWheel(_clicks_)` or `mfd.mW(...)`
Scrolls the mouse wheel up or down.
### `mfd.exec(_commandLineString_)`
Runs a commandline command. Can be used to open programs. If you download someone else's display(s), examine usage of `mfd.exec` very carefully.
### `mfd.url(_url_)`
Opens a URL in the default web browser.
### `mfd.getMouse(_callback_)` or `mfd.gM(...)`
Returns the pixel coordinates of the cursor/mouse in the format: `{"x": 0, "y": 0}`.
### `mfd.getPixel(_x_, _y_, _callback_)` or `mfd.gP(...)`
Returns the RGB values in the format: `{"red": 0, "green": 0, "blue": 0}`.
### `mfd.getScreenshot(_x_, _y_, _width_, _height_, _callback_)` or `mfd.gS(...)`
Gets a screenshot in the format of a URL blob. Example: `mfd.getScreenshot(0,0,1920,1080, url => window.open(url))`
### `mfd.wait(time)` or `mfd.w(...)`
Waits a certain amount of milliseconds (1/1000th of a second) before doing something else. Useful when chaining actions, like: `mfd.mouseClicK('right').wait(100).mouseClick('left');`

## Utilities
You probably won't need these unless you want to completely develop a display from the ground up and ignore the Example display.
### mfd.getCrypto(_callback_)
### mfd.test(_callback_)
### mfd.displays(_callback_)
### mfd.getVersion(_callback_)

## Crypto utilities
You probably won't need these unless you're re-writing large parts of the javascript api. I doubt anyone will ever need to use these.
### mfd.crypto.stringToHex(_string_)
### mfd.crypto.hexToString(_hexstring_)
### mfd.crypto.salt
### mfd.crypto.key
### mfd.crypto.keyByteArray
### mfd.crypto.iterations
### mfd.crypto.keyLength
### mfd.crypto.offset
### mfd.crypto.encrypt(_plainText_, _iv_)
### mfd.crypto.decrypt(_cipherText_, _iv_)
### mfd.crypto.generateKey(_plainTextPassword_)
### mfd.crypto.setKey(_hexKey_)
### mfd.crypto.generateIVHex()

## Key and button reference

### Mouse Buttons

* `left`, `mouse1`, `1`
* `center`, `mouse3`, `3`
* `right`, `mouse2`, `2`
* Wheel clicks are positive when scrolling "down" and toward the user, negative when scrolling "up" and away from the user

### Keyboard keys

* `a` - `z`: english letters
* `0` - `9`: keyboard numbers
* `num0` - `num9`: numpad numbers
* `f1` - `f24`: function keys
* `shift`
* `ctrl`
* `alt`
* `ralt`: right alt / alt graph
* `windows`
* `up`: arrow up
* `down`: arrow down
* `left`: arrow left
* `right`: arrow right
* `numseparator`
* `-`
* `[`
* `]`
* `\\`: backslash, you need the double backslash because of javascript escape characters
* `/`
* `,`
* `.`
* `;`
* `:`
* `'`
* `` ` ``
* `=`
* `numlock`
* `capslock`
* `scrolllock`
* `prtscn`
* `pause`
* `insert`
* `home`
* `pgup`
* `pgdn`
* `end`
* `del`
* `tab`
* `enter`
* `backspace`
* `space`

These keys do not work on their own for `keyDown`/`keyUp`, you must use the corresponding number key with the `shift` modifier. They _do_ work with `typeString` assuming you have a US keyboard:
* `!`
* `@`
* `#`
* `$`
* `^`
* `&`
* `*`
* `(`
* `)`
* `_`
* `+`
* `{`
* `}`
* `|`
* `:`
* `\"`: you need to put a backslash first because of javascript escape characters
* `<`
* `>`
* `?`
* `~`