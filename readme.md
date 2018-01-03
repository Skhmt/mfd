
# Multi-Function Display (MFD)

## What is MFD?
MFD lets you control your computer via your tablet, phone, or another computer with pre-programmed key combinations, key strings, mouse clicks, and other actions. 

Alternatively, it's like an external controller for AutoHotkey.

Similar products include Elgato's Stream Deck, ROCCAT's Power-Grid, Desrat's TouchDown, and LEA Extended Input.


|                       | MFD | Stream Deck | Power-Grid | TouchDown | LEI |
|-----------------------|-----|-------------|------------|-----------|-----|
| Windows Host          |  ✔  |      ✔     |      ✔    |     ✔     |  ✔  |
| MacOS Host            |  ✔  |      ✔     |            |           |     |
| Linux Host            |  ✔  |             |            |           |     |
| Android Client        |  ✔  |             |      ✔     |     ✔    |  ✔  |
| iOS Client            |  ✔  |             |      ✔     |     ✔    |  ✔  |
| PC Client             |  ✔  |             |            |     ✔     |     |
| Proprietary Client    |      |      ✔     |            |           |     |
| Custom Display Layout |  ✔  |             |      ✔     |     ✔    |  ✔  |
| Completely Free       |  ✔  |             |            |     ✔     | Ads |
| More than one Client  |  ✔  |             |            |     ✔     |     |

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

## Usage
* Run `MFD.jar`
* Make sure you allow it through your firewall if a warning comes up
* Go to your taskbar and click/right-click the MFD icon
* Click on `config`, your browser will open up with the config page
* Optionally change the password - it's randomly generated every time you open MFD.jar
* Either enter the IP address shown into your phone/tablet connected to the same local network, or use the QR code to do it automatically
* On your phone/tablet, choose a display like `Example`
* Enter your password there
* Start using it!

## Random Notes
* MFD is written in Kotlin and javascript, and uses additional code from [SparkJava](http://sparkjava.com/), [JSON-java](https://github.com/stleary/JSON-java), [asmcrypto.js](https://github.com/vibornoff/asmcrypto.js), [NoSleep.js](https://github.com/richtr/NoSleep.js), [QRCode.js](https://github.com/davidshimjs/qrcodejs), and [MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/padStart). MFD also uses [FontAwesome](https://fontawesome.com/) icons in the Example Display
* With minimal HTML, CSS, and Javascript knowledge, making your own displays is easy, as displays are simply websites
  * Place displays in their own folder in the `/displays/` directory, see the `Example` display and its folder
* If you need help, try asking at the official [/r/mfd subreddit](https://www.reddit.com/r/mfd/)
* It's theoretically possible to use MFD to create a remote-desktop application, although the performance would be pretty bad unless you modified the Kotlin code
* As much of the processing as possible is pushed to the secondary device, as it probably isn't doing anything while the main computer is gaming or video editing or streaming
* You can use an absurd about of secondary devices with MFD; they can all be connected to the same display, to different displays, or any combination thereof

## Security Notes
* By its very nature, MFD is extremely powerful and can act as a backdoor into your system by anyone with the password
* So be sure you trust other authors whose displays you use - maliciously crafted displays can be as bad as a virus
* I highly discourage making an easy password, but it's allowed
* Don't accidentally show the config page if you're streaming on Twitch or YouTube
* Have a firewall on your network router, not just your computer
* PLEASE do not port forward MFD onto the internet nor use it on a computer connected directly to a modem... That's just asking for trouble
  * MFD is not rate limited, so even with a strong password, someone with your IP address could eventually figure out your password if you port forward MFD or otherwise have no firewall
* MFD runs on port 80 (the default HTTP port). If you're having problems, check if you're running another webserver

# Changelog

* 0.3.0 - 2Jan18
  * Moved the API from `readme.md` to its own file
  * `asmCrypto` moved from `mfd.js` to its own file for better Firefox performance
  * Fixed vertical alignment and fullscreen problems with Firefox and Safari
  * Fixed strange vertical grid sizing issues with Safari
  * Added some visual cues you're pressing a button in the Star Citizen display
* 0.2.0 - 31Dec17
  * Updated readme
  * Fixed some bugs in `example`'s `index.html` - both the icon and text now share the same `<span>` rather than have their own and not using the correct key does not let you log in anyway
  * Added a simple display for Star Citiz 3.0.0 alpha
* 0.1.0 - 29Dec17
  * Initial release