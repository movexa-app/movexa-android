package com.example.movexa_android.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movexa_android.domain.model.*
import com.example.movexa_android.domain.repository.AuthRepository
import com.example.movexa_android.domain.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val userName: StateFlow<String> = authRepository.getSession()
        .map { it?.name ?: "User" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "User"
        )

    val stats: StateFlow<TodayStats> = healthRepository.getTodayStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TodayStats(0, 10000, 0, 0f, 0)
        )

    val hasPermissions: StateFlow<Boolean> = healthRepository.hasAllPermissions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val weekly: StateFlow<List<DayActivity>> = healthRepository.getWeeklyActivity()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Recent activities can still be mock or fetched from WorkoutRepository
    val recent = MutableStateFlow(
        listOf(
            ActivitySummary(1L, ActivityType.RUN, 5.4f, 32, "5:55 /km", "Today"),
            ActivitySummary(2L, ActivityType.CYCLE, 18.2f, 54, "18 km/h", "Yesterday"),
            ActivitySummary(3L, ActivityType.WALK, 2.1f, 28, "13:20 /km", "Mon")
        )
    )
}
