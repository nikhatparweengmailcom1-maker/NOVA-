package com.nova.assistant.data.remote.ai

import com.google.gson.Gson
import com.nova.assistant.BuildConfig
import com.nova.assistant.data.remote.ai.models.OpenAIChatRequest
import com.nova.assistant.data.remote.ai.models.OpenAIError
import com.nova.assistant.data.remote.ai.models.OpenAIMessage
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
 * OpenAI chat completion provider.
 * Supports GPT-4o-mini, GPT-4o, GPT-3.5-turbo.
 */
class OpenAIProvider @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) : AIProvider {

    override val name = "OpenAI"
    override val id   = Constants.PROVIDER_OPENAI

    private var apiKey: String = ""
    private var model: String = Constants.OPENAI_MODEL_DEFAULT

    private val baseUrl = BuildConfig.BASE_URL_OPENAI
    private val json = "application/json; charset=utf-8".toMediaType()

    override fun isConfigured() = apiKey.isNotBlank()

    override fun setApiKey(apiKey: String) { this.apiKey = apiKey.trim() }

    override fun availableModels() = listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo")

    override fun setModel(modelId: String) { this.model = modelId }

    override fun currentModel() = model

    override suspend fun chat(
        messages: List<Message>,
        maxTokens: Int,
        temperature: Float
    ): AIResponse {
        if (!isConfigured()) {
            return AIResponse.Error(
                "OpenAI API key is not set. Go to Settings → API Key.",
                ErrorCode.API_KEY_MISSING
            )
        }

        val startTime = System.currentTimeMillis()
        val openAiMessages = messages.map { msg ->
            OpenAIMessage(
                role = msg.role.apiValue,
                content = msg.content
            )
        }

        val requestBody = OpenAIChatRequest(
            model = model,
            messages = openAiMessages,
            maxTokens = maxTokens,
            temperature = temperature
        )

        val body = gson.toJson(requestBody).toRequestBody(json)
        val request = Request.Builder()
            .url("${baseUrl}chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorCode = when (response.code) {
                    401 -> ErrorCode.API_KEY_INVALID
                    429 -> ErrorCode.RATE_LIMITED
                    else -> ErrorCode.PROVIDER_ERROR
                }
                val errorMsg = tryParseOpenAIError(responseBody, response.code)
                Timber.e("OpenAI error ${response.code}: $errorMsg")
                return AIResponse.Error(errorMsg, errorCode)
            }

            val parsed = gson.fromJson(responseBody, com.nova.assistant.data.remote.ai.models.OpenAIChatResponse::class.java)
            val text = parsed.choices?.firstOrNull()?.message?.content?.trim() ?: ""
            if (text.isEmpty()) {
                return AIResponse.Error("Empty response from OpenAI.", ErrorCode.PROVIDER_ERROR)
            }
            AIResponse.Success(
                text = text,
                provider = id,
                model = parsed.model ?: model,
                tokensUsed = parsed.usage?.totalTokens ?: 0,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: IOException) {
            Timber.e(e, "OpenAI network error")
            AIResponse.Error("Network error: ${e.message}", ErrorCode.NETWORK_ERROR, e)
        } catch (e: Exception) {
            Timber.e(e, "OpenAI unexpected error")
            AIResponse.Error("Unexpected error: ${e.message}", ErrorCode.UNKNOWN, e)
        }
    }

    private fun tryParseOpenAIError(body: String, code: Int): String {
        return try {
            val err = gson.fromJson(body, OpenAIError::class.java)
            err.error?.message ?: "HTTP $code"
        } catch (_: Exception) {
            "HTTP $code"
        }
    }
}
