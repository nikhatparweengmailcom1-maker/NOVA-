package com.nova.assistant

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * NOVA Application class.
 * Initializes Hilt DI, Timber logging, and WorkManager.
 */
@HiltAndroidApp
class NovaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.work.WorkerFactory

    override fun onCreate() {
        super.onCreate()
        initLogging()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR
            )
            .build()

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        Timber.i("NOVA Application started — v${BuildConfig.VERSION_NAME}")
    }

    /** Production logging tree — only logs warnings and errors. */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority < android.util.Log.WARN) return
            // In production you would route to a crash reporter (e.g. Firebase Crashlytics).
            // For now we silently drop debug/info logs.
            if (t != null) {
                android.util.Log.e(tag ?: "NOVA", message, t)
            }
        }
    }
}
