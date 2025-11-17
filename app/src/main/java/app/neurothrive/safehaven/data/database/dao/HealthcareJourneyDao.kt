package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.HealthcareJourney
import kotlinx.coroutines.flow.Flow

/**
 * HealthcareJourney DAO
 * Purpose: CRUD operations for healthcare journey tracking
 *
 * PRIVACY NOTE: All queries return Flow for reactive updates.
 * Encrypted fields must be decrypted in the repository layer before use.
 */
@Dao
interface HealthcareJourneyDao {

    /**
     * Get all journeys for a user (reactive)
     * Returns most recent first
     */
    @Query("SELECT * FROM healthcare_journeys WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllForUserFlow(userId: String): Flow<List<HealthcareJourney>>

    /**
     * Get all active journeys (not completed or cancelled)
     */
    @Query("""
        SELECT * FROM healthcare_journeys
        WHERE userId = :userId
        AND journeyStatus NOT IN ('completed', 'cancelled')
        ORDER BY createdAt DESC
    """)
    fun getActiveJourneysFlow(userId: String): Flow<List<HealthcareJourney>>

    /**
     * Get journeys by status
     */
    @Query("""
        SELECT * FROM healthcare_journeys
        WHERE userId = :userId
        AND journeyStatus = :status
        ORDER BY createdAt DESC
    """)
    fun getJourneysByStatusFlow(userId: String, status: String): Flow<List<HealthcareJourney>>

    /**
     * Get single journey by ID
     */
    @Query("SELECT * FROM healthcare_journeys WHERE id = :journeyId")
    suspend fun getById(journeyId: String): HealthcareJourney?

    /**
     * Get single journey by ID (reactive)
     */
    @Query("SELECT * FROM healthcare_journeys WHERE id = :journeyId")
    fun getByIdFlow(journeyId: String): Flow<HealthcareJourney?>

    /**
     * Get journeys eligible for auto-deletion
     * (completed journeys past their auto-delete date)
     */
    @Query("""
        SELECT * FROM healthcare_journeys
        WHERE autoDeleteAfterCompletion = 1
        AND autoDeleteDate IS NOT NULL
        AND autoDeleteDate <= :currentTimestamp
    """)
    suspend fun getJourneysForAutoDeletion(currentTimestamp: Long): List<HealthcareJourney>

    /**
     * Get upcoming journeys (appointment in next 30 days)
     * Note: appointmentDateEncrypted must be decrypted in repository layer
     */
    @Query("""
        SELECT * FROM healthcare_journeys
        WHERE userId = :userId
        AND journeyStatus IN ('planning', 'confirmed', 'traveling_outbound')
        ORDER BY createdAt ASC
    """)
    fun getUpcomingJourneysFlow(userId: String): Flow<List<HealthcareJourney>>

    /**
     * Get journeys needing arrangements
     * (journeys where key items are not yet arranged)
     */
    @Query("""
        SELECT * FROM healthcare_journeys
        WHERE userId = :userId
        AND journeyStatus NOT IN ('completed', 'cancelled')
        AND (
            (needsChildcare = 1 AND childcareArranged = 0) OR
            (outboundTransportationArranged = 0) OR
            (needsRecoveryHousing = 1 AND recoveryHousingArranged = 0) OR
            (returnTransportationArranged = 0) OR
            (needsFinancialAssistance = 1 AND financialAssistanceArranged = 0) OR
            (needsAccompaniment = 1 AND accompanimentArranged = 0)
        )
        ORDER BY createdAt DESC
    """)
    fun getJourneysNeedingArrangementsFlow(userId: String): Flow<List<HealthcareJourney>>

    /**
     * Insert new journey
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journey: HealthcareJourney)

    /**
     * Update existing journey
     */
    @Update
    suspend fun update(journey: HealthcareJourney)

    /**
     * Delete journey
     */
    @Delete
    suspend fun delete(journey: HealthcareJourney)

    /**
     * Delete journey by ID
     */
    @Query("DELETE FROM healthcare_journeys WHERE id = :journeyId")
    suspend fun deleteById(journeyId: String)

    /**
     * Delete all journeys for a user (for panic delete)
     */
    @Query("DELETE FROM healthcare_journeys WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    /**
     * Mark journey as completed
     */
    @Query("""
        UPDATE healthcare_journeys
        SET journeyStatus = 'completed',
            completedAt = :completedTimestamp,
            lastUpdated = :completedTimestamp,
            autoDeleteDate = :autoDeleteDate
        WHERE id = :journeyId
    """)
    suspend fun markAsCompleted(
        journeyId: String,
        completedTimestamp: Long,
        autoDeleteDate: Long?
    )

    /**
     * Cancel journey
     */
    @Query("""
        UPDATE healthcare_journeys
        SET journeyStatus = 'cancelled',
            cancellationReason = :reason,
            lastUpdated = :timestamp
        WHERE id = :journeyId
    """)
    suspend fun cancelJourney(
        journeyId: String,
        reason: String?,
        timestamp: Long
    )

    /**
     * Update journey status
     */
    @Query("""
        UPDATE healthcare_journeys
        SET journeyStatus = :newStatus,
            lastUpdated = :timestamp
        WHERE id = :journeyId
    """)
    suspend fun updateStatus(
        journeyId: String,
        newStatus: String,
        timestamp: Long
    )

    /**
     * Count active journeys for a user
     */
    @Query("""
        SELECT COUNT(*) FROM healthcare_journeys
        WHERE userId = :userId
        AND journeyStatus NOT IN ('completed', 'cancelled')
    """)
    fun getActiveJourneyCountFlow(userId: String): Flow<Int>
}
