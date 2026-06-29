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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SleepDetailScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); isLoaded = true }

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
                Text("Sleep Analysis", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "SLEEP",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState("sleep_label"),
                    animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                )
            )

            Text(
                "92",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(listOf(Color(0xFF4C1D95), Color(0xFF8B5CF6)))
                ),
                fontWeight = FontWeight.Black,
                modifier = Modifier.sharedBounds(
                    rememberSharedContentState("sleep_number"),
                    animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds()
                )
            )

            AnimatedVisibility(visible = isLoaded, enter = fadeIn(tween(600))) {
                Text("Restful • 7h 20m", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = isLoaded, enter = fadeIn() + slideInVertically { 100 }) {
                SleepGraphCard()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnimatedVisibility(visible = isLoaded, enter = fadeIn() + slideInVertically { 150 }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SleepStatCard("Actual Sleep", "7h 20m", 0.92f, Icons.Rounded.CheckCircle, Color(0xFF8B5CF6))
                    SleepStatCard("Deep Sleep", "1h 45m", 0.85f, Icons.Rounded.Favorite, Color(0xFF4C1D95))
                    SleepStatCard("REM Sleep", "2h 10m", 0.75f, Icons.Rounded.Star, Color(0xFF2DD4BF))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SleepGraphCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Sleep Stages", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                // Simplified mock representation
                val w = size.width
                val h = size.height
                val stages = 4
                val stageH = h / stages

                for (i in 0 until stages) {
                    val y = i * stageH + stageH / 2
                    drawLine(Color.Gray.copy(alpha = 0.1f), Offset(0f, y), Offset(w, y), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                }
                
                // Draw mock blocks
                val path = Path()
                path.moveTo(0f, stageH * 0.5f)
                path.lineTo(w * 0.1f, stageH * 0.5f)
                path.lineTo(w * 0.1f, stageH * 2.5f)
                path.lineTo(w * 0.3f, stageH * 2.5f)
                path.lineTo(w * 0.3f, stageH * 3.5f)
                path.lineTo(w * 0.5f, stageH * 3.5f)
                path.lineTo(w * 0.5f, stageH * 1.5f)
                path.lineTo(w * 0.7f, stageH * 1.5f)
                path.lineTo(w * 0.7f, stageH * 2.5f)
                path.lineTo(w, stageH * 2.5f)

                drawPath(path, Color(0xFF8B5CF6), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("23:30", style = MaterialTheme.typography.labelSmall)
                Text("07:15", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun SleepStatCard(label: String, value: String, progress: Float, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodySmall)
                    Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = color, trackColor = color.copy(alpha = 0.1f))
            }
        }
    }
}
