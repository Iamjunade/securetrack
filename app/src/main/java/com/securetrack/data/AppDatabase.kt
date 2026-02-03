package com.securetrack.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.securetrack.data.dao.CommandLogDao
import com.securetrack.data.dao.EmergencyContactDao
import com.securetrack.data.entities.CommandLog
import com.securetrack.data.entities.EmergencyContact

@Database(
    entities = [
        EmergencyContact::class,
        CommandLog::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun commandLogDao(): CommandLogDao
}
