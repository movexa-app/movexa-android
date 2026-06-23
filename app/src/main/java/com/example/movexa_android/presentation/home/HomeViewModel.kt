package com.example.movexa_android.presentation.home

import androidx.lifecycle.ViewModel
import com.example.movexa_android.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _stats = MutableStateFlow(
        TodayStats(steps = 6240, stepsGoal = 10000, caloriesBurned = 320, distanceKm = 4.2f, activeMinutes = 45)
    )
    val stats = _stats.asStateFlow()

    private val _weekly = MutableStateFlow(
        listOf(
            DayActivity("M", 5.4f), DayActivity("T", 0f),
            DayActivity("W", 8.1f), DayActivity("T", 3.2f),
            DayActivity("F", 6.7f), DayActivity("S", 0f),
            DayActivity("S", 4.2f, isToday = true)
        )
    )
    val weekly = _weekly.asStateFlow()

    private val _recent = MutableStateFlow(
        listOf(
            ActivitySummary(1L, ActivityType.RUN, 5.4f, 32, "5:55 /km", "Today"),
            ActivitySummary(2L, ActivityType.CYCLE, 18.2f, 54, "18 km/h", "Yesterday"),
            ActivitySummary(3L, ActivityType.WALK, 2.1f, 28, "13:20 /km", "Mon")
        )
    )
    val recent = _recent.asStateFlow()
}