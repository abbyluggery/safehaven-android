package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.SafeHavenProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeHavenProfileDao {
    @Query("SELECT * FROM safehaven_profiles WHERE userId = :userId")
    fun getProfileFlow(userId: String): Flow<SafeHavenProfile?>
    
    @Query("SELECT * FROM safehaven_profiles WHERE userId = :userId")
    suspend fun getProfile(userId: String): SafeHavenProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: SafeHavenProfile)
    
    @Update
    suspend fun update(profile: SafeHavenProfile)
    
    @Delete
    suspend fun delete(profile: SafeHavenProfile)
    
    @Query("UPDATE safehaven_profiles SET lastSyncDate = :timestamp WHERE userId = :userId")
    suspend fun updateSyncDate(userId: String, timestamp: Long)
    
    @Query("DELETE FROM safehaven_profiles WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)
}
