package com.nova.assistant.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility functions for date and time operations.
 */
object DateTimeUtil {

    fun now(): Long = System.currentTimeMillis()

    fun today(): Calendar = Calendar.getInstance()

    fun formatDate(millis: Long, pattern: String = "MMM d, yyyy"): String {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatTime(millis: Long, pattern: String = "h:mm a"): String {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDateTime(millis: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return when {
            isSameDay(now, target) -> "Today at ${formatTime(millis)}"
            isTomorrow(now, target) -> "Tomorrow at ${formatTime(millis)}"
            isYesterday(now, target) -> "Yesterday at ${formatTime(millis)}"
            else -> "${formatDate(millis)} at ${formatTime(millis)}"
        }
    }

    fun formatRelative(millis: Long): String {
        val diff = millis - now()
        val absDiff = Math.abs(diff)
        val isPast = diff < 0

        val seconds = absDiff / 1000
        val minutes = seconds / 60
        val hours   = minutes / 60
        val days    = hours / 24

        return when {
            seconds < 60   -> if (isPast) "just now" else "in a moment"
            minutes < 60   -> if (isPast) "$minutes min ago" else "in $minutes min"
            hours < 24     -> if (isPast) "$hours hr ago"  else "in $hours hr"
            days < 7       -> if (isPast) "$days days ago" else "in $days days"
            else           -> formatDate(millis)
        }
    }

    fun getDurationLabel(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val hours   = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return when {
            hours > 0  -> "%d:%02d:%02d".format(hours, minutes, seconds)
            else       -> "%02d:%02d".format(minutes, seconds)
        }
    }

    fun parseNaturalDatePhrase(phrase: String): Long? {
        val lower = phrase.lowercase(Locale.getDefault()).trim()
        val cal = Calendar.getInstance()
        return when {
            lower.contains("tomorrow") -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            lower.contains("next week") -> {
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                cal.timeInMillis
            }
            lower.contains("in an hour") || lower.contains("in 1 hour") -> {
                cal.add(Calendar.HOUR_OF_DAY, 1)
                cal.timeInMillis
            }
            lower.contains("in 30 minutes") || lower.contains("in half an hour") -> {
                cal.add(Calendar.MINUTE, 30)
                cal.timeInMillis
            }
            lower.contains("tonight") -> {
                cal.set(Calendar.HOUR_OF_DAY, 20)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            lower.contains("this morning") -> {
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            else -> null
        }
    }

    fun parseDurationPhrase(phrase: String): Long? {
        val lower = phrase.lowercase(Locale.getDefault())
        val hoursMatch   = Regex("(\\d+)\\s*hour").find(lower)
        val minutesMatch = Regex("(\\d+)\\s*min").find(lower)
        val secondsMatch = Regex("(\\d+)\\s*sec").find(lower)
        val hours   = hoursMatch?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
        val minutes = minutesMatch?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
        val seconds = secondsMatch?.groupValues?.getOrNull(1)?.toLongOrNull() ?: 0L
        val totalMs = (hours * 3600 + minutes * 60 + seconds) * 1000
        return if (totalMs > 0) totalMs else null
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean =
        c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

    private fun isTomorrow(today: Calendar, target: Calendar): Boolean {
        val tomorrow = today.clone() as Calendar
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        return isSameDay(tomorrow, target)
    }

    private fun isYesterday(today: Calendar, target: Calendar): Boolean {
        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, target)
    }

    fun getAlarmCalendar(hour: Int, minute: Int, repeatDaily: Boolean = false): Calendar {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal
    }
}
