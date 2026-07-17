package com.nova.assistant.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.nova.assistant.domain.model.VoiceState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Android SpeechRecognizer for live voice input.
 * Must be called from the main thread (SpeechRecognizer requirement).
 */
@Singleton
class VoiceRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _recognizedText = MutableStateFlow<String?>(null)
    val recognizedText: StateFlow<String?> = _recognizedText.asStateFlow()

    private var isPaused = false

    fun startListening() {
        if (isPaused) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.Error("Speech recognition not available on this device.")
            return
        }
        destroyRecognizer()
        createRecognizer()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }
        speechRecognizer?.startListening(intent)
        _voiceState.value = VoiceState.Listening
        Timber.d("VoiceRecognition: started listening")
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _voiceState.value = VoiceState.Idle
    }

    fun pause() {
        isPaused = true
        speechRecognizer?.stopListening()
        destroyRecognizer()
        _voiceState.value = VoiceState.Paused
        Timber.d("VoiceRecognition: paused")
    }

    fun resume() {
        isPaused = false
        _voiceState.value = VoiceState.Idle
        Timber.d("VoiceRecognition: resumed")
    }

    fun stop() {
        isPaused = true
        speechRecognizer?.stopListening()
        destroyRecognizer()
        _voiceState.value = VoiceState.Idle
        Timber.d("VoiceRecognition: stopped")
    }

    fun destroy() {
        destroyRecognizer()
    }

    private fun createRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = VoiceState.Listening
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                _voiceState.value = VoiceState.Processing
            }
            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO            -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT           -> "Client-side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
                    SpeechRecognizer.ERROR_NETWORK          -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT  -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH         -> "No speech matched"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY  -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER           -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT   -> "No speech input"
                    else                                    -> "Unknown error ($error)"
                }
                Timber.w("SpeechRecognizer error: $msg")
                _voiceState.value = if (error == SpeechRecognizer.ERROR_NO_MATCH ||
                    error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    VoiceState.Idle
                } else {
                    VoiceState.Error(msg)
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    Timber.d("Speech recognized: $text")
                    _recognizedText.value = text
                    _recognizedText.value = null // Reset after consumption
                }
                _voiceState.value = VoiceState.Idle
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Timber.e(e, "Error destroying SpeechRecognizer")
        }
        speechRecognizer = null
    }
}
