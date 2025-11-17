package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Evidence Item Entity
 * Purpose: Encrypted photos, videos, audio recordings of abuse evidence
 *
 * CRITICAL SECURITY FEATURES:
 * - All files stored encrypted (AES-256-GCM)
 * - GPS metadata stripped before encryption
 * - No gallery thumbnails
 * - Soft delete with secure overwrite option
 */
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

    // File (ENCRYPTED on disk)
    val encryptedFilePath: String,
    val originalFileName: String,
    val fileSize: Long,
    val mimeType: String,

    // Metadata
    val captionEncrypted: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Relationships
    val incidentReportId: String? = null,

    // Cloud backup
    val cloudBackupUrl: String? = null,
    val cloudBackupDate: Long? = null,

    // Sync
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null,

    // Deletion (soft delete for recovery)
    val isDeleted: Boolean = false,
    val deletedDate: Long? = null
)
