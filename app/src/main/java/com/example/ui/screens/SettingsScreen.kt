package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.util.RoleHelper

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isRoleHeld by viewModel.isRoleHeld.collectAsStateWithLifecycle()
    val hasContacts by viewModel.hasContactsPermission.collectAsStateWithLifecycle()
    val hasNotifications by viewModel.hasNotificationPermission.collectAsStateWithLifecycle()

    var showTestingGuide by remember { mutableStateOf(false) }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshSystemStatus(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshSystemStatus(context)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshSystemStatus(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        // TARJETA DE ESTADO DEL SISTEMA ANDROID
        Text(
            text = "Integración con el Sistema Operativo",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Fila Rol Call Screening
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneCallback,
                        contentDescription = null,
                        tint = if (isRoleHeld) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rol de Bloqueo (Android 10+)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isRoleHeld) "Activo: Interceptando llamadas con CallScreeningService"
                            else "Inactivo: Requiere ser app de filtro de llamadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isRoleHeld) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    if (!isRoleHeld) {
                        Button(
                            onClick = {
                                RoleHelper.requestCallScreeningRole(context, roleLauncher)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("settings_grant_role_button")
                        ) {
                            Text("Activar")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Activo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider()

                // Fila Permiso Contactos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = null,
                        tint = if (hasContacts) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Acceso a Contactos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (hasContacts) "Concedido: Distingue números conocidos"
                            else "Denegado: No puede verificar contactos",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasContacts) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    if (!hasContacts) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                            },
                            modifier = Modifier.testTag("settings_grant_contacts_button")
                        ) {
                            Text("Permitir")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Concedido",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider()

                // Fila Permiso Notificaciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (hasNotifications) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notificaciones locales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (hasNotifications) "Habilitadas para llamadas bloqueadas"
                            else "Deshabilitadas o silenciadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!hasNotifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                            }
                        ) {
                            Text("Activar")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Habilitado",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // OPCIONES DE BLOQUEO
        Text(
            text = "Reglas de Bloqueo",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Switch Bloquear desconocidos
                SettingToggleRow(
                    title = "Bloquear números desconocidos",
                    subtitle = "Rechaza cualquier llamada cuyo número no esté guardado en tus contactos.",
                    checked = settings.blockUnknownNumbers,
                    onCheckedChange = { viewModel.setBlockUnknownNumbers(it) },
                    testTag = "settings_block_unknown_toggle"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Switch Bloquear privados
                SettingToggleRow(
                    title = "Bloquear números privados y ocultos",
                    subtitle = "Rechaza llamadas entrantes que oculten su identificador de llamada.",
                    checked = settings.blockHiddenNumbers,
                    onCheckedChange = { viewModel.setBlockHiddenNumbers(it) },
                    testTag = "settings_block_hidden_toggle"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Switch Notificar bloqueos
                SettingToggleRow(
                    title = "Notificar llamadas bloqueadas",
                    subtitle = "Muestra una notificación silenciosa cuando se bloquee una llamada.",
                    checked = settings.notifyOnBlockedCall,
                    onCheckedChange = { viewModel.setNotifyOnBlockedCall(it) },
                    testTag = "settings_notify_toggle"
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Switch Ocultar del historial del sistema
                SettingToggleRow(
                    title = "Ocultar del registro general de Android",
                    subtitle = "Si se activa, las llamadas bloqueadas no aparecerán en la app de Teléfono de Android (solo en esta app).",
                    checked = settings.skipCallLogInSystem,
                    onCheckedChange = { viewModel.setSkipCallLogInSystem(it) },
                    testTag = "settings_skip_log_toggle"
                )
            }
        }

        // GUÍA DE PRUEBAS REALES EN TELÉFONO FÍSICO (Requisito clave)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guía de Pruebas Reales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showTestingGuide = !showTestingGuide },
                        modifier = Modifier.testTag("toggle_testing_guide_button")
                    ) {
                        Text(if (showTestingGuide) "Ocultar" else "Ver Guía")
                    }
                }

                AnimatedVisibility(visible = showTestingGuide) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TestingStepItem(
                            title = "PRUEBA 1: Número en contactos",
                            description = "Un contacto guardado llama. Resultado: La llamada entra y timbra con normalidad."
                        )
                        TestingStepItem(
                            title = "PRUEBA 2: Número no guardado (Desconocido)",
                            description = "Activa 'Bloquear desconocidos' y llama desde un número no guardado. Resultado: Android rechaza automáticamente la llamada sin timbrar."
                        )
                        TestingStepItem(
                            title = "PRUEBA 3: Número en Lista Negra",
                            description = "Agrega manualmente un número en la pestaña 'Bloqueados' y llama desde ese número. Resultado: La llamada es rechazada de inmediato."
                        )
                        TestingStepItem(
                            title = "PRUEBA 4: Prioridad de Excepciones",
                            description = "Agrega un número a 'Excepciones' que también esté en la lista negra. Resultado: La llamada entra con normalidad porque la lista blanca tiene prioridad absoluta."
                        )
                        TestingStepItem(
                            title = "PRUEBA 5: Desactivar 'Bloquear desconocidos'",
                            description = "Desactiva la opción y llama desde un número no guardado. Resultado: La llamada timbra normalmente."
                        )
                    }
                }
            }
        }

        // SECCIÓN DE PRIVACIDAD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacidad y Seguridad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "• Operación 100% Fuera de Línea (Offline): Esta aplicación no se conecta a ningún servidor exterior ni requiere conexión a internet para bloquear llamadas.\n" +
                            "• Tus contactos y números permanecen estrictamente en tu dispositivo en una base de datos local SQLite (Room).\n" +
                            "• Sin rastreadores: No se incluye ningún servicio de telemetría, analytics ni recopilación de datos personales.\n" +
                            "• Gratuita y sin pagos: Sin suscripciones ni cobros ocultos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // LIMITACIONES Y APIS DE ANDROID
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Funcionamiento Técnico en Android",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "• API Oficial: Utiliza 'android.telecom.CallScreeningService' y 'RoleManager.ROLE_CALL_SCREENING' disponibles desde Android 10 (API 29).\n" +
                            "• Detección y Rechazo: Android entrega los metadatos de la llamada al servicio antes de sonar. La aplicación examina el número en menos de 50ms y envía la orden 'disallowCall(true) + rejectCall(true)'.\n" +
                            "• Sobre Identificación en Pantalla: Por políticas de seguridad de Android 10+, las aplicaciones que no son el Marcador Predeterminado (Default Dialer) tienen prohibido dibujar ventanas flotantes (overlays) sobre la llamada entrante mientras timbra. Por ello, la información y el motivo se registran inmediatamente en el historial y se informan mediante notificación del sistema.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun TestingStepItem(
    title: String,
    description: String
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
