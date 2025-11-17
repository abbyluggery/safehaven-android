package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.SOSSession
import app.neurothrive.safehaven.data.database.entities.SOSLocationUpdate
import kotlinx.coroutines.flow.Flow

/**
 * DAO for SOS Sessions
 * Manages SOS panic button activation tracking
 */
@Dao
interface SOSSessionDao {

    // ========== SOS Sessions ==========

    /**
     * Get active SOS session for user (should be max 1)
     */
    @Query("SELECT * FROM sos_sessions WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveSession(userId: String): SOSSession?

    /**
     * Get active SOS session as Flow (reactive)
     */
    @Query("SELECT * FROM sos_sessions WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun getActiveSessionFlow(userId: String): Flow<SOSSession?>

    /**
     * Get all SOS sessions for user (history)
     */
    @Query("SELECT * FROM sos_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllSessionsFlow(userId: String): Flow<List<SOSSession>>

    /**
     * Get session by ID
     */
    @Query("SELECT * FROM sos_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SOSSession?

    /**
     * Get recent SOS sessions (last 30 days)
     */
    @Query("SELECT * FROM sos_sessions WHERE userId = :userId AND startTime > :thirtyDaysAgo ORDER BY startTime DESC")
    suspend fun getRecentSessions(userId: String, thirtyDaysAgo: Long): List<SOSSession>

    /**
     * Get session count for user
     */
    @Query("SELECT COUNT(*) FROM sos_sessions WHERE userId = :userId")
    suspend fun getSessionCount(userId: String): Int

    /**
     * Insert new SOS session
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SOSSession): Long

    /**
     * Update SOS session
     */
    @Update
    suspend fun updateSession(session: SOSSession)

    /**
     * End SOS session (mark as inactive)
     */
    @Query("""
        UPDATE sos_sessions
        SET isActive = 0,
            endTime = :endTime,
            durationSeconds = :durationSeconds,
            deactivationMethod = :deactivationMethod,
            deactivatedBy = :deactivatedBy,
            allClearSent = :allClearSent
        WHERE id = :sessionId
    """)
    suspend fun endSession(
        sessionId: Long,
        endTime: Long,
        durationSeconds: Long,
        deactivationMethod: String,
        deactivatedBy: String,
        allClearSent: Boolean
    )

    /**
     * Update location during active session
     */
    @Query("""
        UPDATE sos_sessions
        SET lastLatitude = :latitude,
            lastLongitude = :longitude,
            lastAddress = :address,
            locationUpdateCount = locationUpdateCount + 1
        WHERE id = :sessionId
    """)
    suspend fun updateLocation(sessionId: Long, latitude: Double, longitude: Double, address: String?)

    /**
     * Increment SMS sent count
     */
    @Query("UPDATE sos_sessions SET totalSMSSent = totalSMSSent + :count WHERE id = :sessionId")
    suspend fun incrementSMSCount(sessionId: Long, count: Int)

    /**
     * Update contacts alerted
     */
    @Query("""
        UPDATE sos_sessions
        SET primaryContactsAlerted = :primaryCount,
            secondaryContactsAlerted = :secondaryCount,
            escalationTriggered = :escalationTriggered
        WHERE id = :sessionId
    """)
    suspend fun updateContactsAlerted(
        sessionId: Long,
        primaryCount: Int,
        secondaryCount: Int,
        escalationTriggered: Boolean
    )

    /**
     * Mark as false alarm
     */
    @Query("UPDATE sos_sessions SET falseAlarm = 1 WHERE id = :sessionId")
    suspend fun markAsFalseAlarm(sessionId: Long)

    /**
     * Delete SOS session
     */
    @Delete
    suspend fun deleteSession(session: SOSSession)

    /**
     * Delete all sessions for user (panic delete)
     */
    @Query("DELETE FROM sos_sessions WHERE userId = :userId")
    suspend fun deleteAllSessionsForUser(userId: String)

    /**
     * Get unsynced sessions (for Salesforce sync)
     */
    @Query("SELECT * FROM sos_sessions WHERE syncedToSalesforce = 0")
    suspend fun getUnsyncedSessions(): List<SOSSession>

    /**
     * Mark session as synced
     */
    @Query("UPDATE sos_sessions SET syncedToSalesforce = 1 WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: Long)

    // ========== Location Updates ==========

    /**
     * Insert location update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationUpdate(update: SOSLocationUpdate): Long

    /**
     * Get all location updates for session
     */
    @Query("SELECT * FROM sos_location_updates WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getLocationUpdatesForSession(sessionId: Long): List<SOSLocationUpdate>

    /**
     * Get location updates as Flow
     */
    @Query("SELECT * FROM sos_location_updates WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getLocationUpdatesFlow(sessionId: Long): Flow<List<SOSLocationUpdate>>

    /**
     * Mark location update as sent
     */
    @Query("UPDATE sos_location_updates SET sentToContacts = 1, sentAt = :sentAt WHERE id = :updateId")
    suspend fun markLocationUpdateSent(updateId: Long, sentAt: Long)

    /**
     * Delete all location updates for session
     */
    @Query("DELETE FROM sos_location_updates WHERE sessionId = :sessionId")
    suspend fun deleteLocationUpdatesForSession(sessionId: Long)

    /**
     * Delete all location updates for user (panic delete)
     */
    @Query("DELETE FROM sos_location_updates WHERE userId = :userId")
    suspend fun deleteAllLocationUpdatesForUser(userId: String)
}
