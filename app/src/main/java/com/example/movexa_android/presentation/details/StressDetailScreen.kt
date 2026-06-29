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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun StressDetailScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); isLoaded = true }

    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
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
                Text("Stress Analysis", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .sharedBounds(
                        rememberSharedContentState("stress_orb_box"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val strokeW = 10.dp.toPx()
                    drawArc(Color.LightGray.copy(alpha = 0.2f), 0f, 360f, false, style = Stroke(strokeW, cap = StrokeCap.Round))
                    drawArc(
                        Brush.linearGradient(listOf(Color(0xFFFCD34D), Color(0xFFF59E0B))),
                        -90f, 360f * 0.32f, false, style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                }
                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFFF59E0B).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color(0xFFF59E0B).copy(alpha = 0.3f), radius = 10.dp.toPx() * breatheScale)
                        drawCircle(Color(0xFFF59E0B), radius = 5.dp.toPx())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "STRESS LEVEL",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                "32",
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(listOf(Color(0xFFFCD34D), Color(0xFFF59E0B)))
                ),
                fontWeight = FontWeight.Black
            )

            AnimatedVisibility(visible = isLoaded, enter = fadeIn(tween(600))) {
                Text("Relaxed • Avg today", color = Color(0xFFF59E0B), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Stress Management", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your stress levels have been consistently low today. Keep up the good work!", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
