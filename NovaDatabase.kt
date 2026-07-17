package com.nova.assistant.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nova.assistant.data.local.database.dao.ConversationDao
import com.nova.assistant.data.local.database.dao.MessageDao
import com.nova.assistant.data.local.database.dao.ReminderDao
import com.nova.assistant.data.local.database.dao.TodoDao
import com.nova.assistant.data.local.database.entities.ConversationEntity
import com.nova.assistant.data.local.database.entities.MessageEntity
import com.nova.assistant.data.local.database.entities.ReminderEntity
import com.nova.assistant.data.local.database.entities.TodoEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ReminderEntity::class,
        TodoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun reminderDao(): ReminderDao
    abstract fun todoDao(): TodoDao
}
