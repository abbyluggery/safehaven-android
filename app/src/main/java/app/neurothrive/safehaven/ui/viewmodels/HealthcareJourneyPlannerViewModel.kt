package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.database.entities.LegalResource
import app.neurothrive.safehaven.data.database.entities.HealthcareJourney
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.domain.usecases.CreateHealthcareJourneyUseCase
import app.neurothrive.safehaven.domain.usecases.HealthcareResourceMatcherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Healthcare Journey Planner Screen
 *
 * Purpose: 7-step wizard for planning complete healthcare journeys
 *
 * Steps:
 * 1. Appointment Details - Clinic selection and appointment date
 * 2. Childcare - Arrange childcare during appointment and recovery
 * 3. Outbound Travel - How to get to clinic
 * 4. Recovery - Recovery housing and support
 * 5. Return Travel - How to get home
 * 6. Financial Support - Funding and assistance
 * 7. Privacy Settings - Auto-delete and stealth mode
 *
 * PRIVACY:
 * - All sensitive data encrypted before storage
 * - Auto-delete after 30 days (configurable)
 * - Stealth mode option
 * - No location tracking
 */
@HiltViewModel
class HealthcareJourneyPlannerViewModel @Inject constructor(
    private val repository: SafeHavenRepository,
    private val createJourneyUseCase: CreateHealthcareJourneyUseCase,
    private val resourceMatcher: HealthcareResourceMatcherUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Journey draft (data being collected across steps)
    private val _journeyDraft = MutableStateFlow(JourneyDraft())
    val journeyDraft: StateFlow<JourneyDraft> = _journeyDraft.asStateFlow()

    // Available resources for selection
    private val _availableClinics = MutableStateFlow<List<LegalResource>>(emptyList())
    val availableClinics: StateFlow<List<LegalResource>> = _availableClinics.asStateFlow()

    data class UiState(
        val currentStep: Int = 0, // 0-6
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSaving: Boolean = false,
        val saveSuccess: Boolean = false,
        val createdJourneyId: String? = null
    )

    data class JourneyDraft(
        // Step 1: Appointment Details
        val clinicResourceId: String? = null,
        val clinicName: String? = null,
        val appointmentDate: String? = null,
        val appointmentTime: String? = null,
        val appointmentNotes: String? = null,

        // Step 2: Childcare
        val needsChildcare: Boolean = false,
        val childcareForAppointment: Boolean = false,
        val childcareForRecovery: Boolean = false,
        val childcareNotes: String? = null,

        // Step 3: Outbound Travel
        val departureLocation: String? = null,
        val departureDate: String? = null,
        val outboundTransportMethod: String? = null,
        val outboundTransportNotes: String? = null,

        // Step 4: Recovery
        val needsRecoveryHousing: Boolean = false,
        val recoveryHousingResourceId: String? = null,
        val estimatedRecoveryDays: Int? = null,
        val recoveryNotes: String? = null,

        // Step 5: Return Travel
        val returnDate: String? = null,
        val returnTransportMethod: String? = null,
        val returnTransportNotes: String? = null,

        // Step 6: Financial Support
        val needsFinancialAssistance: Boolean = false,
        val financialAssistanceResourceIds: List<String> = emptyList(),
        val estimatedTotalCost: Double? = null,
        val financialNotes: String? = null,

        // Step 7: Privacy Settings
        val autoDeleteAfterCompletion: Boolean = true,
        val autoDeleteDays: Int = 30,
        val useStealthMode: Boolean = false,

        // Additional
        val needsAccompaniment: Boolean = false,
        val accompanimentResourceId: String? = null
    ) {
        val isStep1Complete: Boolean
            get() = clinicResourceId != null && appointmentDate != null

        val isStep2Complete: Boolean
            get() = !needsChildcare || (childcareForAppointment || childcareForRecovery)

        val isStep3Complete: Boolean
            get() = departureLocation != null && departureDate != null && outboundTransportMethod != null

        val isStep4Complete: Boolean
            get() = !needsRecoveryHousing || (recoveryHousingResourceId != null && estimatedRecoveryDays != null)

        val isStep5Complete: Boolean
            get() = returnDate != null && returnTransportMethod != null

        val isStep6Complete: Boolean
            get() = !needsFinancialAssistance || financialAssistanceResourceIds.isNotEmpty()

        val isStep7Complete: Boolean
            get() = true // Privacy settings always valid (has defaults)

        val canProceedToNextStep: Boolean
            get() = when (_currentStepForValidation) {
                0 -> isStep1Complete
                1 -> isStep2Complete
                2 -> isStep3Complete
                3 -> isStep4Complete
                4 -> isStep5Complete
                5 -> isStep6Complete
                6 -> isStep7Complete
                else -> false
            }

        val isReadyToSave: Boolean
            get() = isStep1Complete && isStep2Complete && isStep3Complete &&
                    isStep4Complete && isStep5Complete && isStep6Complete && isStep7Complete

        companion object {
            private var _currentStepForValidation: Int = 0
            fun setValidationStep(step: Int) {
                _currentStepForValidation = step
            }
        }
    }

    /**
     * Load available clinics for selection
     */
    fun loadAvailableClinics(targetState: String? = null, outOfStateOnly: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val clinics = repository.getReproductiveHealthcareClinics(
                    state = targetState,
                    acceptsOutOfState = outOfStateOnly
                )
                _availableClinics.value = clinics
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load clinics: ${e.message}"
                )
            }
        }
    }

    // ==================== Step 1: Appointment Details ====================

    fun setClinic(clinicResourceId: String, clinicName: String) {
        _journeyDraft.value = _journeyDraft.value.copy(
            clinicResourceId = clinicResourceId,
            clinicName = clinicName
        )
    }

    fun setAppointmentDate(date: String) {
        _journeyDraft.value = _journeyDraft.value.copy(appointmentDate = date)
    }

    fun setAppointmentTime(time: String) {
        _journeyDraft.value = _journeyDraft.value.copy(appointmentTime = time)
    }

    fun setAppointmentNotes(notes: String) {
        _journeyDraft.value = _journeyDraft.value.copy(appointmentNotes = notes)
    }

    // ==================== Step 2: Childcare ====================

    fun setNeedsChildcare(needs: Boolean) {
        _journeyDraft.value = _journeyDraft.value.copy(needsChildcare = needs)
    }

    fun setChildcareForAppointment(needs: Boolean) {
        _journeyDraft.value = _journeyDraft.value.copy(childcareForAppointment = needs)
    }

    fun setChildcareForRecovery(needs: Boolean) {
        _journeyDraft.value = _journeyDraft.value.copy(childcareForRecovery = needs)
    }

    fun setChildcareNotes(notes: String) {
        _journeyDraft.value = _journeyDraft.value.copy(childcareNotes = notes)
    }

    // ==================== Step 3: Outbound Travel ====================

    fun setDepartureLocation(location: String) {
        _journeyDraft.value = _journeyDraft.value.copy(departureLocation = location)
    }

    fun setDepartureDate(date: String) {
        _journeyDraft.value = _journeyDraft.value.copy(departureDate = date)
    }

    fun setOutboundTransportMethod(method: String) {
        _journeyDraft.value = _journeyDraft.value.copy(outboundTransportMethod = method)
    }

    fun setOutboundTransportNotes(notes: String) {
        _journeyDraft.value = _journeyDraft.value.copy(outboundTransportNotes = notes)
    }

    // ==================== Step 4: Recovery ====================

    fun setNeedsRecoveryHousing(needs: Boolean) {
        _journeyDraft.value = _journeyDraft.value.copy(needsRecoveryHousing = needs)
    }

    fun setRecoveryHousing(resourceId: String) {
        _journeyDraft.value = _journeyDraft.value.copy(recoveryHousingResourceId = resourceId)
    }

    fun setEstimatedRecoveryDays(days: Int) {
        _journeyDraft.value = _journeyDraft.value.copy(estimatedRecoveryDays = days)
    }

    fun setRecoveryNotes(notes: String) {
        _journeyDraft.value = _journeyDraft.value.copy(recoveryNotes = notes)
    }

    // ==================== Step 5: Return Travel ====================

    fun setReturnDate(date: String) {
        _journeyDraft.value = _journeyDraft.value.copy(returnDate = date)
    }

    fun setReturnTransportMethod(method: String) {
        _journeyDraft.value = _journeyDraft.value.copy(returnTransportMethod = method)
    }

    fun setReturnTransportNotes(notes: String) {
        _journeyDraft.value = _journeyDraft.value.copy(returnTransportNotes = notes)
    }

    // ==================== Step 6: Financial Support ====================

    fun setNeedsFinancialAssistance(needs: Boolean) {
        _journeyDraft.value = _journeyDraft.value.copy(needsFinancialAssistance = needs)
    }

    fun addFinancialAssistanceResource(resourceId: String) {
        val current = _journeyDraft.value.financialAssistanceResourceIds
        if (!current.contains(resourceId)) {
            _journeyDraft.value = _journeyDraft.value.copy(
                financialAssistanceResourceIds = current + resourceId
            )
        }
    }

    fun removeFinancialAssistanceResource(resourceId: String) {
        val current = _journeyDraft.value.financialAssistanceResourceIds
        _journeyDraft.value = _journeyDraft.value.copy(
            financialAssistanceResourceIds = current - resourceId
        )
    }

    fun setEstimatedTotalCost(cost: Double) {
        _journeyDraft.value = _journeyDraft.value.copy(estimatedTotalCost = cost)
    }

    fun setFinancialNotes(notes: String) {
        _journeyDraft.value = _journeyDraft.value.copy(financialNotes = notes)
    }

    fun setNeedsAccompaniment(needs: Boolean) {
        _journeyDraft.value = _journeyDraft.value.copy(needsAccompaniment = needs)
    }

    fun setAccompanimentResource(resourceId: String) {
        _journeyDraft.value = _journeyDraft.value.copy(accompanimentResourceId = resourceId)
    }

    // ==================== Step 7: Privacy Settings ====================

    fun setAutoDeleteAfterCompletion(enabled: Boolean) {
        _journeyDraft.value = _journeyDraft.value.copy(autoDeleteAfterCompletion = enabled)
    }

    fun setAutoDeleteDays(days: Int) {
        _journeyDraft.value = _journeyDraft.value.copy(autoDeleteDays = days)
    }

    fun setUseStealthMode(enabled: Boolean) {
        _journeyDraft.value = _journeyDraft.value.copy(useStealthMode = enabled)
    }

    // ==================== Navigation ====================

    fun nextStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep < 6) {
            JourneyDraft.setValidationStep(currentStep + 1)
            _uiState.value = _uiState.value.copy(currentStep = currentStep + 1)
        }
    }

    fun previousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep > 0) {
            JourneyDraft.setValidationStep(currentStep - 1)
            _uiState.value = _uiState.value.copy(currentStep = currentStep - 1)
        }
    }

    fun goToStep(step: Int) {
        if (step in 0..6) {
            JourneyDraft.setValidationStep(step)
            _uiState.value = _uiState.value.copy(currentStep = step)
        }
    }

    fun canProceedToNextStep(): Boolean {
        JourneyDraft.setValidationStep(_uiState.value.currentStep)
        return _journeyDraft.value.canProceedToNextStep
    }

    // ==================== Save Journey ====================

    /**
     * Save the healthcare journey
     * Creates new journey with encrypted sensitive data
     */
    fun saveJourney(userId: String) {
        if (!_journeyDraft.value.isReadyToSave) {
            _uiState.value = _uiState.value.copy(
                error = "Please complete all required steps before saving"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val draft = _journeyDraft.value

                createJourneyUseCase.execute(
                    userId = userId,
                    clinicResourceId = draft.clinicResourceId!!,
                    appointmentDate = draft.appointmentDate!!,
                    appointmentTime = draft.appointmentTime,
                    appointmentNotes = draft.appointmentNotes,
                    needsChildcare = draft.needsChildcare,
                    childcareDuringAppointment = draft.childcareForAppointment,
                    childcareDuringRecovery = draft.childcareForRecovery,
                    childcareNotes = draft.childcareNotes,
                    departureLocation = draft.departureLocation!!,
                    departureDate = draft.departureDate!!,
                    outboundTransportMethod = draft.outboundTransportMethod!!,
                    outboundTransportNotes = draft.outboundTransportNotes,
                    needsRecoveryHousing = draft.needsRecoveryHousing,
                    recoveryHousingResourceId = draft.recoveryHousingResourceId,
                    estimatedRecoveryDays = draft.estimatedRecoveryDays,
                    recoveryNotes = draft.recoveryNotes,
                    returnDate = draft.returnDate!!,
                    returnTransportMethod = draft.returnTransportMethod!!,
                    returnTransportNotes = draft.returnTransportNotes,
                    needsFinancialAssistance = draft.needsFinancialAssistance,
                    estimatedTotalCost = draft.estimatedTotalCost,
                    financialNotes = draft.financialNotes,
                    needsAccompaniment = draft.needsAccompaniment,
                    accompanimentResourceId = draft.accompanimentResourceId,
                    autoDeleteAfterCompletion = draft.autoDeleteAfterCompletion,
                    autoDeleteDays = draft.autoDeleteDays,
                    useStealthMode = draft.useStealthMode
                ).onSuccess { journey ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = true,
                        createdJourneyId = journey.id
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Failed to save journey: ${error.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "An error occurred: ${e.message}"
                )
            }
        }
    }

    /**
     * Reset the journey planner
     */
    fun resetJourney() {
        _journeyDraft.value = JourneyDraft()
        _uiState.value = UiState()
        JourneyDraft.setValidationStep(0)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
