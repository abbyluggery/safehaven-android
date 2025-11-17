package app.neurothrive.safehaven.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Legal Resource Entity
 * Purpose: Intersectional resource database (shelters, legal aid, hotlines, etc.)
 *
 * INTERSECTIONAL FILTERS (41 CATEGORIES - CRITICAL):
 * - LGBTQIA+ specialized resources (4 fields)
 * - BIPOC-led organizations (3 fields)
 * - Male survivor resources (1 field - very rare)
 * - Undocumented survivor support (4 fields - U-Visa, no ICE contact)
 * - Disability accessible (4 fields - wheelchair, Deaf/ASL)
 * - Dependent Care (5 fields - children, dependent adults, pets, childcare)
 * - Vulnerable Populations (7 fields - pregnant, substance use, teen dating, elder abuse, trafficking, TBI, criminal record)
 * - Medical/Mental Health (3 fields - medical support, counseling, trauma-informed care)
 * - Language support (1 field)
 * - Cost (2 fields)
 *
 * This enables ALL survivors to find resources that will actually serve them and their dependents.
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

    // INTERSECTIONAL FILTERS - Dependent Care
    val acceptsChildren: Boolean = false,
    val childAgeRestrictions: String? = null,  // "0-12", "0-18", "Any age", null = no restrictions if acceptsChildren=true
    val acceptsDependentAdults: Boolean = false,  // Elderly parents, adult disabled children
    val acceptsPets: Boolean = false,
    val hasChildcare: Boolean = false,

    // INTERSECTIONAL FILTERS - Additional Vulnerable Populations
    val servesPregnant: Boolean = false,
    val servesSubstanceUse: Boolean = false,  // Harm reduction/low barrier
    val servesTeenDating: Boolean = false,  // Teen dating violence (minors)
    val servesElderAbuse: Boolean = false,  // Elder abuse victims (60+)
    val servesTrafficking: Boolean = false,  // Human trafficking survivors
    val servesTBI: Boolean = false,  // Traumatic brain injury support
    val acceptsCriminalRecord: Boolean = false,  // Accepts survivors with criminal records

    // INTERSECTIONAL FILTERS - Medical/Mental Health
    val hasMedicalSupport: Boolean = false,
    val hasMentalHealthCounseling: Boolean = false,
    val traumaInformedCare: Boolean = false,

    // Verification
    val lastVerified: Long,
    val verifiedBy: String? = null,

    // User feedback
    val rating: Float? = null,
    val reviewCount: Int = 0
)
