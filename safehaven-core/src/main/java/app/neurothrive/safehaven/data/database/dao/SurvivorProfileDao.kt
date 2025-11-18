package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.SurvivorProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface SurvivorProfileDao {
    @Query("SELECT * FROM survivor_profiles WHERE userId = :userId")
    fun getProfileFlow(userId: String): Flow<SurvivorProfile?>
    
    @Query("SELECT * FROM survivor_profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): SurvivorProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: SurvivorProfile)
    
    @Update
    suspend fun update(profile: SurvivorProfile)
    
    @Delete
    suspend fun delete(profile: SurvivorProfile)
    
    @Query("DELETE FROM survivor_profiles WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)
}
