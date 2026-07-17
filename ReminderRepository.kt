package com.nova.assistant.domain.repository

import com.nova.assistant.domain.model.Reminder
import com.nova.assistant.domain.model.ReminderType
import com.nova.assistant.domain.model.RepeatMode
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeAll(): Flow<List<Reminder>>
    fun observeUpcoming(): Flow<List<Reminder>>
    suspend fun createReminder(
        title: String,
        triggerAtMillis: Long,
        description: String = "",
        type: ReminderType = ReminderType.REMINDER,
        repeatMode: RepeatMode = RepeatMode.NONE
    ): Reminder
    suspend fun delete(id: String)
    suspend fun markCompleted(id: String)
    suspend fun clearCompleted()
    suspend fun getById(id: String): Reminder?
}
