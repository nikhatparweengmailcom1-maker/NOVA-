package com.nova.assistant.features.contacts

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages contact lookup and phone/SMS initiation.
 */
@Singleton
class ContactsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Finds the first phone number associated with a contact matching [name].
     * Returns null if permission denied or no match found.
     */
    fun findPhoneNumber(name: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            Timber.w("ContactsManager: READ_CONTACTS permission not granted")
            return null
        }
        if (name.isBlank()) return null

        val lower = name.lowercase()
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            while (cursor?.moveToNext() == true) {
                val contactName = cursor.getString(0)?.lowercase() ?: continue
                if (contactName.contains(lower) || lower.contains(contactName)) {
                    val number = cursor.getString(1)?.replace("[^0-9+]".toRegex(), "") ?: continue
                    Timber.d("ContactsManager: found '$contactName' -> $number")
                    return number
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "ContactsManager: query error")
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Opens the dialer with [phoneNumber] pre-filled.
     * Actual dialing requires CALL_PHONE permission and user confirmation.
     */
    fun initiateCall(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(intent)
            } else {
                // Fall back to dial intent (no permission needed)
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
            }
            Timber.d("ContactsManager: calling $phoneNumber")
        } catch (e: Exception) {
            Timber.e(e, "ContactsManager: failed to initiate call")
        }
    }

    /**
     * Opens the SMS compose screen for [phoneNumber].
     */
    fun sendSms(phoneNumber: String, message: String = "") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Timber.d("ContactsManager: SMS compose opened for $phoneNumber")
        } catch (e: Exception) {
            Timber.e(e, "ContactsManager: failed to open SMS")
        }
    }
}
