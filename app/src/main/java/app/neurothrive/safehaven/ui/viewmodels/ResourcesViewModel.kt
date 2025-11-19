package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.database.entities.LegalResource
import app.neurothrive.safehaven.data.database.entities.SurvivorProfile
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.domain.usecases.IntersectionalResourceMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ScoredResource(
    val resource: LegalResource,
    val relevanceScore: Int
)

@HiltViewModel
class ResourcesViewModel @Inject constructor(
    private val repository: SafeHavenRepository,
    private val resourceMatcher: IntersectionalResourceMatcher
) : ViewModel() {

    /**
     * Get all resources
     */
    fun getAllResources(): Flow<List<LegalResource>> {
        return repository.getAllLegalResources()
    }

    /**
     * Get survivor profile
     */
    fun getSurvivorProfile(userId: String): Flow<SurvivorProfile?> {
        return repository.getSurvivorProfileByUserId(userId)
    }

    /**
     * Get resources scored by intersectional matching
     */
    fun getScoredResources(userId: String): StateFlow<List<ScoredResource>> {
        return combine(
            repository.getAllLegalResources(),
            repository.getSurvivorProfileByUserId(userId)
        ) { resources, profile ->
            if (profile == null) {
                // No profile yet - return resources unsorted
                resources.map { ScoredResource(it, 0) }
            } else {
                // Score resources using intersectional matcher
                resources.map { resource ->
                    val score = resourceMatcher.scoreResource(resource, profile)
                    ScoredResource(resource, score)
                }.sortedByDescending { it.relevanceScore }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    /**
     * Filter resources by service type
     */
    fun filterResourcesByType(
        resources: List<ScoredResource>,
        emergencyShelter: Boolean = false,
        legalAdvocacy: Boolean = false,
        counseling: Boolean = false,
        hotline24_7: Boolean = false
    ): List<ScoredResource> {
        return resources.filter { scored ->
            val resource = scored.resource
            (!emergencyShelter || resource.emergencyShelter) &&
            (!legalAdvocacy || resource.legalAdvocacy) &&
            (!counseling || resource.counseling) &&
            (!hotline24_7 || resource.hotline24_7)
        }
    }

    /**
     * Filter resources by intersectional identity
     */
    fun filterResourcesByIdentity(
        resources: List<ScoredResource>,
        transInclusive: Boolean = false,
        servesLGBTQIA: Boolean = false,
        servesBIPOC: Boolean = false,
        servesMale: Boolean = false,
        servesUndocumented: Boolean = false
    ): List<ScoredResource> {
        return resources.filter { scored ->
            val resource = scored.resource
            (!transInclusive || resource.transInclusive) &&
            (!servesLGBTQIA || resource.servesLGBTQIA) &&
            (!servesBIPOC || resource.servesBIPOC) &&
            (!servesMale || resource.servesMaleIdentifying) &&
            (!servesUndocumented || resource.uVisaSupport)
        }
    }
}
