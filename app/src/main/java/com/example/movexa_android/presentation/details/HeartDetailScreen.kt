package com.example.movexa_android.presentation.details

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HeartDetailScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); isLoaded = true }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartScale"
    )

    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("Heart Rate", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color(0xFFF43F5E).copy(alpha = 0.1f), CircleShape)
                    .sharedBounds(
                        rememberSharedContentState("heart_icon_box"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Favorite,
                    contentDescription = "Heart Rate",
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer(scaleX = heartScale, scaleY = heartScale)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "72",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFFB7185)))
                ),
                fontWeight = FontWeight.Black,
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState("heart_number"),
                    animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                )
            )

            AnimatedVisibility(visible = isLoaded, enter = fadeIn(tween(600))) {
                Text("BPM • Resting", color = Color(0xFFF43F5E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(visible = isLoaded, enter = fadeIn() + slideInVertically { 100 }) {
                LiveEkgCard()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedVisibility(visible = isLoaded, enter = fadeIn() + slideInVertically { 150 }) {
                HeartZonesCard()
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun LiveEkgCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "ekg_sweep")
    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    val ekgPath = remember { Path() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Live Trace", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp).clipToBounds()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f
                val sweepX = sweepProgress * w

                // Grid
                val gridColor = Color(0xFFF43F5E).copy(alpha = 0.1f)
                for (i in 1..4) drawLine(gridColor, Offset(0f, h * (i / 5f)), Offset(w, h * (i / 5f)), 1.dp.toPx())
                for (i in 1..9) drawLine(gridColor, Offset(w * (i / 10f), 0f), Offset(w * (i / 10f), h), 1.dp.toPx())

                ekgPath.rewind()
                val segments = 4
                val segmentW = w / segments
                ekgPath.moveTo(0f, midY)
                for (i in 0 until segments) {
                    val startX = i * segmentW
                    ekgPath.lineTo(startX + segmentW * 0.2f, midY)
                    ekgPath.lineTo(startX + segmentW * 0.3f, midY - h * 0.1f)
                    ekgPath.lineTo(startX + segmentW * 0.4f, midY)
                    ekgPath.lineTo(startX + segmentW * 0.45f, midY + h * 0.2f)
                    ekgPath.lineTo(startX + segmentW * 0.5f, midY - h * 0.8f)
                    ekgPath.lineTo(startX + segmentW * 0.55f, midY + h * 0.3f)
                    ekgPath.lineTo(startX + segmentW * 0.6f, midY)
                    ekgPath.lineTo(startX + segmentW * 0.7f, midY - h * 0.15f)
                    ekgPath.lineTo(startX + segmentW * 0.85f, midY)
                    ekgPath.lineTo(startX + segmentW, midY)
                }

                drawPath(
                    path = ekgPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFF43F5E).copy(alpha = 0.8f), Color(0xFFF43F5E)),
                        startX = max(0f, sweepX - w * 0.4f), endX = sweepX
                    ),
                    style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                
                drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(sweepX, midY))
            }
        }
    }
}

@Composable
fun HeartZonesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Time in Zones", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().height(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(Modifier.fillMaxHeight().weight(0.6f).background(MaterialTheme.colorScheme.primary))
                Box(Modifier.fillMaxHeight().weight(0.3f).background(Color(0xFFF59E0B)))
                Box(Modifier.fillMaxHeight().weight(0.1f).background(Color(0xFFF43F5E)))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                ZoneLegend("Rest", "12h 45m", MaterialTheme.colorScheme.primary)
                ZoneLegend("Fat Burn", "2h 15m", Color(0xFFF59E0B))
                ZoneLegend("Peak", "45m", Color(0xFFF43F5E))
            }
        }
    }
}

@Composable
private fun ZoneLegend(label: String, value: String, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 14.dp))
    }
}
