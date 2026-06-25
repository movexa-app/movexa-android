package com.example.movexa_android.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movexa_android.data.local.entity.WorkoutEntity
import com.example.movexa_android.domain.model.ActivityType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text("History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Text("Your past workouts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // ── Weekly Summary ─────────────────────────────────────────────────
        WeeklySummaryCard(
            distanceKm = state.weeklyDistanceKm,
            workoutCount = state.weeklyCount
        )

        Spacer(Modifier.height(12.dp))

        // ── Filter Chips ───────────────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(HistoryFilter.entries) { filter ->
                FilterChip(
                    selected = state.selectedFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = {
                        Text(
                            when (filter) {
                                HistoryFilter.ALL   -> "All"
                                HistoryFilter.RUN   -> "🏃 Run"
                                HistoryFilter.CYCLE -> "🚴 Cycle"
                                HistoryFilter.WALK  -> "🚶 Walk"
                                HistoryFilter.GYM   -> "🏋️ Gym"
                            },
                            fontWeight = if (state.selectedFilter == filter)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Workout List ───────────────────────────────────────────────────
        if (state.workouts.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = state.workouts,
                    key = { it.id }
                ) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onDelete = { viewModel.deleteWorkout(workout) }
                    )
                }
            }
        }
    }
}

// ── Weekly Summary ────────────────────────────────────────────────────────────

@Composable
private fun WeeklySummaryCard(distanceKm: Float, workoutCount: Int) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("This Week", "${"%.1f".format(distanceKm)}", "km")
            Box(
                Modifier
                    .height(48.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            )
            SummaryItem("Workouts", "$workoutCount", "sessions")
            Box(
                Modifier
                    .height(48.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            )
            SummaryItem("Avg Dist", if (workoutCount > 0)
                "${"%.1f".format(distanceKm / workoutCount)}" else "0.0", "km/session")
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

// ── Workout Card ──────────────────────────────────────────────────────────────

@Composable
private fun WorkoutCard(
    workout: WorkoutEntity,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Workout") },
            text = { Text("Are you sure you want to delete this workout?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(workout.type.emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.width(14.dp))

            // Main info
            Column(Modifier.weight(1f)) {
                Text(workout.type.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                Text(workout.timestamp.toDateString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStat("📍", "${"%.2f".format(workout.distanceMeters / 1000f)} km")
                    MiniStat("⏱", workout.durationSeconds.toTimeString())
                    MiniStat("🔥", "${workout.calories} kcal")
                }
            }

            // Delete button
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MiniStat(emoji: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(emoji, fontSize = 12.sp)
        Text(value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏃", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text("No workouts yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Start your first activity!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Utils ─────────────────────────────────────────────────────────────────────

private fun Long.toDateString(): String =
    SimpleDateFormat("EEE, dd MMM · HH:mm", Locale.getDefault()).format(Date(this))

private fun Long.toTimeString(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}