package com.nova.assistant.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val triggerAtMillis: Long,
    val type: String,         // "REMINDER" | "ALARM" | "TIMER"
    val repeatMode: String,   // "NONE" | "DAILY" | ...
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long,
    val alarmRequestCode: Int
)
