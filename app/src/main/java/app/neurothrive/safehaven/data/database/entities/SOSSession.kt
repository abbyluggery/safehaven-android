package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SOS Session Entity
 * Tracks active and historical SOS panic button activations
 *
 * Used for:
 * - Recording when SOS was activated
 * - Tracking duration of emergency
 * - Logging location updates during SOS
 * - Evidence for legal proceedings
 */
@Entity(tableName = "sos_sessions")
data class SOSSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // User who activated SOS
    val userId: String,

    // Session Status
    val isActive: Boolean = true, // Currently active SOS session
    val activationMethod: String, // "long_press", "volume_buttons", "widget"

    // Timestamps
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null, // null if still active
    val durationSeconds: Long? = null, // Calculated when session ends

    // Deactivation
    val deactivationMethod: String? = null, // "safe_code", "long_press", "widget", "auto_timeout"
    val deactivatedBy: String? = null, // "user", "system"

    // Location Data
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val startAddress: String? = null,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null,
    val lastAddress: String? = null,
    val locationUpdateCount: Int = 0, // How many GPS updates sent

    // Alerts Sent
    val primaryContactsAlerted: Int = 0, // Number of primary contacts alerted
    val secondaryContactsAlerted: Int = 0, // Number of secondary contacts alerted
    val totalSMSSent: Int = 0, // Total SMS messages sent
    val escalationTriggered: Boolean = false, // Secondary contacts were alerted

    // Actions Taken During SOS
    val silentRecordingEnabled: Boolean = false,
    val silentRecordingPath: String? = null, // Encrypted path to audio recording
    val screenDisguiseEnabled: Boolean = false,
    val photoCaptureEnabled: Boolean = false,
    val photosCaptured: Int = 0,

    // Resolution
    val allClearSent: Boolean = false, // "All clear" message sent to contacts
    val falseAlarm: Boolean = false, // User marked as false alarm

    // Notes (encrypted)
    val notesEncrypted: String? = null, // Survivor's notes about this SOS event

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),

    // Sync
    val syncedToSalesforce: Boolean = false
)

/**
 * SOS Activation Methods
 */
enum class SOSActivationMethod {
    LONG_PRESS,      // Long-press panic button on main screen
    VOLUME_BUTTONS,  // 5 rapid volume down presses
    WIDGET           // Single tap on home screen widget
}

/**
 * SOS Deactivation Methods
 */
enum class SOSDeactivationMethod {
    SAFE_CODE,       // User entered safe code
    LONG_PRESS,      // Long-pressed panic button again
    WIDGET,          // Tapped widget again
    AUTO_TIMEOUT     // 2-hour timeout reached
}

/**
 * Location Update for SOS Session
 * Stores GPS breadcrumbs during active SOS
 */
@Entity(tableName = "sos_location_updates")
data class SOSLocationUpdate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Link to SOS session
    val sessionId: Long,
    val userId: String,

    // Location
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val accuracy: Float? = null, // GPS accuracy in meters

    // Was this location sent to emergency contacts?
    val sentToContacts: Boolean = false,
    val sentAt: Long? = null
)
