# MFD javascript API
I don't recommend connecting directly with the MFD server API. Use the included javascript API instead, it's much more user friendly, fault tolerant,
and actually has some derived features that the server API itself doesn't implement. 

## Standard Actions
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
Opens a URL in the default web browser of the host computer. You must include `http(s)://` as part of the URL. This feature does not work in Linux.
### `mfd.playMP3(_path_)`
Stops any current MP3 playing in MFD and plays the MP3 in the path. Path is either relative to the `.jar` or an absolute path.
### `mfd.stopMP3()`
Stops any current MP3 playing in MFD.
### `mfd.getMouse(_callback_)` or `mfd.gM(...)`
Gets the pixel coordinates of the cursor/mouse in the format: `{"x": 0, "y": 0}`.
### `mfd.getPixel(_x_, _y_, _callback_)` or `mfd.gP(...)`
Gets the RGB values in the format: `{"red": 0, "green": 0, "blue": 0}`.
### `mfd.getScreenshot(_x_, _y_, _width_, _height_, _callback_)` or `mfd.gS(...)`
Gets a screenshot in the format of a URL blob. Example: `mfd.getScreenshot(0,0,1920,1080, url => window.open(url))`
### `mfd.wait(time)` or `mfd.w(...)`
Waits a certain amount of milliseconds (1/1000th of a second) before doing something else. Useful when chaining actions, like: `mfd.mouseClicK('right').wait(100).mouseClick('left');`
### `mfd.getWindowTitle(_callback_)`
Gets the title of the focused window as a string. Example: `mfd.getWindowTitle(title => alert(title)`
### `mfd.ping(_callback)`
Gets the ping (network latency) in milliseconds. Example: `mfd.ping(p => alert("ping is "+p+"ms"))`

## vJoy Actions
These are functions that only work on Windows with vJoy installed
### `mfd.vJoy.info(_callback_)`
Gets the vJoy information in the format:
```
{
    "enabled": true,
    "existingDevices": 1,
    "manufacturer": "Shaul Eizikovich",
    "maxDevices": 16,
    "product": "vJoy - Virtual Joystick",
    "serialnumber": "2.1.8",
    "version": 536
}
```
### `mfd.vJoy.reset()`
Resets all inputs of all virtual devices and resets ownership of them.
### `mfd.vJoy.device(_rID_).info(_callback_)`
Gets the vJoy virtual device information for given rID in the format:
```
{
    "rz_max": 32767,
    "btnNumber": 20,
    "sl1_max": 32767,
    "y_max": 32767,
    "contPovNumber": 0,
    "ry_max": 32767,
    "pov_max": 3,
    "sl0_max": 32767,
    "ownerPid": -13,
    "x_max": 32767,
    "discPovNumber": 4,
    "z_max": 32767,
    "rx_max": 32767,
    "whl_max": 0,
    "status": "VJD_STAT_FREE"
}
```
If `ownerPid` is negative, it's likely that -13 is free, -12 is missing, and -11 is an error
### `mfd.vJoy.device(_rID_).acquire()`
Acquires the virtual device with the given rID. Requires that device to have a status `VJD_STAT_FREE`. A device must be acquired before it can be used.
### `mfd.vJoy.device(_rID_).relinquish()`
Relinquishes ownership the virtual device with the given rID.
### `mfd.vJoy.device(_rID_).setAxis(_axis_, _value_)`
Sets an axis of a virtual device to a value. Axis should be `x`, `y`, `z`, `rx`, `ry`, `rz`, `sl0`, `sl1`, or `whl`. Value should be from 0 to the max value in `mfd.device(rID).info()`, likely 32767.
### `mfd.vJoy.device(_rID_).setBtn(_btn_, _value_)`
Sets a button of a virtual device to on or off. Btn should be the number of the button, Value should be true (on/down) or false (off/up).
### `mfd.vJoy.device(_rID_).setPovDiscrete(_nPov_, _value_)`
Sets a discrete pov to a value, -1: Center, 0: North, 1: East, 2: South, 3: West
### `mfd.vJoy.device(_rID_).setPovContinuous(_nPov_, _value_)`
Sets a continuous pov to a value, -1: Center, 0: North, 1/4 max value: East, 1/2 max value: South, 3/4 max value: West, etc.
### `mfd.vJoy.device(_rID_).reset()`
Resets all inputs of the given virtual device.
### `mfd.vJoy.device(_rID_).resetBtns()`
Resets the buttons of the given virtual device.
### `mfd.vJoy.device(_rID_).resetPovs()`
Resets the povs of the given virtual device.

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
* `windows`: the Windows windows key, use `win` when used as a modifier for `mfd.keyTap`
* `command`: the macOS ⌘/command/apple/clover/open-apple/splat/pretzel/propeller key, use `cmd` when used as a modifier for `mfd.keyTap`
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
* `esc`

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
* `\" or "` - you need to put a backslash first because of javascript escape characters
* `<`
* `>`
* `?`
* `~`
