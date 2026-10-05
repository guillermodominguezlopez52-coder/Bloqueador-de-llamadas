package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppSettingsDao
import com.example.data.dao.BlockedNumberDao
import com.example.data.dao.CallBlockLogDao
import com.example.data.dao.WhitelistedNumberDao
import com.example.data.entity.AppSettings
import com.example.data.entity.BlockedNumber
import com.example.data.entity.CallBlockLog
import com.example.data.entity.WhitelistedNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BlockedNumber::class,
        WhitelistedNumber::class,
        CallBlockLog::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun whitelistedNumberDao(): WhitelistedNumberDao
    abstract fun callBlockLogDao(): CallBlockLogDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "call_blocker.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Inicializar configuración por defecto
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).appSettingsDao().insert(
                                    AppSettings(
                                        id = 1,
                                        protectionEnabled = true,
                                        blockUnknownNumbers = false,
                                        blockHiddenNumbers = true,
                                        notifyOnBlockedCall = true,
                                        skipCallLogInSystem = false,
                                        onboardingCompleted = false
                                    )
                                )
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
