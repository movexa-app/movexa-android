package com.example.movexa_android.presentation.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movexa_android.data.health.HealthConnectManager
import com.example.movexa_android.domain.model.ActivitySummary
import com.example.movexa_android.domain.model.DayActivity
import com.example.movexa_android.domain.model.TodayStats


@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartActivity: () -> Unit = {}
) {
    val stats by viewModel.stats.collectAsState()
    val weekly by viewModel.weekly.collectAsState()
    val recent by viewModel.recent.collectAsState()
    val hasPermissions by viewModel.hasPermissions.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted: Set<String> ->
        // Handle result if needed
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartActivity,
                text = { Text("Start Activity", fontWeight = FontWeight.SemiBold) },
                icon = {},
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item { HomeHeader("Alex") }
            if (!hasPermissions) {
                item {
                    PermissionCard {
                        permissionLauncher.launch(
                            setOf(
                                androidx.health.connect.client.permission.HealthPermission.getReadPermission(androidx.health.connect.client.records.StepsRecord::class),
                                androidx.health.connect.client.permission.HealthPermission.getReadPermission(androidx.health.connect.client.records.DistanceRecord::class),
                                androidx.health.connect.client.permission.HealthPermission.getReadPermission(androidx.health.connect.client.records.TotalCaloriesBurnedRecord::class),
                                androidx.health.connect.client.permission.HealthPermission.getReadPermission(androidx.health.connect.client.records.ExerciseSessionRecord::class)
                            )
                        )
                    }
                }
            }
            item { TodayGoalCard(stats) }
            item { QuickStatsRow(stats) }
            item { WeeklyChartCard(weekly) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    TextButton(onClick = {}) { Text("See All") }
                }
            }
            items(items = recent) { activity: ActivitySummary ->
                ActivityRow(activity)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Permission Card ──────────────────────────────────────────────────────────

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Health Connect Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Movexa needs access to your health data to track your progress automatically.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Grant Permissions", color = Color.White)
            }
        }
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Good Morning 👋",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Notifications, "Notifications")
            }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(name.firstOrNull()?.toString() ?: "",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ── Today Goal Card (circular progress + linear bar) ────────────────────────

@Composable
private fun TodayGoalCard(stats: TodayStats) {
    val animProgress by animateFloatAsState(
        targetValue = stats.stepsProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "steps"
    )
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {

            // Circular arc
            Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val stroke = 14.dp.toPx()
                    val inset = stroke / 2
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(track, -210f, 240f, false,
                        Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(primary, -210f, (240f * animProgress).coerceIn(0f, 240f), false,
                        Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👟", fontSize = 18.sp)
                    Text("${stats.steps / 1000}K",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge)
                    Text("steps", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(Modifier.weight(1f)) {
                Text("Today's Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("${stats.steps.formatWithComma()} / ${stats.stepsGoal.formatWithComma()} steps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                    color = primary,
                    trackColor = track
                )
                Spacer(Modifier.height(5.dp))
                Text("${(stats.stepsProgress * 100).toInt()}% of daily goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Quick Stats (calories · distance · time) ─────────────────────────────────

@Composable
private fun QuickStatsRow(stats: TodayStats) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatChip("🔥", "${stats.caloriesBurned}", "kcal", Modifier.weight(1f))
        StatChip("📍", "${"%.1f".format(stats.distanceKm)}", "km", Modifier.weight(1f))
        StatChip("⏱", "${stats.activeMinutes}", "min", Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(emoji: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium)
            Text(unit, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Weekly Bar Chart ─────────────────────────────────────────────────────────

@Composable
private fun WeeklyChartCard(weeklyData: List<DayActivity>) {
    val totalKm = weeklyData.sumOf { it.distanceKm.toDouble() }
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text("This Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${"%.1f".format(totalKm)} km total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            WeeklyBars(weeklyData)
        }
    }
}

@Composable
private fun WeeklyBars(data: List<DayActivity>) {
    val primary = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.surfaceVariant
    val maxKm = data.maxOfOrNull { it.distanceKm }?.takeIf { it > 0f } ?: 1f

    Row(Modifier.fillMaxWidth().height(100.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom) {
        data.forEach { day ->
            val barHeight = if (day.distanceKm > 0) (day.distanceKm / maxKm * 72).dp else 4.dp
            val color = when {
                day.isToday  -> primary
                day.distanceKm > 0 -> primary.copy(alpha = 0.45f)
                else -> inactive
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f).height(100.dp)
            ) {
                Box(Modifier.width(18.dp).height(barHeight)
                    .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                    .background(color))
                Spacer(Modifier.height(6.dp))
                Text(day.day,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isToday) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

// ── Recent Activity Row ───────────────────────────────────────────────────────

@Composable
private fun ActivityRow(activity: ActivitySummary) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) {
                Text(activity.type.emoji, fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(activity.type.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(activity.dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(activity.distanceKm)} km",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text(activity.pace,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Util ──────────────────────────────────────────────────────────────────────

private fun Int.formatWithComma(): String = "%,d".format(this)