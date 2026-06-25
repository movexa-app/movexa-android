package com.example.movexa_android.presentation.activity

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movexa_android.domain.model.ActivityType
import com.example.movexa_android.domain.model.TrackingState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 240.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetShadowElevation = 12.dp,
        sheetDragHandle = {
            // Very large touch target for the handle
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        },
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 500.dp) // Ensure the sheet is tall enough to be draggable upwards
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
            ) {
                // Activity type chips — only when idle
                AnimatedVisibility(visible = session.state == TrackingState.IDLE) {
                    Column {
                        Text(
                            "Select Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        ActivityTypeRow(
                            selected = session.type,
                            onSelect = viewModel::selectType
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // Live stats row
                AnimatedVisibility(visible = session.state != TrackingState.IDLE) {
                    Column {
                        LiveStatsRow(session)
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // Control buttons
                when (session.state) {
                    TrackingState.IDLE -> {
                        PrimaryButton("Start ${session.type.label}", session.type.emoji) {
                            viewModel.startActivity()
                        }
                    }
                    TrackingState.ACTIVE -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ControlButton(
                                icon = Icons.Default.Pause,
                                label = "Pause",
                                modifier = Modifier.weight(1f),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) { viewModel.pauseActivity() }
                            ControlButton(
                                icon = Icons.Default.Stop,
                                label = "Finish",
                                modifier = Modifier.weight(1f),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ) { viewModel.stopActivity() }
                        }
                    }
                    TrackingState.PAUSED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ControlButton(
                                icon = Icons.Default.PlayArrow,
                                label = "Resume",
                                modifier = Modifier.weight(1f),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) { viewModel.resumeActivity() }
                            ControlButton(
                                icon = Icons.Default.Stop,
                                label = "Finish",
                                modifier = Modifier.weight(1f),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ) { viewModel.stopActivity() }
                        }
                    }
                    TrackingState.FINISHED -> {
                        FinishedSummary(
                            distance = "${"%.2f".format(session.distanceKm)} km",
                            time = session.elapsedFormatted,
                            calories = "${session.calories} kcal"
                        ) { viewModel.resetActivity() }
                    }
                }
                
                // Extra space to allow dragging the sheet much higher over the map
                Spacer(Modifier.height(300.dp))
            }
        }
    ) { innerPadding ->
        // ── Map fills entire background ────────────────────────────────────
        Box(
            Modifier.fillMaxSize()
        ) {
            ActivityMap(
                routePoints = session.routePoints
            )

            // Floating timer overlay
            if (session.state != TrackingState.IDLE) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = innerPadding.calculateTopPadding() + 16.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = session.elapsedFormatted,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ── Map ──────────────────────────────────────────────────────────────────────
@Composable
private fun ActivityMap(
    routePoints: List<android.location.Location>
) {
    AndroidView(
        factory = { ctx ->
            org.osmdroid.config.Configuration.getInstance()
                .load(ctx, ctx.getSharedPreferences("osmdroid", 0))

            TouchAwareMapView(ctx).apply {
                setTileSource(
                    org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK
                )
                setMultiTouchControls(true)
                controller.setZoom(17.0)
                controller.setCenter(
                    org.osmdroid.util.GeoPoint(20.5937, 78.9629)
                )
            }
        },
        update = { map ->
            if (routePoints.isNotEmpty()) {
                val last = routePoints.last()
                val newPoint = org.osmdroid.util.GeoPoint(last.latitude, last.longitude)
                
                // Only animate if the position has actually changed to avoid interrupting gestures
                if (map.mapCenter.latitude != newPoint.latitude || map.mapCenter.longitude != newPoint.longitude) {
                    map.controller.animateTo(newPoint)
                }

                map.overlays.clear()
                if (routePoints.size >= 2) {
                    val polyline = org.osmdroid.views.overlay.Polyline().apply {
                        setPoints(routePoints.map {
                            org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
                        })
                        outlinePaint.color = android.graphics.Color.parseColor("#2979FF")
                        outlinePaint.strokeWidth = 14f
                    }
                    map.overlays.add(polyline)
                    map.invalidate()
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
// ── Activity Type Row ─────────────────────────────────────────────────────────

@Composable
private fun ActivityTypeRow(selected: ActivityType, onSelect: (ActivityType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActivityType.entries.take(4).forEach { type ->
            val isSelected = type == selected
            Surface(
                modifier = Modifier.clickable { onSelect(type) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) null
                else androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(type.emoji, fontSize = 20.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        type.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ── Live Stats Row ────────────────────────────────────────────────────────────

@Composable
private fun LiveStatsRow(session: com.example.movexa_android.domain.model.ActivitySession) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatItem("Distance", "${"%.2f".format(session.distanceKm)}", "km")
        StatDivider()
        StatItem("Pace", session.paceFormatted, "/km")
        StatDivider()
        StatItem("Calories", "${session.calories}", "kcal")
    }
}

@Composable
private fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold)
        Text(unit, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.height(48.dp).width(1.dp)
        .background(MaterialTheme.colorScheme.outlineVariant))
}

// ── Buttons ───────────────────────────────────────────────────────────────────

@Composable
private fun PrimaryButton(label: String, emoji: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("$emoji  $label", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(icon, contentDescription = label)
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

// ── Finished Summary ──────────────────────────────────────────────────────────

@Composable
private fun FinishedSummary(
    distance: String,
    time: String,
    calories: String,
    onReset: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎉 Activity Complete!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem("Distance", distance.replace(" km", ""), "km")
            StatDivider()
            StatItem("Time", time, "elapsed")
            StatDivider()
            StatItem("Calories", calories.replace(" kcal", ""), "kcal")
        }
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Start New Activity", fontWeight = FontWeight.SemiBold)
        }
    }
}