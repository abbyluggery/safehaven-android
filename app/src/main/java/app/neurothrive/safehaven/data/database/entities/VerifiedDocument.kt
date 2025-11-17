package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Verified Document Entity
 * Purpose: Cryptographic verification of legal documents
 *
 * VERIFICATION METHODS:
 * - SHA-256 hash (64 character hex string)
 * - Blockchain timestamping (Polygon)
 * - Notarization date
 *
 * Use case: Survivor needs to verify ID, passport, or other documents
 * they can't access physically (abuser has originals)
 */
@Entity(
    tableName = "verified_documents",
    foreignKeys = [ForeignKey(
        entity = SafeHavenProfile::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["cryptographicHash"], unique = true)]
)
data class VerifiedDocument(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val userId: String,
    val documentType: String,  // passport, ID, birth_cert, etc.

    // Cryptographic verification
    val cryptographicHash: String,  // SHA-256 (64 chars)
    val blockchainTxHash: String? = null,
    val verificationMethod: String = "SHA256_Blockchain",
    val notarizationDate: Long,

    // File paths (ENCRYPTED)
    val originalPhotoPathEncrypted: String,
    val verifiedPDFPathEncrypted: String,

    // Cloud backup
    val cloudBackupUrl: String? = null,

    // Sync
    val syncedToSalesforce: Boolean = false,
    val salesforceId: String? = null
)
