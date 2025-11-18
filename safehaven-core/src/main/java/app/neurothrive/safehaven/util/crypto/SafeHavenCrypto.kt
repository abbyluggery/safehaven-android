package app.neurothrive.safehaven.util.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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
 * SafeHavenCrypto - AES-256-GCM encryption with Android KeyStore
 * CRITICAL SECURITY COMPONENT
 */
object SafeHavenCrypto {
    
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "SafeHavenMasterKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12
    
    /**
     * Initialize encryption key (call once on app first run)
     * MUST be called in Application.onCreate()
     */
    fun initializeKey() {
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
     * Encrypt string data with AES-256-GCM
     * @param plaintext String to encrypt
     * @return Base64-encoded encrypted string with IV prepended
     */
    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        
        // Prepend IV to ciphertext
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }
    
    /**
     * Decrypt string data
     * @param encrypted Base64-encoded encrypted string
     * @return Original plaintext
     */
    fun decrypt(encrypted: String): String {
        if (encrypted.isEmpty()) return ""
        
        val combined = Base64.decode(encrypted, Base64.NO_WRAP)
        
        // Extract IV (first 12 bytes for GCM)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }
    
    /**
     * Encrypt file (for photos, videos, PDFs)
     * CRITICAL: Used for evidence encryption
     * @param inputFile Unencrypted file
     * @param outputFile Encrypted file destination
     */
    fun encryptFile(inputFile: File, outputFile: File) {
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
    }
    
    /**
     * Decrypt file
     * CRITICAL: Used for viewing evidence
     * @param inputFile Encrypted file
     * @param outputFile Decrypted file destination
     */
    fun decryptFile(inputFile: File, outputFile: File) {
        FileInputStream(inputFile).use { input ->
            // Read IV
            val iv = ByteArray(GCM_IV_LENGTH)
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
    }
    
    /**
     * Generate SHA-256 hash for document verification
     * CRITICAL: For blockchain timestamping
     * @param file File to hash
     * @return 64-character hex string
     */
    fun generateSHA256(file: File): String {
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
    }
    
    /**
     * Secure wipe (overwrite with random data before deleting)
     * CRITICAL: For panic delete - prevents forensic recovery
     * @param file File to securely delete
     */
    fun secureDelete(file: File) {
        if (file.exists() && file.isFile) {
            val random = SecureRandom()
            val fileSize = file.length()
            
            try {
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var remaining = fileSize
                    while (remaining > 0) {
                        val toWrite = minOf(remaining, buffer.size.toLong()).toInt()
                        random.nextBytes(buffer)
                        output.write(buffer, 0, toWrite)
                        remaining -= toWrite
                    }
                    output.flush()
                }
                
                // Delete after overwriting
                file.delete()
            } catch (e: Exception) {
                // Even if secure wipe fails, still delete
                file.delete()
            }
        }
    }
    
    /**
     * Hash password with SHA-256
     * @param password Password to hash
     * @return Hex string hash
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
