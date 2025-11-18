package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "safehaven_profiles")
data class SafeHavenProfile(
    @PrimaryKey
    val userId: String = UUID.randomUUID().toString(),
    
    // Authentication
    val safeHavenPasswordHash: String,
    val duressPasswordHash: String,
    val encryptionKey: String,
    
    // Settings (CRITICAL DEFAULTS)
    val gpsEnabled: Boolean = false,  // DEFAULT: OFF (safety)
    val silentModeEnabled: Boolean = true,
    val panicGPSOverride: Boolean = false,
    val autoCloudBackup: Boolean = true,
    
    // Intersectional Identity (for resource matching)
    val isLGBTQIA: Boolean = false,
    val isTrans: Boolean = false,
    val isNonBinary: Boolean = false,
    val isBIPOC: Boolean = false,
    val culturalIdentity: String? = null,
    val isMaleIdentifying: Boolean = false,
    val isUndocumented: Boolean = false,
    val isDisabled: Boolean = false,
    val isDeaf: Boolean = false,
    val primaryLanguage: String = "English",
    
    // Sync
    val lastSyncDate: Long? = null,
    val salesforceId: String? = null,
    val isActive: Boolean = true
)
