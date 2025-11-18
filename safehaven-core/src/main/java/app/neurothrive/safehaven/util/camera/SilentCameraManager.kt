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
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * SilentCameraManager - CRITICAL SECURITY FEATURE
 * Captures photos without sound, flash, or GPS metadata
 */
class SilentCameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var imageCapture: ImageCapture? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    /**
     * Initialize camera with preview
     * IMPORTANT: Flash mode OFF by default
     */
    fun initialize(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            // Preview
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            
            // Image capture with NO FLASH DEFAULT
            imageCapture = ImageCapture.Builder()
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)  // CRITICAL
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            // Bind to back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * Capture photo SILENTLY
     * CRITICAL STEPS:
     * 1. Mute system volume
     * 2. Capture photo
     * 3. Restore volume
     * 4. Strip GPS metadata
     * 5. Encrypt file
     * 6. Delete temp file
     * 
     * @param userId User ID
     * @param incidentId Optional incident report ID to link
     * @return Result<EvidenceItem>
     */
    suspend fun capturePhotoSilently(
        userId: String,
        incidentId: String?
    ): Result<EvidenceItem> = withContext(Dispatchers.IO) {
        val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
        
        try {
            // STEP 1: MUTE SYSTEM VOLUME
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0)
            
            // STEP 2: Create temp file
            val photoFile = File.createTempFile("evidence_", ".jpg", context.cacheDir)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            
            // STEP 3: Capture photo
            suspendCoroutine<ImageCapture.OutputFileResults> { continuation ->
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
                ) ?: continuation.resumeWithException(
                    IllegalStateException("ImageCapture not initialized")
                )
            }
            
            // STEP 4: RESTORE VOLUME
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalVolume, 0)
            
            // STEP 5: Strip GPS metadata
            MetadataStripper.removeGPS(photoFile)
            
            // STEP 6: Encrypt file
            val encryptedFile = File(context.filesDir, "evidence_${UUID.randomUUID()}.enc")
            SafeHavenCrypto.encryptFile(photoFile, encryptedFile)
            
            // STEP 7: Delete temp file securely
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
            
            Result.success(evidenceItem)
            
        } catch (e: Exception) {
            // ALWAYS restore volume on error
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, originalVolume, 0)
            Result.failure(e)
        }
    }
}
