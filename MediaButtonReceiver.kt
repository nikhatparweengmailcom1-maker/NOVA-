package com.nova.assistant.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import timber.log.Timber

/**
 * Handles hardware media button presses (headset buttons, etc.)
 * to pause/resume NOVA audio playback.
 */
class MediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MEDIA_BUTTON) return
        val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return
        if (keyEvent.action != KeyEvent.ACTION_DOWN) return
        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                Timber.d("MediaButton: pause/stop")
                val svc = Intent(context, NovaForegroundService::class.java)
                    .setAction(com.nova.assistant.util.Constants.ACTION_PAUSE_SERVICE)
                context.startService(svc)
            }
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                Timber.d("MediaButton: play/toggle")
                val svc = Intent(context, NovaForegroundService::class.java)
                    .setAction(com.nova.assistant.util.Constants.ACTION_RESUME_SERVICE)
                context.startService(svc)
            }
        }
    }
}
