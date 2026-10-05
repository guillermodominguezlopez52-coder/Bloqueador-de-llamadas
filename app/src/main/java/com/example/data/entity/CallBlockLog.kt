package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que registra cada llamada real que fue interceptada y bloqueada por la aplicación.
 */
@Entity(
    tableName = "call_block_logs",
    indices = [Index(value = ["timestamp"])]
)
data class CallBlockLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val cleanDigits: String = "",
    val contactName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String, // e.g. "Número no guardado en contactos", "Número bloqueado manualmente", "Marcado como spam", "Número privado / oculto"
    val callTimes: Int = 1
)
