package com.nova.assistant.data.local.database.dao

import androidx.room.*
import com.nova.assistant.data.local.database.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY triggerAtMillis ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 AND isCompleted = 0 ORDER BY triggerAtMillis ASC")
    fun observeActive(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReminderEntity)

    @Update
    suspend fun update(entity: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE reminders SET isCompleted = 1, isActive = 0 WHERE id = :id")
    suspend fun markCompleted(id: String)

    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun clearCompleted()

    @Query("SELECT * FROM reminders WHERE triggerAtMillis <= :now AND isCompleted = 0 AND isActive = 1")
    suspend fun getDueSince(now: Long): List<ReminderEntity>
}
