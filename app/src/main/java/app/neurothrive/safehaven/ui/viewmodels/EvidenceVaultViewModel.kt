package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.database.entities.EvidenceItem
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EvidenceVaultViewModel @Inject constructor(
    private val repository: SafeHavenRepository,
    private val crypto: SafeHavenCrypto
) : ViewModel() {

    /**
     * Get all evidence items for user
     */
    fun getAllEvidence(userId: String): Flow<List<EvidenceItem>> {
        return repository.getAllEvidenceItems(userId)
    }

    /**
     * Get evidence as StateFlow for Compose
     */
    fun getAllEvidenceState(userId: String): StateFlow<List<EvidenceItem>> {
        return repository.getAllEvidenceItems(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * Decrypt evidence file for viewing
     */
    fun decryptEvidenceFile(encryptedFilePath: String): File? {
        return try {
            val encryptedFile = File(encryptedFilePath)
            if (encryptedFile.exists()) {
                crypto.decryptFile(encryptedFile)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete evidence item (secure delete)
     */
    fun deleteEvidence(evidence: EvidenceItem, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                // Securely delete the file first
                val file = File(evidence.encryptedFilePath)
                if (file.exists()) {
                    crypto.secureDelete(file)
                }

                // Delete from database
                repository.deleteEvidenceItem(evidence)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Get storage size of all evidence
     */
    fun getTotalStorageSize(evidenceList: List<EvidenceItem>): Long {
        return evidenceList.sumOf { it.fileSizeBytes }
    }

    /**
     * Get evidence by incident report ID
     */
    fun getEvidenceForIncident(incidentId: String, userId: String): Flow<List<EvidenceItem>> {
        // Filter evidence linked to specific incident
        return kotlinx.coroutines.flow.flow {
            repository.getAllEvidenceItems(userId).collect { allEvidence ->
                emit(allEvidence.filter { it.linkedIncidentReportId == incidentId })
            }
        }
    }
}
