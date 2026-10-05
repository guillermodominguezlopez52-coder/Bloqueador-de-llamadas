package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.BlockedNumber
import com.example.ui.MainViewModel
import com.example.util.PhoneNumberNormalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BlacklistScreen(viewModel: MainViewModel) {
    val blockedList by viewModel.blockedNumbers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.blacklistSearchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<BlockedNumber?>(null) }
    var itemToDelete by remember { mutableStateOf<BlockedNumber?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_blocked_number_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar número a lista negra")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Números bloqueados",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${blockedList.size} números en lista negra",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.blacklistSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("blacklist_search_field"),
                placeholder = { Text("Buscar en bloqueados...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.blacklistSearchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            if (blockedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Sin resultados" else "No hay números bloqueados",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Prueba buscando con otros dígitos" else "Toca el botón '+' para agregar un número específico que desees rechazar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(blockedList, key = { it.id }) { item ->
                        BlockedNumberItem(
                            item = item,
                            onEdit = { itemToEdit = item },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // Diálogo para Agregar
    if (showAddDialog) {
        BlockedNumberDialog(
            title = "Bloquear número",
            initialNumber = "",
            initialLabel = "",
            initialReason = "Número bloqueado manualmente",
            onDismiss = { showAddDialog = false },
            onConfirm = { number, label, reason ->
                viewModel.addBlockedNumber(number, label, reason)
                showAddDialog = false
            }
        )
    }

    // Diálogo para Editar
    if (itemToEdit != null) {
        BlockedNumberDialog(
            title = "Editar número bloqueado",
            initialNumber = itemToEdit!!.phoneNumber,
            initialLabel = itemToEdit!!.label,
            initialReason = itemToEdit!!.reason,
            onDismiss = { itemToEdit = null },
            onConfirm = { number, label, reason ->
                viewModel.updateBlockedNumber(
                    itemToEdit!!.copy(
                        phoneNumber = number,
                        cleanDigits = PhoneNumberNormalizer.extractDigits(number),
                        label = label,
                        reason = reason
                    )
                )
                itemToEdit = null
            }
        )
    }

    // Diálogo de Confirmación para Eliminar
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Desbloquear número") },
            text = {
                Text("¿Deseas desbloquear ${PhoneNumberNormalizer.formatForDisplay(itemToDelete!!.phoneNumber)}? Las llamadas futuras de este número podrán entrar.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeBlockedNumber(itemToDelete!!.id)
                        itemToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_unblock_button")
                ) {
                    Text("Desbloquear")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BlockedNumberItem(
    item: BlockedNumber,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(item.dateAdded))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("blocked_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = PhoneNumberNormalizer.formatForDisplay(item.phoneNumber),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (item.label.isNotBlank()) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agregado: $dateString",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    if (item.blockedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${item.blockedCount} bloqueadas",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedNumberDialog(
    title: String,
    initialNumber: String,
    initialLabel: String,
    initialReason: String,
    onDismiss: () -> Unit,
    onConfirm: (number: String, label: String, reason: String) -> Unit
) {
    var number by remember { mutableStateOf(initialNumber) }
    var label by remember { mutableStateOf(initialLabel) }
    var reason by remember { mutableStateOf(initialReason) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Número telefónico") },
                    placeholder = { Text("+52 993 123 4567") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Ejemplo: +52 993 123 4567 o 9931234567")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_phone_input")
                )

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Etiqueta / Nombre (opcional)") },
                    placeholder = { Text("Ej. Vendedor, Cobranza, Spam") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_label_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanDigits = PhoneNumberNormalizer.extractDigits(number)
                    if (cleanDigits.length < 7) {
                        errorMessage = "Ingresa un número telefónico válido (mínimo 7 dígitos)"
                    } else {
                        onConfirm(number.trim(), label.trim(), reason.trim())
                    }
                },
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
