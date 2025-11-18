package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "legal_resources",
    indices = [
        Index("resourceType"),
        Index("state", "city"),
        Index("servesLGBTQIA"),
        Index("transInclusive"),
        Index("servesBIPOC"),
        Index("servesMaleIdentifying"),
        Index("servesUndocumented")
    ]
)
data class LegalResource(
    @PrimaryKey
    val id: String,
    
    // Basic info
    val resourceType: String,  // shelter, legal_aid, hotline, therapy, etc.
    val organizationName: String,
    val phone: String? = null,
    val website: String? = null,
    val email: String? = null,
    
    // Location
    val address: String? = null,
    val city: String,
    val state: String,
    val zipCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Services
    val servicesJson: String,  // JSON array: ["shelter", "legal_aid", "therapy"]
    val hours: String? = null,
    val is24_7: Boolean = false,
    
    // INTERSECTIONAL FILTERS (CRITICAL for matching algorithm)
    val servesLGBTQIA: Boolean = false,
    val lgbtqSpecialized: Boolean = false,
    val transInclusive: Boolean = false,
    val nonBinaryInclusive: Boolean = false,
    
    val servesBIPOC: Boolean = false,
    val bipocLed: Boolean = false,
    val culturallySpecificJson: String? = null,  // ["Black", "Latina", etc.]
    
    val servesMaleIdentifying: Boolean = false,
    
    val servesUndocumented: Boolean = false,
    val uVisaSupport: Boolean = false,
    val vawaSupport: Boolean = false,
    val noICEContact: Boolean = false,
    
    val servesDisabled: Boolean = false,
    val wheelchairAccessible: Boolean = false,
    val servesDeaf: Boolean = false,
    val aslInterpreter: Boolean = false,
    
    // Language
    val languagesJson: String,  // JSON array: ["English", "Spanish", "ASL"]
    
    // Cost
    val isFree: Boolean = true,
    val slidingScale: Boolean = false,
    
    // Verification
    val lastVerified: Long,
    val verifiedBy: String? = null,
    
    // User feedback
    val rating: Float? = null,
    val reviewCount: Int = 0
)
