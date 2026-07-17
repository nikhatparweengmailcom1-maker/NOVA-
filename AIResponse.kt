package com.nova.assistant.domain.model

/**
 * Represents the result of an AI chat request.
 */
sealed class AIResponse {

    /** Successful response from the AI model. */
    data class Success(
        val text: String,
        val provider: String,
        val model: String,
        val totalTokens: Int = 0
    ) : AIResponse()

    /** The request is in progress (for streaming / UI state). */
    object Loading : AIResponse()

    /** An error occurred. */
    data class Error(
        val message: String,
        val code: ErrorCode = ErrorCode.UNKNOWN
    ) : AIResponse()
}

enum class ErrorCode {
    API_KEY_MISSING,
    NETWORK_ERROR,
    RATE_LIMITED,
    CONTEXT_TOO_LONG,
    SERVER_ERROR,
    UNKNOWN
}
