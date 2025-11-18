package app.neurothrive.safehaven.domain.usecases

import android.content.Context
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * PanicDeleteUseCase - Emergency data deletion
 * CRITICAL: Must complete in <2 seconds
 * Triggered by shake gesture or duress password
 */
class PanicDeleteUseCase @Inject constructor(
    private val repository: SafeHavenRepository,
    @ApplicationContext private val context: Context
) {
    
    /**
     * Execute panic delete
     * CRITICAL STEPS:
     * 1. Get all evidence files
     * 2. Securely delete all files (overwrite then delete)
     * 3. Delete database records
     * 4. Clear app cache
     * 
     * @param userId User ID
     * @return Result<Unit>
     */
    suspend fun execute(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // STEP 1: Get all evidence files
            val evidenceItems = repository.getAllEvidence(userId)
            val documents = repository.getAllDocuments(userId)
            
            // STEP 2: Securely delete all files
            evidenceItems.forEach { item ->
                val file = File(item.encryptedFilePath)
                if (file.exists()) {
                    SafeHavenCrypto.secureDelete(file)  // Overwrite then delete
                }
            }
            
            documents.forEach { doc ->
                File(doc.originalPhotoPathEncrypted).let { file ->
                    if (file.exists()) SafeHavenCrypto.secureDelete(file)
                }
                File(doc.verifiedPDFPathEncrypted).let { file ->
                    if (file.exists()) SafeHavenCrypto.secureDelete(file)
                }
            }
            
            // STEP 3: Delete database records
            repository.deleteAllIncidents(userId)
            repository.deleteAllEvidence(userId)
            repository.deleteAllDocuments(userId)
            repository.deleteSurvivorProfile(userId)
            repository.deleteProfile(userId)
            
            // STEP 4: Clear app cache
            context.cacheDir.deleteRecursively()
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
