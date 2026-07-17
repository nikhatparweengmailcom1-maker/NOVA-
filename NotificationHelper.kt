package com.nova.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nova.assistant.MainActivity
import com.nova.assistant.R
import com.nova.assistant.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized notification management for NOVA.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        val serviceChannel = NotificationChannel(
            Constants.CHANNEL_SERVICE,
            context.getString(R.string.notification_channel_service),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_service_desc)
            setShowBadge(false)
            enableLights(true)
            lightColor = android.graphics.Color.CYAN
        }

        val remindersChannel = NotificationChannel(
            Constants.CHANNEL_REMINDERS,
            context.getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_reminders_desc)
            enableVibration(true)
            enableLights(true)
            lightColor = android.graphics.Color.CYAN
        }

        val updatesChannel = NotificationChannel(
            Constants.CHANNEL_UPDATES,
            context.getString(R.string.notification_channel_updates),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_updates_desc)
        }

        notificationManager.createNotificationChannels(
            listOf(serviceChannel, remindersChannel, updatesChannel)
        )
    }

    /** Foreground service notification — shown while NOVA is active. */
    fun buildServiceNotification(
        isPaused: Boolean = false
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Pause / Resume action
        val toggleAction = if (isPaused) {
            val resumeIntent = Intent(context, NovaForegroundService::class.java)
                .setAction(Constants.ACTION_RESUME_SERVICE)
            val pi = PendingIntent.getService(
                context, 1, resumeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                context.getString(R.string.notification_action_resume), pi
            )
        } else {
            val pauseIntent = Intent(context, NovaForegroundService::class.java)
                .setAction(Constants.ACTION_PAUSE_SERVICE)
            val pi = PendingIntent.getService(
                context, 2, pauseIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.notification_action_pause), pi
            )
        }

        // Stop action
        val stopIntent = Intent(context, NovaForegroundService::class.java)
            .setAction(Constants.ACTION_STOP_SERVICE)
        val stopPi = PendingIntent.getService(
            context, 3, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_delete,
            context.getString(R.string.notification_action_stop), stopPi
        )

        return NotificationCompat.Builder(context, Constants.CHANNEL_SERVICE)
            .setContentTitle(context.getString(R.string.notification_service_title))
            .setContentText(
                if (isPaused) context.getString(R.string.notification_service_paused)
                else context.getString(R.string.notification_service_text)
            )
            .setSmallIcon(R.drawable.ic_nova_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(toggleAction)
            .addAction(stopAction)
            .build()
    }

    /** Show a reminder / alarm notification. */
    fun showReminderNotification(id: Int, title: String, text: String) {
        val contentIntent = PendingIntent.getActivity(
            context, id,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java)
            .setAction(Constants.ACTION_DISMISS_ALARM)
            .putExtra(Constants.EXTRA_ALARM_ID, id)
        val dismissPi = PendingIntent.getBroadcast(
            context, id + 1000, dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_REMINDERS)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_nova_notification)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPi)
            .build()

        notificationManager.notify(id, notification)
    }

    /** Show a simple informational notification. */
    fun showUpdateNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(context, Constants.CHANNEL_UPDATES)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_nova_notification)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun cancelNotification(id: Int) = notificationManager.cancel(id)

    fun cancelAll() = notificationManager.cancelAll()
}
