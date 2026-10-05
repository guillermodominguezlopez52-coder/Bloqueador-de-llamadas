package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un número telefónico bloqueado manualmente o catalogado como spam (Lista Negra).
 */
@Entity(
    tableName = "blocked_numbers",
    indices = [Index(value = ["cleanDigits"], unique = true)]
)
data class BlockedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val cleanDigits: String,
    val label: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val blockedCount: Int = 0,
    val reason: String = "Número bloqueado manualmente"
)
