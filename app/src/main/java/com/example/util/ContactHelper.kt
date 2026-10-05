package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

/**
 * Consulta oficial de los contactos del dispositivo mediante ContactsContract.
 */
object ContactHelper {

    /**
     * Verifica si un número telefónico existe en la libreta de contactos del dispositivo.
     */
    fun isNumberInContacts(context: Context, phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            // Sin permiso no podemos verificar contactos
            return false
        }

        val clean = PhoneNumberNormalizer.cleanNumber(phoneNumber)
        if (clean.isEmpty()) return false

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(clean)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup._ID)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                cursor.moveToFirst() && cursor.count > 0
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Obtiene el nombre del contacto asociado a un número telefónico si existe.
     */
    fun getContactName(context: Context, phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank()) return null

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return null

        val clean = PhoneNumberNormalizer.cleanNumber(phoneNumber)
        if (clean.isEmpty()) return null

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(clean)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (index != -1) cursor.getString(index) else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }
}
