package com.example.movexa_android.presentation.details

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movexa_android.domain.model.TodayStats
import com.example.movexa_android.presentation.home.BioOrbCanvas
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.VitalityDetailScreen(
    stats: TodayStats,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isLoaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Rounded.ArrowBack, "Back")
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "Vitality Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BioOrbCanvas(
                modifier = Modifier
                    .size(280.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "vitality_orb"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                stepsProgress = stats.stepsProgress,
                calsProgress = stats.caloriesBurned / 2000f,
                actProgress = stats.activeMinutes / 60f
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "VITALITY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.sharedBounds(
                        rememberSharedContentState(key = "vitality_label"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
                Text(
                    "${(stats.stepsProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.sharedBounds(
                        rememberSharedContentState(key = "vitality_number"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AnimatedVisibility(
            visible = isLoaded,
            enter = slideInVertically { it / 2 } + fadeIn()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailStatCard("Steps", "${stats.steps}", stats.stepsProgress, Icons.Rounded.DirectionsWalk, MaterialTheme.colorScheme.primary)
                DetailStatCard("Calories", "${stats.caloriesBurned} kcal", stats.caloriesBurned / 2000f, Icons.Rounded.LocalFireDepartment, Color(0xFFFF7043))
                DetailStatCard("Activity", "${stats.activeMinutes} min", stats.activeMinutes / 60f, Icons.Rounded.Bolt, Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
private fun DetailStatCard(
    label: String,
    value: String,
    progress: Float,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f)
                )
            }
        }
    }
}
