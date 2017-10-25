
# Multi-Function Display API

## Actions
These are the functions that you'll call to control the server computer.
### mfd.keyTap(_key_[, _modifiers_])
### mfd.keyOn(_key_)
### mfd.keyOff(_key_)
### mfd.typeString(_string_)
### mfd.mouseMove(_x_, _y_)
### mfd.mouseClick(_button_)
### mfd.mouseOn(_button_)
### mfd.mouseOff(_button_)
### mfd.mouseWheel(_clicks_)
### mfd.exec(_commandLineString_)
### mfd.url(_url_)
### mfd.getPixel(_x_, _y_, _callback_)
### mfd.getScreenshot(_x_, _y_, _width_, _height_, _callback_)


## Utilities
Unless you're developing advanced features, you won't need these.
### mfd.setDelay(_delayInMilliSeconds_)
### mfd.getDelay()
### mfd.getCrypto(_callback_)
### mfd.test(_callback_)
### mfd.token()


## Crypto utilities
Unless you're developing REALLY advanced features, you won't need these. And even then, you still probably won't need these.
### mfd.crypto.stringToHex(_string_)
### mfd.crypto.hexToString(_hexstring_)
### mfd.crypto.salt
### mfd.crypto.key
### mfd.crypto.keyByteArray
### mfd.crypto.iterations
### mfd.crypto.keyLength
### mfd.crypto.encrypt(_plainText_, _iv_)
### mfd.crypto.decrypt(_cipherText_, _iv_)
### mfd.crypto.generateKey(_plainTextPassword_)
### mfd.crypto.setKey(_hexKey_)
### mfd.crypto.generateIV()
### mfd.crypto.generateIVHex()

## Key and button reference

### Mouse Buttons

* `left`
* `center`
* `right`
* Wheel clicks are positive when scrolling "down" and toward the user, negative when scrolling "up" and away from the user

### Keyboard keys

* `a` - `z`: english letters
* `0` - `9`: keyboard numbers
* `num0` - `num9`: numpad numbers
* `f1` - `f24`: function keys
* `shift`
* `ctrl`
* `alt`
* `windows`
* `up`: arrow up
* `down`: arrow down
* `left`: arrow left
* `right`: arrow right
* `*`
* `-`
* `+`
* `numseparator`
* `!`
* `@`
* `#`
* `$`
* `^`
* `&`
* `(`
* `)`
* `_`
* `[`
* `]`
* `\\`: backslash
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