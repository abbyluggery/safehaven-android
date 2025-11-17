package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.database.entities.LegalResource
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.domain.usecases.HealthcareResourceMatcherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Healthcare Resource Finder Screen
 *
 * Purpose: Search and filter reproductive healthcare resources
 *
 * PRIVACY FEATURES:
 * - All searches are local-only (no network calls)
 * - No search history stored
 * - No location tracking
 * - Results can be quickly cleared
 */
@HiltViewModel
class HealthcareResourceFinderViewModel @Inject constructor(
    private val repository: SafeHavenRepository,
    private val resourceMatcher: HealthcareResourceMatcherUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Resource availability
    private val _resourceAvailability = MutableStateFlow<ResourceAvailability?>(null)
    val resourceAvailability: StateFlow<ResourceAvailability?> = _resourceAvailability.asStateFlow()

    data class UiState(
        val resources: List<LegalResource> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchQuery: String = "",
        val selectedState: String? = null,
        val showClinicsOnly: Boolean = false,
        val showRecoveryHousing: Boolean = false,
        val showChildcare: Boolean = false,
        val showFinancialAssistance: Boolean = false,
        val showAccompaniment: Boolean = false
    )

    data class ResourceAvailability(
        val totalClinics: Int,
        val totalRecoveryHousing: Int,
        val totalChildcare: Int,
        val totalFinancialAid: Int,
        val totalAccompaniment: Int
    ) {
        val totalResources: Int
            get() = totalClinics + totalRecoveryHousing + totalChildcare +
                    totalFinancialAid + totalAccompaniment
    }

    init {
        loadResourceAvailability()
    }

    /**
     * Load resource availability summary
     */
    private fun loadResourceAvailability() {
        viewModelScope.launch {
            resourceMatcher.getResourceAvailability()
                .onSuccess { availability ->
                    _resourceAvailability.value = ResourceAvailability(
                        totalClinics = availability.clinicsAvailable,
                        totalRecoveryHousing = availability.recoveryHousingAvailable,
                        totalChildcare = availability.childcareAvailable,
                        totalFinancialAid = availability.financialAidAvailable,
                        totalAccompaniment = availability.accompanimentAvailable
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to load resource availability: ${error.message}"
                    )
                }
        }
    }

    /**
     * Update search query
     * Note: Search is performed locally on filtered results
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    /**
     * Toggle clinic filter
     */
    fun toggleClinicsFilter() {
        _uiState.value = _uiState.value.copy(
            showClinicsOnly = !_uiState.value.showClinicsOnly
        )
        applyFilters()
    }

    /**
     * Toggle recovery housing filter
     */
    fun toggleRecoveryHousingFilter() {
        _uiState.value = _uiState.value.copy(
            showRecoveryHousing = !_uiState.value.showRecoveryHousing
        )
        applyFilters()
    }

    /**
     * Toggle childcare filter
     */
    fun toggleChildcareFilter() {
        _uiState.value = _uiState.value.copy(
            showChildcare = !_uiState.value.showChildcare
        )
        applyFilters()
    }

    /**
     * Toggle financial assistance filter
     */
    fun toggleFinancialAssistanceFilter() {
        _uiState.value = _uiState.value.copy(
            showFinancialAssistance = !_uiState.value.showFinancialAssistance
        )
        applyFilters()
    }

    /**
     * Toggle accompaniment filter
     */
    fun toggleAccompanimentFilter() {
        _uiState.value = _uiState.value.copy(
            showAccompaniment = !_uiState.value.showAccompaniment
        )
        applyFilters()
    }

    /**
     * Set state filter
     */
    fun setStateFilter(state: String?) {
        _uiState.value = _uiState.value.copy(selectedState = state)
        applyFilters()
    }

    /**
     * Apply filters and load matching resources
     */
    fun applyFilters() {
        val currentState = _uiState.value

        // If no filters selected, show empty list
        if (!currentState.showClinicsOnly &&
            !currentState.showRecoveryHousing &&
            !currentState.showChildcare &&
            !currentState.showFinancialAssistance &&
            !currentState.showAccompaniment) {
            _uiState.value = currentState.copy(resources = emptyList())
            return
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val requirements = HealthcareResourceMatcherUseCase.JourneyRequirements(
                    needsClinic = currentState.showClinicsOnly,
                    targetState = currentState.selectedState,
                    needsRecoveryHousing = currentState.showRecoveryHousing,
                    needsChildcareDuringAppointment = currentState.showChildcare,
                    needsChildcareDuringRecovery = currentState.showChildcare,
                    needsFinancialAssistance = currentState.showFinancialAssistance,
                    needsAccompaniment = currentState.showAccompaniment
                )

                resourceMatcher.findMatchingResources(requirements)
                    .onSuccess { matchResult ->
                        val allResources = matchResult.clinics +
                                         matchResult.recoveryHousing +
                                         matchResult.childcare +
                                         matchResult.financialAssistance +
                                         matchResult.accompaniment

                        // Apply search query filter if present
                        val filteredResources = if (currentState.searchQuery.isNotEmpty()) {
                            allResources.filter { resource ->
                                resource.organizationName.contains(currentState.searchQuery, ignoreCase = true) ||
                                resource.city.contains(currentState.searchQuery, ignoreCase = true) ||
                                resource.state.contains(currentState.searchQuery, ignoreCase = true)
                            }
                        } else {
                            allResources
                        }

                        _uiState.value = currentState.copy(
                            resources = filteredResources,
                            isLoading = false
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            error = "Failed to load resources: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "An error occurred: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _uiState.value = UiState()
    }

    /**
     * Get resource by ID
     */
    fun getResourceById(resourceId: String): LegalResource? {
        return _uiState.value.resources.find { it.id == resourceId }
    }

    /**
     * Load clinics for a specific state
     * Useful for journey planner integration
     */
    fun loadClinicsForState(state: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val clinics = repository.getReproductiveHealthcareClinics(
                    state = state,
                    acceptsOutOfState = false
                )
                _uiState.value = _uiState.value.copy(
                    resources = clinics,
                    isLoading = false,
                    selectedState = state,
                    showClinicsOnly = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load clinics: ${e.message}"
                )
            }
        }
    }

    /**
     * Load all clinics accepting out-of-state patients
     * CRITICAL for survivors traveling from restricted states
     */
    fun loadOutOfStateClinics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val clinics = repository.getReproductiveHealthcareClinics(
                    state = null,
                    acceptsOutOfState = true
                )
                _uiState.value = _uiState.value.copy(
                    resources = clinics,
                    isLoading = false,
                    showClinicsOnly = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load clinics: ${e.message}"
                )
            }
        }
    }

    /**
     * Load comprehensive journey resources
     * Returns all resources needed for a complete journey
     */
    fun loadComprehensiveJourneyResources(targetState: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val requirements = HealthcareResourceMatcherUseCase.JourneyRequirements(
                needsClinic = true,
                targetState = targetState,
                needsOutOfStateClinic = true,
                needsRecoveryHousing = true,
                needsChildcareDuringAppointment = true,
                needsChildcareDuringRecovery = true,
                needsFinancialAssistance = true,
                needsAccompaniment = true
            )

            resourceMatcher.getComprehensiveJourneyPackage(requirements)
                .onSuccess { matchResult ->
                    val allResources = matchResult.clinics +
                                     matchResult.recoveryHousing +
                                     matchResult.childcare +
                                     matchResult.financialAssistance +
                                     matchResult.accompaniment

                    _uiState.value = _uiState.value.copy(
                        resources = allResources,
                        isLoading = false,
                        selectedState = targetState,
                        showClinicsOnly = true,
                        showRecoveryHousing = true,
                        showChildcare = true,
                        showFinancialAssistance = true,
                        showAccompaniment = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load journey resources: ${error.message}"
                    )
                }
        }
    }
}
