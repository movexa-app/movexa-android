package com.example.movexa_android.presentation.activity

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movexa_android.data.repository.LocationRepository
import com.example.movexa_android.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.example.movexa_android.data.local.entity.WorkoutEntity
import com.example.movexa_android.data.repository.WorkoutRepository

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val locationRepo: LocationRepository,
    private val workoutRepo: WorkoutRepository
) : ViewModel() {

    private val _session = MutableStateFlow(ActivitySession())
    val session = _session.asStateFlow()

    private var timerJob: Job? = null
    private var locationJob: Job? = null
    private var lastLocation: Location? = null

    fun selectType(type: ActivityType) {
        if (_session.value.state == TrackingState.IDLE)
            _session.update { it.copy(type = type) }
    }

    fun startActivity() {
        _session.update { it.copy(state = TrackingState.ACTIVE) }
        startTimer()
        startLocationTracking()
    }

    fun pauseActivity() {
        timerJob?.cancel()
        locationJob?.cancel()
        lastLocation = null
        _session.update { it.copy(state = TrackingState.PAUSED) }
    }

    fun resumeActivity() {
        _session.update { it.copy(state = TrackingState.ACTIVE) }
        startTimer()
        startLocationTracking()
    }

    fun stopActivity() {
        timerJob?.cancel()
        locationJob?.cancel()
        val current = _session.value
        _session.update { it.copy(state = TrackingState.FINISHED) }

        viewModelScope.launch {
            workoutRepo.saveWorkout(
                WorkoutEntity(
                    type = current.type,
                    distanceMeters = current.distanceMeters,
                    durationSeconds = current.elapsedSeconds,
                    calories = current.calories,
                    avgPaceSecPerKm = current.currentPaceSecPerKm
                )
            )
        }
    }

    fun resetActivity() {
        timerJob?.cancel()
        locationJob?.cancel()
        lastLocation = null
        _session.value = ActivitySession()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _session.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun startLocationTracking() {
        locationJob = viewModelScope.launch {
            locationRepo.locationUpdates()
                .catch { /* permission not granted — handle in UI */ }
                .collect { location -> processLocation(location) }
        }
    }

    private fun processLocation(location: Location) {
        val prev = lastLocation
        val added = prev?.distanceTo(location) ?: 0f
        val current = _session.value

        val newDistance = current.distanceMeters + added
        val pace = if (newDistance > 50 && current.elapsedSeconds > 0)
            (current.elapsedSeconds.toFloat() / (newDistance / 1000f)).toInt() else 0
        val calories = ((current.elapsedSeconds / 3600f) * 8f * 70f).toInt()

        _session.update {
            it.copy(
                distanceMeters = newDistance,
                currentPaceSecPerKm = pace,
                calories = calories,
                routePoints = it.routePoints + location
            )
        }
        lastLocation = location
    }
}