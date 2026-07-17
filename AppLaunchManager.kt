package com.nova.assistant.features.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Launches installed apps by name or package name.
 */
@Singleton
class AppLaunchManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pm: PackageManager get() = context.packageManager

    /**
     * Tries to find and launch an app matching [appName].
     * Returns true if the app was launched.
     */
    fun launchApp(appName: String): Boolean {
        if (appName.isBlank()) return false

        // Try exact package name first
        val byPackage = tryLaunchByPackage(appName)
        if (byPackage) return true

        // Search installed apps by label
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val lower = appName.lowercase()

        val best = installed.firstOrNull { app ->
            val label = pm.getApplicationLabel(app).toString().lowercase()
            label == lower || label.contains(lower) || lower.contains(label)
        }

        if (best != null) {
            return tryLaunchByPackage(best.packageName)
        }

        Timber.w("AppLaunchManager: no app found for '$appName'")
        return false
    }

    private fun tryLaunchByPackage(packageName: String): Boolean {
        return try {
            val intent = pm.getLaunchIntentForPackage(packageName) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Timber.d("AppLaunchManager: launched '$packageName'")
            true
        } catch (e: Exception) {
            Timber.e(e, "AppLaunchManager: failed to launch '$packageName'")
            false
        }
    }

    /** Returns a list of installed app names. */
    fun getInstalledAppNames(): List<String> {
        return try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map { pm.getApplicationLabel(it).toString() }
                .sorted()
        } catch (e: Exception) {
            Timber.e(e, "AppLaunchManager: failed to list apps")
            emptyList()
        }
    }
}
