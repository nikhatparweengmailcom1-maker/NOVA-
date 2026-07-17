package com.nova.assistant.di

import android.content.Context
import com.nova.assistant.features.apps.AppLaunchManager
import com.nova.assistant.features.calendar.CalendarManager
import com.nova.assistant.features.contacts.ContactsManager
import com.nova.assistant.features.flashlight.FlashlightManager
import com.nova.assistant.features.ocr.OCRManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides @Singleton
    fun provideFlashlightManager(@ApplicationContext ctx: Context): FlashlightManager =
        FlashlightManager(ctx)

    @Provides @Singleton
    fun provideAppLaunchManager(@ApplicationContext ctx: Context): AppLaunchManager =
        AppLaunchManager(ctx)

    @Provides @Singleton
    fun provideContactsManager(@ApplicationContext ctx: Context): ContactsManager =
        ContactsManager(ctx)

    @Provides @Singleton
    fun provideCalendarManager(@ApplicationContext ctx: Context): CalendarManager =
        CalendarManager(ctx)

    @Provides @Singleton
    fun provideOCRManager(): OCRManager = OCRManager()
}
