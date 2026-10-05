package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un número telefónico que NUNCA debe bloquearse (Lista Blanca / Excepciones).
 * Esta lista tiene prioridad absoluta sobre la lista negra y el bloqueo de números desconocidos.
 */
@Entity(
    tableName = "whitelisted_numbers",
    indices = [Index(value = ["cleanDigits"], unique = true)]
)
data class WhitelistedNumber(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val cleanDigits: String,
    val label: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val notes: String = ""
)
