package com.example.movexa_android.presentation.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.movexa_android.domain.model.ActivitySummary
import com.example.movexa_android.domain.model.DayActivity
import com.example.movexa_android.domain.model.TodayStats
import kotlin.math.min

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateToVitality: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSeeAllActivities: () -> Unit,
    onStartActivity: () -> Unit,
    onNavigateToHeart: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToStress: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val weekly by viewModel.weekly.collectAsState()
    val recent by viewModel.recent.collectAsState()
    val hasPermissions by viewModel.hasPermissions.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { _: Set<String> -> }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartActivity,
                text = { Text("Start Activity", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Rounded.Add, null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item { 
                HomeHeader(
                    name = userName, 
                    onProfileClick = onNavigateToProfile,
                    onNotificationsClick = onNavigateToNotifications
                ) 
            }
            
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

            item {
                Spacer(modifier = Modifier.height(16.dp))
                GoalCalendarSection(weekly, onClick = onNavigateToCalendar)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                VitalityOrbCard(
                    stats = stats,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = onNavigateToVitality
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniScoreCard(
                        title = "HEART",
                        value = "72",
                        unit = "BPM",
                        icon = Icons.Rounded.Favorite,
                        color = Color(0xFFF43F5E),
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        iconKey = "heart_icon_box",
                        numberKey = "heart_number",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHeart
                    )
                    MiniScoreCard(
                        title = "SLEEP",
                        value = "92",
                        unit = "SCORE",
                        icon = Icons.Rounded.Bedtime,
                        color = Color(0xFF8B5CF6),
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        iconKey = "sleep_icon_box", // Note: Changed to icon box for consistent transition if needed
                        numberKey = "sleep_number",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSleep
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                StressScoreCard(
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = onNavigateToStress
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                QuickStatsRow(stats)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    TextButton(onClick = onSeeAllActivities) { Text("See All") }
                }
            }
            items(items = recent) { activity: ActivitySummary ->
                ActivityRow(activity)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Vitality Orb Card ────────────────────────────────────────────────────────

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun VitalityOrbCard(
    stats: TodayStats,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .premiumShadow(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            with(sharedTransitionScope) {
                BioOrbCanvas(
                    modifier = Modifier
                        .size(120.dp)
                        .sharedElement(
                            rememberSharedContentState(key = "vitality_orb"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    stepsProgress = stats.stepsProgress,
                    calsProgress = min(1f, stats.caloriesBurned / 2000f),
                    actProgress = min(1f, stats.activeMinutes / 60f)
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Column {
                with(sharedTransitionScope) {
                    Text(
                        "VITALITY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.sharedBounds(
                            rememberSharedContentState(key = "vitality_label"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                    Text(
                        "${(stats.stepsProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.sharedBounds(
                            rememberSharedContentState(key = "vitality_number"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                }
                Text(
                    "Excellent • +5% from avg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MiniScoreCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    iconKey: String,
    numberKey: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .premiumShadow(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            with(sharedTransitionScope) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape)
                        .sharedBounds(
                            rememberSharedContentState(iconKey),
                            animatedVisibilityScope
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.Bottom) {
                with(sharedTransitionScope) {
                    Text(
                        value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.sharedBounds(
                            rememberSharedContentState(numberKey),
                            animatedVisibilityScope,
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                        )
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun StressScoreCard(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .premiumShadow(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            with(sharedTransitionScope) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .sharedBounds(
                            rememberSharedContentState("stress_orb_box"),
                            animatedVisibilityScope
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(56.dp)) {
                        drawArc(Color.LightGray.copy(alpha = 0.2f), 0f, 360f, false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(
                            androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFCD34D), Color(0xFFF59E0B))),
                            -90f, 360f * 0.32f, false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text("STRESS LEVEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("32", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text("Relaxed • Avg today", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B))
            }
        }
    }
}

@Composable
fun BioOrbCanvas(
    modifier: Modifier,
    stepsProgress: Float,
    calsProgress: Float,
    actProgress: Float
) {
    val stepsColor = MaterialTheme.colorScheme.primary
    val calsColor = Color(0xFFFF7043)
    val actColor = Color(0xFF3B82F6)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    val animatedSteps by animateFloatAsState(stepsProgress, tween(1000, easing = FastOutSlowInEasing), label = "steps")
    val animatedCals by animateFloatAsState(calsProgress, tween(1200, easing = FastOutSlowInEasing), label = "cals")
    val animatedAct by animateFloatAsState(actProgress, tween(1400, easing = FastOutSlowInEasing), label = "act")

    Canvas(modifier = modifier.padding(4.dp)) {
        val strokeWidth = size.minDimension * 0.12f
        val spacing = strokeWidth * 1.3f
        
        // Background circles
        drawCircle(trackColor, radius = size.minDimension / 2, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        drawCircle(trackColor, radius = size.minDimension / 2 - spacing, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        drawCircle(trackColor, radius = size.minDimension / 2 - spacing * 2, style = Stroke(strokeWidth, cap = StrokeCap.Round))

        // Progress Arcs
        drawArc(
            color = stepsColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedSteps,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = calsColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedCals,
            useCenter = false,
            topLeft = Offset(spacing, spacing),
            size = Size(size.width - spacing * 2, size.height - spacing * 2),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = actColor,
            startAngle = -90f,
            sweepAngle = 360f * animatedAct,
            useCenter = false,
            topLeft = Offset(spacing * 2, spacing * 2),
            size = Size(size.width - spacing * 4, size.height - spacing * 4),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
    }
}

// ── Goal Calendar Section ────────────────────────────────────────────────────

@Composable
private fun GoalCalendarSection(weeklyData: List<DayActivity>, onClick: () -> Unit) {
    val listState = rememberLazyListState()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Your Journey",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(weeklyData) { day ->
                CalendarDayCard(day, onClick = onClick)
            }
        }
    }
}

@Composable
private fun CalendarDayCard(day: DayActivity, onClick: () -> Unit) {
    val isToday = day.isToday
    val backgroundColor = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = Modifier
            .width(64.dp)
            .height(100.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(day.day, style = MaterialTheme.typography.labelMedium, color = contentColor.copy(alpha = 0.7f))
            Text("${day.distanceKm.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = contentColor)
            if (day.distanceKm > 5f) {
                Icon(Icons.Rounded.CheckCircle, null, tint = if (isToday) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            } else {
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    name: String,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Welcome back,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Outlined.Notifications, "Notifications")
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(name.firstOrNull()?.toString() ?: "",
                    color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

// ── Quick Stats Row ─────────────────────────────────────────────────────────

@Composable
private fun QuickStatsRow(stats: TodayStats) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip("🔥", "${stats.caloriesBurned}", "kcal", Color(0xFFFF7043).copy(alpha = 0.1f), Modifier.weight(1f))
        StatChip("📍", "%.1f".format(stats.distanceKm), "km", Color(0xFF2ECC8A).copy(alpha = 0.1f), Modifier.weight(1f))
        StatChip("⏱", "${stats.activeMinutes}", "min", Color(0xFF3B82F6).copy(alpha = 0.1f), Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(emoji: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = { /* Could navigate to specific stat detail */ }
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(40.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium)
            Text(unit, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Recent Activity Row ───────────────────────────────────────────────────────

@Composable
private fun ActivityRow(activity: ActivitySummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { /* Navigate to activity details */ }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center) {
                Text(activity.type.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(activity.type.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                Text(activity.dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(activity.distanceKm)} km",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary)
                Text(activity.pace,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Security, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(12.dp))
                Text("Health Connect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("We need your permission to sync steps and activities automatically.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Grant Access", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun Modifier.premiumShadow(shape: androidx.compose.ui.graphics.Shape) = this.shadow(
    elevation = 8.dp,
    shape = shape,
    ambientColor = Color(0x05000000),
    spotColor = Color(0x1A000000)
)
