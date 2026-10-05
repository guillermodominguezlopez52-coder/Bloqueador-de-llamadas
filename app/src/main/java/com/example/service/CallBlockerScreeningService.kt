package com.example.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import com.example.data.AppDatabase
import com.example.data.CallBlockerRepository
import com.example.data.CallDecision
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio oficial de Telecom de Android (android.telecom.CallScreeningService).
 * Intercepta y examina llamadas entrantes antes de que suenen en el dispositivo
 * cuando la app tiene el rol ROLE_CALL_SCREENING.
 */
@RequiresApi(Build.VERSION_CODES.N)
class CallBlockerScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        // Solo evaluamos llamadas entrantes
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            val response = CallResponse.Builder()
                .setDisallowCall(false)
                .build()
            respondToCall(callDetails, response)
            return
        }

        // Obtener el número telefónico del handle (ej. tel:+529931234567 -> +529931234567)
        val handle = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart

        serviceScope.launch {
            val database = AppDatabase.getInstance(applicationContext)
            val repository = CallBlockerRepository(database)
            val settings = repository.getSettingsSync()

            val decision = repository.evaluateIncomingCall(applicationContext, rawNumber)

            when (decision) {
                is CallDecision.Block -> {
                    // API Oficial de Bloqueo de Android Telecom
                    val responseBuilder = CallResponse.Builder()
                        .setDisallowCall(true)      // Rechazar la llamada
                        .setRejectCall(true)        // Enviar señal de rechazo
                        .setSkipNotification(true)  // No mostrar alerta de timbrado en pantalla
                        .setSkipCallLog(settings.skipCallLogInSystem)

                    respondToCall(callDetails, responseBuilder.build())

                    val targetNumber = if (!rawNumber.isNullOrBlank()) rawNumber else decision.phoneNumber

                    // Guardar en el historial local de Room
                    repository.recordBlockLog(
                        phoneNumber = targetNumber,
                        reason = decision.reason,
                        contactName = decision.contactName
                    )

                    // Mostrar notificación local si está habilitado en configuración
                    if (settings.notifyOnBlockedCall) {
                        NotificationHelper.showBlockedCallNotification(
                            context = applicationContext,
                            phoneNumber = targetNumber,
                            reason = decision.reason
                        )
                    }
                }

                is CallDecision.Allow -> {
                    // Permitir la llamada normalmente
                    val response = CallResponse.Builder()
                        .setDisallowCall(false)
                        .build()
                    respondToCall(callDetails, response)
                }
            }
        }
    }
}
