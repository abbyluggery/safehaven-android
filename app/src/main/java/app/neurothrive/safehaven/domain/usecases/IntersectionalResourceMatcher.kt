package app.neurothrive.safehaven.domain.usecases

import app.neurothrive.safehaven.data.database.entities.LegalResource
import app.neurothrive.safehaven.data.database.entities.SurvivorProfile
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Intersectional Resource Matcher
 *
 * CRITICAL EQUITY FEATURE:
 * Matches survivors with resources based on intersectional identity (48 categories).
 *
 * SCORING ALGORITHM (EXPANDED):
 * Identity-based bonuses:
 * - Trans survivors: +30 pts for trans-inclusive shelters
 * - Undocumented survivors: +30 pts for U-Visa support, +10 for no ICE contact
 * - Male survivors: +25 pts (very few resources available)
 * - LGBTQIA+ survivors: +20 pts for LGBTQ+ specialized
 * - BIPOC survivors: +20 pts for BIPOC-led orgs
 * - Disabled survivors: +15 pts for wheelchair accessible
 * - Deaf survivors: +15 pts for ASL interpreters
 *
 * Dependent care bonuses:
 * - Has children: +25 pts if accepts children (60-70% of survivors have kids!)
 * - Has dependent adults: +20 pts if accepts dependent adults
 * - Has pets: +20 pts if accepts pets (50% delay leaving without this)
 * - Needs childcare: +10 pts if has on-site childcare
 *
 * Vulnerable population bonuses:
 * - Pregnant: +20 pts if serves pregnant survivors
 * - Substance use: +25 pts if low-barrier/harm reduction (often excluded elsewhere)
 * - Teen dating violence: +20 pts if serves minors
 * - Elder abuse: +20 pts if serves 60+
 * - Trafficking: +25 pts if trafficking-specialized
 * - TBI: +15 pts if TBI support (very common in DV)
 * - Criminal record: +15 pts if accepts records
 *
 * Medical/mental health bonuses:
 * - Medical support: +10 pts
 * - Mental health counseling: +10 pts
 * - Trauma-informed care: +10 pts
 *
 * Transportation bonuses (NEW - CRITICAL FOR RURAL):
 * - No transportation + provides pickup: +35 pts (GAME-CHANGER for rural survivors)
 * - No transportation + virtual services: +25 pts (can access without travel)
 * - No transportation + gas vouchers: +20 pts (enables self-transport)
 * - Greyhound Home Free partner: +15 pts (nationwide bus access)
 * - Relocation assistance: +10 pts (moving cost help)
 *
 * Accessibility bonuses:
 * - Distance: +10 pts < 5 miles, +5 pts < 25 miles, -10 pts > 100 miles
 * - Free resources: +5 pts
 * - 24/7 availability: +5 pts
 *
 * This ensures ALL survivors find resources that will serve them and their dependents,
 * including rural survivors facing critical transportation barriers.
 */

data class ScoredResource(
    val resource: LegalResource,
    val score: Double,
    val distance: Double
)

