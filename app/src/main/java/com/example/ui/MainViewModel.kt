package com.example.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CallBlockerRepository
import com.example.data.entity.AppSettings
import com.example.data.entity.BlockedNumber
import com.example.data.entity.CallBlockLog
import com.example.data.entity.WhitelistedNumber
import com.example.util.PhoneNumberNormalizer
import com.example.util.RoleHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    DASHBOARD("Inicio"),
    HISTORY("Historial"),
    BLACKLIST("Bloqueados"),
    WHITELIST("Excepciones"),
    SETTINGS("Configuración")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = CallBlockerRepository(database)

    // Pantalla activa
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Estados de permisos y rol del sistema
    private val _isRoleHeld = MutableStateFlow(RoleHelper.isCallScreeningRoleHeld(application))
    val isRoleHeld: StateFlow<Boolean> = _isRoleHeld.asStateFlow()

    private val _hasContactsPermission = MutableStateFlow(
        ContextCompat.checkSelfPermission(application, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    )
    val hasContactsPermission: StateFlow<Boolean> = _hasContactsPermission.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(application, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    )
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    // Configuración local
    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    // Búsqueda en listas
    val historySearchQuery = MutableStateFlow("")
    val blacklistSearchQuery = MutableStateFlow("")
    val whitelistSearchQuery = MutableStateFlow("")

    // Listas observables con filtrado reactivo
    val blockedNumbers: StateFlow<List<BlockedNumber>> = combine(
        repository.allBlockedNumbers,
        blacklistSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.phoneNumber.contains(query, ignoreCase = true) ||
                    it.label.contains(query, ignoreCase = true) ||
                    it.cleanDigits.contains(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedNumbers: StateFlow<List<WhitelistedNumber>> = combine(
        repository.allWhitelistedNumbers,
        whitelistSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.phoneNumber.contains(query, ignoreCase = true) ||
                    it.label.contains(query, ignoreCase = true) ||
                    it.cleanDigits.contains(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallBlockLog>> = combine(
        repository.allLogs,
        historySearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.phoneNumber.contains(query, ignoreCase = true) ||
                    (it.contactName?.contains(query, ignoreCase = true) == true) ||
                    it.reason.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestLog: StateFlow<CallBlockLog?> = repository.latestLog.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    // Estadísticas
    val totalBlockedCount: StateFlow<Int> = repository.totalBlockedCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val todayBlockedCount: StateFlow<Int> = repository.getTodayBlockedCount().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val weekBlockedCount: StateFlow<Int> = repository.getWeekBlockedCount().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val monthBlockedCount: StateFlow<Int> = repository.getMonthBlockedCount().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun refreshSystemStatus(context: Context) {
        _isRoleHeld.value = RoleHelper.isCallScreeningRoleHeld(context)
        _hasContactsPermission.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        _hasNotificationPermission.value = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    // Configuración
    fun setProtectionEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setProtectionEnabled(enabled)
    }

    fun setBlockUnknownNumbers(enabled: Boolean) = viewModelScope.launch {
        repository.setBlockUnknownNumbers(enabled)
    }

    fun setBlockHiddenNumbers(enabled: Boolean) = viewModelScope.launch {
        repository.setBlockHiddenNumbers(enabled)
    }

    fun setNotifyOnBlockedCall(enabled: Boolean) = viewModelScope.launch {
        repository.setNotifyOnBlockedCall(enabled)
    }

    fun setSkipCallLogInSystem(enabled: Boolean) = viewModelScope.launch {
        repository.setSkipCallLogInSystem(enabled)
    }

    fun completeOnboarding() = viewModelScope.launch {
        repository.setOnboardingCompleted(true)
    }

    // Lista Negra
    fun addBlockedNumber(phoneNumber: String, label: String = "", reason: String = "Número bloqueado manualmente", onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.addBlockedNumber(phoneNumber, label, reason)
            onComplete(success)
        }
    }

    fun updateBlockedNumber(blockedNumber: BlockedNumber) = viewModelScope.launch {
        repository.updateBlockedNumber(blockedNumber)
    }

    fun removeBlockedNumber(id: Long) = viewModelScope.launch {
        repository.removeBlockedNumber(id)
    }

    // Lista Blanca
    fun addWhitelistedNumber(phoneNumber: String, label: String = "", notes: String = "", onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.addWhitelistedNumber(phoneNumber, label, notes)
            onComplete(success)
        }
    }

    fun removeWhitelistedNumber(id: Long) = viewModelScope.launch {
        repository.removeWhitelistedNumber(id)
    }

    // Historial
    fun deleteLog(id: Long) = viewModelScope.launch {
        repository.deleteLog(id)
    }

    fun clearHistory() = viewModelScope.launch {
        repository.clearHistory()
    }

    fun blockFromHistory(log: CallBlockLog, label: String = "") {
        viewModelScope.launch {
            repository.addBlockedNumber(
                phoneNumber = log.phoneNumber,
                label = label.ifBlank { log.contactName ?: "Bloqueado desde historial" },
                reason = "Bloqueado desde historial"
            )
        }
    }

    fun whitelistFromHistory(log: CallBlockLog, label: String = "") {
        viewModelScope.launch {
            repository.addWhitelistedNumber(
                phoneNumber = log.phoneNumber,
                label = label.ifBlank { log.contactName ?: "Excepción desde historial" },
                notes = "Agregado desde historial de llamadas"
            )
        }
    }
}
