package com.movexa.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Spacing scale ─────────────────────────────────────────────────────────────
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp

    // Screen horizontal padding
    val screenPadding: Dp = 20.dp

    // Card internal padding
    val cardPadding: Dp = 16.dp

    // Bottom nav height
    val bottomNavHeight: Dp = 80.dp
}

// ── Shape scale ───────────────────────────────────────────────────────────────
val MovexaShapes = Shapes(
    // Small: chips, badges, small buttons
    small = RoundedCornerShape(8.dp),
    // Medium: cards, text fields, list items
    medium = RoundedCornerShape(16.dp),
    // Large: bottom sheets, dialogs, big cards
    large = RoundedCornerShape(24.dp),
    // Extra large: full bottom sheet
    extraLarge = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
)