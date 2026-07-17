package com.nova.assistant.data.remote.ai

import com.google.gson.Gson
import com.nova.assistant.BuildConfig
import com.nova.assistant.data.remote.ai.models.*
import com.nova.assistant.domain.model.AIResponse
import com.nova.assistant.domain.model.ErrorCode
import com.nova.assistant.domain.model.Message
import com.nova.assistant.domain.model.MessageRole
import com.nova.assistant.util.Constants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

/**
 * Google Gemini Pro chat provider.
 */
class GeminiProvider @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) : AIProvider {

    override val name = "Google Gemini"
    override val id   = Constants.PROVIDER_GEMINI

    private var apiKey: String = ""
    private var model: String = Constants.GEMINI_MODEL_DEFAULT

    private val baseUrl = BuildConfig.BASE_URL_GEMINI
    private val json = "application/json; charset=utf-8".toMediaType()

    override fun isConfigured() = apiKey.isNotBlank()
    override fun setApiKey(apiKey: String) { this.apiKey = apiKey.trim() }
    override fun availableModels() = listOf("gemini-pro", "gemini-1.5-flash", "gemini-1.5-pro")
    override fun setModel(modelId: String) { this.model = modelId }
    override fun currentModel() = model

    override suspend fun chat(
        messages: List<Message>,
        maxTokens: Int,
        temperature: Float
    ): AIResponse {
        if (!isConfigured()) {
            return AIResponse.Error(
                "Gemini API key is not set. Go to Settings → API Key.",
                ErrorCode.API_KEY_MISSING
            )
        }

        val startTime = System.currentTimeMillis()

        // Separate system message
        val systemMsg = messages.firstOrNull { it.role == MessageRole.SYSTEM }
        val conversationMessages = messages
            .filter { it.role != MessageRole.SYSTEM }
            .map { msg ->
                GeminiContent(
                    role = if (msg.role == MessageRole.USER) "user" else "model",
                    parts = listOf(GeminiPart(msg.content))
                )
            }

        val systemInstruction = systemMsg?.let {
            GeminiContent(role = "user", parts = listOf(GeminiPart(it.content)))
        }

        val requestBody = GeminiRequest(
            contents = conversationMessages,
            systemInstruction = systemInstruction,
            generationConfig = GeminiGenerationConfig(
                maxOutputTokens = maxTokens,
                temperature = temperature
            )
        )

        val body = gson.toJson(requestBody).toRequestBody(json)
        val url = "${baseUrl}models/$model:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorCode = when (response.code) {
                    400 -> ErrorCode.API_KEY_INVALID
                    429 -> ErrorCode.RATE_LIMITED
                    else -> ErrorCode.PROVIDER_ERROR
                }
                val errorMsg = tryParseGeminiError(responseBody, response.code)
                Timber.e("Gemini error ${response.code}: $errorMsg")
                return AIResponse.Error(errorMsg, errorCode)
            }

            val parsed = gson.fromJson(responseBody, GeminiResponse::class.java)
            val text = parsed.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.joinToString("") { it.text }
                ?.trim() ?: ""

            if (text.isEmpty()) {
                return AIResponse.Error("Empty response from Gemini.", ErrorCode.PROVIDER_ERROR)
            }

            AIResponse.Success(
                text = text,
                provider = id,
                model = model,
                tokensUsed = parsed.usageMetadata?.totalTokenCount ?: 0,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: IOException) {
            Timber.e(e, "Gemini network error")
            AIResponse.Error("Network error: ${e.message}", ErrorCode.NETWORK_ERROR, e)
        } catch (e: Exception) {
            Timber.e(e, "Gemini unexpected error")
            AIResponse.Error("Unexpected error: ${e.message}", ErrorCode.UNKNOWN, e)
        }
    }

    private fun tryParseGeminiError(body: String, code: Int): String {
        return try {
            val err = gson.fromJson(body, GeminiError::class.java)
            err.error?.message ?: "HTTP $code"
        } catch (_: Exception) {
            "HTTP $code"
        }
    }
}
