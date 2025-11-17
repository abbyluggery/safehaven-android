package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.IncidentReport
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Incident Reports
 * Handles legal-formatted abuse incident documentation
 */
@Dao
interface IncidentReportDao {

    @Query("SELECT * FROM incident_reports WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getAll(userId: String): List<IncidentReport>

    @Query("SELECT * FROM incident_reports WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllFlow(userId: String): Flow<List<IncidentReport>>

    @Query("SELECT * FROM incident_reports WHERE id = :id")
    suspend fun getById(id: String): IncidentReport?

    @Query("SELECT * FROM incident_reports WHERE syncedToSalesforce = 0")
    suspend fun getUnsynced(): List<IncidentReport>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: IncidentReport)

    @Update
    suspend fun update(report: IncidentReport)

    @Delete
    suspend fun delete(report: IncidentReport)

    @Query("DELETE FROM incident_reports WHERE userId = :userId")
    suspend fun deleteAll(userId: String)

    @Query("SELECT COUNT(*) FROM incident_reports WHERE userId = :userId")
    suspend fun getCount(userId: String): Int

    @Query("UPDATE incident_reports SET syncedToSalesforce = 1, salesforceId = :salesforceId WHERE id = :id")
    suspend fun markAsSynced(id: String, salesforceId: String)
}
