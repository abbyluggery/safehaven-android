package app.neurothrive.safehaven.domain.usecases

import app.neurothrive.safehaven.data.database.dao.LegalResourceDao
import app.neurothrive.safehaven.data.database.entities.LegalResource
import app.neurothrive.safehaven.data.database.entities.SurvivorProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.*

/**
 * IntersectionalResourceMatcher - CRITICAL ALGORITHM
 * Centers marginalized survivors in resource matching
 * 
 * Scoring priorities:
 * - Trans survivors: +30 pts for trans-specific resources
 * - Undocumented: +30 pts for U-Visa support
 * - Male survivors: +25 pts (very few resources exist)
 * - LGBTQIA+: +20 pts
 * - BIPOC: +20 pts for BIPOC-led orgs
 * - Disabled: +15 pts
 * - Deaf: +15 pts
 */
class IntersectionalResourceMatcher @Inject constructor(
    private val resourceDao: LegalResourceDao
) {
    
    data class ScoredResource(
        val resource: LegalResource,
        val score: Double,
        val distance: Double
    )
    
    /**
     * Find resources matching survivor's intersectional identity
     * 
     * @param profile Survivor's intersectional profile
     * @param currentLatitude Current location (optional, may be disabled for safety)
     * @param currentLongitude Current location (optional)
     * @param resourceType Type of resource (shelter, legal_aid, etc.)
     * @return List of scored resources, sorted by relevance
     */
    suspend fun findResources(
        profile: SurvivorProfile,
        currentLatitude: Double?,
        currentLongitude: Double?,
        resourceType: String
    ): List<ScoredResource> = withContext(Dispatchers.IO) {
        
        // Get all resources of type
        val allResources = resourceDao.getByType(resourceType)
        
        // Score each resource
        val scored = allResources.map { resource ->
            val score = calculateScore(resource, profile, currentLatitude, currentLongitude)
            val distance = calculateDistance(resource, currentLatitude, currentLongitude)
            ScoredResource(resource, score, distance)
        }
        
        // Sort by score (highest first), then distance (closest first)
        scored.sortedWith(
            compareByDescending<ScoredResource> { it.score }
                .thenBy { it.distance }
        )
    }
    
    /**
     * Calculate intersectional priority score
     * CRITICAL: This algorithm centers marginalized survivors
     */
    private fun calculateScore(
        resource: LegalResource,
        profile: SurvivorProfile,
        currentLat: Double?,
        currentLon: Double?
    ): Double {
        var score = 10.0  // Base score
        
        // ========== INTERSECTIONAL BONUSES (CRITICAL SECTION) ==========
        
        // Trans survivors get +30 for trans-specific resources
        if (profile.isTrans && resource.transInclusive) {
            score += 30.0
            if (resource.lgbtqSpecialized) score += 10.0
        }
        
        // Undocumented survivors get +30 for U-Visa support
        if (profile.isUndocumented && resource.servesUndocumented) {
            score += 30.0
            if (resource.uVisaSupport) score += 10.0
            if (resource.vawaSupport) score += 5.0
            if (resource.noICEContact) score += 10.0  // CRITICAL for safety
        }
        
        // Male survivors get +25 (very few resources exist)
        if (profile.isMaleIdentifying && resource.servesMaleIdentifying) {
            score += 25.0
        }
        
        // LGBTQIA+ survivors get +20
        if (profile.isLGBTQIA && resource.servesLGBTQIA) {
            score += 20.0
            if (resource.lgbtqSpecialized) score += 10.0
        }
        
        // Non-binary survivors
        if (profile.isNonBinary && resource.nonBinaryInclusive) {
            score += 20.0
        }
        
        // BIPOC survivors get +20 for BIPOC-led orgs
        if (profile.isBIPOC && resource.servesBIPOC) {
            score += 20.0
            if (resource.bipocLed) score += 10.0
        }
        
        // Disabled survivors
        if (profile.isDisabled && resource.servesDisabled) {
            score += 15.0
            if (resource.wheelchairAccessible) score += 5.0
        }
        
        // Deaf survivors
        if (profile.isDeaf && resource.servesDeaf) {
            score += 15.0
            if (resource.aslInterpreter) score += 10.0
        }
        
        // ========== DISTANCE BONUS (if location provided) ==========
        if (currentLat != null && currentLon != null &&
            resource.latitude != null && resource.longitude != null) {
            val distance = calculateDistance(resource, currentLat, currentLon)
            when {
                distance < 5.0 -> score += 10.0   // Within 5 miles
                distance < 25.0 -> score += 5.0   // Within 25 miles
                distance > 100.0 -> score -= 10.0 // Very far
            }
        }
        
        // ========== SERVICE BONUSES ==========
        
        // Free resources get bonus
        if (resource.isFree) score += 5.0
        
        // 24/7 availability gets bonus
        if (resource.is24_7) score += 5.0
        
        // Sliding scale payment
        if (resource.slidingScale) score += 3.0
        
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
            resource.latitude == null || resource.longitude == null) {
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
