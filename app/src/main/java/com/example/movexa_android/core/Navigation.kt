package com.example.movexa_android.core

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.ui.graphics.vector.ImageVector

// ── Route constants ───────────────────────────────────────────────────────────
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Activity : Screen("activity")
    data object History : Screen("history")
    data object Profile : Screen("profile")
}

// ── Bottom nav items ──────────────────────────────────────────────────────────
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Activity,
        label = "Activity",
        selectedIcon = Icons.Filled.DirectionsRun,
        unselectedIcon = Icons.Outlined.DirectionsRun
    ),
    BottomNavItem(
        screen = Screen.History,
        label = "History",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),
    BottomNavItem(
        screen = Screen.Profile,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    ),
)