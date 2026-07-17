package com.nova.assistant.domain.model

/**
 * Parsed result of a recognized voice command.
 */
data class VoiceCommand(
    val rawText: String,
    val intent: VoiceIntent,
    val extras: Map<String, String> = emptyMap()
)

enum class VoiceIntent {
    // System control
    WAKE,
    STOP,
    PAUSE,
    RESUME,

    // General AI query
    QUERY,

    // Device
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    VOLUME_UP,
    VOLUME_DOWN,
    MEDIA_PLAY,
    MEDIA_PAUSE,

    // Communication
    CALL,
    SEND_SMS,

    // Apps
    OPEN_APP,

    // Information
    WEB_SEARCH,
    SET_REMINDER,
    SET_TIMER,
    SET_ALARM,
    ADD_TODO,
    GET_CALENDAR,

    // Camera
    TAKE_PHOTO,
    SCAN_QR
}
