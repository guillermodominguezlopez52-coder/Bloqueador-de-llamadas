package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.WhitelistedNumber
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistedNumberDao {

    @Query("SELECT * FROM whitelisted_numbers ORDER BY dateAdded DESC")
    fun getAllWhitelistedNumbers(): Flow<List<WhitelistedNumber>>

    @Query("SELECT * FROM whitelisted_numbers")
    suspend fun getAllWhitelistedNumbersSync(): List<WhitelistedNumber>

    @Query("SELECT * FROM whitelisted_numbers WHERE cleanDigits = :cleanDigits LIMIT 1")
    suspend fun findByCleanDigits(cleanDigits: String): WhitelistedNumber?

    @Query("SELECT * FROM whitelisted_numbers WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): WhitelistedNumber?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(whitelistedNumber: WhitelistedNumber): Long

    @Update
    suspend fun update(whitelistedNumber: WhitelistedNumber)

    @Delete
    suspend fun delete(whitelistedNumber: WhitelistedNumber)

    @Query("DELETE FROM whitelisted_numbers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM whitelisted_numbers")
    fun countTotal(): Flow<Int>
}
