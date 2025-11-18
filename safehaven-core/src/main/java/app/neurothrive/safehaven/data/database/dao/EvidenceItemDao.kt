package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.EvidenceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceItemDao {
    @Query("SELECT * FROM evidence_items WHERE userId = :userId AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllFlow(userId: String): Flow<List<EvidenceItem>>
    
    @Query("SELECT * FROM evidence_items WHERE userId = :userId AND isDeleted = 0 ORDER BY timestamp DESC")
    suspend fun getAll(userId: String): List<EvidenceItem>
    
    @Query("SELECT * FROM evidence_items WHERE incidentReportId = :reportId AND isDeleted = 0")
    suspend fun getByIncident(reportId: String): List<EvidenceItem>
    
    @Query("SELECT * FROM evidence_items WHERE id = :id")
    suspend fun getById(id: String): EvidenceItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: EvidenceItem)
    
    @Update
    suspend fun update(item: EvidenceItem)
    
    @Delete
    suspend fun delete(item: EvidenceItem)
    
    @Query("UPDATE evidence_items SET isDeleted = 1, deletedDate = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long)
    
    @Query("DELETE FROM evidence_items WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
