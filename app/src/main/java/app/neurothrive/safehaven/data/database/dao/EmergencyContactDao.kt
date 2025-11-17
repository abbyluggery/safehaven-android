package app.neurothrive.safehaven.data.database.dao

import androidx.room.*
import app.neurothrive.safehaven.data.database.entities.EmergencyContact
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Emergency Contacts
 * Manages trusted contacts for SOS panic button alerts
 */
@Dao
interface EmergencyContactDao {

    /**
     * Get all emergency contacts for a user (Flow for reactive updates)
     */
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId ORDER BY isPrimary DESC, name ASC")
    fun getAllContactsFlow(userId: String): Flow<List<EmergencyContact>>

    /**
     * Get all emergency contacts for a user (one-time)
     */
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId ORDER BY isPrimary DESC, name ASC")
    suspend fun getAllContacts(userId: String): List<EmergencyContact>

    /**
     * Get primary contacts (alerted immediately during SOS)
     */
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND isPrimary = 1 ORDER BY name ASC")
    suspend fun getPrimaryContacts(userId: String): List<EmergencyContact>

    /**
     * Get secondary contacts (escalation after 15 minutes)
     */
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND isPrimary = 0 ORDER BY name ASC")
    suspend fun getSecondaryContacts(userId: String): List<EmergencyContact>

    /**
     * Get verified contacts only (confirmed they receive alerts)
     */
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND isVerified = 1 ORDER BY isPrimary DESC, name ASC")
    suspend fun getVerifiedContacts(userId: String): List<EmergencyContact>

    /**
     * Get contact by ID
     */
    @Query("SELECT * FROM emergency_contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: Long): EmergencyContact?

    /**
     * Get contact count for user
     */
    @Query("SELECT COUNT(*) FROM emergency_contacts WHERE userId = :userId")
    suspend fun getContactCount(userId: String): Int

    /**
     * Get primary contact count
     */
    @Query("SELECT COUNT(*) FROM emergency_contacts WHERE userId = :userId AND isPrimary = 1")
    suspend fun getPrimaryContactCount(userId: String): Int

    /**
     * Insert new emergency contact
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    /**
     * Insert multiple contacts
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<EmergencyContact>)

    /**
     * Update emergency contact
     */
    @Update
    suspend fun updateContact(contact: EmergencyContact)

    /**
     * Update last tested date
     */
    @Query("UPDATE emergency_contacts SET lastTestedDate = :timestamp, isVerified = :verified WHERE id = :contactId")
    suspend fun updateLastTested(contactId: Long, timestamp: Long, verified: Boolean)

    /**
     * Mark contact as verified
     */
    @Query("UPDATE emergency_contacts SET isVerified = :verified WHERE id = :contactId")
    suspend fun updateVerificationStatus(contactId: Long, verified: Boolean)

    /**
     * Delete emergency contact
     */
    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    /**
     * Delete contact by ID
     */
    @Query("DELETE FROM emergency_contacts WHERE id = :contactId")
    suspend fun deleteContactById(contactId: Long)

    /**
     * Delete all contacts for user (used during panic delete)
     */
    @Query("DELETE FROM emergency_contacts WHERE userId = :userId")
    suspend fun deleteAllContactsForUser(userId: String)

    /**
     * Get unsynced contacts (for Salesforce sync)
     */
    @Query("SELECT * FROM emergency_contacts WHERE syncedToSalesforce = 0")
    suspend fun getUnsyncedContacts(): List<EmergencyContact>

    /**
     * Mark contact as synced
     */
    @Query("UPDATE emergency_contacts SET syncedToSalesforce = 1 WHERE id = :contactId")
    suspend fun markAsSynced(contactId: Long)
}
