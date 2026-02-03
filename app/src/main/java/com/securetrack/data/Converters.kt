package com.securetrack.data

import androidx.room.TypeConverter
import com.securetrack.data.entities.CommandStatus

class Converters {
    
    @TypeConverter
    fun fromCommandStatus(status: CommandStatus): String = status.name

    @TypeConverter
    fun toCommandStatus(value: String): CommandStatus = 
        CommandStatus.valueOf(value)
}
