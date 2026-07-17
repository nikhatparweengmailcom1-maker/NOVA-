package com.nova.assistant.data.remote.ai.models

import com.google.gson.annotations.SerializedName

// ── Request ────────────────────────────────────────────────────────────────

data class GeminiRequest(
    @SerializedName("contents")         val contents: List<GeminiContent>,
    @SerializedName("systemInstruction") val systemInstruction: GeminiContent? = null,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    @SerializedName("role")  val role: String,    // "user" | "model"
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String
)

data class GeminiGenerationConfig(
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 1024,
    @SerializedName("temperature")     val temperature: Float = 0.7f
)

// ── Response ───────────────────────────────────────────────────────────────

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>?,
    @SerializedName("usageMetadata") val usageMetadata: GeminiUsage?
)

data class GeminiCandidate(
    @SerializedName("content")      val content: GeminiContent?,
    @SerializedName("finishReason") val finishReason: String?,
    @SerializedName("index")        val index: Int = 0
)

data class GeminiUsage(
    @SerializedName("promptTokenCount")     val promptTokenCount: Int = 0,
    @SerializedName("candidatesTokenCount") val candidatesTokenCount: Int = 0,
    @SerializedName("totalTokenCount")      val totalTokenCount: Int = 0
)

data class GeminiError(
    @SerializedName("error") val error: GeminiErrorDetail?
)

data class GeminiErrorDetail(
    @SerializedName("code")    val code: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("status")  val status: String?
)
