package com.example.movexa_android.presentation.auth.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movexa.android.ui.theme.DarkBackground
import com.movexa.android.ui.theme.MovexaBlue
import com.movexa.android.ui.theme.MovexaBlueLight
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    onFinished: (SplashViewModel.AuthState) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    // Stage machine
    var stage by remember { mutableStateOf(SplashStage.LogoIn) }

    // Animatables
    val logoAlpha   = remember { Animatable(0f) }
    val logoScale   = remember { Animatable(0.92f) }
    val glowAlpha   = remember { Animatable(0f) }
    val linesAlpha  = remember { Animatable(0f) }
    val lineHeight  = remember { Animatable(0f) }
    val gapWidth    = remember { Animatable(0f) }

    LaunchedEffect(authState) {
        if (authState == null) return@LaunchedEffect

        // Step 1: Logo & Glow Fade In
        stage = SplashStage.LogoIn
        coroutineScope {
            launch { logoAlpha.animateTo(1f, tween(1000, easing = EaseOutQuart)) }
            launch { logoScale.animateTo(1f, tween(1200, easing = EaseOutBack)) }
            launch { glowAlpha.animateTo(0.6f, tween(1500, easing = LinearEasing)) }
        }
        delay(600.milliseconds)

        // Step 2: Logo Fade Out, Lines Appear
        stage = SplashStage.LinesGrow
        coroutineScope {
            launch { logoAlpha.animateTo(0f, tween(300, easing = EaseInCubic)) }
            launch { logoScale.animateTo(0.95f, tween(300, easing = EaseInCubic)) }
            launch { glowAlpha.animateTo(0f, tween(400)) }
        }
        
        linesAlpha.snapTo(0f)
        linesAlpha.animateTo(1f, tween(200))

        // Step 3: Vertical Lines Expand
        lineHeight.animateTo(1f, tween(700, easing = ExpoEaseOut))
        delay(100.milliseconds)

        // Step 4: Curtain Reveal (Gap opens)
        stage = SplashStage.Reveal
        gapWidth.animateTo(1f, tween(1800, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)))

        // Step 5: Final Transition
        linesAlpha.animateTo(0f, tween(400))
        stage = SplashStage.Done
        delay(100.milliseconds)
        onFinished(authState!!)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // 1. Glowing background effect during logo stage
        if (stage == SplashStage.LogoIn) {
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .graphicsLayer(alpha = glowAlpha.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(MovexaBlue.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset.Unspecified,
                            radius = 600f
                        )
                    )
            )
        }

        // 2. Final Content (Revealed by curtain)
        SplashBrandingContent(alpha = 1f)

        // 3. The "Curtain" Overlay (Opens to reveal)
        CurtainOverlay(gapFrac = gapWidth.value)

        // 4. Vertical "Energy" Lines
        if (stage == SplashStage.LinesGrow || stage == SplashStage.Reveal) {
            TwinLines(
                heightFrac = lineHeight.value,
                gapFrac = gapWidth.value,
                alpha = linesAlpha.value
            )
        }

        // 5. Initial Logo Entrance
        if (stage == SplashStage.LogoIn) {
            SplashBrandingContent(
                alpha = logoAlpha.value,
                scale = logoScale.value
            )
        }
    }
}

@Composable
private fun SplashBrandingContent(alpha: Float, scale: Float = 1f) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
    ) {
        Text(
            text = "MOVEXA",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Black,
            letterSpacing = 10.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(4.dp).background(MovexaBlueLight, CircleShape))
            Text(
                text = " TRACK · TRAIN · TRANSFORM ",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )
            Box(Modifier.size(4.dp).background(MovexaBlueLight, CircleShape))
        }
    }
}

@Composable
private fun TwinLines(heightFrac: Float, gapFrac: Float, alpha: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = alpha)
    ) {
        val cx = size.width / 2f
        val halfGap = (gapFrac * size.width / 2f).coerceAtLeast(1f)
        val halfH   = (size.height * heightFrac) / 2f
        val top     = size.height / 2f - halfH
        val bottom  = size.height / 2f + halfH
        
        // Use brand color for the lines
        val lineColor = MovexaBlueLight

        drawLine(
            color = lineColor,
            start = Offset(cx - halfGap, top),
            end = Offset(cx - halfGap, bottom),
            strokeWidth = 3f
        )
        drawLine(
            color = lineColor,
            start = Offset(cx + halfGap, top),
            end = Offset(cx + halfGap, bottom),
            strokeWidth = 3f
        )
    }
}

@Composable
private fun CurtainOverlay(gapFrac: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val halfGap = (gapFrac * w / 2f).coerceIn(0f, w / 2f)

        if (halfGap >= w / 2f) return@Canvas

        // Left curtain
        drawRect(
            color = DarkBackground,
            topLeft = Offset.Zero,
            size = Size(w / 2f - halfGap, h)
        )
        // Right curtain
        drawRect(
            color = DarkBackground,
            topLeft = Offset(w / 2f + halfGap, 0f),
            size = Size(w / 2f - halfGap, h)
        )
    }
}

// Custom Easing for that premium feel
private val ExpoEaseOut = CubicBezierEasing(0.14f, 1f, 0.34f, 1f)
private val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
private val EaseInCubic = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)

private enum class SplashStage { LogoIn, LinesGrow, Reveal, Done }