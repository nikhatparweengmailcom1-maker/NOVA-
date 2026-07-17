package com.nova.assistant.data.remote.ai.models

import com.google.gson.annotations.SerializedName

// ── Request ────────────────────────────────────────────────────────────────

data class OpenAIChatRequest(
    @SerializedName("model")       val model: String,
    @SerializedName("messages")    val messages: List<OpenAIMessage>,
    @SerializedName("max_tokens")  val maxTokens: Int = 1024,
    @SerializedName("temperature") val temperature: Float = 0.7f,
    @SerializedName("stream")      val stream: Boolean = false
)

data class OpenAIMessage(
    @SerializedName("role")    val role: String,
    @SerializedName("content") val content: String
)

// ── Response ───────────────────────────────────────────────────────────────

data class OpenAIChatResponse(
    @SerializedName("id")      val id: String?,
    @SerializedName("object")  val obj: String?,
    @SerializedName("model")   val model: String?,
    @SerializedName("choices") val choices: List<OpenAIChoice>?,
    @SerializedName("usage")   val usage: OpenAIUsage?
)

data class OpenAIChoice(
    @SerializedName("index")         val index: Int,
    @SerializedName("message")       val message: OpenAIMessage?,
    @SerializedName("finish_reason") val finishReason: String?
)

data class OpenAIUsage(
    @SerializedName("prompt_tokens")     val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens")      val totalTokens: Int
)

data class OpenAIError(
    @SerializedName("error") val error: OpenAIErrorDetail?
)

data class OpenAIErrorDetail(
    @SerializedName("message") val message: String?,
    @SerializedName("type")    val type: String?,
    @SerializedName("code")    val code: String?
)
