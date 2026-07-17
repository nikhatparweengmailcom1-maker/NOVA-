package com.nova.assistant.di

import com.google.gson.Gson
import com.nova.assistant.data.remote.ai.GeminiProvider
import com.nova.assistant.data.remote.ai.NovaAIManager
import com.nova.assistant.data.remote.ai.OpenAIProvider
import com.nova.assistant.data.local.preferences.NovaPreferences
import com.nova.assistant.util.SecureStorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideOpenAIProvider(
        httpClient: OkHttpClient,
        gson: Gson
    ): OpenAIProvider = OpenAIProvider(httpClient, gson)

    @Provides
    @Singleton
    fun provideGeminiProvider(
        httpClient: OkHttpClient,
        gson: Gson
    ): GeminiProvider = GeminiProvider(httpClient, gson)

    @Provides
    @Singleton
    fun provideNovaAIManager(
        openAIProvider: OpenAIProvider,
        geminiProvider: GeminiProvider,
        preferences: NovaPreferences,
        secureStorage: SecureStorageManager
    ): NovaAIManager = NovaAIManager(openAIProvider, geminiProvider, preferences, secureStorage)
}
