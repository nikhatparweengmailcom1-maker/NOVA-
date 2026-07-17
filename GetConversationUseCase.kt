package com.nova.assistant.domain.usecase

import com.nova.assistant.domain.model.Conversation
import com.nova.assistant.domain.model.Message
import com.nova.assistant.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for reading conversations and messages.
 */
class GetConversationUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    /** Get or create the currently-active conversation. */
    suspend fun getOrCreateActive(): Conversation = repository.getOrCreateActiveConversation()

    /** Observe live messages for a conversation. */
    fun observeMessages(conversationId: String): Flow<List<Message>> =
        repository.observeMessages(conversationId)

    /** Observe all conversations. */
    fun observeAll(): Flow<List<Conversation>> = repository.observeConversations()

    /** Clear all conversations and messages. */
    suspend fun clearAll() = repository.clearAll()

    /** Delete a single conversation. */
    suspend fun delete(conversationId: String) = repository.deleteConversation(conversationId)
}
