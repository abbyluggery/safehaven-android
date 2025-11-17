package app.neurothrive.safehaven.domain.usecases

import android.content.Context
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Panic Delete Use Case
 *
 * CRITICAL SAFETY FEATURE:
 * Securely deletes ALL SafeHaven data in <2 seconds when triggered.
 *
 * What gets deleted:
 * - All evidence files (photos, videos, audio) - securely overwritten
 * - All incident reports
 * - All verified documents
 * - User profile
 * - App cache
 * - Database records
 *
 * Triggered by:
 * - Shake-to-delete (3 rapid shakes)
 * - Duress password entry
 * - Manual panic button
 *
 * PERFORMANCE TARGET: Complete deletion in < 2 seconds
 */
class PanicDeleteUseCase @Inject constructor(
    private val repository: SafeHavenRepository,
    @ApplicationContext private val context: Context
) {

    /**
     * Execute panic delete
     * Returns success/failure result
     */
    suspend fun execute(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            Timber.w("PANIC DELETE INITIATED for user: $userId")

            // STEP 1: Get all evidence files before deleting database records
            val evidenceItems = repository.getAllEvidence(userId)
            val documents = repository.getAllDocuments(userId)

            Timber.d("Found ${evidenceItems.size} evidence items, ${documents.size} documents")

            // STEP 2: Securely delete all evidence files
            evidenceItems.forEach { item ->
                val file = File(item.encryptedFilePath)
                if (file.exists()) {
                    try {
                        SafeHavenCrypto.secureDelete(file)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to delete evidence file: ${file.name}")
                        // Continue deleting other files even if one fails
                    }
                }
            }

            // STEP 3: Securely delete all verified document files
            documents.forEach { doc ->
                try {
                    val photoFile = File(doc.originalPhotoPathEncrypted)
                    if (photoFile.exists()) {
                        SafeHavenCrypto.secureDelete(photoFile)
                    }

                    val pdfFile = File(doc.verifiedPDFPathEncrypted)
                    if (pdfFile.exists()) {
                        SafeHavenCrypto.secureDelete(pdfFile)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to delete document files")
                }
            }

            // STEP 4: Delete database records (cascades due to foreign keys)
            repository.deleteAllIncidents(userId)
            repository.deleteAllEvidence(userId)
            repository.deleteAllDocuments(userId)
            repository.deleteProfile(userId)

            // STEP 5: Clear app cache
            context.cacheDir.deleteRecursively()

            val elapsedTime = System.currentTimeMillis() - startTime
            Timber.w("PANIC DELETE COMPLETED in ${elapsedTime}ms")

            if (elapsedTime > 2000) {
                Timber.w("WARNING: Panic delete took ${elapsedTime}ms (target: <2000ms)")
            }

            Result.success(Unit)

        } catch (e: Exception) {
            val elapsedTime = System.currentTimeMillis() - startTime
            Timber.e(e, "PANIC DELETE FAILED after ${elapsedTime}ms")
            Result.failure(e)
        }
    }

    /**
     * Estimate deletion time based on file count
     * Used to warn user if deletion might take too long
     */
    suspend fun estimateDeletionTime(userId: String): Long {
        return try {
            val evidenceCount = repository.getAllEvidence(userId).size
            val documentCount = repository.getAllDocuments(userId).size

            // Rough estimate: 50ms per file
            (evidenceCount + documentCount) * 50L
        } catch (e: Exception) {
            0L
        }
    }
}
