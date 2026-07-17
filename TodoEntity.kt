package com.nova.assistant.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // LOW | MEDIUM | HIGH | CRITICAL
    val dueDate: Long? = null,
    val createdAt: Long,
    val completedAt: Long? = null,
    val tagsJson: String = "[]"      // JSON array of tag strings
)
