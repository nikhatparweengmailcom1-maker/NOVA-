package com.nova.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import timber.log.Timber

/**
 * Utilities for requesting battery optimization exemption so NOVA's
 * foreground service can stay alive while the screen is off.
 */
class BatteryOptimizationUtil(private val context: Context) {

    private val powerManager: PowerManager
        get() = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    /**
     * Returns true if the app is already exempt from battery optimization.
     */
    fun isIgnoringBatteryOptimizations(): Boolean =
        powerManager.isIgnoringBatteryOptimizations(context.packageName)

    /**
     * Opens the system dialog asking the user to exempt this app.
     * Must be called from an Activity context when the app is in the foreground.
     */
    fun requestExemption(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "BatteryOptimizationUtil: could not open settings")
            // Fallback: open the general battery optimization settings screen
            try {
                val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (e2: Exception) {
                Timber.e(e2, "BatteryOptimizationUtil: fallback also failed")
            }
        }
    }
}
