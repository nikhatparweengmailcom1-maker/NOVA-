package com.nova.assistant.domain.usecase

import com.nova.assistant.domain.model.Priority
import com.nova.assistant.domain.model.Todo
import com.nova.assistant.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for all to-do list management.
 */
class ManageTodosUseCase @Inject constructor(
    private val repository: TodoRepository
) {
    fun observeAll(): Flow<List<Todo>> = repository.observeAll()

    fun observeActive(): Flow<List<Todo>> = repository.observeActive()

    fun observeCompleted(): Flow<List<Todo>> = repository.observeCompleted()

    suspend fun addTodo(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        dueDate: Long? = null,
        tags: List<String> = emptyList()
    ) = repository.createTodo(title, description, priority, dueDate, tags)

    suspend fun toggleCompleted(id: String) = repository.toggleCompleted(id)

    suspend fun delete(id: String) = repository.delete(id)

    suspend fun clearCompleted() = repository.clearCompleted()

    suspend fun update(todo: Todo) = repository.update(todo)

    suspend fun getById(id: String): Todo? = repository.getById(id)
}
