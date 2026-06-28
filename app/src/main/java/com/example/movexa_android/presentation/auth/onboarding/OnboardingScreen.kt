package com.example.movexa_android.presentation.auth.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState { onboardingPages.size }
    val scope = rememberCoroutineScope()

    val handleFinish = {
        viewModel.completeOnboarding()
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(page = page, pagerState = pagerState)
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val offsetFrac = pagerState.currentPageOffsetFraction
                repeat(onboardingPages.size) { i ->
                    val selected = when (i) {
                        pagerState.currentPage -> 1f - offsetFrac.absoluteValue
                        pagerState.currentPage + 1 -> offsetFrac.coerceAtLeast(0f)
                        pagerState.currentPage - 1 -> (-offsetFrac).coerceAtLeast(0f)
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(lerp(6.dp, 28.dp, selected))
                            .clip(CircleShape)
                            .background(
                                lerp(
                                    Color.White.copy(alpha = 0.2f),
                                    onboardingPages[i].accentColor,
                                    selected
                                )
                            )
                    )
                }
            }

            // CTA Button
            val isLast = pagerState.currentPage == onboardingPages.size - 1
            val frac = pagerState.currentPageOffsetFraction
            val btnColor = when {
                frac > 0 && pagerState.currentPage < onboardingPages.size - 1 ->
                    lerp(
                        onboardingPages[pagerState.currentPage].accentColor,
                        onboardingPages[pagerState.currentPage + 1].accentColor,
                        frac
                    )
                frac < 0 && pagerState.currentPage > 0 ->
                    lerp(
                        onboardingPages[pagerState.currentPage].accentColor,
                        onboardingPages[pagerState.currentPage - 1].accentColor,
                        -frac
                    )
                else -> onboardingPages[pagerState.currentPage].accentColor
            }

            Button(
                onClick = {
                    if (isLast) handleFinish()
                    else scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = btnColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(58.dp)
                    .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = btnColor.copy(alpha = 0.5f))
            ) {
                Text(
                    if (isLast) "Get Started" else "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Skip
            if (!isLast) {
                TextButton(onClick = handleFinish) {
                    Text("Skip", color = Color.White.copy(alpha = 0.45f), fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPage(page: Int, pagerState: PagerState) {
    val data = onboardingPages[page]
    val offset = pagerState.pageOffset(page)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RectangleShape)
            .background(data.bgColor)
    ) {
        // Blobs layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val blobAlpha = (1f - offset.absoluteValue).coerceIn(0f, 1f)
            data.blobs.forEach { (xFrac, yFrac, rFrac) ->
                val cx = size.width * xFrac + offset * size.width * 0.15f
                val cy = size.height * yFrac
                val r  = size.width * rFrac
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            data.accentColor.copy(alpha = 0.22f * blobAlpha),
                            data.accentColor.copy(alpha = 0f)
                        ),
                        center = Offset(cx, cy),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
        }

        // Shapes layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            data.shapes.forEachIndexed { i, (xFrac, yFrac) ->
                val parallaxX = offset * size.width * 0.32f
                val cx = size.width * xFrac + parallaxX
                val cy = size.height * yFrac
                val baseR = (24f + i * 12f).dp.toPx()
                val alpha = (1f - offset.absoluteValue).coerceIn(0f, 1f)

                if (i % 2 == 0) {
                    drawCircle(
                        color = data.shapeColor.copy(alpha = alpha * 0.45f),
                        radius = baseR,
                        center = Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                    )
                    drawCircle(
                        color = data.shapeColor.copy(alpha = alpha * 0.12f),
                        radius = baseR * 0.5f,
                        center = Offset(cx, cy)
                    )
                } else {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx, cy - baseR)
                        lineTo(cx + baseR * 0.55f, cy)
                        lineTo(cx, cy + baseR)
                        lineTo(cx - baseR * 0.55f, cy)
                        close()
                    }
                    drawPath(
                        path,
                        color = data.shapeColor.copy(alpha = alpha * 0.30f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                    )
                }
            }
        }

        // Text content
        val textParallax = offset * 0.72f
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 280.dp, start = 32.dp, end = 32.dp)
                .offset(x = (-textParallax * 55).dp)
                .alpha((1f - offset.absoluteValue * 1.8f).coerceIn(0f, 1f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun PagerState.pageOffset(page: Int): Float =
    (currentPage - page + currentPageOffsetFraction).coerceIn(-1f, 1f)

private fun lerp(start: Dp, stop: Dp, fraction: Float): Dp =
    start + (stop - start) * fraction.coerceIn(0f, 1f)

private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    val t = fraction.coerceIn(0f, 1f)
    return Color(
        red   = start.red   + (stop.red   - start.red)   * t,
        green = start.green + (stop.green - start.green) * t,
        blue  = start.blue  + (stop.blue  - start.blue)  * t,
        alpha = start.alpha + (stop.alpha - start.alpha) * t
    )
}