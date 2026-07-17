package com.nova.assistant.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.nova.assistant.data.local.preferences.NovaPreferences
import com.nova.assistant.domain.model.VoiceState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TextToSpeech manager for NOVA.
 * Wraps Android TTS with preferences-backed speed/pitch.
 */
@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: NovaPreferences
) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.ENGLISH
                    Timber.w("TTS: default locale not supported, falling back to English")
                }
                applyPreferences()
                Timber.d("TTS initialized successfully")
            } else {
                Timber.e("TTS initialization failed with status: $status")
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                _isSpeaking.value = true
            }
            override fun onDone(utteranceId: String) {
                _isSpeaking.value = false
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) {
                _isSpeaking.value = false
                Timber.e("TTS error for utterance: $utteranceId")
            }
            override fun onError(utteranceId: String, errorCode: Int) {
                _isSpeaking.value = false
                Timber.e("TTS error code $errorCode for utterance: $utteranceId")
            }
        })
    }

    private fun applyPreferences() {
        try {
            val speed = runBlocking { preferences.ttsSpeed.first() }
            val pitch = runBlocking { preferences.ttsPitch.first() }
            tts?.setSpeechRate(speed)
            tts?.setPitch(pitch)
        } catch (e: Exception) {
            Timber.e(e, "Failed to apply TTS preferences")
        }
    }

    fun speak(text: String) {
        if (!isReady || text.isBlank()) return
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.1f, 4.0f))
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.1f, 2.0f))
    }

    fun isEngineReady() = isReady

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
        Timber.d("TTS shut down")
    }
}
