package com.securetrack.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Emergency Contact Entity
 * Stores trusted phone numbers for alerts and recovery
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    val phoneNumber: String,
    
    val isPrimary: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis(),
    
    val updatedAt: Long = System.currentTimeMillis()
)
