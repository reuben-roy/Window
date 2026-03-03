package com.window.app.di

import android.content.Context
import androidx.room.Room
import com.window.app.data.db.AppUsageSessionDao
import com.window.app.data.db.WindowContentEventDao
import com.window.app.data.db.WindowDatabase
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
    fun provideWindowDatabase(
        @ApplicationContext context: Context
    ): WindowDatabase = Room.databaseBuilder(
        context,
        WindowDatabase::class.java,
        WindowDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()   // Replace with proper migrations in production
        .build()

    @Provides
    @Singleton
    fun provideAppUsageSessionDao(db: WindowDatabase): AppUsageSessionDao =
        db.appUsageSessionDao()

    @Provides
    @Singleton
    fun provideWindowContentEventDao(db: WindowDatabase): WindowContentEventDao =
        db.windowContentEventDao()
}

