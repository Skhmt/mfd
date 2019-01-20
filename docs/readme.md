
# Multi-Function Display (MFD)

## What is MFD?
MFD lets you control your computer via your tablet, phone, or another computer with pre-programmed key combinations, key strings, mouse clicks, and other actions. 

Alternatively, it's like an external controller for AutoHotkey.

Similar products include Elgato's Stream Deck, ROCCAT's Power-Grid, Desrat's TouchDown, and LEA Extended Input.


|                       | MFD | [Stream Deck](https://www.elgato.com/en/gaming/stream-deck) | [Power-Grid](https://en.roccat.org/Software/Power-Grid/Download) | [TouchDown](http://www.desratsworld.co.uk/TouchDown/) | [LEI](https://www.leaextendedinput.com/) | [Deckboard](https://deckboard.app/) |
|-----------------------|-----|-------------|------------|-----------|-----|-----------|
| Windows Host          |  ✔  |      ✔      |      ✔    |     ✔     |  ✔  | ✔ |
| MacOS Host            |  ✔  |      ✔      |           |           |     | |
| Linux Host            |  ✔  |             |            |           |     | |
| Android Client        |  ✔  |             |     ✔     |     ✔    |  ✔  | ✔ |
| iOS Client            |  ✔  |             |     ✔     |     ✔    |  ✔  | |
| PC Client             |  ✔  |             |            |     ✔     |     | |
| Proprietary Client    |     |       ✔     |            |           |     | |
| Custom Display Layout |  ✔  |             |      ✔     |     ✔    |  ✔  | ✔ |
| Completely Free       |  ✔  |             |            |     ✔     | Ads | Ads |
| More than one Client  |  ✔  |             |            |     ✔     |     | ? |

## Ok, but what is an MFD?
Multi-Function Display. [Aircraft use them](https://i.imgur.com/F3zrq6g.jpg) and have been transitioning away from traditional knobs, switches, and gauges and towards more and larger MFDs. 

Cars also have them, although they're also frequently called "infotainment systems".

As silly as it sounds, their defining feature is a video screen that can do many things.

The MFD icon and example display take inspiration from the early MFDs found in fighters like the F-16.

## Why did you make MFD?
To use it with anything that requires a ton of keyboard shortcuts and/or macros that aren't easy to remember, like Star Citizen and other space/flight sims, MMORPGs, video editing, and Twitch/YouTube Streaming.

It can also be used for desktop macros, or pretty much anything really.

## Installation
* Download the [Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/jre9-downloads-3848532.html)
  * On Windows, if you use the `exe` version, you do not need the `Java Runtime Environment`
* Use Chrome, Firefox, Edge, or Safari... don't use Internet Explorer

## Usage
* Windows
  * Double click `MFD.jar` or `MFD.exe`
  * Make sure you allow it through your firewall if a warning comes up
  * You might need to run it as administrator
* macOS
  * `System Preferences` -> `Security & Privacy`-> click `Open Anyway`
  * Run via terminal, `sudo java -jar MFD.jar`
* Linux (Ubuntu)
  * Right click in the folder and select `Open in Terminal`
  * Run via terminal, `sudo java -jar MFD.jar`
* Go to your taskbar and right-click the MFD icon
  * In macOS, left-click it
* Click on `config`, your browser will open up with the config page
  * In Linux, this will output the URL to the terminal. Right click the URL in your terminal and select `Open Link`
* Optionally change the password - it's randomly generated every time you open MFD.jar
* Either enter the IP address shown into your phone/tablet/other computer connected to the same local network, or use the QR code to do it automatically
* On your phone/tablet/other computer, choose a display like `Example`
* Enter your password there if it asks you for it
* Start using it!

## Random Notes
* MFD is written in Kotlin and javascript, and uses additional code from [SparkJava](http://sparkjava.com/), [JSON-java](https://github.com/stleary/JSON-java), [JLayer](http://www.javazoom.net/javalayer/sources.html), [asmcrypto.js](https://github.com/vibornoff/asmcrypto.js), [NoSleep.js](https://github.com/richtr/NoSleep.js), [QRCode.js](https://github.com/davidshimjs/qrcodejs), and [MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/padStart). MFD also uses [FontAwesome](https://fontawesome.com/) icons in the Example Display
* With minimal HTML, CSS, and Javascript knowledge, making your own displays is easy, as displays are simply websites
  * Place displays in their own folder in the `/displays/` directory, see the `Example` display and its folder
* If you need help, try asking at the official [/r/mfd subreddit](https://www.reddit.com/r/mfd/)
* It's theoretically possible to use MFD to create a remote-desktop application, although the performance would be pretty bad unless you modified the Kotlin code
* As much of the processing as possible is pushed to the secondary device, as it probably isn't doing anything while the main computer is gaming or video editing or streaming
* You can use an absurd about of secondary devices with MFD; they can all be connected to the same display, to different displays, or any combination thereof
* Development and testing is done on a Windows 10 x64 desktop, an Ubuntu 16.04.3 virtual machine, a Pixel 2 XL, an Amazon Fire HD 8, a MacBook from 2008, and a simulated iPhone X

## Security Notes
* By its very nature, MFD is extremely powerful and can act as a backdoor into your system by anyone with the password
* So be sure you trust other authors whose displays you use - maliciously crafted displays can be as bad as a virus
* I highly discourage making an easy password, but it's allowed
* Don't accidentally show the config page if you're streaming on Twitch or YouTube
* Have a firewall on your network router, not just your computer
* PLEASE do not port forward MFD onto the internet nor use it on a computer connected directly to a modem... That's just asking for trouble
  * MFD is not rate limited, so even with a strong password, someone with your IP address could eventually figure out your password if you port forward MFD or otherwise have no firewall

## FAQ
* What port does MFD run on?
  * 80 by default
  * See the Command/Terminal Options section for details on how to change the port
* On Windows, MFD starts then closes immediately
  * This is likely because port 80 is already in use by something. You can check by going to `http://localhost` - if you see anything (including a 404 error) but a message like "This site can’t be reached", you probably have a program with port 80 open
  * Either close this program/process (you can find the process id by opening an admin console and typing `netstat -abo`) or change the port that MFD is running on
  * Likely culprits include `Skype`, `IIS`, `World Wide Web Publishing Service`, `Web Deployment Agent Service (MsDepSvc)`, `SQL Server Reporting Services (MSSQLSERVER)`, and some Razer Synapse programs
* On Linux, MFD starts then closes immediately
  * MFD needs to be run via the command line as a super user, see the `Usage` section above
* On macOS, MFD starts then closes immediately
  * MFD needs to be run via the command line as a super user and pass through Apple's security, see the `Usage` section above
* The IP displayed in the config page is not correct
  * I try to make a best guess for the IP to connect to, but if it's wrong, you can find the IP address yourself with `ipconfig` on the Windows console or `ifconfig` on the macOS/Linux terminal
* Opening a URL on Linux doen't work (like the top row in the Example display)
  * Linux doesn't support the `BROWSE` command, so MFD can't open any URLs on Linux
* Clicking `config` or `/r/mfd` doesn't work on Linux
  * For the same reason as the above question, MFD will display the link to go to in the Terminal
* I have another bug that isn't addressed here
  * Please run MFD via the console or terminal and add the verbose tag along with a different port, in macOS/Linux: `sudo java -jar MFD.jar -v -p 65432` or in Windows `java -jar MFD.jar -v -p 65432`
  * Post a screenshot of your full terminal/console output when reporting it to make the bug fixing faster, thanks!
* vJoy doesn't work on Linux and macOS
  * vJoy itself only works on Windows and thus vJoy support in MFD only extends to Windows host computers

## Command/Terminal Options
* `-p [port]` sets the port
  * Example: (`sudo` in macOS/Linux) `java -jar MFD.jar -p 8080` sets MFD to use port 8080 instead of the default port 80
* `-k [password]` sets the password instead of randomly generating it
  * Example: (`sudo` in macOS/Linux) `java -jar MFD.jar -k hunter2` sets the password to `hunter2` instead of randomly generating it
  * You can also change the password in the config page after starting MFD
* `-v` runs MFD in verbose mode
  * Example: (`sudo` in macOS/Linux) `java -jar MFD.jar -v` will output everything MFD is doing
* `-ip [ip address]` sets the config page to your given IP address
  * This won't make MFD run on that ip address, but it will let you fix bugs with multiple-NIC systems and still allow the QR code to work
* `-s` turns on "Safe Mode"
  * It's "safe" because it disables running console/terminal commands (`mfd.exec`), visiting urls (`mfd.url`), changing the password (`mfd.changePass`), and using the `windows` and `command` keys, but this doesn't mean you shouldn't take every precaution, someone could still click on your command prompt or terminal and type something in manually
* All the options can be combined in any order, as long as they're after `MFD.jar` 
  * Example: `sudo java -jar MFD.jar -v -p 8080 -k hunter2` 

# Changelog

* 1.7.0 - 20Jan19
  * Fixed a bug that allowed unauthorized users to replay commands in some situations
  * Added API to use media buttons, `mfd.media(_button_)`
  * Added API to get system sensors (cpu load as a %, cpu temp, ram load as a %, gpu temp) if you're running MFD as administrator, `mfd.sensors(_callback_)`
  * Added API to set the mp3 volume, `mfd.volMP3(_volume_)`
* 1.6.1 - 17Dec18
  * *If 1.6.0 is working for you, there's no need to update*
  * Changed from JavaFX to JLayer for playing mp3s
* 1.6.0 - 16Dec18
  * Added an API to play and stop an mp3 on the host computer, `mfd.playMP3(_path_)` and `mfd.stopMP3()`
  * Updated some dependencies
  * Removed all displays besides `example` and `keypad` to put them in their own projects
* 1.5.0 - 10Oct18
  * Added an api for getting the focused window's title (`mfd.getWindowTitle(title => {console.log(title);})`)
  * Transitioned from `spark-kotlin 1.0.0-alpha` to `spark-core 2.8.0` for security reasons
  * [Bugfix] Fixed the youtube example in the `Example` display
  * [Bugfix] vJoy status was displaying even when not in verbose mode
* 1.4.0 - 3Sep18
  * Added the escape key (`esc`) to the API
  * Updated included displays
  * Added github repository link to the taskbar menu
  * Cleaned up some dependencies
  * Slightly modified the main page
  * [Bugfix] blank passwords should work better now (but still aren't recommended for security reasons)
* 1.3.0 - 17Feb18
  * Added support for the macOS ⌘ key
  * Added the `-ip [ip address]` command line switch to force the config page to use your supplied ip address so the QR code works with multiple-NIC systems - this doesn't change how the program works at all
  * Added the `-s` command line switch for safe mode, which blocks `mfd.exec`, `mfd.url`, `mfd.changePass`, and the Windows and Command keys in `mfd.keyTap` and `mfd.keyOn`
  * Added the `keypad` display
* 1.2.0 - 23Jan18
  * Added vJoy support for Windows, see `api.md` for info on how to run it
  * Added commandline options for setting the port and running in verbose mode
  * Added a FAQ section dealing with common problems in getting MFD to work
* 1.1.0 - 7Jan18
  * Cleaned up the console output and added some useful logging for those that use MFD via console
  * [Bugfix] Caught a bunch of errors in Linux and macOS relating to random number generation, `mfd.url`, and `mfd.exec`
  * [Bugfix] Fixed an error that incorrectly capitalized the URL for displays, which would cause a 404 in Linux
* 1.0.3 - 3Jan18
  * [Bugfix] [Edge] No idea why the bug exists, but I fixed it and it's gone
* 1.0.2 - 3Jan18
  * [Bugfix] [iOS] [Safari] Older versions of ioOS Safari, had a problem with `let` or `const` declared variables that share the name as an element's `id`.
* 1.0.1 - 3Jan18
  * [Bugfix] `Star Citizen` display didn't release the modifier key when holding power/cooler change buttons
  * [Bugfix] `Star Citizen` display had incorrect hotkey for landing mode
  * [Bugfix] [iOS] [Safari] The `fullscreen` doesn't exist and that fact caused it to crash
* 1.0.0 - 2Jan18
  * Moved the API from `readme.md` to its own file
  * `asmCrypto` moved from `mfd.js` to its own file for better Firefox performance
    * This will require an additional `<script src="../asmCrypto.min.js"></script>` before the similar `mfd.js` script tag in your display `header`s based on previous versions of MFD
  * [Bugfix] [Firefox] [Safari] vertical alignment and fullscreen issues
  * [Bugfix] [Safari] strange vertical grid sizing issue
  * Added some visual cues you're pressing a button in the Star Citizen display
* 0.2.0 - 31Dec17
  * Updated readme
  * [Bugfix] `Example` display wouldn't vertically align properly unless both the icon and text share the same `span`
  * [Bugfix] Not using the correct key does not let you log in anyway (it still didn't let you do anything, but it looked like you logged in correctly)
  * Added a simple display for `Star Citizen 3.0.0 alpha`
* 0.1.0 - 29Dec17
  * Initial release
  
# Features/bugs being worked on

* Make a nicer main page
* Fix a bug with displays not going full screen if you have to enter a password
* GUI-based simple display creator
* Streamlabs-OBS support via JSON-RPC
* Videos on how to use MFD