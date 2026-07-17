package com.nova.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

// ── Context extensions ─────────────────────────────────────────────────────

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "Failed to open URL: $url")
    }
}

fun Context.openAppSettings() {
    try {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "Failed to open app settings")
    }
}

// ── String extensions ──────────────────────────────────────────────────────

fun String.containsIgnoreCase(other: String): Boolean =
    this.lowercase().contains(other.lowercase())

fun String.isValidUrl(): Boolean {
    return startsWith("http://") || startsWith("https://")
}

fun String.truncate(maxLength: Int, suffix: String = "…"): String =
    if (length <= maxLength) this else substring(0, maxLength - suffix.length) + suffix

fun String.toSentenceCase(): String =
    if (isEmpty()) this else this[0].uppercaseChar() + substring(1).lowercase()

// ── Date extensions ────────────────────────────────────────────────────────

fun Long.toFormattedDate(pattern: String = "MMM d, yyyy"): String {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

fun Long.toFormattedTime(pattern: String = "h:mm a"): String {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

fun Long.toFormattedDateTime(): String {
    val date = toFormattedDate()
    val time = toFormattedTime()
    return "$date at $time"
}

// ── Coroutine extensions ───────────────────────────────────────────────────

/**
 * Safely launches a coroutine, logging any non-cancellation exceptions.
 */
fun CoroutineScope.launchSafely(
    tag: String = "NOVA",
    block: suspend CoroutineScope.() -> Unit
) = launch(Dispatchers.Main) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.e(e, "[$tag] Unhandled coroutine exception")
    }
}

// ── Flow extensions ────────────────────────────────────────────────────────

fun <T> T.toResult(): Result<T> = Result.success(this)

// ── Number extensions ──────────────────────────────────────────────────────

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun Long.toMinutesAndSeconds(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}

// ── Voice command helpers ──────────────────────────────────────────────────

fun String.isWakePhrase(): Boolean =
    trim().lowercase() == Constants.WAKE_PHRASE

fun String.isStopCommand(): Boolean =
    trim().lowercase().containsIgnoreCase(Constants.STOP_COMMAND)

fun String.isPauseCommand(): Boolean =
    trim().lowercase() == Constants.PAUSE_COMMAND

fun String.isResumeCommand(): Boolean =
    trim().lowercase() == Constants.RESUME_COMMAND

fun String.isVoiceCommand(): Boolean =
    isWakePhrase() || isStopCommand() || isPauseCommand() || isResumeCommand()
