package com.window.app.di

import android.content.Context
import com.window.app.data.ai.GeminiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGeminiRepository(
        @ApplicationContext context: Context
    ): GeminiRepository = GeminiRepository(context)
}

