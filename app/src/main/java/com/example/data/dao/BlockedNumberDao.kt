package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.BlockedNumber
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {

    @Query("SELECT * FROM blocked_numbers ORDER BY dateAdded DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumber>>

    @Query("SELECT * FROM blocked_numbers")
    suspend fun getAllBlockedNumbersSync(): List<BlockedNumber>

    @Query("SELECT * FROM blocked_numbers WHERE cleanDigits = :cleanDigits LIMIT 1")
    suspend fun findByCleanDigits(cleanDigits: String): BlockedNumber?

    @Query("SELECT * FROM blocked_numbers WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): BlockedNumber?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blockedNumber: BlockedNumber): Long

    @Update
    suspend fun update(blockedNumber: BlockedNumber)

    @Delete
    suspend fun delete(blockedNumber: BlockedNumber)

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE blocked_numbers SET blockedCount = blockedCount + 1 WHERE id = :id")
    suspend fun incrementBlockedCount(id: Long)

    @Query("UPDATE blocked_numbers SET blockedCount = blockedCount + 1 WHERE cleanDigits = :cleanDigits")
    suspend fun incrementBlockedCountByDigits(cleanDigits: String)

    @Query("SELECT COUNT(*) FROM blocked_numbers")
    fun countTotal(): Flow<Int>
}
