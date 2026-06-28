package com.example.movexa_android.domain.repository

import com.example.movexa_android.domain.model.TodayStats
import com.example.movexa_android.domain.model.DayActivity
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    fun getTodayStats(): Flow<TodayStats>
    fun getWeeklyActivity(): Flow<List<DayActivity>>
    suspend fun requestPermissions(): Boolean
    fun hasAllPermissions(): Flow<Boolean>
}