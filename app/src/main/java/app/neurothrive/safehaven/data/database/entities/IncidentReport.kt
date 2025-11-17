package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Incident Report Entity
 * Purpose: Legal-formatted documentation of abuse incidents
 *
 * ENCRYPTED FIELDS:
 * - descriptionEncrypted (AES-256-GCM)
 * - witnessesEncrypted (AES-256-GCM)
 * - injuriesEncrypted (AES-256-GCM)
 */
@Entity(
    tableName = "incident_reports",
    foreignKeys = [ForeignKey(
        entity = SafeHavenProfile::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class IncidentReport(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val userId: String,
    val timestamp: Long,

    // Incident details
    val incidentType: String,  // physical, verbal, emotional, financial, sexual, stalking
    val descriptionEncrypted: String,
    val locationText: String? = null,

    // GPS (only if enabled by user)
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Evidence
    val witnessesEncrypted: String? = null,
    val injuriesEncrypted: String? = null,
    val photoEvidenceIds: String? = null,  // Comma-separated IDs

    // Legal
    val policeInvolved: Boolean = false,
    val policeReportNumber: String? = null,
    val medicalAttention: Boolean = false,

    // Sync
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null,
    val exportedToPDF: Boolean = false
)
