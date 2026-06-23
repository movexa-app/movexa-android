package com.example.movexa_android.domain.model

import android.location.Location

enum class TrackingState { IDLE, ACTIVE, PAUSED, FINISHED }

data class ActivitySession(
    val type: ActivityType = ActivityType.RUN,
    val state: TrackingState = TrackingState.IDLE,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Float = 0f,
    val currentPaceSecPerKm: Int = 0,
    val calories: Int = 0,
    val routePoints: List<Location> = emptyList()
) {
    val distanceKm get() = distanceMeters / 1000f
    val paceFormatted
        get() = if (currentPaceSecPerKm <= 0) "--:--"
        else "%d:%02d".format(currentPaceSecPerKm / 60, currentPaceSecPerKm % 60)
    val elapsedFormatted
        get() = "%02d:%02d:%02d".format(
            elapsedSeconds / 3600,
            (elapsedSeconds % 3600) / 60,
            elapsedSeconds % 60
        )
}