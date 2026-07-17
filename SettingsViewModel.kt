package com.nova.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.assistant.data.local.preferences.NovaPreferences
import com.nova.assistant.util.SecureStorageManager
import com.nova.assistant.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val aiProvider: String = Constants.DEFAULT_PROVIDER,
    val apiKey: String = "",
    val wakePhraseEnabled: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val darkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val language: String = "en",
    val savedMessage: String? = null,
    val hasApiKey: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: NovaPreferences,
    private val secureStorage: SecureStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferences.aiProvider,
                preferences.wakePhraseEnabled,
                preferences.ttsSpeed,
                preferences.ttsPitch,
                preferences.notificationsEnabled
            ) { provider, wake, speed, pitch, notifs ->
                val hasKey = secureStorage.hasApiKey(provider)
                _uiState.update {
                    it.copy(
                        aiProvider = provider,
                        wakePhraseEnabled = wake,
                        ttsSpeed = speed,
                        ttsPitch = pitch,
                        notificationsEnabled = notifs,
                        hasApiKey = hasKey
                    )
                }
            }.launchIn(this)

            preferences.darkMode.collect { dark ->
                _uiState.update { it.copy(darkMode = dark) }
            }
        }
    }

    fun setAiProvider(provider: String) {
        viewModelScope.launch {
            preferences.setAiProvider(provider)
            val hasKey = secureStorage.hasApiKey(provider)
            _uiState.update { it.copy(aiProvider = provider, hasApiKey = hasKey, apiKey = "") }
        }
    }

    fun onApiKeyChanged(key: String) {
        _uiState.update { it.copy(apiKey = key) }
    }

    fun saveApiKey() {
        val provider = _uiState.value.aiProvider
        val key = _uiState.value.apiKey.trim()
        if (key.isBlank()) return
        secureStorage.saveApiKey(provider, key)
        _uiState.update { it.copy(apiKey = "", hasApiKey = true, savedMessage = "API key saved securely") }
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(savedMessage = null) }
        }
    }

    fun setWakePhraseEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setWakePhraseEnabled(enabled)
        }
    }

    fun setTtsSpeed(speed: Float) {
        viewModelScope.launch {
            preferences.setTtsSpeed(speed)
        }
    }

    fun setTtsPitch(pitch: Float) {
        viewModelScope.launch {
            preferences.setTtsPitch(pitch)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setNotificationsEnabled(enabled)
        }
    }

    fun clearApiKey() {
        val provider = _uiState.value.aiProvider
        secureStorage.remove("${provider}_api_key")
        _uiState.update { it.copy(hasApiKey = false, apiKey = "") }
    }
}
