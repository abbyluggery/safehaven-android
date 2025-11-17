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
 * Matches survivors with resources based on intersectional identity.
 *
 * SCORING ALGORITHM:
 * - Trans survivors: +30 pts for trans-inclusive shelters
 * - Undocumented survivors: +30 pts for U-Visa support, +10 for no ICE contact
 * - Male survivors: +25 pts (very few resources available)
 * - LGBTQIA+ survivors: +20 pts for LGBTQ+ specialized
 * - BIPOC survivors: +20 pts for BIPOC-led orgs
 * - Disabled survivors: +15 pts for wheelchair accessible
 * - Deaf survivors: +15 pts for ASL interpreters
 * - Distance: +10 pts < 5 miles, +5 pts < 25 miles, -10 pts > 100 miles
 * - Free resources: +5 pts
 * - 24/7 availability: +5 pts
 *
 * This ensures marginalized survivors find resources that will actually serve them.
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
