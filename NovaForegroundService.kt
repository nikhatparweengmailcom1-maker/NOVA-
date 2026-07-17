package com.nova.assistant.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import android.os.PowerManager
import com.nova.assistant.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * NOVA Foreground Service.
 *
 * Keeps NOVA alive while the screen is off or other apps are in the foreground.
 * Hosts:
 *  - Wake word detector ("Hi NOVA")
 *  - Voice recognition pipeline
 *  - TTS output
 *
 * Controlled by notification actions: Pause / Resume / Stop.
 */
@AndroidEntryPoint
class NovaForegroundService : Service() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var wakeWordDetector: WakeWordDetector
    @Inject lateinit var ttsManager: TextToSpeechManager

    private var wakeLock: PowerManager.WakeLock? = null
    private var isPaused = false

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, NovaForegroundService::class.java)
                .setAction(Constants.ACTION_START_SERVICE)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, NovaForegroundService::class.java)
                .setAction(Constants.ACTION_STOP_SERVICE)
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("NovaForegroundService: onCreate")
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Timber.d("NovaForegroundService: action=$action")

        when (action) {
            Constants.ACTION_STOP_SERVICE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            Constants.ACTION_PAUSE_SERVICE -> {
                isPaused = true
                wakeWordDetector.stop()
                ttsManager.stop()
                updateNotification()
                return START_STICKY
            }
            Constants.ACTION_RESUME_SERVICE -> {
                isPaused = false
                wakeWordDetector.start()
                updateNotification()
                return START_STICKY
            }
        }

        // START or any other command — ensure foreground and wake detector
        startForeground(Constants.NOTIF_SERVICE_ID, notificationHelper.buildServiceNotification())
        setupWakeWordDetector()
        wakeWordDetector.start()

        return START_STICKY
    }

    private fun setupWakeWordDetector() {
        wakeWordDetector.onWakePhrase = {
            if (!isPaused) {
                Timber.i("NOVA woken by wake phrase")
                // Broadcast wake event to MainActivity / ChatViewModel via intent
                val broadcastIntent = Intent("com.nova.assistant.WAKE_ACTIVATED")
                sendBroadcast(broadcastIntent)
                // Brief acknowledgement TTS
                ttsManager.speak("Yes?")
            }
        }
    }

    private fun updateNotification() {
        val notif = notificationHelper.buildServiceNotification(isPaused)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(Constants.NOTIF_SERVICE_ID, notif)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Nova::ServiceWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // Max 10 minutes, re-acquired as needed
    }

    override fun onDestroy() {
        Timber.d("NovaForegroundService: onDestroy")
        wakeWordDetector.stop()
        ttsManager.stop()
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
