package com.securetrack.data.dao

import androidx.room.*
import com.securetrack.data.entities.EmergencyContact
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {

    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    suspend fun getAllContactsList(): List<EmergencyContact>

    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContactsSync(): List<EmergencyContact>

    @Query("SELECT * FROM emergency_contacts WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryContact(): EmergencyContact?

    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    suspend fun getContactById(id: Long): EmergencyContact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    @Update
    suspend fun updateContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("UPDATE emergency_contacts SET isPrimary = 0")
    suspend fun clearPrimaryStatus()

    @Transaction
    suspend fun setPrimaryContact(contactId: Long) {
        clearPrimaryStatus()
        val contact = getContactById(contactId)
        contact?.let {
            updateContact(it.copy(isPrimary = true, updatedAt = System.currentTimeMillis()))
        }
    }

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getContactCount(): Int
}
