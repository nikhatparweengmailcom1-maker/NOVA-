package com.nova.assistant.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nova.assistant.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.APP_PREFS_NAME
)

/**
 * Typed DataStore wrapper for all NOVA user preferences.
 */
@Singleton
class NovaPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.dataStore

    // ── Keys ──────────────────────────────────────────────────────────────
    private object Keys {
        val AI_PROVIDER          = stringPreferencesKey(Constants.PREF_AI_PROVIDER)
        val WAKE_PHRASE_ENABLED  = booleanPreferencesKey(Constants.PREF_WAKE_PHRASE_ENABLED)
        val TTS_SPEED            = floatPreferencesKey(Constants.PREF_TTS_SPEED)
        val TTS_PITCH            = floatPreferencesKey(Constants.PREF_TTS_PITCH)
        val DARK_MODE            = booleanPreferencesKey(Constants.PREF_DARK_MODE)
        val NOTIFICATIONS        = booleanPreferencesKey(Constants.PREF_NOTIFICATIONS)
        val LANGUAGE             = stringPreferencesKey(Constants.PREF_LANGUAGE)
    }

    // ── Flows ─────────────────────────────────────────────────────────────
    val aiProvider: Flow<String> = store.data
        .catch { e -> Timber.e(e, "NovaPreferences: read error"); emit(emptyPreferences()) }
        .map { it[Keys.AI_PROVIDER] ?: Constants.DEFAULT_PROVIDER }

    val wakePhraseEnabled: Flow<Boolean> = store.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.WAKE_PHRASE_ENABLED] ?: true }

    val ttsSpeed: Flow<Float> = store.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.TTS_SPEED] ?: 1.0f }

    val ttsPitch: Flow<Float> = store.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.TTS_PITCH] ?: 1.0f }

    val darkMode: Flow<Boolean> = store.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.DARK_MODE] ?: true }

    val notificationsEnabled: Flow<Boolean> = store.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.NOTIFICATIONS] ?: true }

    val language: Flow<String> = store.data
        .catch { emit(emptyPreferences()) }
        .map { it[Keys.LANGUAGE] ?: "en" }

    // ── Setters ───────────────────────────────────────────────────────────
    suspend fun setAiProvider(provider: String) =
        store.edit { it[Keys.AI_PROVIDER] = provider }

    suspend fun setWakePhraseEnabled(enabled: Boolean) =
        store.edit { it[Keys.WAKE_PHRASE_ENABLED] = enabled }

    suspend fun setTtsSpeed(speed: Float) =
        store.edit { it[Keys.TTS_SPEED] = speed.coerceIn(0.1f, 4.0f) }

    suspend fun setTtsPitch(pitch: Float) =
        store.edit { it[Keys.TTS_PITCH] = pitch.coerceIn(0.1f, 2.0f) }

    suspend fun setDarkMode(enabled: Boolean) =
        store.edit { it[Keys.DARK_MODE] = enabled }

    suspend fun setNotificationsEnabled(enabled: Boolean) =
        store.edit { it[Keys.NOTIFICATIONS] = enabled }

    suspend fun setLanguage(lang: String) =
        store.edit { it[Keys.LANGUAGE] = lang }
}
