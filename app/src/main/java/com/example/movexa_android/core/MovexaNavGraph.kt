package com.example.movexa_android.core

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.movexa_android.presentation.home.HomeScreen
import com.example.movexa_android.presentation.activity.ActivityScreen
import com.example.movexa_android.presentation.history.HistoryScreen
import com.example.movexa_android.presentation.profile.ProfileScreen

@Composable
fun MovexaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Activity.route) {
            ActivityScreen()
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}