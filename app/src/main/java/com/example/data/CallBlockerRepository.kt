package com.example.data

import android.content.Context
import com.example.data.entity.AppSettings
import com.example.data.entity.BlockedNumber
import com.example.data.entity.CallBlockLog
import com.example.data.entity.WhitelistedNumber
import com.example.util.ContactHelper
import com.example.util.PhoneNumberNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar

sealed class CallDecision {
    data class Allow(val reason: String, val contactName: String? = null) : CallDecision()
    data class Block(
        val reason: String,
        val phoneNumber: String,
        val contactName: String? = null
    ) : CallDecision()
}

class CallBlockerRepository(private val database: AppDatabase) {

    private val blockedDao = database.blockedNumberDao()
    private val whitelistDao = database.whitelistedNumberDao()
    private val logDao = database.callBlockLogDao()
    private val settingsDao = database.appSettingsDao()

    // Flujos de datos reactivos
    val allBlockedNumbers: Flow<List<BlockedNumber>> = blockedDao.getAllBlockedNumbers()
    val allWhitelistedNumbers: Flow<List<WhitelistedNumber>> = whitelistDao.getAllWhitelistedNumbers()
    val allLogs: Flow<List<CallBlockLog>> = logDao.getAllLogs()
    val latestLog: Flow<CallBlockLog?> = logDao.getLatestLog()

    val settings: Flow<AppSettings> = settingsDao.getSettings().map {
        it ?: AppSettings()
    }

    val totalBlockedCount: Flow<Int> = logDao.getTotalCount()

