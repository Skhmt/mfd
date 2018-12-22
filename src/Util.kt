import com.sun.jna.Platform
import org.json.JSONObject
import java.net.Inet4Address

data class MFDState(
        var port: Int,
        val verbose: Boolean,
        var key: String,
        val safeMode: Boolean,
        val setIP: String,
        val version: String,
        val name: String,
        val crypto: MFDCrypto,
        val os: String
)

fun getOS(): String {
    val os = when {
        Platform.isWindows() -> "Windows"
        Platform.isMac() -> "macOS"
        Platform.isLinux() -> "*nix"
        else -> "unknown"
    }

    return os
}

fun getIPv4(): String {
    val ip: ByteArray = Inet4Address.getLocalHost().address
    val oct1: Int = if (ip[0].toInt() < 0) ip[0].toInt() + 256 else ip[0].toInt()
    val oct2: Int = if (ip[1].toInt() < 0) ip[1].toInt() + 256 else ip[1].toInt()
    val oct3: Int = if (ip[2].toInt() < 0) ip[2].toInt() + 256 else ip[2].toInt()
    val oct4: Int = if (ip[3].toInt() < 0) ip[3].toInt() + 256 else ip[3].toInt()
    return "$oct1.$oct2.$oct3.$oct4"
}

fun setCrypto(state: MFDState) {
    if (state.verbose) println("> Generating salt")
    state.crypto.salt = state.crypto.generateSalt()
    state.crypto.iterations = 10000 // default value is 10000

    if (state.verbose) println("> Generating key")
    state.crypto.key = state.crypto.generateKey(state.key)
}

fun getCrypto(state: MFDState): String {
    val salt = state.crypto.byteArrayToHex(state.crypto.salt)
    val iterations = state.crypto.iterations
    val json = JSONObject()
    json.put("salt", salt)
    json.put("iterations", iterations)
    return json.toString()
}