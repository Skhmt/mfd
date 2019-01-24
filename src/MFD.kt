/**
 * Multi-Function Display
 * https://github.com/Skhmt/mfd
 *
 * This creates a SystemTray and a webserver.
 *
 * @author skhmt
 */

fun main(args: Array<String>) {

    val name = "MFD"
    val version = "1.7.0"

    var port = 80
    var verbose = false
    var key = ""
    var safeMode = false
    var setIP = ""

    for (i in 0 until args.size) {
        when (args[i]) {
            "-p" -> {
                if (args.size > i + 1) port = args[i+1].toInt()
            }
            "-v" -> {
                verbose = true
            }
            "-k" -> {
                if (args.size > i+1) key = args[i+1]
            }
            "-s" -> {
                safeMode = true
            }
            "-ip" -> {
                if (args.size > i+1) setIP = args[i+1]
            }
            else -> {}
        }
    }

    val state = MFDState(
            port,
            verbose,
            key,
            safeMode,
            setIP,
            version,
            name,
            MFDCrypto(),
            getOS()
    )

    if (verbose) println("> Creating tray icon")
    tray(state)
    if (verbose) println("> Creating spark server")
    server(state)
    if (verbose) println("> --- ${state.name} initialized")
}