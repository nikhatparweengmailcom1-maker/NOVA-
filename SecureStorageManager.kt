package com.nova.assistant.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure key-value storage backed by EncryptedSharedPreferences.
 * Used to store API keys and other sensitive values on-device.
 */
@Singleton
class SecureStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREFS_FILE = "nova_secure_prefs"
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "SecureStorageManager: falling back to plaintext prefs")
            context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        }
    }

    // ── Generic operations ────────────────────────────────────────────────

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String = ""): String =
        prefs.getString(key, default) ?: default

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun contains(key: String): Boolean = prefs.contains(key)

    fun clear() {
        prefs.edit().clear().apply()
    }

    // ── API key helpers ───────────────────────────────────────────────────

    /**
     * Save an API key for the given [provider] ("openai" | "gemini").
     */
    fun saveApiKey(provider: String, apiKey: String) {
        putString("${provider}_api_key", apiKey)
        Timber.d("SecureStorageManager: saved API key for $provider")
    }

    /**
     * Retrieve the API key for the given [provider].
     * Returns null if not set.
     */
    fun getApiKey(provider: String): String? {
        val key = getString("${provider}_api_key", "")
        return if (key.isBlank()) null else key
    }

    /**
     * Returns true if a non-empty API key is stored for [provider].
     */
    fun hasApiKey(provider: String): Boolean = getApiKey(provider) != null

    /**
     * Clear the API key for [provider].
     */
    fun clearApiKey(provider: String) {
        remove("${provider}_api_key")
    }
}
