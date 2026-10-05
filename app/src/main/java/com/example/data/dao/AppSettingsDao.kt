package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettings)

    @Update
    suspend fun update(settings: AppSettings)

    @Query("UPDATE app_settings SET blockUnknownNumbers = :enabled WHERE id = 1")
    suspend fun setBlockUnknownNumbers(enabled: Boolean)

    @Query("UPDATE app_settings SET protectionEnabled = :enabled WHERE id = 1")
    suspend fun setProtectionEnabled(enabled: Boolean)

    @Query("UPDATE app_settings SET blockHiddenNumbers = :enabled WHERE id = 1")
    suspend fun setBlockHiddenNumbers(enabled: Boolean)

    @Query("UPDATE app_settings SET notifyOnBlockedCall = :enabled WHERE id = 1")
    suspend fun setNotifyOnBlockedCall(enabled: Boolean)

    @Query("UPDATE app_settings SET skipCallLogInSystem = :enabled WHERE id = 1")
    suspend fun setSkipCallLogInSystem(enabled: Boolean)

    @Query("UPDATE app_settings SET onboardingCompleted = :completed WHERE id = 1")
    suspend fun setOnboardingCompleted(completed: Boolean)
}
