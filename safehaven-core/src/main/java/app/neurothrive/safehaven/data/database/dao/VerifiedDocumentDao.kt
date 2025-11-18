package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.VerifiedDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface VerifiedDocumentDao {
    @Query("SELECT * FROM verified_documents WHERE userId = :userId")
    fun getAllFlow(userId: String): Flow<List<VerifiedDocument>>
    
    @Query("SELECT * FROM verified_documents WHERE userId = :userId")
    suspend fun getAll(userId: String): List<VerifiedDocument>
    
    @Query("SELECT * FROM verified_documents WHERE cryptographicHash = :hash")
    suspend fun getByHash(hash: String): VerifiedDocument?
    
    @Query("SELECT * FROM verified_documents WHERE id = :id")
    suspend fun getById(id: String): VerifiedDocument?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: VerifiedDocument)
    
    @Update
    suspend fun update(document: VerifiedDocument)
    
    @Delete
    suspend fun delete(document: VerifiedDocument)
    
    @Query("DELETE FROM verified_documents WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
