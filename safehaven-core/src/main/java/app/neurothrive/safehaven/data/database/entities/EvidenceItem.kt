package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "evidence_items",
    foreignKeys = [
        ForeignKey(
            entity = SafeHavenProfile::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IncidentReport::class,
            parentColumns = ["id"],
            childColumns = ["incidentReportId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("userId"),
        Index("incidentReportId")
    ]
)
data class EvidenceItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val userId: String,
    val evidenceType: String,  // photo, video, audio, screenshot
    val timestamp: Long,
    
    // File (ENCRYPTED)
    val encryptedFilePath: String,
    val originalFileName: String,  // For user reference
    val fileSize: Long,  // Bytes
    val mimeType: String,  // image/jpeg, video/mp4, audio/m4a
    
    // Metadata
    val captionEncrypted: String? = null,
    val latitude: Double? = null,  // Only if GPS enabled
    val longitude: Double? = null,
    
    // Relationships
    val incidentReportId: String? = null,  // Link to incident
    
    // Cloud backup
    val cloudBackupUrl: String? = null,
    val cloudBackupDate: Long? = null,
    
    // Sync
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null,
    
    // Deletion
    val isDeleted: Boolean = false,
    val deletedDate: Long? = null
)
