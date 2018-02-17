import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

fun main(ars: Array<String>) {
    // Crypto tests

    val crypto = MFDCrypto()
    val password = "password"
    crypto.salt = crypto.generateSalt()
    crypto.iterations = 10000
    crypto.key = crypto.generateKey(password)

    val plainText = "Hello darkness my old friend; I've come to talk to you again."
    val ivText = crypto.encrypt(plainText)

    val unCipherText = crypto.decrypt(ivText[0], ivText[1])

    println("AES GCM")
    println("Iterations:     ${crypto.iterations}")
    println("Password:       $password")
    println("Salt:           ${crypto.byteArrayToHex(crypto.salt)}")
    println("Key:            ${crypto.byteArrayToHex(crypto.key)}")
    println("IV:             ${crypto.byteArrayToHex(ivText[0])}")
    println("Plain Text:     $plainText")
    println("Cipher Text:    ${crypto.byteArrayToHex(ivText[1])}")
    println("Decrypted Text: $unCipherText")

    val hexTest = "8FE3A946A9DDAE47E4C98574B88734E1"
    println("Input hex:      $hexTest")
    println("Output hex:     ${crypto.byteArrayToHex(crypto.hexToByteArray(hexTest))}")

    println("Hex -> Str:     ${crypto.hexToString("48656c6c6f206461726b6e657373206d79206f6c6420667269656e64212121")}")

    println("\nTest using constants:")
    crypto.key = crypto.hexToByteArray("A99F75EB6BD0A9D7E44E2F0EF22B47C0")
    println(
            crypto.decrypt(
                    crypto.hexToByteArray("CD0F513186B2CC2371D5CA4E"),
                    crypto.hexToByteArray("EABC8ECEAD2914A580745DED16C7D67217A824B1ADB0EBFA970F9453E32587A3A6033529F5337401FC61FE89E3A332DBC2CB4218AC9B91180B6C7D95E92F94BBB5D315C16CCBAE31CEA787328E")
            )
    )

    println("\nRandom password generation:")
    println(crypto.randomPassword(9))
    println(crypto.randomPassword(9))
}

class MFDCrypto() {

    var adata = "mfd-1.0.0".toByteArray()

    var iterations = 10000
    var salt = ByteArray(0)
    var key = ByteArray(0)
    var random = SecureRandom()

    fun randomPassword(length: Int): String {
        val alphaUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val alphaLower = "abcdefghijklmnopqrstuvwxyz"
        val deciNums = "1234567890"
        val chars = alphaUpper + alphaLower + deciNums

        var password = ""
        for (i in 1..length) {
            val nextCharIndex = random.nextInt(chars.length)
            password += chars[nextCharIndex]
        }

        return password
    }

    fun encrypt(plainText: String): Array<ByteArray> {
        val sKey = SecretKeySpec(key, "AES")
        val iv = ByteArray(12)
        random.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.ENCRYPT_MODE, sKey, gcmSpec)
        cipher.updateAAD(adata)
        val cipherText: ByteArray = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return arrayOf(iv, cipherText)
    }

    @Throws(javax.crypto.AEADBadTagException::class)
    fun decrypt(iv: ByteArray, cipherText: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val sKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, sKey, gcmSpec)
        cipher.updateAAD(adata)
        return String(cipher.doFinal(cipherText))
    }

    // sets the salt property and also returns the value that it set it to
    fun generateSalt(): ByteArray {
        val sr = SecureRandom.getInstance("SHA1PRNG")
        val saltBA = ByteArray(16)
        sr.nextBytes(saltBA)
        salt = saltBA
        return saltBA
    }

    // sets the key property and also returns the value that it set it to
    fun generateKey(password: String): ByteArray {
        val chars: CharArray = password.toCharArray()
        val spec = PBEKeySpec(chars, salt, iterations, 16*8) // 16, 24, or 32
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash: ByteArray = skf.generateSecret(spec).encoded
        key = hash
        return hash
    }

    fun byteArrayToHex(array: ByteArray): String {
        var output = ""
        for (item in array) {
            output += String.format("%02X", item)
        }
        return output
    }

    fun hexToByteArray(hex: String): ByteArray {
        val hexUC = hex.toUpperCase()
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = hex.length/2
        var output = ByteArray(bytes)
        for (i in 0..bytes-1) {
            val index = i*2
            val firstDec = HEX_CHARS.indexOf(hexUC[index]);
            val secondDec = HEX_CHARS.indexOf(hexUC[index + 1]);
            val octet = firstDec.shl(4).or(secondDec)
            output[i] = octet.toByte()
        }
        return output
    }

    fun hexToString(hex: String): String {
        var output = ""
        val bytes = hex.length/2
        for (i in 0..bytes-1) {
            val index = i*2
            val newChar = hex.substring(index, index+2)
            output += Integer.parseInt(newChar, 16).toChar()
        }
        return output
    }
}