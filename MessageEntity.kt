package com.nova.assistant.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String,       // "user" | "assistant" | "system"
    val content: String,
    val timestamp: Long,
    val isError: Boolean = false,
    val isVoice: Boolean = false,
    val imageUri: String? = null,
    val processingTimeMs: Long? = null
)
