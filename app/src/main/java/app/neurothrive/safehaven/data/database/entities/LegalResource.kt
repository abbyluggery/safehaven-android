package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Legal Resource Entity
 * Purpose: Intersectional resource database (shelters, legal aid, hotlines, etc.)
 *
 * INTERSECTIONAL FILTERS (CRITICAL):
 * - LGBTQIA+ specialized resources
 * - Trans-inclusive shelters
 * - BIPOC-led organizations
 * - Male survivor resources (very rare)
 * - Undocumented survivor support (U-Visa, no ICE contact)
 * - Disability accessible
 * - Deaf/ASL services
 *
 * This enables marginalized survivors to find resources that will actually serve them.
 */
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
    val resourceType: String,  // shelter, legal_aid, hotline, counseling, etc.
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
    val servicesJson: String,  // JSON array of services
    val hours: String? = null,
    val is24_7: Boolean = false,

    // INTERSECTIONAL FILTERS - LGBTQIA+
    val servesLGBTQIA: Boolean = false,
    val lgbtqSpecialized: Boolean = false,
    val transInclusive: Boolean = false,
    val nonBinaryInclusive: Boolean = false,

    // INTERSECTIONAL FILTERS - BIPOC
    val servesBIPOC: Boolean = false,
    val bipocLed: Boolean = false,
    val culturallySpecificJson: String? = null,  // JSON array

    // INTERSECTIONAL FILTERS - Male survivors
    val servesMaleIdentifying: Boolean = false,

    // INTERSECTIONAL FILTERS - Undocumented
    val servesUndocumented: Boolean = false,
    val uVisaSupport: Boolean = false,
    val vawaSupport: Boolean = false,
    val noICEContact: Boolean = false,

    // INTERSECTIONAL FILTERS - Disability
    val servesDisabled: Boolean = false,
    val wheelchairAccessible: Boolean = false,
    val servesDeaf: Boolean = false,
    val aslInterpreter: Boolean = false,

    // Language support
    val languagesJson: String,  // JSON array

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
