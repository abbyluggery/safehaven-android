package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

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
    val documentType: String,  // birth_cert, ss_card, passport, etc.
    
    // Cryptographic verification (CRITICAL)
    val cryptographicHash: String,  // SHA-256 hash (64 chars)
    val blockchainTxHash: String? = null,  // Polygon transaction hash
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
