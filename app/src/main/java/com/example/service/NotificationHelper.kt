package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.util.PhoneNumberNormalizer

object NotificationHelper {

    const val CHANNEL_ID = "call_blocker_blocked_channel"
    private const val CHANNEL_NAME = "Llamadas bloqueadas"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones automáticas cuando se bloquea una llamada entrante"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun showBlockedCallNotification(
        context: Context,
        phoneNumber: String,
        reason: String
    ) {
        createNotificationChannel(context)

        val formattedNumber = PhoneNumberNormalizer.formatForDisplay(phoneNumber)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "history")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Llamada bloqueada")
            .setContentText("$formattedNumber • $reason")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Se bloqueó la llamada de $formattedNumber.\nMotivo: $reason.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                val notifId = NOTIFICATION_ID_BASE + (System.currentTimeMillis() % 500).toInt()
                notificationManager.notify(notifId, notification)
            }
        } catch (_: SecurityException) {
            // Permiso POST_NOTIFICATIONS no concedido en Android 13+
        }
    }
}
