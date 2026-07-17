package com.nova.assistant.domain.usecase

import com.nova.assistant.domain.model.Reminder
import com.nova.assistant.domain.model.ReminderType
import com.nova.assistant.domain.model.RepeatMode
import com.nova.assistant.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for all reminder, alarm, and timer management.
 */
class ManageRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    fun observeAll(): Flow<List<Reminder>> = repository.observeAll()

    fun observeUpcoming(): Flow<List<Reminder>> = repository.observeUpcoming()

    suspend fun createReminder(
        title: String,
        triggerAtMillis: Long,
        description: String = "",
        type: ReminderType = ReminderType.REMINDER,
        repeatMode: RepeatMode = RepeatMode.NONE
    ) = repository.createReminder(title, triggerAtMillis, description, type, repeatMode)

    suspend fun createTimer(title: String, durationMs: Long) {
        val triggerAt = System.currentTimeMillis() + durationMs
        repository.createReminder(
            title = title,
            triggerAtMillis = triggerAt,
            type = ReminderType.TIMER,
            repeatMode = RepeatMode.NONE
        )
    }

    suspend fun createAlarm(title: String, triggerAtMillis: Long, repeatMode: RepeatMode = RepeatMode.NONE) =
        repository.createReminder(title, triggerAtMillis, type = ReminderType.ALARM, repeatMode = repeatMode)

    suspend fun delete(id: String) = repository.delete(id)

    suspend fun markCompleted(id: String) = repository.markCompleted(id)

    suspend fun clearCompleted() = repository.clearCompleted()

    suspend fun getById(id: String): Reminder? = repository.getById(id)
}
