package com.example.movexa_android.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movexa_android.data.local.entity.WorkoutEntity
import com.example.movexa_android.data.repository.WorkoutRepository
import com.example.movexa_android.domain.model.ActivityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HistoryFilter { ALL, RUN, CYCLE, WALK, GYM }

data class HistoryUiState(
    val workouts: List<WorkoutEntity> = emptyList(),
    val weeklyDistanceKm: Float = 0f,
    val weeklyCount: Int = 0,
    val selectedFilter: HistoryFilter = HistoryFilter.ALL
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    private val _state = MutableStateFlow(HistoryUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _filter,
                repository.getAllWorkouts(),
                repository.getThisWeekDistance().map { it ?: 0f },
                repository.getThisWeekCount()
            ) { filter, all, weekDist, weekCount ->
                val filtered = when (filter) {
                    HistoryFilter.ALL  -> all
                    HistoryFilter.RUN  -> all.filter { it.type == ActivityType.RUN }
                    HistoryFilter.CYCLE -> all.filter { it.type == ActivityType.CYCLE }
                    HistoryFilter.WALK -> all.filter { it.type == ActivityType.WALK }
                    HistoryFilter.GYM  -> all.filter { it.type == ActivityType.GYM }
                }
                HistoryUiState(
                    workouts = filtered,
                    weeklyDistanceKm = weekDist / 1000f,
                    weeklyCount = weekCount,
                    selectedFilter = filter
                )
            }.collect { _state.value = it }
        }
    }

    fun setFilter(filter: HistoryFilter) { _filter.value = filter }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch { repository.deleteWorkout(workout) }
    }
}