package com.nova.assistant.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nova.assistant.BuildConfig
import com.nova.assistant.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(Constants.NETWORK_CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(Constants.NETWORK_READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(Constants.NETWORK_WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }
}
