package com.nova.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.assistant.domain.model.*
import com.nova.assistant.domain.usecase.GetConversationUseCase
import com.nova.assistant.domain.usecase.ManageRemindersUseCase
import com.nova.assistant.domain.usecase.ManageTodosUseCase
import com.nova.assistant.domain.usecase.ProcessVoiceCommandUseCase
import com.nova.assistant.domain.usecase.SendMessageUseCase
import com.nova.assistant.features.apps.AppLaunchManager
import com.nova.assistant.features.contacts.ContactsManager
import com.nova.assistant.features.flashlight.FlashlightManager
import com.nova.assistant.service.TextToSpeechManager
import com.nova.assistant.service.VoiceRecognitionManager
import com.nova.assistant.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val voiceState: VoiceState = VoiceState.Idle,
    val conversationId: String = "",
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPermissionDialog: Boolean = false,
    val permissionNeeded: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val processVoiceCommandUseCase: ProcessVoiceCommandUseCase,
    private val manageRemindersUseCase: ManageRemindersUseCase,
    private val manageTodosUseCase: ManageTodosUseCase,
    private val ttsManager: TextToSpeechManager,
    private val voiceRecognitionManager: VoiceRecognitionManager,
    private val flashlightManager: FlashlightManager,
    private val appLaunchManager: AppLaunchManager,
    private val contactsManager: ContactsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val voiceState: StateFlow<VoiceState> get() = voiceRecognitionManager.voiceState

    init {
        initConversation()
        observeVoiceState()
    }

    private fun initConversation() {
        viewModelScope.launch {
            val conversation = getConversationUseCase.getOrCreateActive()
            _uiState.update { it.copy(conversationId = conversation.id) }
            observeMessages(conversation.id)
        }
    }

    private fun observeMessages(conversationId: String) {
        getConversationUseCase.observeMessages(conversationId)
            .onEach { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeVoiceState() {
        voiceRecognitionManager.voiceState
            .onEach { state -> _uiState.update { it.copy(voiceState = state) } }
            .launchIn(viewModelScope)

        voiceRecognitionManager.recognizedText
            .filterNotNull()
            .onEach { text -> handleRecognizedSpeech(text) }
            .launchIn(viewModelScope)
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendTextMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return
        _uiState.update { it.copy(inputText = "", isLoading = true) }
        sendMessage(text, isVoice = false)
    }

    fun startVoiceInput() {
        voiceRecognitionManager.startListening()
    }

    fun stopVoiceInput() {
        voiceRecognitionManager.stopListening()
    }

    fun speakText(text: String) {
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun clearConversation() {
        viewModelScope.launch {
            getConversationUseCase.clearAll()
            initConversation()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handleRecognizedSpeech(text: String) {
        val command = processVoiceCommandUseCase(text)
        Timber.d("Voice command: ${command.intent} — raw: $text")
        viewModelScope.launch {
            when (command.intent) {
                VoiceIntent.STOP  -> voiceRecognitionManager.stop()
                VoiceIntent.PAUSE -> voiceRecognitionManager.pause()
                VoiceIntent.RESUME -> voiceRecognitionManager.resume()
                VoiceIntent.WAKE  -> { /* Wake already handled by service */ }
                VoiceIntent.FLASHLIGHT_ON  -> flashlightManager.turnOn()
                VoiceIntent.FLASHLIGHT_OFF -> flashlightManager.turnOff()
                VoiceIntent.VOLUME_UP    -> { /* Handled by MediaControlService */ }
                VoiceIntent.VOLUME_DOWN  -> { /* Handled by MediaControlService */ }
                VoiceIntent.OPEN_APP -> {
                    val appName = command.extras["app"] ?: ""
                    if (!appLaunchManager.launchApp(appName)) {
                        speakText("I couldn't find the app \"$appName\".")
                    }
                }
                VoiceIntent.CALL -> {
                    val contact = command.extras["contact"] ?: ""
                    val phone = contactsManager.findPhoneNumber(contact)
                    if (phone != null) {
                        contactsManager.initiateCall(phone)
                    } else {
                        speakText("I couldn't find a contact named \"$contact\".")
                    }
                }
                VoiceIntent.SET_REMINDER -> {
                    val raw = command.extras["raw"] ?: text
                    val time = DateTimeUtil.parseNaturalDatePhrase(raw)
                    if (time != null) {
                        manageRemindersUseCase.createReminder(
                            title = raw.take(50),
                            triggerAtMillis = time
                        )
                        speakText("Reminder set!")
                    } else {
                        sendMessage(text, isVoice = true)
                    }
                }
                VoiceIntent.SET_TIMER -> {
                    val raw = command.extras["raw"] ?: text
                    val duration = DateTimeUtil.parseDurationPhrase(raw)
                    if (duration != null) {
                        manageRemindersUseCase.createTimer("Timer", duration)
                        speakText("Timer started!")
                    } else {
                        sendMessage(text, isVoice = true)
                    }
                }
                VoiceIntent.ADD_TODO -> {
                    val task = command.extras["task"] ?: text
                    manageTodosUseCase.addTodo(task)
                    speakText("Task added to your to-do list.")
                }
                else -> sendMessage(text, isVoice = true)
            }
        }
    }

    private fun sendMessage(text: String, isVoice: Boolean) {
        val conversationId = _uiState.value.conversationId
        if (conversationId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val response = sendMessageUseCase(conversationId, text, isVoice)) {
                is AIResponse.Success -> {
                    if (isVoice) ttsManager.speak(response.text)
                }
                is AIResponse.Error -> {
                    _uiState.update { it.copy(error = response.message) }
                    if (isVoice) ttsManager.speak(response.message)
                }
                else -> Unit
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
