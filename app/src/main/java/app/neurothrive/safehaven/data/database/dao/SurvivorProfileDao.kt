package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.SurvivorProfile
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Survivor Profile
 * Handles extended demographic and preference data
 */
@Dao
interface SurvivorProfileDao {

    @Query("SELECT * FROM survivor_profiles WHERE userId = :userId")
    suspend fun getByUserId(userId: String): SurvivorProfile?

    @Query("SELECT * FROM survivor_profiles WHERE userId = :userId")
    fun getByUserIdFlow(userId: String): Flow<SurvivorProfile?>

    @Query("SELECT * FROM survivor_profiles WHERE id = :id")
    suspend fun getById(id: String): SurvivorProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: SurvivorProfile)

    @Update
    suspend fun update(profile: SurvivorProfile)

    @Query("UPDATE survivor_profiles SET updatedAt = :timestamp WHERE userId = :userId")
    suspend fun updateTimestamp(userId: String, timestamp: Long)

    @Query("UPDATE survivor_profiles SET currentlySafe = :safe WHERE userId = :userId")
    suspend fun updateSafetyStatus(userId: String, safe: Boolean)

    @Query("UPDATE survivor_profiles SET needsImmediateShelter = :needs WHERE userId = :userId")
    suspend fun updateShelterNeed(userId: String, needs: Boolean)

    @Delete
    suspend fun delete(profile: SurvivorProfile)

    @Query("DELETE FROM survivor_profiles WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)
}
