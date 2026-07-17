package com.nova.assistant.domain.model

import java.util.UUID

/**
 * Domain model for a reminder / alarm / timer.
 */
data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val triggerAtMillis: Long,
    val type: ReminderType = ReminderType.REMINDER,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val alarmRequestCode: Int = id.hashCode() and 0xFFFF
)

enum class ReminderType {
    REMINDER,
    ALARM,
    TIMER
}

enum class RepeatMode {
    NONE,
    DAILY,
    WEEKLY,
    WEEKDAYS,
    WEEKENDS,
    MONTHLY
}
