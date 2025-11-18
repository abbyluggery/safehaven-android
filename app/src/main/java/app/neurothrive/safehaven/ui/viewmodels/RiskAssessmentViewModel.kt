package app.neurothrive.safehaven.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.neurothrive.safehaven.data.database.dao.RiskAssessmentDao
import app.neurothrive.safehaven.data.database.entities.RiskAssessment
import app.neurothrive.safehaven.data.database.entities.RiskLevel
import app.neurothrive.safehaven.data.session.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Risk Assessment ViewModel
 *
 * Manages AI-powered risk assessments and crisis detection
 *
 * Features:
 * - Display latest risk assessment
 * - Create new assessment (triggers Salesforce AI analysis)
 * - Track risk score trends
 * - Crisis alerts for critical risk
 * - Pattern detection visualizations
 */
@HiltViewModel
class RiskAssessmentViewModel @Inject constructor(
    private val riskAssessmentDao: RiskAssessmentDao,
    private val userSession: UserSession
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Latest risk assessment (reactive)
    val latestAssessment: StateFlow<RiskAssessment?> = userSession.currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            riskAssessmentDao.getLatestAssessmentFlow(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // All assessments for user (reactive)
    val allAssessments: StateFlow<List<RiskAssessment>> = userSession.currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            riskAssessmentDao.getAllAssessmentsFlow(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Unacknowledged assessments (reactive)
    val unacknowledgedAssessments: StateFlow<List<RiskAssessment>> = userSession.currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            riskAssessmentDao.getUnacknowledgedAssessmentsFlow(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Has critical risk flag
    val hasCriticalRisk: StateFlow<Boolean> = latestAssessment
        .map { it?.isCriticalRisk == true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Create new risk assessment
     * Calls Salesforce API to generate AI-powered risk analysis
     *
     * TODO: Implement Salesforce API call when OAuth is configured
     * For now, this creates a placeholder that will be synced later
     */
    fun createAssessment() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val userId = userSession.currentUserId.value
                if (userId == null) {
                    _errorMessage.value = "User not logged in"
                    return@launch
                }

                // TODO: Call Salesforce API
                // val response = riskAssessmentApi.createAssessment(userId)
                // saveAssessmentFromApi(response)

                _errorMessage.value = "API integration pending - OAuth configuration required. See OAUTH_CONNECTED_APP_SETUP.md"

            } catch (e: Exception) {
                _errorMessage.value = "Failed to create assessment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Acknowledge risk assessment (mark as seen by user)
     */
    fun acknowledgeAssessment(assessmentId: Long) {
        viewModelScope.launch {
            try {
                riskAssessmentDao.markAsAcknowledged(
                    assessmentId = assessmentId,
                    acknowledged = true,
                    acknowledgedAt = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to acknowledge assessment: ${e.message}"
            }
        }
    }

    /**
     * Get risk score trend for visualization
     */
    suspend fun getRiskScoreTrend(): List<Pair<Long, Int>> {
        val userId = userSession.currentUserId.value ?: return emptyList()
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        return riskAssessmentDao.getRiskScoreTrend(userId, thirtyDaysAgo)
            .map { it.assessmentDate to it.overallRiskScore }
    }

    /**
     * Get pattern frequency statistics
     */
    suspend fun getPatternFrequency(): Map<String, Int> {
        val userId = userSession.currentUserId.value ?: return emptyMap()
        val allPatternStrings = riskAssessmentDao.getAllPatternsForUser(userId)

        val patternCounts = mutableMapOf<String, Int>()
        allPatternStrings.forEach { patternsString ->
            patternsString.split(",").forEach { pattern ->
                val trimmed = pattern.trim()
                if (trimmed.isNotBlank()) {
                    patternCounts[trimmed] = (patternCounts[trimmed] ?: 0) + 1
                }
            }
        }
        return patternCounts
    }

    /**
     * Check if user should be prompted for new assessment
     * Recommendation: New assessment every 7 days or after new incident
     */
    suspend fun shouldPromptForAssessment(): Boolean {
        val userId = userSession.currentUserId.value ?: return false
        val daysSince = riskAssessmentDao.getDaysSinceLastAssessment(
            userId,
            System.currentTimeMillis()
        )

        return daysSince == null || daysSince >= 7
    }

    /**
     * Get assessment statistics
     */
    suspend fun getStatistics(): AssessmentStatistics {
        val userId = userSession.currentUserId.value
            ?: return AssessmentStatistics()

        return AssessmentStatistics(
            totalAssessments = riskAssessmentDao.getAssessmentCount(userId),
            latestScore = riskAssessmentDao.getLatestRiskScore(userId),
            averageScore = riskAssessmentDao.getAverageRiskScore(userId),
            criticalCount = riskAssessmentDao.getCountByRiskLevel(userId, RiskLevel.CRITICAL),
            highCount = riskAssessmentDao.getCountByRiskLevel(userId, RiskLevel.HIGH),
            hasStrangulationHistory = riskAssessmentDao.hasStrangulationHistory(userId),
            hasThreatsToKillHistory = riskAssessmentDao.hasThreatsToKillHistory(userId)
        )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Save assessment from API response (for future API integration)
     */
    private suspend fun saveAssessmentFromApi(response: app.neurothrive.safehaven.data.api.RiskAssessmentResponse) {
        if (!response.success || response.assessmentId == null) {
            _errorMessage.value = response.errorMessage ?: "Assessment creation failed"
            return
        }

        val userId = userSession.currentUserId.value ?: return

        val assessment = RiskAssessment(
            userId = userId,
            assessmentId = response.assessmentId,
            overallRiskScore = response.overallScore ?: 0,
            lethalityRiskScore = response.lethalityScore ?: 0,
            escalationRiskScore = response.escalationScore ?: 0,
            frequencyRiskScore = response.frequencyScore ?: 0,
            riskLevel = RiskLevel.fromString(response.riskLevel ?: "low"),
            aiSummary = response.summary ?: "",
            recommendedActions = response.recommendations ?: "",
            crisisResources = response.crisisResources ?: "",
            detectedPatterns = response.detectedPatterns?.joinToString(",") ?: "",
            isCriticalRisk = response.riskLevel == "critical",
            syncedToSalesforce = true,
            lastSyncedAt = System.currentTimeMillis()
        )

        riskAssessmentDao.insertAssessment(assessment)
    }
}

/**
 * Assessment statistics data class
 */
data class AssessmentStatistics(
    val totalAssessments: Int = 0,
    val latestScore: Int? = null,
    val averageScore: Double? = null,
    val criticalCount: Int = 0,
    val highCount: Int = 0,
    val hasStrangulationHistory: Boolean = false,
    val hasThreatsToKillHistory: Boolean = false
)
