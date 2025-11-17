package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Emergency Contact Entity
 * Stores trusted contacts for SOS panic button alerts
 *
 * Used for:
 * - SOS emergency SMS alerts
 * - Location sharing during emergencies
 * - Emergency alert escalation
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // User who owns this contact
    val userId: String,

    // Contact Information
    val name: String,
    val phoneNumber: String,
    val relationship: String, // "friend", "family", "advocate", "attorney", "therapist"

    // Priority (primary contacts alerted first, secondary after 15 min if no response)
    val isPrimary: Boolean = true, // Primary = always alerted, Secondary = escalation only

    // Custom message for this contact (optional)
    val customMessage: String? = null,

    // Verification
    val lastTestedDate: Long? = null, // Timestamp of last test alert
    val isVerified: Boolean = false, // Contact confirmed they received test alert

    // Alert preferences
    val sendLocationUpdates: Boolean = true, // Send GPS updates every 5 minutes
    val sendSilentRecordingNotification: Boolean = false, // Notify if silent recording is active

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),

    // Sync
    val syncedToSalesforce: Boolean = false
)

/**
 * Emergency Contact Group Types
 */
enum class ContactRelationship {
    FRIEND,
    FAMILY,
    ADVOCATE,
    ATTORNEY,
    THERAPIST,
    DOMESTIC_VIOLENCE_SHELTER,
    CRISIS_HOTLINE,
    OTHER
}
