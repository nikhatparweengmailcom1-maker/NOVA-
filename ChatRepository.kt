package com.nova.assistant.domain.repository

import com.nova.assistant.domain.model.AIResponse
import com.nova.assistant.domain.model.Conversation
import com.nova.assistant.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getOrCreateActiveConversation(): Conversation
    fun observeConversations(): Flow<List<Conversation>>
    fun observeMessages(conversationId: String): Flow<List<Message>>
    suspend fun sendMessage(conversationId: String, text: String, isVoice: Boolean): AIResponse
    suspend fun deleteConversation(conversationId: String)
    suspend fun clearAll()
}
