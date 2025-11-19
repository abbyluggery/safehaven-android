package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.domain.usecases.PanicDeleteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SafeHavenRepository,
    private val panicDeleteUseCase: PanicDeleteUseCase
) : ViewModel() {

    // Get total incident count
    val incidentCount: StateFlow<Int> = repository.getAllIncidentReports("current_user_id")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        .run {
            kotlinx.coroutines.flow.map { it.size }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0
                )
        }

    // Get total evidence count
    val evidenceCount: StateFlow<Int> = repository.getAllEvidenceItems("current_user_id")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        .run {
            kotlinx.coroutines.flow.map { it.size }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0
                )
        }

    /**
     * Trigger panic delete - destroys all evidence in <2 seconds
     */
    fun triggerPanicDelete(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                panicDeleteUseCase.execute("current_user_id")
                onComplete()
            } catch (e: Exception) {
                // Log error but still complete to protect user
                onComplete()
            }
        }
    }
}
