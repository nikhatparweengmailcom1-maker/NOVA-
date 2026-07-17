package com.nova.assistant.domain.model

/**
 * Represents all states of the NOVA voice pipeline.
 */
sealed class VoiceState {
    /** No active listening or speaking. */
    object Idle : VoiceState()

    /** Actively listening for speech. */
    object Listening : VoiceState()

    /** Speech captured, waiting for AI response. */
    object Processing : VoiceState()

    /** TTS output in progress. */
    data class Speaking(val text: String = "") : VoiceState()

    /** Explicitly paused by the user or a voice command. */
    object Paused : VoiceState()

    /** An error occurred in the voice pipeline. */
    data class Error(val message: String) : VoiceState()
}
