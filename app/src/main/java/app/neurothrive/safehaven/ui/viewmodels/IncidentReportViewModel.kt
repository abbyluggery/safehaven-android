package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.database.entities.IncidentReport
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class IncidentReportViewModel @Inject constructor(
    private val repository: SafeHavenRepository,
    private val crypto: SafeHavenCrypto
) : ViewModel() {

    /**
     * Save encrypted incident report
     */
    fun saveIncidentReport(
        userId: String,
        description: String,
        witnesses: String?,
        policeInvolved: Boolean,
        policeReportNumber: String?,
        medicalAttention: Boolean,
        location: String?,
        linkedEvidenceIds: List<String>?,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Encrypt sensitive fields
                val encryptedDescription = crypto.encrypt(description)
                val encryptedWitnesses = witnesses?.let { crypto.encrypt(it) }
                val encryptedLocation = location?.let { crypto.encrypt(it) }

                val report = IncidentReport(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    incidentDate = System.currentTimeMillis(),
                    descriptionEncrypted = encryptedDescription,
                    witnessesEncrypted = encryptedWitnesses,
                    policeInvolved = policeInvolved,
                    policeReportNumber = policeReportNumber,
                    medicalAttention = medicalAttention,
                    locationEncrypted = encryptedLocation,
                    linkedEvidenceIds = linkedEvidenceIds,
                    pdfPath = null, // Generated later if exported
                    isSynced = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                repository.insertIncidentReport(report)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Get all incident reports for user
     */
    fun getAllIncidentReports(userId: String): Flow<List<IncidentReport>> {
        return repository.getAllIncidentReports(userId)
    }

    /**
     * Get single incident report by ID
     */
    fun getIncidentReportById(id: String): Flow<IncidentReport?> {
        return repository.getIncidentReportById(id)
    }

    /**
     * Update existing incident report
     */
    fun updateIncidentReport(report: IncidentReport, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateIncidentReport(report)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Delete incident report
     */
    fun deleteIncidentReport(report: IncidentReport, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteIncidentReport(report)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
