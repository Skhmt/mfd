
import java.awt.*
import java.awt.image.BufferedImage
import java.net.URI
import javax.imageio.ImageIO

fun tray(state: MFDState) {
    if (!SystemTray.isSupported()) {
        println("SystemTray not supported")
        return
    }

    if (state.verbose) println("> Reading mfd.png")
    // Image reading
    var icon: BufferedImage? = null
    try {
        val fileIcon = state.javaClass.getResource("icon/mfd.png")

        icon = ImageIO.read(fileIcon)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (state.verbose) println("> Creating popup menu")
    // Creating the values we need
    val popup = PopupMenu()
    val trayIcon = TrayIcon(icon, "MFD ${state.version}")
    val tray = SystemTray.getSystemTray()

    if (state.verbose) println("> Creating menuItems")
    // Create pop-up menu components
    val configItem = MenuItem("Config")
    val redditItem = MenuItem("/r/mfd")
    val githubItem = MenuItem("GitHub repo")
    val exitItem = MenuItem("Exit")

    if (state.verbose) println("> Adding components to pop-up menu")
    // Add components to pop-up menu
    popup.add(configItem)
    popup.addSeparator()
    popup.add(redditItem)
    popup.add(githubItem)
    popup.addSeparator()
    popup.add(exitItem)

    trayIcon.popupMenu = popup
    trayIcon.isImageAutoSize = true
    try {
        tray.add(trayIcon)
    } catch (e: AWTException) {
        println("TrayIcon could not be added")
    }

    // Listeners
    redditItem.addActionListener {
        if (Desktop.isDesktopSupported()) {
            val uri = "https://reddit.com/r/mfd"
            try {
                Desktop.getDesktop().browse(URI(uri))
            } catch (e: java.lang.UnsupportedOperationException) {
                println("Please visit the following URL:")
                println(uri)
            }
        }
    }
    githubItem.addActionListener {
        if (Desktop.isDesktopSupported()) {
            val uri = "https://github.com/Skhmt/mfd"
            try {
                Desktop.getDesktop().browse(URI(uri))
            } catch (e: java.lang.UnsupportedOperationException) {
                println("Please visit the following URL:")
                println(uri)
            }
        }
    }
    configItem.addActionListener {
        if (Desktop.isDesktopSupported()) {
            var urlQuery: String
            if (state.setIP == "") {
                urlQuery = getIPv4()
                if (state.port != 80) urlQuery += ":${state.port}"
            }
            else {
                urlQuery = state.setIP
            }

            val uri = "http://localhost:${state.port}/?ip=$urlQuery#.${state.key}"

            println("Please visit the following URL:")
            println(uri)
            try {
                Desktop.getDesktop().browse(URI(uri))
            } catch (e: java.lang.UnsupportedOperationException) {
                // Computer doesn't support the BROWSE operation - likely on Linux
            }
        }
    }
    exitItem.addActionListener {
        tray.remove(trayIcon)
        System.exit(0)
    }

    if (state.verbose) println("> Displaying version message")
    trayIcon.displayMessage("Multi-Function Display", "MFD ${state.version} is running", TrayIcon.MessageType.NONE)
}