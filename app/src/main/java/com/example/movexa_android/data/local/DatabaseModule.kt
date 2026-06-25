package com.example.movexa_android.data.local

import android.content.Context
import androidx.room.Room
import com.example.movexa_android.data.local.dao.WorkoutDao
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
    fun provideDatabase(@ApplicationContext context: Context): MovexaDatabase =
        Room.databaseBuilder(
            context,
            MovexaDatabase::class.java,
            MovexaDatabase.DATABASE_NAME
        ).build()

    @Provides
    fun provideWorkoutDao(db: MovexaDatabase): WorkoutDao = db.workoutDao()
}