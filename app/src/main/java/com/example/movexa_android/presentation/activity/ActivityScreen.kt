package com.example.movexa_android.presentation.activity

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.material.icons.filled.Share
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.foundation.text.BasicTextField
import com.example.movexa_android.domain.model.ActivitySession.MapType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
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
    var searchQuery by remember { mutableStateOf("") }

    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = sheetState,
// ...
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
                        Spacer(Modifier.height(12.dp))
                        session.distanceToDestination?.let { dist ->
                            DestinationInfo(dist)
                        }
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
                        val context = LocalContext.current
                        val density = LocalDensity.current
                        val layoutDirection = LocalLayoutDirection.current
                        val graphicsLayer = rememberGraphicsLayer()
                        
                        Box(modifier = Modifier.drawWithContent {
                            graphicsLayer.record(
                                density, 
                                layoutDirection, 
                                IntSize(size.width.toInt(), size.height.toInt())
                            ) {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }) {
                            FinishedSummary(
                                distance = "${"%.2f".format(session.distanceKm)} km",
                                time = session.elapsedFormatted,
                                calories = "${session.calories} kcal"
                            ) { viewModel.resetActivity() }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                shareWorkout(context, graphicsLayer, session)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share Achievement")
                        }
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
                routePoints = session.routePoints,
                currentLocation = session.currentLocation,
                bearing = session.bearing,
                isFollowMode = session.isFollowMode,
                mapType = session.mapType,
                startPoint = session.startPoint,
                destination = session.destination,
                onMapLongClick = { lat, lon ->
                    val loc = android.location.Location("manual").apply {
                        latitude = lat
                        longitude = lon
                    }
                    viewModel.setDestination(loc)
                }
            )

            // Search Bar Overlay (Google Maps Style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(top = innerPadding.calculateTopPadding())
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search destination...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    IconButton(onClick = { 
                        // Force follow mode to recenter
                        if (!session.isFollowMode) viewModel.toggleFollowMode()
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Current Location", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Map Controls (Floating)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { viewModel.cycleMapType() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Map Type")
                }

                SmallFloatingActionButton(
// ...
                    onClick = { viewModel.toggleFollowMode() },
                    containerColor = if (session.isFollowMode) MaterialTheme.colorScheme.primary 
                                     else MaterialTheme.colorScheme.surface,
                    contentColor = if (session.isFollowMode) Color.White 
                                   else MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(
                        if (session.isFollowMode) Icons.Default.Navigation else Icons.Default.Explore,
                        contentDescription = "Follow Mode"
                    )
                }
                
                if (session.destination != null) {
                    SmallFloatingActionButton(
                        onClick = { viewModel.clearDestination() },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Destination")
                    }
                }
            }

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
    routePoints: List<android.location.Location>,
    currentLocation: android.location.Location? = null,
    bearing: Float = 0f,
    isFollowMode: Boolean = true,
    mapType: MapType = MapType.NORMAL,
    startPoint: android.location.Location? = null,
    destination: android.location.Location? = null,
    onMapLongClick: (Double, Double) -> Unit = { _, _ -> }
) {
    AndroidView(
        factory = { ctx ->
            org.osmdroid.config.Configuration.getInstance()
                .load(ctx, ctx.getSharedPreferences("osmdroid", 0))
            
            // Set User Agent to avoid being blocked by tile servers
            org.osmdroid.config.Configuration.getInstance().userAgentValue = ctx.packageName

            TouchAwareMapView(ctx).apply {
                setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(18.0)
                
                // Set a default center (e.g., a city) instead of 0,0 to avoid blue screen
                // This will be overridden as soon as the first GPS location arrives
                controller.setCenter(org.osmdroid.util.GeoPoint(20.5937, 78.9629))
                
                // Enable rotation gestures
                val rotationGestureOverlay = org.osmdroid.views.overlay.gestures.RotationGestureOverlay(this)
                rotationGestureOverlay.isEnabled = true
                overlays.add(rotationGestureOverlay)

                val mEventsReceiver = object : org.osmdroid.events.MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: org.osmdroid.util.GeoPoint): Boolean = false
                    override fun longPressHelper(p: org.osmdroid.util.GeoPoint): Boolean {
                        onMapLongClick(p.latitude, p.longitude)
                        return true
                    }
                }
                overlays.add(org.osmdroid.views.overlay.MapEventsOverlay(mEventsReceiver))
            }
        },
        update = { map ->
            // Update Tile Source based on MapType
            val newSource = when (mapType) {
                MapType.NORMAL -> org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK
                MapType.SATELLITE -> org.osmdroid.tileprovider.tilesource.XYTileSource(
                    "USGS_Sat", 0, 18, 256, ".jpg", arrayOf("https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryOnly/MapServer/tile/")
                )
                MapType.TERRAIN -> org.osmdroid.tileprovider.tilesource.TileSourceFactory.OpenTopo
            }
            
            if (map.tileProvider.tileSource.name() != newSource.name()) {
                map.setTileSource(newSource)
            }

            // Keep specialized overlays
// ...
            val rotationOverlay = map.overlays.find { it is org.osmdroid.views.overlay.gestures.RotationGestureOverlay }
            val eventsOverlay = map.overlays.find { it is org.osmdroid.views.overlay.MapEventsOverlay }
            map.overlays.clear()
            rotationOverlay?.let { map.overlays.add(it) }
            eventsOverlay?.let { map.overlays.add(it) }

            // 1. Draw Destination Line (Dashed) if navigating
            if (currentLocation != null && destination != null) {
                val line = org.osmdroid.views.overlay.Polyline().apply {
                    setPoints(listOf(
                        org.osmdroid.util.GeoPoint(currentLocation.latitude, currentLocation.longitude),
                        org.osmdroid.util.GeoPoint(destination.latitude, destination.longitude)
                    ))
                    outlinePaint.color = android.graphics.Color.DKGRAY
                    outlinePaint.strokeWidth = 6f
                    outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(20f, 20f), 0f)
                }
                map.overlays.add(line)
            }

            // 2. Draw Traveled Route
            if (routePoints.size >= 2) {
                val polyline = org.osmdroid.views.overlay.Polyline().apply {
                    setPoints(routePoints.map {
                        org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
                    })
                    outlinePaint.color = "#2979FF".toColorInt()
                    outlinePaint.strokeWidth = 14f
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                }
                map.overlays.add(polyline)
            }

            // 3. Start Marker
            startPoint?.let {
                val marker = org.osmdroid.views.overlay.Marker(map).apply {
                    position = org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    icon = map.context.getDrawable(android.R.drawable.ic_input_add) // Simplified
                }
                map.overlays.add(marker)
            }

            // 4. Destination Marker
            destination?.let {
                val marker = org.osmdroid.views.overlay.Marker(map).apply {
                    position = org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    title = "Goal"
                }
                map.overlays.add(marker)
            }

            // 5. Directional User Icon (Navigation Style)
            currentLocation?.let {
                val marker = org.osmdroid.views.overlay.Marker(map).apply {
                    position = org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    rotation = -bearing // OSMDroid rotation is inverted or needs adjustment
                    icon = map.context.getDrawable(android.R.drawable.ic_menu_directions)
                }
                map.overlays.add(marker)
            }

            // 6. Navigation Follow Logic
            if (isFollowMode) {
                val centerLoc = currentLocation ?: routePoints.lastOrNull()
                centerLoc?.let {
                    val newPoint = org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
                    map.controller.animateTo(newPoint)
                    map.mapOrientation = -bearing 
                }
            } else if (currentLocation != null && map.mapCenter.latitude == 0.0) {
                map.controller.setCenter(org.osmdroid.util.GeoPoint(currentLocation.latitude, currentLocation.longitude))
            }
            
            map.invalidate()
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

@Composable
private fun DestinationInfo(distanceMeters: Float) {
    val km = distanceMeters / 1000f
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Remaining to destination: ${"%.2f".format(km)} km",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ── Live Stats Row ────────────────────────────────────────────────────────────

@Composable
private fun LiveStatsRow(session: com.example.movexa_android.domain.model.ActivitySession) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatItem("Distance", "%.2f".format(session.distanceKm), "km")
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

private fun shareWorkout(
    context: android.content.Context,
    graphicsLayer: GraphicsLayer,
    session: com.example.movexa_android.domain.model.ActivitySession
) {
    val scope = CoroutineScope(Dispatchers.Main)
    scope.launch {
        try {
            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
            val file = File(context.cacheDir, "movexa_share_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Just completed a ${"%.2f".format(session.distanceKm)} km ${session.type.label} with Movexa! 🚀")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Activity"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
