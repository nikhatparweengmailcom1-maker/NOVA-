package com.nova.assistant.di

import android.content.Context
import androidx.room.Room
import com.nova.assistant.data.local.database.NovaDatabase
import com.nova.assistant.data.local.database.dao.ConversationDao
import com.nova.assistant.data.local.database.dao.MessageDao
import com.nova.assistant.data.local.database.dao.ReminderDao
import com.nova.assistant.data.local.database.dao.TodoDao
import com.nova.assistant.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNovaDatabase(@ApplicationContext context: Context): NovaDatabase =
        Room.databaseBuilder(context, NovaDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration() // Safe for v1
            .build()

    @Provides
    fun provideConversationDao(db: NovaDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: NovaDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideReminderDao(db: NovaDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideTodoDao(db: NovaDatabase): TodoDao = db.todoDao()
}
