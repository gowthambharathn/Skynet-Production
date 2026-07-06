package skynetbee.developers.production.NeuralEngine.Database

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class SecurityKeyGenerator(private val context: Context) {

    private val MASTER_SECRET: String = "M@\$t3rS3cr3tK3y!2024#Secure"

    private val HASH_SALT: String = "Xk9#mP2@qL5vR8!n"


    data class SecurityKeyResult(
        val originalImei    : String,
        val transformedImei : String,
        val sha256Hash      : String,
        val aesEncrypted    : String,
        val manipulatedString: String,
        val derivedKeyFor7  : String,
        val finalAesKey     : String
    )

    fun generateSecurityKey(): SecurityKeyResult {

        val originalImei = getDeviceIdentifier()

        val transformedImei = applyStringInjection(originalImei)
        val sha256Hash = computeSha256(transformedImei + HASH_SALT)

        val masterKeyBytes = normaliseAesKey(MASTER_SECRET, 32, asBytes = true)
        val aesEncrypted   = encryptAesCbc(sha256Hash, masterKeyBytes)

        val manipulated = applyStringManipulation(aesEncrypted)

        val first24        = extractFirst24(aesEncrypted)
        val derivedKeyFor7 = normaliseAesKey(first24, 32)
        val derivedKeyBytes= normaliseAesKey(first24, 32, asBytes = true)

        val secondAesRaw = encryptAesCbc(sha256Hash, derivedKeyBytes)
        val finalAesKey  = sanitiseToAesKey(secondAesRaw, targetBytes = 32)

        return SecurityKeyResult(
            originalImei     = originalImei,
            transformedImei  = transformedImei,
            sha256Hash       = sha256Hash,
            aesEncrypted     = aesEncrypted,
            manipulatedString= manipulated,
            derivedKeyFor7   = derivedKeyFor7,
            finalAesKey      = finalAesKey
        )
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun getDeviceIdentifier(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return getFallbackAndroidId()
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val imei: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tm?.getImei(0)
            } else {
                @Suppress("DEPRECATION")
                tm?.deviceId
            }
            if (!imei.isNullOrBlank() && imei.matches(Regex("\\d{15}"))) imei
            else getFallbackAndroidId()
        } catch (e: SecurityException) {
            getFallbackAndroidId()
        } catch (e: Exception) {
            getFallbackAndroidId()
        }
    }

    @SuppressLint("HardwareIds")
    private fun getFallbackAndroidId(): String {
        val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return if (!id.isNullOrBlank()) id else "FALLBACK_${Build.BOARD}"
    }
    fun applyStringInjection(input: String): String {
        var s = input

        s = insertAt(s, 2, "0")

        s = insertAt(s, s.length / 2, "X9")

        val rng  = seededRandom(input)
        val pool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#"
        val step = 5
        val sb   = StringBuilder()
        var count = 0
        for (ch in s) {
            sb.append(ch); count++
            if (count % step == 0) {
                sb.append(pool[rng.nextInt(pool.length)])
                sb.append(pool[rng.nextInt(pool.length)])
            }
        }
        s = sb.toString()

        s += computeSha256(input).takeLast(8).uppercase()

        return s
    }

    private fun insertAt(str: String, index: Int, value: String): String {
        val i = index.coerceIn(0, str.length)
        return str.substring(0, i) + value + str.substring(i)
    }

    private fun seededRandom(seed: String): SecureRandom =
        SecureRandom(
            MessageDigest.getInstance("SHA-256")
                .digest(seed.toByteArray(StandardCharsets.UTF_8))
        )

    fun computeSha256(input: String): String =
        try {
            bytesToHex(
                MessageDigest.getInstance("SHA-256")
                    .digest(input.toByteArray(StandardCharsets.UTF_8))
            )
        } catch (e: Exception) {
            throw SecurityException("SHA-256 unavailable", e)
        }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    fun encryptAesCbc(plaintext: String, aesKeyBytes: ByteArray): String =
        try {
            val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").also {
                it.init(
                    Cipher.ENCRYPT_MODE,
                    SecretKeySpec(aesKeyBytes, "AES"),
                    IvParameterSpec(iv)
                )
            }
            Base64.encodeToString(
                iv + cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8)),
                Base64.NO_WRAP
            )
        } catch (e: Exception) {
            throw SecurityException("AES encryption failed", e)
        }

    fun decryptAesCbc(base64Input: String, aesKeyBytes: ByteArray): String =
        try {
            val raw        = Base64.decode(base64Input, Base64.NO_WRAP)
            val iv         = raw.copyOfRange(0, 16)
            val cipherText = raw.copyOfRange(16, raw.size)
            val cipher     = Cipher.getInstance("AES/CBC/PKCS5Padding").also {
                it.init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(aesKeyBytes, "AES"),
                    IvParameterSpec(iv)
                )
            }
            String(cipher.doFinal(cipherText), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("AES decryption failed", e)
        }

    fun applyStringManipulation(input: String): String {
        var s = input
        s = reverseSection(s, s.length / 4, s.length / 2)
        s = rotateLeft(s, (s.length % 7) + 3)
        s = swapMiddleAndEdges(s)
        s = shuffleChars(s)
        s = rotateRight(s, PRIME_OFFSET)
        return s
    }

    private fun reverseSection(str: String, from: Int, to: Int): String {
        if (from >= to || str.isEmpty()) return str
        val c = str.toCharArray()
        var l = from.coerceIn(0, c.size - 1)
        var r = (to - 1).coerceIn(0, c.size - 1)
        while (l < r) { val t = c[l]; c[l] = c[r]; c[r] = t; l++; r-- }
        return String(c)
    }

    private fun rotateLeft(str: String, n: Int): String {
        if (str.isEmpty()) return str
        val s = n % str.length
        return str.substring(s) + str.substring(0, s)
    }

    private fun swapMiddleAndEdges(str: String): String {
        if (str.length < 3) return str
        val t = str.length / 3
        return str.substring(t, 2 * t) + str.substring(0, t) + str.substring(2 * t)
    }

    private fun shuffleChars(str: String): String {
        val c = str.toCharArray()
        val r = seededRandom(str)
        for (i in c.indices.reversed()) {
            val j = r.nextInt(i + 1)
            val t = c[i]; c[i] = c[j]; c[j] = t
        }
        return String(c)
    }

    private fun rotateRight(str: String, n: Int): String {
        if (str.isEmpty()) return str
        val s = n % str.length
        return str.substring(str.length - s) + str.substring(0, str.length - s)
    }

    private fun extractFirst24(source: String): String {
        if (source.length >= 24) return source.substring(0, 24)
        // Defensive pad — unlikely in practice
        var padded = source
        while (padded.length < 24) padded += computeSha256(padded)
        return padded.substring(0, 24)
    }

    fun normaliseAesKey(raw: String, targetBytes: Int = 32): String {
        require(targetBytes == 16 || targetBytes == 32) {
            "AES key size must be 16 or 32 bytes, got $targetBytes"
        }
        return computeSha256(raw).substring(0, targetBytes * 2)
    }

    fun normaliseAesKey(raw: String, targetBytes: Int = 32, asBytes: Boolean): ByteArray =
        hexToBytes(normaliseAesKey(raw, targetBytes))

    private fun hexToBytes(hex: String): ByteArray {
        val out = ByteArray(hex.length / 2)
        for (i in out.indices)
            out[i] = ((Character.digit(hex[i * 2], 16) shl 4) +
                    Character.digit(hex[i * 2 + 1], 16)).toByte()
        return out
    }

    private fun sanitiseToAesKey(base64Input: String, targetBytes: Int): String {
        val clean = base64Input.filter { it.isLetterOrDigit() || it == '+' || it == '/' }
        if (clean.length >= targetBytes) return clean.substring(0, targetBytes)
        var padded = clean
        while (padded.length < targetBytes) {
            padded += computeSha256(padded).filter { it.isLetterOrDigit() }
        }
        return padded.substring(0, targetBytes)
    }


    companion object {
        private const val TAG          = "SecurityKeyGenerator"
        private const val PRIME_OFFSET = 13
    }
}


fun SecurityKeyGenerator.SecurityKeyResult.prettyPrint() {
    val div = "═".repeat(66)
    println(div)
    println("   SECURITY KEY PIPELINE — FULL RESULT")
    println(div)
    println("  [Step 1] Original IMEI / Android ID")
    println("           $originalImei")
    println()
    println("  [Step 2] Transformed IMEI (injected)")
    println("           $transformedImei")
    println()
    println("  [Step 3] SHA-256 Hash  ← also plaintext for Step 7")
    println("           $sha256Hash")
    println()
    println("  [Step 4] AES-CBC Pass 1 — Base64 output")
    println("           $aesEncrypted")
    println()
    println("  [Step 5] Manipulated String")
    println("           $manipulatedString")
    println()
    println("  [Step 6] Derived Key (first 24 of Step 4 → normalised 32-byte hex)")
    println("           $derivedKeyFor7")
    println()
    println("  [Step 7] ★  FINAL AES-256 SECURITY KEY  ★")
    println("           (AES-CBC: plaintext=SHA256, key=derivedKey)")
    println("           $finalAesKey")
    println(div)
}