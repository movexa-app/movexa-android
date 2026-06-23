package com.example.movexa_android.domain.model

data class TodayStats(
    val steps: Int = 6_240,
    val stepsGoal: Int = 10_000,
    val caloriesBurned: Int = 320,
    val distanceKm: Float = 4.2f,
    val activeMinutes: Int = 45
) {
    val stepsProgress get() = steps.toFloat() / stepsGoal.toFloat()
}

data class ActivitySummary(
    val id: Long,
    val type: ActivityType,
    val distanceKm: Float,
    val durationMinutes: Int,
    val pace: String,
    val dateLabel: String
)

enum class ActivityType(val label: String, val emoji: String) {
    RUN("Run", "🏃"), CYCLE("Cycle", "🚴"), WALK("Walk", "🚶"),
    SWIM("Swim", "🏊"), GYM("Gym", "🏋️")
}

data class DayActivity(
    val day: String,
    val distanceKm: Float,
    val isToday: Boolean = false
)