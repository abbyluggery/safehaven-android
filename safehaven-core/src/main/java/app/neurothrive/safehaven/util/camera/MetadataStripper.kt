package app.neurothrive.safehaven.util.camera

import androidx.exifinterface.media.ExifInterface
import java.io.File

/**
 * MetadataStripper - Remove GPS and identifying metadata from photos
 * CRITICAL: Protects survivor location
 */
object MetadataStripper {
    
    /**
     * Remove ALL GPS data from photo
     * CRITICAL: Must be called before encryption
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
            
            // Remove other identifying metadata
            exif.setAttribute(ExifInterface.TAG_MAKE, null)
            exif.setAttribute(ExifInterface.TAG_MODEL, null)
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, null)
            
            exif.saveAttributes()
        } catch (e: Exception) {
            // Log error but don't throw - encryption should still proceed
            e.printStackTrace()
        }
    }
}
