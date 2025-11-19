package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.database.entities.SafeHavenProfile
import app.neurothrive.safehaven.data.database.entities.SurvivorProfile
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SafeHavenRepository,
    private val crypto: SafeHavenCrypto
) : ViewModel() {

    /**
     * Get SafeHaven profile
     */
    fun getProfile(userId: String): Flow<SafeHavenProfile?> {
        return repository.getSafeHavenProfileByUserId(userId)
    }

    /**
     * Get SafeHaven profile as StateFlow
     */
    fun getProfileState(userId: String): StateFlow<SafeHavenProfile?> {
        return repository.getSafeHavenProfileByUserId(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    /**
     * Get survivor profile
     */
    fun getSurvivorProfile(userId: String): Flow<SurvivorProfile?> {
        return repository.getSurvivorProfileByUserId(userId)
    }

    /**
     * Update GPS setting
     */
    fun updateGPSSetting(userId: String, gpsEnabled: Boolean, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            try {
                repository.getSafeHavenProfileByUserId(userId).collect { profile ->
                    if (profile != null) {
                        val updated = profile.copy(
                            gpsEnabled = gpsEnabled,
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.updateSafeHavenProfile(updated)
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Update panic delete sensitivity
     */
    fun updatePanicDeleteSensitivity(
        userId: String,
        shakeSensitivity: Float,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.getSafeHavenProfileByUserId(userId).collect { profile ->
                    if (profile != null) {
                        val updated = profile.copy(
                            shakeSensitivity = shakeSensitivity,
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.updateSafeHavenProfile(updated)
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Update survivor profile (intersectional identity)
     */
    fun updateSurvivorProfile(
        userId: String,
        isLGBTQIA: Boolean,
        isTrans: Boolean,
        isBIPOC: Boolean,
        isMaleIdentifying: Boolean,
        isUndocumented: Boolean,
        hasDisability: Boolean,
        primaryLanguage: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.getSurvivorProfileByUserId(userId).collect { existing ->
                    val profile = if (existing != null) {
                        existing.copy(
                            isLGBTQIA = isLGBTQIA,
                            isTrans = isTrans,
                            isBIPOC = isBIPOC,
                            isMaleIdentifying = isMaleIdentifying,
                            isUndocumented = isUndocumented,
                            hasDisability = hasDisability,
                            primaryLanguage = primaryLanguage,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else {
                        SurvivorProfile(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            isLGBTQIA = isLGBTQIA,
                            isTrans = isTrans,
                            isBIPOC = isBIPOC,
                            isMaleIdentifying = isMaleIdentifying,
                            isUndocumented = isUndocumented,
                            hasDisability = hasDisability,
                            primaryLanguage = primaryLanguage,
                            currentLatitude = null,
                            currentLongitude = null,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    }

                    if (existing != null) {
                        repository.updateSurvivorProfile(profile)
                    } else {
                        repository.insertSurvivorProfile(profile)
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * Change password (encrypt with new password)
     */
    fun changePassword(
        userId: String,
        newPasswordHash: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.getSafeHavenProfileByUserId(userId).collect { profile ->
                    if (profile != null) {
                        val updated = profile.copy(
                            passwordHash = crypto.generateSHA256(newPasswordHash.toByteArray()),
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.updateSafeHavenProfile(updated)
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
