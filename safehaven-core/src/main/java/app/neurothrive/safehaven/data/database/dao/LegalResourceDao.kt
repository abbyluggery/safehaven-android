package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.LegalResource
import kotlinx.coroutines.flow.Flow

@Dao
interface LegalResourceDao {
    @Query("""
        SELECT * FROM legal_resources 
        WHERE resourceType = :type 
        AND latitude IS NOT NULL 
        AND longitude IS NOT NULL
        LIMIT 100
    """)
    suspend fun getByType(type: String): List<LegalResource>
    
    @Query("""
        SELECT * FROM legal_resources
        WHERE resourceType = :type
        AND (:lgbtq = 0 OR servesLGBTQIA = 1)
        AND (:bipoc = 0 OR servesBIPOC = 1)
        AND (:male = 0 OR servesMaleIdentifying = 1)
        AND (:undoc = 0 OR servesUndocumented = 1)
        LIMIT 100
    """)
    suspend fun getFiltered(
        type: String,
        lgbtq: Boolean,
        bipoc: Boolean,
        male: Boolean,
        undoc: Boolean
    ): List<LegalResource>
    
    @Query("SELECT * FROM legal_resources WHERE id = :id")
    suspend fun getById(id: String): LegalResource?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resource: LegalResource)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<LegalResource>)
    
    @Update
    suspend fun update(resource: LegalResource)
    
    @Delete
    suspend fun delete(resource: LegalResource)
    
    @Query("DELETE FROM legal_resources")
    suspend fun deleteAll()
}
