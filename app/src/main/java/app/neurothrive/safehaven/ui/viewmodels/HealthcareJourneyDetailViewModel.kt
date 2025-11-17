package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.database.entities.HealthcareJourney
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.domain.usecases.UpdateHealthcareJourneyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Healthcare Journey Detail Screen
 *
 * Purpose: View and manage existing healthcare journeys
 *
 * Features:
 * - View journey details
 * - Update journey status
 * - Mark arrangements as complete
 * - Complete or cancel journey
 * - Track progress
 *
 * PRIVACY:
 * - Sensitive data decrypted only when displayed
 * - Quick exit capability
 * - Supports stealth mode
 */
@HiltViewModel
class HealthcareJourneyDetailViewModel @Inject constructor(
    private val repository: SafeHavenRepository,
    private val updateJourneyUseCase: UpdateHealthcareJourneyUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Current journey
    private val _journey = MutableStateFlow<HealthcareJourney?>(null)
    val journey: StateFlow<HealthcareJourney?> = _journey.asStateFlow()

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isUpdating: Boolean = false,
        val updateSuccess: Boolean = false,
        val showDeleteConfirmation: Boolean = false,
        val showCompleteConfirmation: Boolean = false,
        val showCancelConfirmation: Boolean = false
    )

    /**
     * Load journey by ID
     */
    fun loadJourney(journeyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Observe journey changes reactively
            repository.getHealthcareJourneyByIdFlow(journeyId).collect { journey ->
                _journey.value = journey
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Update journey status
     */
    fun updateJourneyStatus(newStatus: String) {
        val currentJourney = _journey.value ?: return

        _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

        viewModelScope.launch {
            updateJourneyUseCase.updateStatus(currentJourney.id, newStatus)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "Failed to update status: ${error.message}"
                    )
                }
        }
    }

    // ==================== Mark Arrangements as Complete ====================

    fun markChildcareArranged() {
        val currentJourney = _journey.value ?: return

        viewModelScope.launch {
            updateJourneyUseCase.markChildcareArranged(currentJourney.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(updateSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update: ${error.message}"
                    )
                }
        }
    }

    fun markOutboundTransportArranged() {
        val currentJourney = _journey.value ?: return

        viewModelScope.launch {
            updateJourneyUseCase.markOutboundTransportArranged(currentJourney.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(updateSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update: ${error.message}"
                    )
                }
        }
    }

    fun markRecoveryHousingArranged() {
        val currentJourney = _journey.value ?: return

        viewModelScope.launch {
            updateJourneyUseCase.markRecoveryHousingArranged(currentJourney.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(updateSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update: ${error.message}"
                    )
                }
        }
    }

    fun markReturnTransportArranged() {
        val currentJourney = _journey.value ?: return

        viewModelScope.launch {
            updateJourneyUseCase.markReturnTransportArranged(currentJourney.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(updateSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update: ${error.message}"
                    )
                }
        }
    }

    fun markFinancialAssistanceArranged() {
        val currentJourney = _journey.value ?: return

        viewModelScope.launch {
            updateJourneyUseCase.markFinancialAssistanceArranged(currentJourney.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(updateSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update: ${error.message}"
                    )
                }
        }
    }

    fun markAccompanimentArranged() {
        val currentJourney = _journey.value ?: return

        viewModelScope.launch {
            updateJourneyUseCase.markAccompanimentArranged(currentJourney.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(updateSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update: ${error.message}"
                    )
                }
        }
    }

    // ==================== Journey Completion ====================

    fun showCompleteConfirmation() {
        _uiState.value = _uiState.value.copy(showCompleteConfirmation = true)
    }

    fun hideCompleteConfirmation() {
        _uiState.value = _uiState.value.copy(showCompleteConfirmation = false)
    }

    fun completeJourney() {
        val currentJourney = _journey.value ?: return

        _uiState.value = _uiState.value.copy(
            isUpdating = true,
            error = null,
            showCompleteConfirmation = false
        )

        viewModelScope.launch {
            updateJourneyUseCase.completeJourney(currentJourney.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "Failed to complete journey: ${error.message}"
                    )
                }
        }
    }

    // ==================== Journey Cancellation ====================

    fun showCancelConfirmation() {
        _uiState.value = _uiState.value.copy(showCancelConfirmation = true)
    }

    fun hideCancelConfirmation() {
        _uiState.value = _uiState.value.copy(showCancelConfirmation = false)
    }

    fun cancelJourney(reason: String? = null) {
        val currentJourney = _journey.value ?: return

        _uiState.value = _uiState.value.copy(
            isUpdating = true,
            error = null,
            showCancelConfirmation = false
        )

        viewModelScope.launch {
            updateJourneyUseCase.cancelJourney(currentJourney.id, reason)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "Failed to cancel journey: ${error.message}"
                    )
                }
        }
    }

    // ==================== Journey Deletion ====================

    fun showDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
    }

    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
    }

    fun deleteJourney() {
        val currentJourney = _journey.value ?: return

        _uiState.value = _uiState.value.copy(
            isUpdating = true,
            error = null,
            showDeleteConfirmation = false
        )

        viewModelScope.launch {
            try {
                repository.deleteHealthcareJourney(currentJourney.id)
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = "Failed to delete journey: ${e.message}"
                )
            }
        }
    }

    // ==================== Helper Functions ====================

    /**
     * Get journey completion percentage
     */
    fun getCompletionPercentage(): Int {
        val journey = _journey.value ?: return 0

        var completed = 0
        var total = 0

        // Appointment (required)
        total++
        if (journey.appointmentDateEncrypted != null) completed++

        // Childcare
        if (journey.needsChildcare) {
            total++
            if (journey.childcareArranged) completed++
        }

        // Outbound transport (required)
        total++
        if (journey.outboundTransportationArranged) completed++

        // Recovery housing
        if (journey.needsRecoveryHousing) {
            total++
            if (journey.recoveryHousingArranged) completed++
        }

        // Return transport (required)
        total++
        if (journey.returnTransportationArranged) completed++

        // Financial assistance
        if (journey.needsFinancialAssistance) {
            total++
            if (journey.financialAssistanceArranged) completed++
        }

        // Accompaniment
        if (journey.needsAccompaniment) {
            total++
            if (journey.accompanimentArranged) completed++
        }

        return if (total > 0) (completed * 100) / total else 0
    }

    /**
     * Check if all required arrangements are complete
     */
    fun areAllArrangementsComplete(): Boolean {
        val journey = _journey.value ?: return false

        val requiredComplete = journey.outboundTransportationArranged &&
                             journey.returnTransportationArranged

        val optionalComplete = (!journey.needsChildcare || journey.childcareArranged) &&
                              (!journey.needsRecoveryHousing || journey.recoveryHousingArranged) &&
                              (!journey.needsFinancialAssistance || journey.financialAssistanceArranged) &&
                              (!journey.needsAccompaniment || journey.accompanimentArranged)

        return requiredComplete && optionalComplete
    }

    /**
     * Get list of pending arrangements
     */
    fun getPendingArrangements(): List<String> {
        val journey = _journey.value ?: return emptyList()
        val pending = mutableListOf<String>()

        if (!journey.outboundTransportationArranged) {
            pending.add("Outbound transportation")
        }

        if (journey.needsChildcare && !journey.childcareArranged) {
            pending.add("Childcare")
        }

        if (journey.needsRecoveryHousing && !journey.recoveryHousingArranged) {
            pending.add("Recovery housing")
        }

        if (!journey.returnTransportationArranged) {
            pending.add("Return transportation")
        }

        if (journey.needsFinancialAssistance && !journey.financialAssistanceArranged) {
            pending.add("Financial assistance")
        }

        if (journey.needsAccompaniment && !journey.accompanimentArranged) {
            pending.add("Accompaniment")
        }

        return pending
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear update success flag
     */
    fun clearUpdateSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }
}
