package com.nova.assistant.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nova.assistant.data.local.database.dao.ReminderDao
import com.nova.assistant.data.local.database.entities.ReminderEntity
import com.nova.assistant.domain.model.Reminder
import com.nova.assistant.domain.model.ReminderType
import com.nova.assistant.domain.model.RepeatMode
import com.nova.assistant.domain.repository.ReminderRepository
import com.nova.assistant.service.AlarmReceiver
import com.nova.assistant.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: ReminderDao
) : ReminderRepository {

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun observeAllReminders(): Flow<List<Reminder>> =
        reminderDao.observeAll().map { it.map(ReminderEntity::toDomain) }

    override fun observeActiveReminders(): Flow<List<Reminder>> =
        reminderDao.observeActive().map { it.map(ReminderEntity::toDomain) }

    override suspend fun getReminderById(id: String): Reminder? =
        withContext(Dispatchers.IO) { reminderDao.getById(id)?.toDomain() }

    override suspend fun createReminder(reminder: Reminder): Reminder =
        withContext(Dispatchers.IO) {
            reminderDao.insert(reminder.toEntity())
            reminder
        }

    override suspend fun updateReminder(reminder: Reminder) =
        withContext(Dispatchers.IO) { reminderDao.update(reminder.toEntity()) }

    override suspend fun deleteReminder(id: String) =
        withContext(Dispatchers.IO) { reminderDao.deleteById(id) }

    override suspend fun markCompleted(id: String) =
        withContext(Dispatchers.IO) { reminderDao.markCompleted(id) }

    override suspend fun clearCompletedReminders() =
        withContext(Dispatchers.IO) { reminderDao.clearCompleted() }

    override suspend fun scheduleAlarm(reminder: Reminder) {
        val intent = AlarmReceiver.buildIntent(context, reminder)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.alarmRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                // Fall back to inexact alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.triggerAtMillis, pendingIntent)
                Timber.w("Exact alarms not permitted, using inexact for reminder=${reminder.id}")
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAtMillis,
                    pendingIntent
                )
            }
            Timber.d("Alarm scheduled: ${reminder.title} at ${reminder.triggerAtMillis}")
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException scheduling alarm, falling back to inexact")
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.triggerAtMillis, pendingIntent)
        }
    }

    override suspend fun cancelAlarm(reminder: Reminder) {
        val intent = AlarmReceiver.buildIntent(context, reminder)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.alarmRequestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Timber.d("Alarm cancelled: ${reminder.id}")
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun ReminderEntity.toDomain() = Reminder(
        id = id,
        title = title,
        description = description,
        triggerAtMillis = triggerAtMillis,
        type = ReminderType.valueOf(type),
        repeatMode = RepeatMode.valueOf(repeatMode),
        isActive = isActive,
        isCompleted = isCompleted,
        createdAt = createdAt,
        alarmRequestCode = alarmRequestCode
    )

    private fun Reminder.toEntity() = ReminderEntity(
        id = id,
        title = title,
        description = description,
        triggerAtMillis = triggerAtMillis,
        type = type.name,
        repeatMode = repeatMode.name,
        isActive = isActive,
        isCompleted = isCompleted,
        createdAt = createdAt,
        alarmRequestCode = alarmRequestCode
    )
}
