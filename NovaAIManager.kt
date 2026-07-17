package com.nova.assistant.data.remote.ai

import com.nova.assistant.data.local.preferences.NovaPreferences
import com.nova.assistant.domain.model.AIResponse
import com.nova.assistant.domain.model.ErrorCode
import com.nova.assistant.domain.model.Message
import com.nova.assistant.domain.model.MessageRole
import com.nova.assistant.util.Constants
import com.nova.assistant.util.SecureStorageManager
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Routes AI requests to the active provider (OpenAI or Gemini).
 * Injects the NOVA system prompt and trims context to the last N messages.
 */
@Singleton
class NovaAIManager @Inject constructor(
    private val openAIProvider: OpenAIProvider,
    private val geminiProvider: GeminiProvider,
    private val preferences: NovaPreferences,
    private val secureStorage: SecureStorageManager
) {
    /**
     * Send [messages] to the currently-selected AI provider.
     * [messages] should already include the new user message.
     *
     * Returns [AIResponse.Success] or [AIResponse.Error].
     */
    suspend fun chat(messages: List<Message>): AIResponse {
        val provider = preferences.aiProvider.first()
        val apiKey   = secureStorage.getApiKey(provider)

        if (apiKey.isNullOrBlank()) {
            return AIResponse.Error(
                "API key not set for $provider. Go to Settings to add your key.",
                ErrorCode.API_KEY_MISSING
            )
        }

        // Take the last N messages to stay within context limits
        val context = messages.takeLast(Constants.MAX_CONTEXT_MESSAGES)

        return try {
            val activeProvider: AIProvider = when (provider) {
                Constants.PROVIDER_GEMINI -> geminiProvider
                else -> openAIProvider
            }
            activeProvider.setApiKey(apiKey)
            activeProvider.chat(
                systemPrompt = Constants.NOVA_SYSTEM_PROMPT,
                messages     = context
            )
        } catch (e: Exception) {
            Timber.e(e, "NovaAIManager: uncaught exception during chat")
            AIResponse.Error(
                "Unexpected error: ${e.message ?: "unknown"}",
                ErrorCode.UNKNOWN
            )
        }
    }

    fun currentProvider(): String = try {
        // Synchronous read — used only for display, not latency-sensitive
        runBlocking { preferences.aiProvider.first() }
    } catch (e: Exception) {
        Constants.DEFAULT_PROVIDER
    }

    fun availableModels(provider: String): List<String> =
        when (provider) {
            Constants.PROVIDER_OPENAI -> openAIProvider.availableModels()
            Constants.PROVIDER_GEMINI -> geminiProvider.availableModels()
            else -> emptyList()
        }
}

// ── Simple blocking helper (no dispatcher needed; called infrequently) ────────
private fun <T> runBlocking(block: suspend () -> T): T =
    kotlinx.coroutines.runBlocking { block() }
