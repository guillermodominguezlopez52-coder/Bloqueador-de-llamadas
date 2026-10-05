package com.example.util

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.activity.result.ActivityResultLauncher

/**
 * Gestiona el rol oficial de Android: RoleManager.ROLE_CALL_SCREENING.
 * En Android 10+ (API 29+), este rol es el mecanismo oficial del sistema operativo
 * para que una app filtre y bloquee llamadas entrantes legítimamente.
 */
object RoleHelper {

    /**
     * Comprueba si el dispositivo tiene asignado el rol de Call Screening a esta aplicación.
     */
    fun isCallScreeningRoleHeld(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        } else {
            // En versiones previas a Android 10
            true
        }
    }

    /**
     * Comprueba si el rol de Call Screening está disponible en este sistema operativo.
     */
    fun isCallScreeningRoleAvailable(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true
        } else {
            false
        }
    }

    /**
     * Solicita al usuario otorgar el rol oficial de Identificación y Bloqueo de llamadas.
     */
    fun requestCallScreeningRole(context: Context, launcher: ActivityResultLauncher<Intent>): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                launcher.launch(intent)
                true
            } else {
                openDefaultAppsSettings(context)
                false
            }
        } else {
            openDefaultAppsSettings(context)
            false
        }
    }

    /**
     * Abre los ajustes del sistema para configurar las aplicaciones predeterminadas manualmente
     * si el diálogo directo no se encuentra disponible.
     */
    fun openDefaultAppsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val appSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(appSettings)
            } catch (_: Exception) {
                // Silencioso
            }
        }
    }

    /**
     * Abre la pantalla de configuración de la aplicación para gestionar permisos manualmente si fueron denegados.
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Silencioso
        }
    }
}
