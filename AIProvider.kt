package com.nova.assistant.data.remote.ai

import com.nova.assistant.domain.model.AIResponse
import com.nova.assistant.domain.model.Message

/**
 * Contract for pluggable AI provider implementations (OpenAI, Gemini, …).
 */
interface AIProvider {

    /** Human-readable provider name (e.g. "OpenAI"). */
    val name: String

    /** Set the API key to use for subsequent requests. */
    fun setApiKey(key: String)

    /**
     * Send [messages] to the AI model.
     *
     * @param systemPrompt  Optional system instruction injected before the conversation.
     * @param messages      Ordered conversation history (most recent last).
     * @return              [AIResponse.Success] or [AIResponse.Error].
     */
    suspend fun chat(
        systemPrompt: String = "",
        messages: List<Message>
    ): AIResponse

    /** Returns the list of models available through this provider. */
    fun availableModels(): List<String>
}
