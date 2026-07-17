package com.nova.assistant.data.local.database.dao

import androidx.room.*
import com.nova.assistant.data.local.database.entities.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY priority DESC, createdAt DESC")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun observeActive(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun observeCompleted(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE priority = :priority AND isCompleted = 0")
    fun observeByPriority(priority: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: String): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TodoEntity)

    @Update
    suspend fun update(entity: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun clearCompleted()

    @Query("DELETE FROM todos")
    suspend fun deleteAll()

    @Query("UPDATE todos SET isCompleted = CASE WHEN isCompleted = 0 THEN 1 ELSE 0 END, completedAt = CASE WHEN isCompleted = 0 THEN :now ELSE NULL END WHERE id = :id")
    suspend fun toggleCompleted(id: String, now: Long)
}
