package com.nova.assistant.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized permission management for NOVA.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val MICROPHONE_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )

        val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )

        val CONTACT_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS
        )

        val PHONE_PERMISSIONS = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )

        val SMS_PERMISSIONS = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
        )

        val CALENDAR_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )

        val STORAGE_PERMISSIONS: Array<String>
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }

        val NOTIFICATION_PERMISSIONS: Array<String>
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                emptyArray()
            }

        val CORE_PERMISSIONS: Array<String>
            get() = MICROPHONE_PERMISSIONS + NOTIFICATION_PERMISSIONS

        val ALL_PERMISSIONS: Array<String>
            get() = CORE_PERMISSIONS +
                    CAMERA_PERMISSIONS +
                    CONTACT_PERMISSIONS +
                    PHONE_PERMISSIONS +
                    SMS_PERMISSIONS +
                    CALENDAR_PERMISSIONS +
                    STORAGE_PERMISSIONS
    }

    fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun areAllGranted(permissions: Array<String>): Boolean =
        permissions.all { isGranted(it) }

    fun hasMicrophonePermission(): Boolean = areAllGranted(MICROPHONE_PERMISSIONS)

    fun hasCameraPermission(): Boolean = areAllGranted(CAMERA_PERMISSIONS)

    fun hasContactsPermission(): Boolean = areAllGranted(CONTACT_PERMISSIONS)

    fun hasPhonePermission(): Boolean = areAllGranted(PHONE_PERMISSIONS)

    fun hasSmsPermission(): Boolean = areAllGranted(SMS_PERMISSIONS)

    fun hasCalendarPermission(): Boolean = areAllGranted(CALENDAR_PERMISSIONS)

    fun hasStoragePermission(): Boolean = areAllGranted(STORAGE_PERMISSIONS)

    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

    fun hasCorePermissions(): Boolean = areAllGranted(CORE_PERMISSIONS)

    fun shouldShowRationale(activity: Activity, permission: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

    fun getMissingPermissions(permissions: Array<String>): Array<String> =
        permissions.filter { !isGranted(it) }.toTypedArray()

    fun getMissingCorePermissions(): Array<String> =
        getMissingPermissions(CORE_PERMISSIONS)
}
