package com.nova.assistant.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nova.assistant.data.local.database.dao.TodoDao
import com.nova.assistant.data.local.database.entities.TodoEntity
import com.nova.assistant.domain.model.Priority
import com.nova.assistant.domain.model.Todo
import com.nova.assistant.domain.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao,
    private val gson: Gson
) : TodoRepository {

    override fun observeAllTodos(): Flow<List<Todo>> =
        todoDao.observeAll().map { it.map { e -> e.toDomain() } }

    override fun observeActiveTodos(): Flow<List<Todo>> =
        todoDao.observeActive().map { it.map { e -> e.toDomain() } }

    override fun observeCompletedTodos(): Flow<List<Todo>> =
        todoDao.observeCompleted().map { it.map { e -> e.toDomain() } }

    override fun observeTodosByPriority(priority: Priority): Flow<List<Todo>> =
        todoDao.observeByPriority(priority.name).map { it.map { e -> e.toDomain() } }

    override suspend fun getTodoById(id: String): Todo? =
        withContext(Dispatchers.IO) { todoDao.getById(id)?.toDomain() }

    override suspend fun createTodo(todo: Todo): Todo =
        withContext(Dispatchers.IO) {
            todoDao.insert(todo.toEntity())
            todo
        }

    override suspend fun updateTodo(todo: Todo) =
        withContext(Dispatchers.IO) { todoDao.update(todo.toEntity()) }

    override suspend fun deleteTodo(id: String) =
        withContext(Dispatchers.IO) { todoDao.deleteById(id) }

    override suspend fun toggleCompleted(id: String) =
        withContext(Dispatchers.IO) { todoDao.toggleCompleted(id, System.currentTimeMillis()) }

    override suspend fun clearCompletedTodos() =
        withContext(Dispatchers.IO) { todoDao.clearCompleted() }

    override suspend fun deleteAllTodos() =
        withContext(Dispatchers.IO) { todoDao.deleteAll() }

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun TodoEntity.toDomain(): Todo {
        val tagType = object : TypeToken<List<String>>() {}.type
        val tags: List<String> = try {
            gson.fromJson(tagsJson, tagType) ?: emptyList()
        } catch (_: Exception) { emptyList() }
        return Todo(
            id = id, title = title, description = description,
            isCompleted = isCompleted,
            priority = try { Priority.valueOf(priority) } catch (_: Exception) { Priority.MEDIUM },
            dueDate = dueDate, createdAt = createdAt, completedAt = completedAt, tags = tags
        )
    }

    private fun Todo.toEntity() = TodoEntity(
        id = id, title = title, description = description,
        isCompleted = isCompleted, priority = priority.name,
        dueDate = dueDate, createdAt = createdAt, completedAt = completedAt,
        tagsJson = gson.toJson(tags)
    )
}
