package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para almacenar la configuración de bloqueo local en el dispositivo.
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val protectionEnabled: Boolean = true,
    val blockUnknownNumbers: Boolean = false,
    val blockHiddenNumbers: Boolean = true,
    val notifyOnBlockedCall: Boolean = true,
    val skipCallLogInSystem: Boolean = false,
    val onboardingCompleted: Boolean = false
)
