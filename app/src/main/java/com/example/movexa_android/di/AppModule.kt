package com.example.movexa_android.di

import android.content.Context
import com.example.movexa_android.data.health.HealthConnectManager
import com.example.movexa_android.data.local.UserPreferences
import com.example.movexa_android.data.local.dao.WorkoutDao
import com.example.movexa_android.data.repository.AuthRepositoryImpl
import com.example.movexa_android.data.repository.HealthRepositoryImpl
import com.example.movexa_android.data.repository.WorkoutRepository
import com.example.movexa_android.domain.repository.AuthRepository
import com.example.movexa_android.domain.repository.HealthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(userPreferences: UserPreferences): AuthRepository {
        return AuthRepositoryImpl(userPreferences)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(workoutDao: WorkoutDao): WorkoutRepository {
        return WorkoutRepository(workoutDao)
    }

    @Provides
    @Singleton
    fun provideHealthConnectManager(@ApplicationContext context: Context): HealthConnectManager {
        return HealthConnectManager(context)
    }

    @Provides
    @Singleton
    fun provideHealthRepository(
        workoutRepository: WorkoutRepository,
        healthConnectManager: HealthConnectManager
    ): HealthRepository {
        return HealthRepositoryImpl(workoutRepository, healthConnectManager)
    }
}