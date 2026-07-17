package com.nova.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nova.assistant.domain.model.Priority
import com.nova.assistant.domain.model.Todo
import com.nova.assistant.domain.usecase.ManageTodosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodoUiState(
    val activeTodos: List<Todo> = emptyList(),
    val completedTodos: List<Todo> = emptyList(),
    val showCompleted: Boolean = false,
    val showAddDialog: Boolean = false,
    val lastDeletedTodo: Todo? = null,
    val error: String? = null
)

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val manageTodosUseCase: ManageTodosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        manageTodosUseCase.observeActive()
            .onEach { todos -> _uiState.update { it.copy(activeTodos = todos) } }
            .launchIn(viewModelScope)

        manageTodosUseCase.observeCompleted()
            .onEach { todos -> _uiState.update { it.copy(completedTodos = todos) } }
            .launchIn(viewModelScope)
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun toggleShowCompleted() = _uiState.update { it.copy(showCompleted = !it.showCompleted) }

    fun addTodo(title: String, priority: Priority = Priority.MEDIUM, dueDate: Long? = null) {
        viewModelScope.launch {
            try {
                manageTodosUseCase.addTodo(title, priority = priority, dueDate = dueDate)
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleCompleted(id: String) {
        viewModelScope.launch { manageTodosUseCase.toggleCompleted(id) }
    }

    fun delete(todo: Todo) {
        viewModelScope.launch {
            _uiState.update { it.copy(lastDeletedTodo = todo) }
            manageTodosUseCase.delete(todo.id)
        }
    }

    fun undoDelete() {
        val todo = _uiState.value.lastDeletedTodo ?: return
        viewModelScope.launch {
            manageTodosUseCase.addTodo(
                todo.title, todo.description, todo.priority, todo.dueDate, todo.tags
            )
            _uiState.update { it.copy(lastDeletedTodo = null) }
        }
    }

    fun clearCompleted() {
        viewModelScope.launch { manageTodosUseCase.clearCompleted() }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }
}
