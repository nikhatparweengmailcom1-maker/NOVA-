package com.nova.assistant.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.nova.assistant.data.local.preferences.NovaPreferences
import com.nova.assistant.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Continuously listens for the wake phrase "Hi NOVA" in the background.
 * Uses short listening bursts with intervals to preserve battery.
 *
 * Note: Continuous background listening is constrained by Android's
 * power management. This runs within the foreground service context.
 */
@Singleton
class WakeWordDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: NovaPreferences
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isActive = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Callback invoked when wake phrase is detected. */
    var onWakePhrase: (() -> Unit)? = null

    fun start() {
        if (isActive) return
        isActive = true
        listenCycle()
        Timber.d("WakeWordDetector started")
    }

    fun stop() {
        isActive = false
        destroyRecognizer()
        Timber.d("WakeWordDetector stopped")
    }

    private fun listenCycle() {
        if (!isActive) return
        scope.launch {
            val enabled = preferences.wakePhraseEnabled.first()
            if (!enabled || !SpeechRecognizer.isRecognitionAvailable(context)) {
                delay(Constants.WAKE_LISTEN_INTERVAL_MS * 5)
                if (isActive) listenCycle()
                return@launch
            }
            startListeningBurst()
        }
    }

    private fun startListeningBurst() {
        if (!isActive) return
        destroyRecognizer()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?: return
                for (match in matches) {
                    if (match.trim().lowercase().contains(Constants.WAKE_PHRASE)) {
                        Timber.d("Wake phrase detected: '$match'")
                        onWakePhrase?.invoke()
                        break
                    }
                }
                scheduleNextBurst()
            }

            override fun onError(error: Int) {
                Timber.v("WakeWordDetector error: $error (normal during idle listening)")
                scheduleNextBurst()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Check partial results for immediate response
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return
                if (partial.trim().lowercase().contains(Constants.WAKE_PHRASE)) {
                    Timber.d("Wake phrase detected in partial: '$partial'")
                    onWakePhrase?.invoke()
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Timber.e(e, "WakeWordDetector: error starting listening")
            scheduleNextBurst()
        }
    }

    private fun scheduleNextBurst() {
        if (!isActive) return
        scope.launch {
            delay(Constants.WAKE_LISTEN_INTERVAL_MS)
            if (isActive) listenCycle()
        }
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Timber.e(e, "WakeWordDetector: error destroying recognizer")
        }
        speechRecognizer = null
    }
}
