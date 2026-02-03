package com.securetrack.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Command Log Entity
 * Tracks all SMS commands received and their execution status
 */
@Entity(tableName = "command_logs")
data class CommandLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val command: String,
    
    val senderNumber: String,
    
    val timestamp: Long = System.currentTimeMillis(),
    
    val status: CommandStatus = CommandStatus.RECEIVED,
    
    val resultMessage: String? = null,
    
    val locationLat: Double? = null,
    
    val locationLng: Double? = null
)

enum class CommandStatus {
    RECEIVED,
    PROCESSING,
    SUCCESS,
    FAILED,
    UNAUTHORIZED
}