    // Estadísticas temporales
    fun getTodayBlockedCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return logDao.getCountSince(calendar.timeInMillis)
    }

    fun getWeekBlockedCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return logDao.getCountSince(calendar.timeInMillis)
    }

    fun getMonthBlockedCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return logDao.getCountSince(calendar.timeInMillis)
    }

    /**
     * EVALUACIÓN REAL DE LLAMADA ENTRANTE
     * Aplica estrictamente las reglas oficiales requeridas:
     * 1. Lista Blanca (Excepciones): Máxima prioridad. Si está aquí, NUNCA se bloquea.
     * 2. Lista Negra (Bloqueados): Si está aquí, se bloquea.
     * 3. Bloquear desconocidos: Si está activo y no está en contactos, se bloquea.
     * 4. Números privados / ocultos.
     */
    suspend fun evaluateIncomingCall(context: Context, rawNumber: String?): CallDecision = withContext(Dispatchers.IO) {
        val currentSettings = settingsDao.getSettingsSync() ?: AppSettings()

        if (!currentSettings.protectionEnabled) {
            return@withContext CallDecision.Allow("Protección general pausada por el usuario")
        }

        // Manejo de llamadas privadas / número oculto
        if (rawNumber.isNullOrBlank() || rawNumber.contains("private", ignoreCase = true) || rawNumber == "-1" || rawNumber == "-2") {
            return@withContext if (currentSettings.blockHiddenNumbers || currentSettings.blockUnknownNumbers) {
                CallDecision.Block(
                    reason = "Número privado u oculto",
                    phoneNumber = "Número Privado / Oculto",
                    contactName = null
                )
            } else {
                CallDecision.Allow("Número privado permitido por configuración")
            }
        }

        val cleanNumber = PhoneNumberNormalizer.cleanNumber(rawNumber)
        val cleanDigits = PhoneNumberNormalizer.extractDigits(rawNumber)

        // 1. REGLA DE ORO: LISTA BLANCA (EXCEPCIONES) TIENE PRIORIDAD ABSOLUTA
        val whitelist = whitelistDao.getAllWhitelistedNumbersSync()
        for (whiteItem in whitelist) {
            if (PhoneNumberNormalizer.areSameNumber(whiteItem.phoneNumber, rawNumber)) {
                return@withContext CallDecision.Allow(
                    reason = "Excepción autorizada: ${whiteItem.label.ifBlank { "Lista blanca" }}"
                )
            }
        }

        // 2. LISTA NEGRA (NÚMEROS BLOQUEADOS MANUALMENTE O SPAM)
        val blacklist = blockedDao.getAllBlockedNumbersSync()
        for (blackItem in blacklist) {
            if (PhoneNumberNormalizer.areSameNumber(blackItem.phoneNumber, rawNumber)) {
                // Incrementar contador de bloqueos para este número
                blockedDao.incrementBlockedCount(blackItem.id)
                val blockReason = if (blackItem.label.isNotBlank()) {
                    "Bloqueado: ${blackItem.label}"
                } else {
                    blackItem.reason.ifBlank { "Número bloqueado manualmente" }
                }
                return@withContext CallDecision.Block(
                    reason = blockReason,
                    phoneNumber = rawNumber,
                    contactName = null
                )
            }
        }

        // 3. BLOQUEO DE DESCONOCIDOS (NÚMEROS QUE NO ESTÁN EN CONTACTOS)
        if (currentSettings.blockUnknownNumbers) {
            val inContacts = ContactHelper.isNumberInContacts(context, rawNumber)
            if (inContacts) {
                val contactName = ContactHelper.getContactName(context, rawNumber)
                return@withContext CallDecision.Allow(
                    reason = "Número verificado en contactos",
                    contactName = contactName
                )
            } else {
                return@withContext CallDecision.Block(
                    reason = "Número no guardado en contactos",
                    phoneNumber = rawNumber,
                    contactName = null
                )
            }
        }

        // De lo contrario, se permite la llamada normal
        val contactName = ContactHelper.getContactName(context, rawNumber)
        CallDecision.Allow("Llamada permitida", contactName = contactName)
    }

    /**
     * Registra un evento de bloqueo en el historial local.
     */
    suspend fun recordBlockLog(phoneNumber: String, reason: String, contactName: String? = null) = withContext(Dispatchers.IO) {
        val cleanDigits = PhoneNumberNormalizer.extractDigits(phoneNumber)
        val previousTimes = logDao.getCallCountForNumber(cleanDigits)
        val log = CallBlockLog(
            phoneNumber = phoneNumber,
            cleanDigits = cleanDigits,
            contactName = contactName,
            timestamp = System.currentTimeMillis(),
            reason = reason,
            callTimes = previousTimes + 1
        )
        logDao.insert(log)
    }

    // Operaciones sobre Lista Negra
    suspend fun addBlockedNumber(phoneNumber: String, label: String = "", reason: String = "Número bloqueado manualmente"): Boolean = withContext(Dispatchers.IO) {
        val cleanDigits = PhoneNumberNormalizer.extractDigits(phoneNumber)
        if (cleanDigits.isEmpty()) return@withContext false

        val existing = blockedDao.findByCleanDigits(cleanDigits)
        if (existing != null) {
            blockedDao.update(existing.copy(label = label, reason = reason))
            return@withContext true
        }

        blockedDao.insert(
            BlockedNumber(
                phoneNumber = phoneNumber.trim(),
                cleanDigits = cleanDigits,
                label = label.trim(),
                reason = reason.trim()
            )
        )
        true
    }

    suspend fun updateBlockedNumber(blockedNumber: BlockedNumber) = withContext(Dispatchers.IO) {
        blockedDao.update(blockedNumber)
    }

    suspend fun removeBlockedNumber(id: Long) = withContext(Dispatchers.IO) {
        blockedDao.deleteById(id)
    }

    // Operaciones sobre Lista Blanca
    suspend fun addWhitelistedNumber(phoneNumber: String, label: String = "", notes: String = ""): Boolean = withContext(Dispatchers.IO) {
        val cleanDigits = PhoneNumberNormalizer.extractDigits(phoneNumber)
        if (cleanDigits.isEmpty()) return@withContext false

        val existing = whitelistDao.findByCleanDigits(cleanDigits)
        if (existing != null) {
            whitelistDao.update(existing.copy(label = label, notes = notes))
            return@withContext true
        }

        whitelistDao.insert(
            WhitelistedNumber(
                phoneNumber = phoneNumber.trim(),
                cleanDigits = cleanDigits,
                label = label.trim(),
                notes = notes.trim()
            )
        )
        true
    }

    suspend fun removeWhitelistedNumber(id: Long) = withContext(Dispatchers.IO) {
        whitelistDao.deleteById(id)
    }

    // Operaciones sobre Historial
    suspend fun deleteLog(id: Long) = withContext(Dispatchers.IO) {
        logDao.deleteById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        logDao.clearAll()
    }

    // Operaciones de Configuración
    suspend fun setProtectionEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        settingsDao.setProtectionEnabled(enabled)
    }

    suspend fun setBlockUnknownNumbers(enabled: Boolean) = withContext(Dispatchers.IO) {
        settingsDao.setBlockUnknownNumbers(enabled)
    }

    suspend fun setBlockHiddenNumbers(enabled: Boolean) = withContext(Dispatchers.IO) {
        settingsDao.setBlockHiddenNumbers(enabled)
    }

    suspend fun setNotifyOnBlockedCall(enabled: Boolean) = withContext(Dispatchers.IO) {
        settingsDao.setNotifyOnBlockedCall(enabled)
    }

    suspend fun setSkipCallLogInSystem(enabled: Boolean) = withContext(Dispatchers.IO) {
        settingsDao.setSkipCallLogInSystem(enabled)
    }

    suspend fun setOnboardingCompleted(completed: Boolean) = withContext(Dispatchers.IO) {
        settingsDao.setOnboardingCompleted(completed)
    }

    suspend fun updateSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsDao.update(settings)
    }

    suspend fun getSettingsSync(): AppSettings = withContext(Dispatchers.IO) {
        settingsDao.getSettingsSync() ?: AppSettings().also { settingsDao.insert(it) }
    }
}
