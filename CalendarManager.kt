package com.nova.assistant.features.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String,
    val startMillis: Long,
    val endMillis: Long,
    val location: String = ""
)

/**
 * Manages calendar events via CalendarContract ContentProvider.
 */
@Singleton
class CalendarManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED

    private fun hasWritePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED

    /** Get upcoming events within the next [days] days. */
    fun getUpcomingEvents(days: Int = 7): List<CalendarEvent> {
        if (!hasPermission()) return emptyList()
        val now = System.currentTimeMillis()
        val end = now + (days * 24 * 60 * 60 * 1000L)
        val events = mutableListOf<CalendarEvent>()
        try {
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, now)
            ContentUris.appendId(builder, end)
            val cursor = context.contentResolver.query(
                builder.build(),
                arrayOf(
                    CalendarContract.Instances.EVENT_ID,
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.DESCRIPTION,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.EVENT_LOCATION
                ),
                null, null,
                CalendarContract.Instances.START_DAY + " ASC"
            )
            cursor?.use { c ->
                while (c.moveToNext()) {
                    events.add(
                        CalendarEvent(
                            id          = c.getLong(0),
                            title       = c.getString(1) ?: "",
                            description = c.getString(2) ?: "",
                            startMillis = c.getLong(3),
                            endMillis   = c.getLong(4),
                            location    = c.getString(5) ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "CalendarManager: failed to query events")
        }
        return events
    }

    /**
     * Opens the system calendar insert UI to create a new event.
     * User confirms in the system UI.
     */
    fun openCreateEventIntent(
        title: String,
        startMillis: Long,
        endMillis: Long = startMillis + 3600_000L,
        description: String = "",
        location: String = ""
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "CalendarManager: failed to open create event intent")
        }
    }

    fun formatEventSummary(events: List<CalendarEvent>): String {
        if (events.isEmpty()) return "No upcoming events in the next 7 days."
        val sdf = java.text.SimpleDateFormat("EEE, MMM d 'at' h:mm a", java.util.Locale.getDefault())
        return events.take(5).joinToString("\n") { event ->
            "• ${event.title} — ${sdf.format(java.util.Date(event.startMillis))}"
        }
    }
}
