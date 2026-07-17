package com.nova.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.assistant.domain.model.Reminder
import com.nova.assistant.domain.model.RepeatMode
import com.nova.assistant.domain.usecase.ManageRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val manageRemindersUseCase: ManageRemindersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        manageRemindersUseCase.observeAll()
            .onEach { reminders -> _uiState.update { it.copy(reminders = reminders) } }
            .launchIn(viewModelScope)
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }

    fun createReminder(
        title: String,
        triggerAtMillis: Long,
        repeatMode: RepeatMode = RepeatMode.NONE
    ) {
        viewModelScope.launch {
            try {
                manageRemindersUseCase.createReminder(title, triggerAtMillis, repeatMode = repeatMode)
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to create reminder: ${e.message}") }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try { manageRemindersUseCase.delete(id) }
            catch (e: Exception) { _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun markCompleted(id: String) {
        viewModelScope.launch { manageRemindersUseCase.markCompleted(id) }
    }

    fun clearCompleted() {
        viewModelScope.launch { manageRemindersUseCase.clearCompleted() }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }
}
