package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.CallBlockLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CallBlockLogDao {

    @Query("SELECT * FROM call_block_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CallBlockLog>>

    @Query("SELECT * FROM call_block_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<CallBlockLog>>

    @Query("SELECT * FROM call_block_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLog(): Flow<CallBlockLog?>

    @Query("SELECT COUNT(*) FROM call_block_logs WHERE cleanDigits = :cleanDigits")
    suspend fun getCallCountForNumber(cleanDigits: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CallBlockLog): Long

    @Delete
    suspend fun delete(log: CallBlockLog)

    @Query("DELETE FROM call_block_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM call_block_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM call_block_logs")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM call_block_logs WHERE timestamp >= :sinceTimestamp")
    fun getCountSince(sinceTimestamp: Long): Flow<Int>
}
