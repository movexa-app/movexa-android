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

    init {
        startLocationTracking()
    }

    fun selectType(type: ActivityType) {
        if (_session.value.state == TrackingState.IDLE)
            _session.update { it.copy(type = type) }
    }

    fun startActivity() {
        val currentLoc = lastLocation
        _session.update { 
            it.copy(
                state = TrackingState.ACTIVE,
                startPoint = currentLoc,
                routePoints = if (currentLoc != null) listOf(currentLoc) else emptyList()
            ) 
        }
        startTimer()
    }

    fun setDestination(location: Location) {
        if (_session.value.state == TrackingState.IDLE || _session.value.state == TrackingState.ACTIVE) {
            _session.update { it.copy(destination = location) }
        }
    }

    fun clearDestination() {
        _session.update { it.copy(destination = null) }
    }

    fun toggleFollowMode() {
        _session.update { it.copy(isFollowMode = !it.isFollowMode) }
    }

    fun cycleMapType() {
        _session.update {
            val next = when (it.mapType) {
                ActivitySession.MapType.NORMAL -> ActivitySession.MapType.SATELLITE
                ActivitySession.MapType.SATELLITE -> ActivitySession.MapType.TERRAIN
                ActivitySession.MapType.TERRAIN -> ActivitySession.MapType.NORMAL
            }
            it.copy(mapType = next)
        }
    }

    fun pauseActivity() {
        timerJob?.cancel()
        _session.update { it.copy(state = TrackingState.PAUSED) }
    }

    fun resumeActivity() {
        _session.update { it.copy(state = TrackingState.ACTIVE) }
        startTimer()
    }

    fun stopActivity() {
        timerJob?.cancel()
        val current = _session.value
        _session.update { it.copy(state = TrackingState.FINISHED) }
// ...
// keep rest same

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
        val current = _session.value
        val isTracking = current.state == TrackingState.ACTIVE
        
        val prev = lastLocation
        // Only count distance if accuracy is good (less than 20 meters)
        val added = if (isTracking && location.accuracy < 20f) (prev?.distanceTo(location) ?: 0f) else 0f

        val newDistance = current.distanceMeters + added
        val pace = if (isTracking && newDistance > 50 && current.elapsedSeconds > 0)
            (current.elapsedSeconds.toFloat() / (newDistance / 1000f)).toInt() else current.currentPaceSecPerKm
        
        // Dynamic Calories based on ActivityType
        val metValue = when(current.type) {
            ActivityType.RUN -> 10f
            ActivityType.CYCLE -> 8f
            ActivityType.WALK -> 3.5f
            ActivityType.SWIM -> 7f
            ActivityType.GYM -> 5f
        }
        val weightKg = 70f // Default weight
        val calories = if (isTracking) ((current.elapsedSeconds / 3600f) * metValue * weightKg).toInt() else current.calories

        val newBearing = if (location.hasBearing()) location.bearing else current.bearing

        _session.update {
            it.copy(
                currentLocation = location,
                bearing = newBearing,
                distanceMeters = newDistance,
                currentPaceSecPerKm = pace,
                calories = calories,
                routePoints = if (isTracking && added > 0.5f) it.routePoints + location else it.routePoints
            )
        }
        lastLocation = location
    }
}
