package com.nova.assistant.domain.usecase

import com.nova.assistant.domain.model.VoiceCommand
import com.nova.assistant.domain.model.VoiceIntent
import com.nova.assistant.util.Constants
import javax.inject.Inject

/**
 * Classifies a raw voice transcript into a [VoiceCommand] with [VoiceIntent].
 * Pure function — no I/O, no coroutines. Called from ChatViewModel on the main thread.
 */
class ProcessVoiceCommandUseCase @Inject constructor() {

    operator fun invoke(rawText: String): VoiceCommand {
        val text = rawText.trim().lowercase()

        // ── System control ────────────────────────────────────────────────
        if (matchesAny(text, Constants.WAKE_PHRASE.split(","))) {
            return VoiceCommand(rawText, VoiceIntent.WAKE)
        }
        if (matchesAny(text, Constants.VOICE_STOP_PHRASES.split(","))) {
            return VoiceCommand(rawText, VoiceIntent.STOP)
        }
        if (matchesAny(text, Constants.VOICE_PAUSE_PHRASES.split(","))) {
            return VoiceCommand(rawText, VoiceIntent.PAUSE)
        }
        if (matchesAny(text, Constants.VOICE_RESUME_PHRASES.split(","))) {
            return VoiceCommand(rawText, VoiceIntent.RESUME)
        }

        // ── Flashlight ────────────────────────────────────────────────────
        if (contains(text, "flashlight on", "turn on flashlight", "enable flashlight", "torch on")) {
            return VoiceCommand(rawText, VoiceIntent.FLASHLIGHT_ON)
        }
        if (contains(text, "flashlight off", "turn off flashlight", "disable flashlight", "torch off")) {
            return VoiceCommand(rawText, VoiceIntent.FLASHLIGHT_OFF)
        }

        // ── Volume ────────────────────────────────────────────────────────
        if (contains(text, "volume up", "louder", "increase volume", "turn it up")) {
            return VoiceCommand(rawText, VoiceIntent.VOLUME_UP)
        }
        if (contains(text, "volume down", "quieter", "decrease volume", "turn it down", "lower volume")) {
            return VoiceCommand(rawText, VoiceIntent.VOLUME_DOWN)
        }

        // ── Media ─────────────────────────────────────────────────────────
        if (contains(text, "play music", "resume music", "resume playback")) {
            return VoiceCommand(rawText, VoiceIntent.MEDIA_PLAY)
        }
        if (contains(text, "pause music", "pause playback", "stop music")) {
            return VoiceCommand(rawText, VoiceIntent.MEDIA_PAUSE)
        }

        // ── Open app ──────────────────────────────────────────────────────
        val openAppRegex = Regex("""open\s+(?:the\s+)?(.+?)(?:\s+app)?$""")
        val launchRegex  = Regex("""(?:launch|start)\s+(.+?)(?:\s+app)?$""")
        (openAppRegex.find(text) ?: launchRegex.find(text))?.let { match ->
            val appName = match.groupValues[1].trim()
            return VoiceCommand(rawText, VoiceIntent.OPEN_APP, mapOf("app" to appName))
        }

        // ── Call ──────────────────────────────────────────────────────────
        val callRegex = Regex("""(?:call|ring|phone|dial)\s+(.+)""")
        callRegex.find(text)?.let { match ->
            val contact = match.groupValues[1].trim()
            return VoiceCommand(rawText, VoiceIntent.CALL, mapOf("contact" to contact))
        }

        // ── SMS ───────────────────────────────────────────────────────────
        val smsRegex = Regex("""(?:text|sms|message|send a message to)\s+(.+?)(?:\s+saying\s+(.+))?$""")
        smsRegex.find(text)?.let { match ->
            val contact = match.groupValues[1].trim()
            val msg     = match.groupValues.getOrNull(2)?.trim() ?: ""
            return VoiceCommand(rawText, VoiceIntent.SEND_SMS, mapOf("contact" to contact, "message" to msg))
        }

        // ── Reminder ──────────────────────────────────────────────────────
        if (contains(text, "remind me", "set a reminder", "add reminder", "reminder for")) {
            return VoiceCommand(rawText, VoiceIntent.SET_REMINDER, mapOf("raw" to rawText))
        }

        // ── Timer ─────────────────────────────────────────────────────────
        if (contains(text, "set a timer", "set timer", "start timer", "timer for")) {
            return VoiceCommand(rawText, VoiceIntent.SET_TIMER, mapOf("raw" to rawText))
        }

        // ── Alarm ─────────────────────────────────────────────────────────
        if (contains(text, "set an alarm", "set alarm", "wake me up at", "alarm at", "alarm for")) {
            return VoiceCommand(rawText, VoiceIntent.SET_ALARM, mapOf("raw" to rawText))
        }

        // ── To-Do ─────────────────────────────────────────────────────────
        val todoRegex = Regex("""(?:add task|add to[- ]?do|todo|add item)[: ]+(.+)""")
        todoRegex.find(text)?.let { match ->
            return VoiceCommand(rawText, VoiceIntent.ADD_TODO, mapOf("task" to match.groupValues[1].trim()))
        }
        if (contains(text, "add to my list", "add to to-do", "add a task")) {
            return VoiceCommand(rawText, VoiceIntent.ADD_TODO, mapOf("task" to rawText))
        }

        // ── Calendar ──────────────────────────────────────────────────────
        if (contains(text, "what's on my calendar", "my schedule", "upcoming events", "what do i have today")) {
            return VoiceCommand(rawText, VoiceIntent.GET_CALENDAR)
        }

        // ── Web search ────────────────────────────────────────────────────
        val searchRegex = Regex("""(?:search for|search|look up|google|find me)\s+(.+)""")
        searchRegex.find(text)?.let { match ->
            return VoiceCommand(rawText, VoiceIntent.WEB_SEARCH, mapOf("query" to match.groupValues[1].trim()))
        }

        // ── Camera ────────────────────────────────────────────────────────
        if (contains(text, "scan qr", "scan barcode", "scan qr code")) {
            return VoiceCommand(rawText, VoiceIntent.SCAN_QR)
        }
        if (contains(text, "take a photo", "take picture", "open camera")) {
            return VoiceCommand(rawText, VoiceIntent.TAKE_PHOTO)
        }

        // ── Default: pass to AI ───────────────────────────────────────────
        return VoiceCommand(rawText, VoiceIntent.QUERY)
    }

    private fun matchesAny(text: String, phrases: List<String>): Boolean =
        phrases.any { phrase -> text.trim() == phrase.trim() || text.contains(phrase.trim()) }

    private fun contains(text: String, vararg phrases: String): Boolean =
        phrases.any { text.contains(it) }
}
