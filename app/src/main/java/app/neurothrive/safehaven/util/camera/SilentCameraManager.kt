package app.neurothrive.safehaven.util.camera

import android.content.Context
import android.media.AudioManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import app.neurothrive.safehaven.data.database.entities.EvidenceItem
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Silent Camera Manager
 *
 * CRITICAL FEATURES:
 * - NO camera shutter sound (mutes system volume during capture)
 * - NO flash by default
 * - GPS metadata stripped
 * - Immediate encryption after capture
 * - No gallery thumbnails
 * - Temp files securely deleted
 *
 * This is THE core differentiator for SafeHaven - enables survivors to
 * document abuse silently without alerting the abuser.
 */
class SilentCameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    private var imageCapture: ImageCapture? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var cameraProvider: ProcessCameraProvider? = null

    /**
     * Initialize camera preview
     */
    fun initialize(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                // Image capture with NO FLASH DEFAULT
                imageCapture = ImageCapture.Builder()
                    .setFlashMode(ImageCapture.FLASH_MODE_OFF) // CRITICAL: No flash
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Use back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind all before rebinding
                cameraProvider?.unbindAll()

                // Bind to lifecycle
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                Timber.d("Silent camera initialized")
            } catch (e: Exception) {
                Timber.e(e, "Camera initialization failed")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Capture photo SILENTLY
     *
     * CRITICAL STEPS:
     * 1. Mute system volume
     * 2. Capture photo
     * 3. Restore volume
     * 4. Strip GPS metadata
     * 5. Encrypt file
     * 6. Delete temp file
     * 7. Return EvidenceItem
     */
    suspend fun capturePhotoSilently(
        userId: String,
        incidentId: String? = null
    ): Result<EvidenceItem> = withContext(Dispatchers.IO) {
        var originalVolume = 0

        try {
            // STEP 1: MUTE SYSTEM VOLUME (prevents shutter sound)
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)

            Timber.d("System volume muted for silent capture")

            // STEP 2: Create temp file
            val photoFile = File.createTempFile("evidence_", ".jpg", context.cacheDir)

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            // STEP 3: Capture photo
            val result = suspendCoroutine<ImageCapture.OutputFileResults> { continuation ->
                imageCapture?.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            continuation.resume(output)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            continuation.resumeWithException(exception)
                        }
                    }
                )
            }

            // STEP 4: RESTORE VOLUME
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalVolume, 0)

            Timber.d("Photo captured silently: ${photoFile.name}")

            // STEP 5: Strip GPS metadata
            MetadataStripper.removeGPS(photoFile)

            // STEP 6: Encrypt file
            val encryptedFile = File(context.filesDir, "evidence_${UUID.randomUUID()}.enc")
            SafeHavenCrypto.encryptFile(photoFile, encryptedFile)

            Timber.d("Photo encrypted: ${encryptedFile.name}")

            // STEP 7: Delete temp file (secure delete)
            SafeHavenCrypto.secureDelete(photoFile)

            // STEP 8: Create EvidenceItem
            val evidenceItem = EvidenceItem(
                userId = userId,
                evidenceType = "photo",
                timestamp = System.currentTimeMillis(),
                encryptedFilePath = encryptedFile.absolutePath,
                originalFileName = "Photo_${System.currentTimeMillis()}.jpg",
                fileSize = encryptedFile.length(),
                mimeType = "image/jpeg",
                incidentReportId = incidentId
            )

            Timber.d("Evidence item created: ${evidenceItem.id}")

            Result.success(evidenceItem)

        } catch (e: Exception) {
            // ALWAYS restore volume on error
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalVolume, 0)
            } catch (volumeError: Exception) {
                Timber.e(volumeError, "Failed to restore volume")
            }

            Timber.e(e, "Silent photo capture failed")
            Result.failure(e)
        }
    }

    /**
     * Toggle flash mode
     */
    fun setFlashMode(enabled: Boolean) {
        imageCapture?.flashMode = if (enabled) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    /**
     * Release camera resources
     */
    fun release() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        Timber.d("Camera released")
    }
}
