package com.nova.assistant.di

import com.nova.assistant.data.repository.ChatRepositoryImpl
import com.nova.assistant.data.repository.ReminderRepositoryImpl
import com.nova.assistant.data.repository.TodoRepositoryImpl
import com.nova.assistant.domain.repository.ChatRepository
import com.nova.assistant.domain.repository.ReminderRepository
import com.nova.assistant.domain.repository.TodoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository
}
