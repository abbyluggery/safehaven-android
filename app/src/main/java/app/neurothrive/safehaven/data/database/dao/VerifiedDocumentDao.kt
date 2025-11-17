package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.VerifiedDocument
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Verified Documents
 * Handles cryptographically verified legal documents
 */
@Dao
interface VerifiedDocumentDao {

    @Query("SELECT * FROM verified_documents WHERE userId = :userId ORDER BY notarizationDate DESC")
    suspend fun getAll(userId: String): List<VerifiedDocument>

    @Query("SELECT * FROM verified_documents WHERE userId = :userId ORDER BY notarizationDate DESC")
    fun getAllFlow(userId: String): Flow<List<VerifiedDocument>>

    @Query("SELECT * FROM verified_documents WHERE id = :id")
    suspend fun getById(id: String): VerifiedDocument?

    @Query("SELECT * FROM verified_documents WHERE cryptographicHash = :hash")
    suspend fun getByHash(hash: String): VerifiedDocument?

    @Query("SELECT * FROM verified_documents WHERE documentType = :type AND userId = :userId")
    suspend fun getByType(userId: String, type: String): List<VerifiedDocument>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: VerifiedDocument)

    @Update
    suspend fun update(document: VerifiedDocument)

    @Delete
    suspend fun delete(document: VerifiedDocument)

    @Query("DELETE FROM verified_documents WHERE userId = :userId")
    suspend fun deleteAll(userId: String)

    @Query("UPDATE verified_documents SET syncedToSalesforce = 1, salesforceId = :salesforceId WHERE id = :id")
    suspend fun markAsSynced(id: String, salesforceId: String)
}
