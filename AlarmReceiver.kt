package com.nova.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nova.assistant.domain.model.Reminder
import com.nova.assistant.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Receives alarm/reminder/timer triggers from AlarmManager.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        fun buildIntent(context: Context, reminder: Reminder): Intent {
            val action = when (reminder.type) {
                com.nova.assistant.domain.model.ReminderType.ALARM  -> Constants.ACTION_ALARM_TRIGGER
                com.nova.assistant.domain.model.ReminderType.TIMER  -> Constants.ACTION_TIMER_TRIGGER
                else -> Constants.ACTION_REMINDER_TRIGGER
            }
            return Intent(context, AlarmReceiver::class.java).apply {
                setAction(action)
                putExtra(Constants.EXTRA_REMINDER_ID, reminder.id)
                putExtra(Constants.EXTRA_REMINDER_TITLE, reminder.title)
                putExtra(Constants.EXTRA_ALARM_ID, reminder.alarmRequestCode)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(Constants.EXTRA_REMINDER_TITLE) ?: "NOVA Reminder"
        val alarmId = intent.getIntExtra(Constants.EXTRA_ALARM_ID, 0)

        Timber.d("AlarmReceiver: action=${intent.action}, title=$title")

        when (intent.action) {
            Constants.ACTION_REMINDER_TRIGGER -> {
                notificationHelper.showReminderNotification(
                    id = Constants.NOTIF_REMINDER_BASE_ID + alarmId,
                    title = "⏰ Reminder",
                    text = title
                )
            }
            Constants.ACTION_ALARM_TRIGGER -> {
                notificationHelper.showReminderNotification(
                    id = Constants.NOTIF_ALARM_BASE_ID + alarmId,
                    title = "🔔 Alarm",
                    text = title
                )
            }
            Constants.ACTION_TIMER_TRIGGER -> {
                notificationHelper.showReminderNotification(
                    id = Constants.NOTIF_TIMER_BASE_ID + alarmId,
                    title = "⏱ Timer Done",
                    text = title
                )
            }
            Constants.ACTION_DISMISS_ALARM -> {
                notificationHelper.cancelNotification(alarmId)
            }
        }
    }
}
