package com.nova.assistant.domain.model

import java.util.UUID

/**
 * Domain model for a conversation session.
 */
data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Conversation",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<Message> = emptyList(),
    val isPinned: Boolean = false
)
