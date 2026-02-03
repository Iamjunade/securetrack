package com.securetrack.data.dao

import androidx.room.*
import com.securetrack.data.entities.CommandLog
import com.securetrack.data.entities.CommandStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandLogDao {

    @Query("SELECT * FROM command_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CommandLog>>

    @Query("SELECT * FROM command_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<CommandLog>>

    @Query("SELECT * FROM command_logs WHERE id = :id")
    suspend fun getLogById(id: Long): CommandLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommandLog): Long

    @Update
    suspend fun updateLog(log: CommandLog)

    @Query("UPDATE command_logs SET status = :status, resultMessage = :message WHERE id = :id")
    suspend fun updateLogStatus(id: Long, status: CommandStatus, message: String?)

    @Query("UPDATE command_logs SET locationLat = :lat, locationLng = :lng WHERE id = :id")
    suspend fun updateLogLocation(id: Long, lat: Double, lng: Double)

    @Delete
    suspend fun deleteLog(log: CommandLog)

    @Query("DELETE FROM command_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM command_logs WHERE timestamp < :timestamp")
    suspend fun deleteLogsOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM command_logs WHERE status = :status")
    suspend fun getCountByStatus(status: CommandStatus): Int
}
