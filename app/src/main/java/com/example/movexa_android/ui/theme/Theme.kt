package com.movexa.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MovexaLightColorScheme = lightColorScheme(
    // Primary (main blue)
    primary = MovexaBlue,
    onPrimary = NeutralWhite,
    primaryContainer = MovexaBlueContainer,
    onPrimaryContainer = MovexaBlueDark,

    // Secondary (soft blue for chips, tags)
    secondary = MovexaBlueLight,
    onSecondary = NeutralWhite,
    secondaryContainer = MovexaBlueSurface,
    onSecondaryContainer = MovexaBlueDark,

    // Tertiary (green for achievements)
    tertiary = MovexaGreen,
    onTertiary = NeutralWhite,
    tertiaryContainer = MovexaGreenContainer,
    onTertiaryContainer = Color(0xFF0A5C3A),

    // Background & Surface
    background = NeutralSurface,
    onBackground = NeutralTextPrimary,
    surface = NeutralCard,
    onSurface = NeutralTextPrimary,
    surfaceVariant = MovexaBlueSurface,
    onSurfaceVariant = NeutralTextSecondary,

    // Outline
    outline = NeutralBorder,
    outlineVariant = MovexaBlueContainer,

    // Error
    error = MovexaOrange,
    onError = NeutralWhite,
    errorContainer = MovexaOrangeContainer,
    onErrorContainer = Color(0xFF7A1F00),
)

private val MovexaDarkColorScheme = darkColorScheme(
    // Primary
    primary = MovexaBlueLight,
    onPrimary = DarkBackground,
    primaryContainer = MovexaBlueDark,
    onPrimaryContainer = MovexaBlueSurface,

    // Secondary
    secondary = MovexaBlueLight,
    onSecondary = DarkBackground,
    secondaryContainer = MovexaBlueDark,
    onSecondaryContainer = MovexaBlueSurface,

    // Tertiary
    tertiary = MovexaGreen,
    onTertiary = DarkBackground,
    tertiaryContainer = Color(0xFF0A5C3A),
    onTertiaryContainer = MovexaGreenContainer,

    // Background & Surface
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,

    // Outline
    outline = DarkBorder,
    outlineVariant = DarkCard,

    // Error
    error = MovexaOrange,
    onError = DarkBackground,
    errorContainer = Color(0xFF7A1F00),
    onErrorContainer = MovexaOrangeContainer,
)

@Composable
fun MovexaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color uses wallpaper colors on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MovexaDarkColorScheme
        else -> MovexaLightColorScheme
    }

    // Make status bar transparent and match theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MovexaTypography,
        content = content
    )
}