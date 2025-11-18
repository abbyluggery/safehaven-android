package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.RiskAssessment
import app.neurothrive.safehaven.data.database.entities.RiskLevel
import kotlinx.coroutines.flow.Flow

/**
 * Risk Assessment DAO
 *
 * Database access for AI-generated risk assessments
 */
@Dao
interface RiskAssessmentDao {

    // ==================== INSERT ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: RiskAssessment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessments(assessments: List<RiskAssessment>)

    // ==================== UPDATE ====================

    @Update
    suspend fun updateAssessment(assessment: RiskAssessment)

    @Query("""
        UPDATE risk_assessments
        SET userAcknowledged = :acknowledged,
            acknowledgedAt = :acknowledgedAt
        WHERE id = :assessmentId
    """)
    suspend fun markAsAcknowledged(assessmentId: Long, acknowledged: Boolean, acknowledgedAt: Long)

    @Query("""
        UPDATE risk_assessments
        SET alertSent = :sent,
            alertSentAt = :sentAt
        WHERE id = :assessmentId
    """)
    suspend fun updateAlertStatus(assessmentId: Long, sent: Boolean, sentAt: Long?)

    @Query("""
        UPDATE risk_assessments
        SET syncedToSalesforce = :synced,
            lastSyncedAt = :syncedAt
        WHERE id = :assessmentId
    """)
    suspend fun updateSyncStatus(assessmentId: Long, synced: Boolean, syncedAt: Long?)

    // ==================== QUERY (FLOW) ====================

    /**
     * Get latest assessment for user (reactive)
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        ORDER BY assessmentDate DESC
        LIMIT 1
    """)
    fun getLatestAssessmentFlow(userId: String): Flow<RiskAssessment?>

    /**
     * Get all assessments for user (reactive)
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        ORDER BY assessmentDate DESC
    """)
    fun getAllAssessmentsFlow(userId: String): Flow<List<RiskAssessment>>

    /**
     * Get critical risk assessments (reactive)
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        AND isCriticalRisk = 1
        ORDER BY assessmentDate DESC
    """)
    fun getCriticalAssessmentsFlow(userId: String): Flow<List<RiskAssessment>>

    /**
     * Get unacknowledged assessments (reactive)
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        AND userAcknowledged = 0
        ORDER BY assessmentDate DESC
    """)
    fun getUnacknowledgedAssessmentsFlow(userId: String): Flow<List<RiskAssessment>>

    // ==================== QUERY (SUSPEND) ====================

    /**
     * Get latest assessment for user (one-time)
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        ORDER BY assessmentDate DESC
        LIMIT 1
    """)
    suspend fun getLatestAssessment(userId: String): RiskAssessment?

    /**
     * Get assessment by ID
     */
    @Query("SELECT * FROM risk_assessments WHERE id = :id")
    suspend fun getAssessmentById(id: Long): RiskAssessment?

    /**
     * Get assessment by Salesforce ID
     */
    @Query("SELECT * FROM risk_assessments WHERE assessmentId = :assessmentId")
    suspend fun getAssessmentBySalesforceId(assessmentId: String): RiskAssessment?

    /**
     * Get all assessments for user
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        ORDER BY assessmentDate DESC
    """)
    suspend fun getAllAssessments(userId: String): List<RiskAssessment>

    /**
     * Get assessments by risk level
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        AND riskLevel = :riskLevel
        ORDER BY assessmentDate DESC
    """)
    suspend fun getAssessmentsByRiskLevel(userId: String, riskLevel: RiskLevel): List<RiskAssessment>

    /**
     * Get critical risk assessments
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        AND isCriticalRisk = 1
        ORDER BY assessmentDate DESC
    """)
    suspend fun getCriticalAssessments(userId: String): List<RiskAssessment>

    /**
     * Get unacknowledged assessments
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        AND userAcknowledged = 0
        ORDER BY assessmentDate DESC
    """)
    suspend fun getUnacknowledgedAssessments(userId: String): List<RiskAssessment>

    /**
     * Get assessments within date range
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        AND assessmentDate BETWEEN :startDate AND :endDate
        ORDER BY assessmentDate DESC
    """)
    suspend fun getAssessmentsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): List<RiskAssessment>

    /**
     * Get assessments with specific pattern
     */
    @Query("""
        SELECT * FROM risk_assessments
        WHERE userId = :userId
        AND detectedPatterns LIKE '%' || :pattern || '%'
        ORDER BY assessmentDate DESC
    """)
    suspend fun getAssessmentsWithPattern(userId: String, pattern: String): List<RiskAssessment>