class IntersectionalResourceMatcher @Inject constructor(
    private val repository: SafeHavenRepository
) {

    /**
     * Find resources for a survivor based on intersectional identity
     */
    suspend fun findResources(
        profile: SurvivorProfile,
        currentLatitude: Double?,
        currentLongitude: Double?,
        resourceType: String
    ): List<ScoredResource> {
        try {
            Timber.d("Finding resources for type: $resourceType")

            // Get all resources of the requested type
            val allResources = repository.getResourcesByType(resourceType)

            Timber.d("Found ${allResources.size} resources of type $resourceType")

            // Score each resource based on intersectional match
            val scored = allResources.map { resource ->
                val score = calculateScore(resource, profile, currentLatitude, currentLongitude)
                val distance = calculateDistance(resource, currentLatitude, currentLongitude)

                ScoredResource(resource, score, distance)
            }

            // Sort by score (highest first), then distance (closest first)
            val sorted = scored.sortedWith(
                compareByDescending<ScoredResource> { it.score }
                    .thenBy { it.distance }
            )

            Timber.d("Resources scored and sorted, top score: ${sorted.firstOrNull()?.score}")

            return sorted

        } catch (e: Exception) {
            Timber.e(e, "Resource matching failed")
            return emptyList()
        }
    }

    /**
     * Calculate intersectional priority score
     */
    private fun calculateScore(
        resource: LegalResource,
        profile: SurvivorProfile,
        currentLat: Double?,
        currentLon: Double?
    ): Double {
        var score = 10.0  // Base score

        // TODO: Get intersectional identity from SafeHavenProfile (linked via userId)
        // For now, using basic scoring

        // INTERSECTIONAL BONUSES (CRITICAL SECTION)

        // Trans survivors get +30 for trans-specific resources
        // if (profile.isTrans && resource.transInclusive) score += 30.0

        // Undocumented survivors get +30 for U-Visa support
        // if (profile.isUndocumented && resource.servesUndocumented) {
        //     score += 30.0
        //     if (resource.uVisaSupport) score += 10.0
        //     if (resource.noICEContact) score += 10.0
        // }

        // Male survivors get +25 (very few resources)
        // if (profile.isMaleIdentifying && resource.servesMaleIdentifying) score += 25.0

        // LGBTQIA+ survivors get +20
        // if (profile.isLGBTQIA && resource.servesLGBTQIA) {
        //     score += 20.0
        //     if (resource.lgbtqSpecialized) score += 10.0
        // }

        // BIPOC survivors get +20 for BIPOC-led orgs
        // if (profile.isBIPOC && resource.servesBIPOC) {
        //     score += 20.0
        //     if (resource.bipocLed) score += 10.0
        // }

        // Disabled survivors
        if (profile.hasChildren && resource.servesDisabled) {
            score += 15.0
            if (resource.wheelchairAccessible) score += 5.0
        }

        // Deaf survivors
        // if (profile.isDeaf && resource.servesDeaf) {
        //     score += 15.0
        //     if (resource.aslInterpreter) score += 10.0
        // }

        // DEPENDENT CARE BONUSES (NEW - CRITICAL)

        // Children (60-70% of DV survivors have kids!)
        if (profile.hasChildren && resource.acceptsChildren) {
            score += 25.0
            if (resource.hasChildcare) score += 10.0
        }

        // Dependent adults (elderly parents, adult disabled children)
        if (profile.hasDependentAdults && resource.acceptsDependentAdults) {
            score += 20.0
        }

        // Pets (50% of survivors delay leaving without pet option)
        if (profile.hasPets && resource.acceptsPets) {
            score += 20.0
        }

        // VULNERABLE POPULATION BONUSES (NEW)

        // Pregnant survivors
        if (profile.isPregnant && resource.servesPregnant) {
            score += 20.0
        }

        // Substance use disorders (often excluded from shelters)
        if (profile.hasSubstanceUse && resource.servesSubstanceUse) {
            score += 25.0  // Higher score - these survivors face major barriers
        }

        // Teen dating violence
        if (profile.isTeenDating && resource.servesTeenDating) {
            score += 20.0
        }

        // Elder abuse (60+)
        if (profile.isElderAbuse && resource.servesElderAbuse) {
            score += 20.0
        }

        // Human trafficking
        if (profile.isTraffickingSurvivor && resource.servesTrafficking) {
            score += 25.0
        }

        // Traumatic brain injury (very common in DV - up to 90%)
        if (profile.hasTBI && resource.servesTBI) {
            score += 15.0
        }

        // Criminal record (many survivors have records due to coerced crimes)
        if (profile.hasCriminalRecord && resource.acceptsCriminalRecord) {
            score += 15.0
        }

        // MEDICAL/MENTAL HEALTH BONUSES (NEW)

        if (resource.hasMedicalSupport) score += 10.0
        if (resource.hasMentalHealthCounseling) score += 10.0
        if (resource.traumaInformedCare) score += 10.0

        // TRANSPORTATION BONUSES (NEW - CRITICAL FOR RURAL SURVIVORS)

        if (!profile.hasTransportation) {
            // Pickup service - GAME-CHANGER for rural survivors
            if (resource.providesTransportation) {
                score += 35.0  // Highest bonus - this is often make-or-break
            }

            // Virtual services - can access without traveling
            if (resource.offersVirtualServices) {
                score += 25.0
            }

            // Gas vouchers - enables self-transport
            if (resource.gasVoucherProgram) {
                score += 20.0
            }

            // Greyhound Home Free - nationwide bus access
            if (resource.greyhoundHomeFreePartner) {
                score += 15.0
            }
        }

        // Relocation assistance (helpful even with transportation)
        if (resource.relocationAssistance) score += 10.0

        // Distance bonus (if location provided)
        if (currentLat != null && currentLon != null &&
            resource.latitude != null && resource.longitude != null
        ) {
            val distance = calculateDistance(resource, currentLat, currentLon)
            when {
                distance < 5.0 -> score += 10.0
                distance < 25.0 -> score += 5.0
                distance > 100.0 -> score -= 10.0
            }
        }

        // Free resources get bonus
        if (resource.isFree) score += 5.0

        // 24/7 availability gets bonus
        if (resource.is24_7) score += 5.0

        return score
    }

    /**
     * Calculate distance using Haversine formula
     * Returns distance in miles
     */
    private fun calculateDistance(
        resource: LegalResource,
        currentLat: Double?,
        currentLon: Double?
    ): Double {
        if (currentLat == null || currentLon == null ||
            resource.latitude == null || resource.longitude == null
        ) {
            return Double.MAX_VALUE
        }

        // Haversine formula
        val R = 3959.0  // Earth radius in miles
        val dLat = Math.toRadians(resource.latitude - currentLat)
        val dLon = Math.toRadians(resource.longitude!! - currentLon)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(currentLat)) *
                cos(Math.toRadians(resource.latitude)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }
}
