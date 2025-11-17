package app.neurothrive.safehaven.util.camera

import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.File

/**
 * Metadata Stripper
 *
 * CRITICAL SECURITY: Removes GPS and identifying metadata from photos
 * to protect survivor location and identity.
 *
 * Removes:
 * - GPS coordinates (latitude, longitude, altitude)
 * - GPS timestamps
 * - Device make/model
 * - Camera settings (optional)
 */
object MetadataStripper {

    /**
     * Remove GPS metadata from photo
     * CRITICAL: Call this before encrypting and storing evidence photos
     */
    fun removeGPS(file: File) {
        try {
            val exif = ExifInterface(file.absolutePath)

            // Remove ALL location data
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null)
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null)
            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, null)
            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null)
            exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null)
            exif.setAttribute(ExifInterface.TAG_GPS_SPEED, null)
            exif.setAttribute(ExifInterface.TAG_GPS_SPEED_REF, null)

            exif.saveAttributes()

            Timber.d("GPS metadata stripped from ${file.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to strip GPS metadata")
            throw e
        }
    }

    /**
     * Remove ALL metadata (most secure, but loses timestamp)
     * Use this for maximum anonymity
     */
    fun removeAllMetadata(file: File) {
        try {
            val exif = ExifInterface(file.absolutePath)

            // Remove GPS
            removeGPS(file)

            // Remove device info
            exif.setAttribute(ExifInterface.TAG_MAKE, null)
            exif.setAttribute(ExifInterface.TAG_MODEL, null)
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, null)

            // Remove camera settings
            exif.setAttribute(ExifInterface.TAG_FLASH, null)
            exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, null)
            exif.setAttribute(ExifInterface.TAG_APERTURE_VALUE, null)
            exif.setAttribute(ExifInterface.TAG_ISO_SPEED, null)

            // Remove timestamps (survivor can add manually if needed)
            exif.setAttribute(ExifInterface.TAG_DATETIME, null)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, null)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, null)

            exif.saveAttributes()

            Timber.d("All metadata stripped from ${file.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to strip all metadata")
            throw e
        }
    }
}
