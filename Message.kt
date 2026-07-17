package com.nova.assistant.domain.model

import java.util.UUID

/**
 * Domain model representing a single chat message.
 */
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val metadata: MessageMetadata? = null
)

enum class MessageRole(val apiValue: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system")
}

/**
 * Optional metadata attached to a message (e.g. voice, image, search).
 */
data class MessageMetadata(
    val isVoice: Boolean = false,
    val hasImage: Boolean = false,
    val imageUri: String? = null,
    val searchQuery: String? = null,
    val processingTimeMs: Long? = null
)
