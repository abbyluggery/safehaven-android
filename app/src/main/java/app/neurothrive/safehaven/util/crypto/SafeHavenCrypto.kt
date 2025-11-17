package app.neurothrive.safehaven.util.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * SafeHaven Encryption Utility
 *
 * CRITICAL SECURITY FEATURES:
 * - AES-256-GCM encryption (authenticated encryption)
 * - Android KeyStore integration (hardware-backed if available)
 * - File encryption for photos/videos/PDFs
 * - SHA-256 hashing for document verification
 * - Secure deletion with random overwrite
 *
 * Usage:
 * - Call initializeKey() once on app first launch
 * - Use encrypt()/decrypt() for database fields
 * - Use encryptFile()/decryptFile() for evidence files
 * - Use generateSHA256() for document verification
 * - Use secureDelete() for panic delete feature
 */
object SafeHavenCrypto {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "SafeHavenMasterKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    /**
     * Initialize encryption key (call once on app first run)
     * Creates AES-256 key in Android KeyStore
     */
    fun initializeKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )

                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)  // No biometric for panic delete speed
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()

                Timber.d("SafeHaven encryption key initialized")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize encryption key")
            throw e
        }
    }

    /**
     * Get secret key from Android KeyStore
     */
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Encrypt string data (for database fields)
     * Returns Base64-encoded string with IV prepended
     */
    fun encrypt(plaintext: String): String {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

            val iv = cipher.iv
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            // Prepend IV to ciphertext
            val combined = iv + ciphertext
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e(e, "Encryption failed")
            throw e
        }
    }

    /**
     * Decrypt string data
     */
    fun decrypt(encrypted: String): String {
        try {
            val combined = Base64.decode(encrypted, Base64.NO_WRAP)

            // Extract IV (first 12 bytes for GCM)
            val iv = combined.copyOfRange(0, 12)
            val ciphertext = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val plaintext = cipher.doFinal(ciphertext)
            return String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            Timber.e(e, "Decryption failed")
            throw e
        }
    }

    /**
     * Encrypt file (photos, videos, PDFs)
     * IV is written to the beginning of the output file
     */
    fun encryptFile(inputFile: File, outputFile: File) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

            val iv = cipher.iv

            FileInputStream(inputFile).use { input ->
                FileOutputStream(outputFile).use { output ->
                    // Write IV first
                    output.write(iv)

                    // Encrypt file in chunks
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val encrypted = cipher.update(buffer, 0, bytesRead)
                        if (encrypted != null) {
                            output.write(encrypted)
                        }
                    }

                    // Write final block
                    val finalBlock = cipher.doFinal()
                    if (finalBlock != null) {
                        output.write(finalBlock)
                    }
                }
            }

            Timber.d("File encrypted: ${inputFile.name} -> ${outputFile.name}")
        } catch (e: Exception) {
            Timber.e(e, "File encryption failed")
            throw e
        }
    }

    /**
     * Decrypt file
     * Reads IV from the beginning of the input file
     */
    fun decryptFile(inputFile: File, outputFile: File) {
        try {
            FileInputStream(inputFile).use { input ->
                // Read IV
                val iv = ByteArray(12)
                input.read(iv)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val decrypted = cipher.update(buffer, 0, bytesRead)
                        if (decrypted != null) {
                            output.write(decrypted)
                        }
                    }

                    val finalBlock = cipher.doFinal()
                    if (finalBlock != null) {
                        output.write(finalBlock)
                    }
                }
            }

            Timber.d("File decrypted: ${inputFile.name} -> ${outputFile.name}")
        } catch (e: Exception) {
            Timber.e(e, "File decryption failed")
            throw e
        }
    }

    /**
     * Generate SHA-256 hash (for document verification)
     * Returns 64-character hex string
     */
    fun generateSHA256(file: File): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val hashBytes = digest.digest()
            return hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Timber.e(e, "SHA-256 hash generation failed")
            throw e
        }
    }

    /**
     * Secure wipe (overwrite with random data before deleting)
     * CRITICAL for panic delete feature
     */
    fun secureDelete(file: File) {
        try {
            if (file.exists()) {
                val random = SecureRandom()
                val fileSize = file.length()

                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var remaining = fileSize
                    while (remaining > 0) {
                        val toWrite = minOf(remaining, buffer.size.toLong()).toInt()
                        random.nextBytes(buffer)
                        output.write(buffer, 0, toWrite)
                        remaining -= toWrite
                    }
                }

                file.delete()
                Timber.d("File securely deleted: ${file.name}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Secure deletion failed")
            throw e
        }
    }
}