    // ==================== DELETE ====================

    @Delete
    suspend fun deleteAssessment(assessment: RiskAssessment)

    @Query("DELETE FROM risk_assessments WHERE id = :assessmentId")
    suspend fun deleteAssessmentById(assessmentId: Long)

    @Query("DELETE FROM risk_assessments WHERE userId = :userId")
    suspend fun deleteAllAssessmentsForUser(userId: String)

    @Query("DELETE FROM risk_assessments")
    suspend fun deleteAllAssessments()

    // ==================== STATISTICS ====================

    /**
     * Get assessment count for user
     */
    @Query("SELECT COUNT(*) FROM risk_assessments WHERE userId = :userId")
    suspend fun getAssessmentCount(userId: String): Int

    /**
     * Get count by risk level
     */
    @Query("""
        SELECT COUNT(*) FROM risk_assessments
        WHERE userId = :userId
        AND riskLevel = :riskLevel
    """)
    suspend fun getCountByRiskLevel(userId: String, riskLevel: RiskLevel): Int

    /**
     * Get latest overall risk score
     */
    @Query("""
        SELECT overallRiskScore FROM risk_assessments
        WHERE userId = :userId
        ORDER BY assessmentDate DESC
        LIMIT 1
    """)
    suspend fun getLatestRiskScore(userId: String): Int?

    /**
     * Get average risk score
     */
    @Query("""
        SELECT AVG(overallRiskScore) FROM risk_assessments
        WHERE userId = :userId
    """)
    suspend fun getAverageRiskScore(userId: String): Double?

    /**
     * Get risk score trend (last 30 days)
     */
    @Query("""
        SELECT overallRiskScore, assessmentDate
        FROM risk_assessments
        WHERE userId = :userId
        AND assessmentDate >= :thirtyDaysAgo
        ORDER BY assessmentDate ASC
    """)
    suspend fun getRiskScoreTrend(userId: String, thirtyDaysAgo: Long): List<RiskScoreTrend>

    /**
     * Check if user has critical risk assessment
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM risk_assessments
            WHERE userId = :userId
            AND isCriticalRisk = 1
        )
    """)
    suspend fun hasCriticalRisk(userId: String): Boolean

    /**
     * Check if user has unacknowledged assessment
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM risk_assessments
            WHERE userId = :userId
            AND userAcknowledged = 0
        )
    """)
    suspend fun hasUnacknowledgedAssessment(userId: String): Boolean

    /**
     * Get days since last assessment
     */
    @Query("""
        SELECT ((:currentTime - assessmentDate) / (1000 * 60 * 60 * 24)) AS daysSinceAssessment
        FROM risk_assessments
        WHERE userId = :userId
        ORDER BY assessmentDate DESC
        LIMIT 1
    """)
    suspend fun getDaysSinceLastAssessment(userId: String, currentTime: Long): Long?

    // ==================== PATTERN ANALYSIS ====================

    /**
     * Get most common patterns for user
     */
    @Query("""
        SELECT detectedPatterns
        FROM risk_assessments
        WHERE userId = :userId
        AND detectedPatterns IS NOT NULL
        AND detectedPatterns != ''
    """)
    suspend fun getAllPatternsForUser(userId: String): List<String>

    /**
     * Check if specific lethality indicator is present
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM risk_assessments
            WHERE userId = :userId
            AND strangulationAttempted = 1
        )
    """)
    suspend fun hasStrangulationHistory(userId: String): Boolean

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM risk_assessments
            WHERE userId = :userId
            AND threatsToKill = 1
        )
    """)
    suspend fun hasThreatsToKillHistory(userId: String): Boolean
}

/**
 * Data class for risk score trend
 */
data class RiskScoreTrend(
    val overallRiskScore: Int,
    val assessmentDate: Long
)
