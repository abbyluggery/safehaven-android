package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survivor_profiles")
data class SurvivorProfile(
    @PrimaryKey
    val userId: String,
    
    // Identity (all optional)
    val isLGBTQIA: Boolean = false,
    val isTrans: Boolean = false,
    val isNonBinary: Boolean = false,
    val genderIdentity: String? = null,
    
    val isBIPOC: Boolean = false,
    val culturalIdentity: String? = null,
    
    val isMaleIdentifying: Boolean = false,
    
    val isUndocumented: Boolean = false,
    val immigrationStatus: String? = null,
    
    val isDisabled: Boolean = false,
    val disabilityType: String? = null,
    
    val isDeaf: Boolean = false,
    val isBlind: Boolean = false,
    
    val primaryLanguage: String = "English",
    val additionalLanguagesJson: String? = null,
    
    val faithPreference: String? = null,
    
    val hasSubstanceUseHistory: Boolean = false,
    val formerlyIncarcerated: Boolean = false,
    
    // Situation
    val hasChildren: Boolean = false,
    val childrenAgesJson: String? = null,
    
    val hasHealthInsurance: Boolean = false,
    val insuranceType: String? = null,
    
    val currentHousingSituation: String? = null,
    
    // Location (for resource matching)
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val safeToProvideLocation: Boolean = true
)
