package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

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
    
    // Incident details (ENCRYPTED)
    val incidentType: String,  // physical, verbal, emotional, financial, sexual, stalking
    val descriptionEncrypted: String,  // AES-256 encrypted
    val locationText: String? = null,
    
    // GPS (only if enabled)
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Evidence (ENCRYPTED)
    val witnessesEncrypted: String? = null,
    val injuriesEncrypted: String? = null,
    val photoEvidenceIds: String? = null,  // Comma-separated EvidenceItem IDs
    
    // Legal
    val policeInvolved: Boolean = false,
    val policeReportNumber: String? = null,
    val medicalAttention: Boolean = false,
    
    // Sync
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null,
    val exportedToPDF: Boolean = false
)
