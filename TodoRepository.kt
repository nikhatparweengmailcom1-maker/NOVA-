package com.nova.assistant.domain.repository

import com.nova.assistant.domain.model.Priority
import com.nova.assistant.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeAll(): Flow<List<Todo>>
    fun observeActive(): Flow<List<Todo>>
    fun observeCompleted(): Flow<List<Todo>>
    suspend fun createTodo(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        dueDate: Long? = null,
        tags: List<String> = emptyList()
    ): Todo
    suspend fun update(todo: Todo)
    suspend fun toggleCompleted(id: String)
    suspend fun delete(id: String)
    suspend fun clearCompleted()
    suspend fun getById(id: String): Todo?
}
