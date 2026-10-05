package com.example.util

import android.telephony.PhoneNumberUtils

/**
 * Normaliza y compara números telefónicos para garantizar coincidencias
 * fiables ante diferentes formatos (códigos internacionales como +52 en México,
 * espacios, guiones, paréntesis o prefijos locales).
 */
object PhoneNumberNormalizer {

    /**
     * Limpia un número removiendo espacios, guiones, puntos y paréntesis,
     * conservando dígitos y el prefijo '+' inicial si está presente.
     */
    fun cleanNumber(raw: String?): String {
        if (raw == null) return ""
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        val hasPlus = trimmed.startsWith("+")
        val digitsOnly = trimmed.filter { it.isDigit() }
        return if (hasPlus) "+$digitsOnly" else digitsOnly
    }

    /**
     * Extrae estrictamente la secuencia de dígitos numéricos (0-9).
     */
    fun extractDigits(raw: String?): String {
        if (raw == null) return ""
        return raw.filter { it.isDigit() }
    }

    /**
     * Obtiene los últimos N dígitos significativos (típicamente 10 para México/Norteamérica,
     * o 7-8 para números fijos locales) para comparación de sufijo.
     */
    fun getSignificantSuffix(number: String, digitCount: Int = 10): String {
        val digits = extractDigits(number)
        return if (digits.length >= digitCount) {
            digits.substring(digits.length - digitCount)
        } else {
            digits
        }
    }

    /**
     * Compara dos números telefónicos para determinar si representan a la misma persona/línea.
     * Utiliza:
     * 1. Coincidencia exacta de números limpios
     * 2. Android official PhoneNumberUtils.compare
     * 3. Coincidencia de los últimos 10 dígitos (estándar mexicano y móvil internacional)
     */
    fun areSameNumber(num1: String?, num2: String?): Boolean {
        if (num1.isNullOrBlank() || num2.isNullOrBlank()) return false

        val clean1 = cleanNumber(num1)
        val clean2 = cleanNumber(num2)
        if (clean1 == clean2) return true

        // Android Telecom comparison
        try {
            if (PhoneNumberUtils.compare(clean1, clean2)) {
                return true
            }
        } catch (_: Exception) {
            // Continuar con comparación de dígitos propios
        }

        val digits1 = extractDigits(num1)
        val digits2 = extractDigits(num2)

        if (digits1 == digits2) return true

        // Coincidencia de sufijo significativo (10 dígitos en México como 9931234567 vs +529931234567 vs +5219931234567)
        if (digits1.length >= 10 && digits2.length >= 10) {
            val suffix1 = digits1.takeLast(10)
            val suffix2 = digits2.takeLast(10)
            if (suffix1 == suffix2) return true
        }

        // Si alguno es de 7 a 9 dígitos (línea fija local sin lada completa)
        if (digits1.length >= 7 && digits2.length >= 7) {
            val minLen = minOf(digits1.length, digits2.length)
            if (minLen in 7..9) {
                if (digits1.takeLast(minLen) == digits2.takeLast(minLen)) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Formatea un número para visualización clara y legible en la interfaz de usuario.
     */
    fun formatForDisplay(raw: String?): String {
        if (raw.isNullOrBlank()) return "Número desconocido"
        val clean = cleanNumber(raw)
        val digits = extractDigits(clean)

        // Formato para números mexicanos de 10 dígitos (ej. +52 993 123 4567)
        return when {
            clean.startsWith("+52") && digits.length == 12 -> {
                val lada = digits.substring(2, 5)
                val p1 = digits.substring(5, 8)
                val p2 = digits.substring(8, 12)
                "+52 $lada $p1 $p2"
            }
            digits.length == 10 -> {
                val lada = digits.substring(0, 3)
                val p1 = digits.substring(3, 6)
                val p2 = digits.substring(6, 10)
                "$lada $p1 $p2"
            }
            clean.startsWith("+") && clean.length > 7 -> {
                // Formato internacional genérico
                val code = clean.substring(0, minOf(3, clean.length))
                val rest = clean.substring(minOf(3, clean.length))
                "$code $rest"
            }
            else -> clean
        }
    }
}
