package com.nova.assistant.domain.usecase

import com.nova.assistant.domain.model.AIResponse
import com.nova.assistant.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case for sending a user message and receiving an AI response.
 */
class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        text: String,
        isVoice: Boolean = false
    ): AIResponse = repository.sendMessage(conversationId, text, isVoice)
}
