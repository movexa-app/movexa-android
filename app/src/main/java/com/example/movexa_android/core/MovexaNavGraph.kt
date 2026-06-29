package com.example.movexa_android.core

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.movexa_android.presentation.activity.ActivityScreen
import com.example.movexa_android.presentation.auth.login.LoginScreen
import com.example.movexa_android.presentation.auth.onboarding.OnboardingScreen
import com.example.movexa_android.presentation.auth.signup.SignUpScreen
import com.example.movexa_android.presentation.auth.splash.SplashScreen
import com.example.movexa_android.presentation.auth.splash.SplashViewModel
import com.example.movexa_android.presentation.details.*
import com.example.movexa_android.presentation.history.HistoryScreen
import com.example.movexa_android.presentation.home.HomeScreen
import com.example.movexa_android.presentation.home.HomeViewModel
import com.example.movexa_android.presentation.notifications.NotificationsScreen
import com.example.movexa_android.presentation.profile.ProfileScreen

// ── Auth route constants ──────────────────────────────────────────────────────
private object AuthRoutes {
    const val SPLASH      = "splash"
    const val ONBOARDING  = "onboarding"
    const val LOGIN       = "login"
    const val SIGN_UP     = "signup"
    const val MAIN        = "main"
}

// ── Shared Transition Constants ──────────────────────────────────────────────
object DetailedRoutes {
    const val VITALITY = "vitality_detail"
    const val CALENDAR = "calendar_detail"
    const val NOTIFICATIONS = "notifications"
    const val HEART = "heart_detail"
    const val SLEEP = "sleep_detail"
    const val STRESS = "stress_detail"
}

// ── Root nav graph (auth flow → main app) ────────────────────────────────────
@Composable
fun MovexaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.SPLASH
    ) {

        composable(AuthRoutes.SPLASH) {
            SplashScreen(
                onFinished = { authState ->
                    val destination = when (authState) {
                        is SplashViewModel.AuthState.Authenticated -> AuthRoutes.MAIN
                        is SplashViewModel.AuthState.NotAuthenticated -> AuthRoutes.LOGIN
                        is SplashViewModel.AuthState.FirstTime -> AuthRoutes.ONBOARDING
                    }
                    navController.navigate(destination) {
                        popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(AuthRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(AuthRoutes.LOGIN) {
                        popUpTo(AuthRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AuthRoutes.MAIN) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AuthRoutes.SIGN_UP)
                }
            )
        }

        composable(AuthRoutes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(AuthRoutes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(AuthRoutes.MAIN) {
            MainScreen(onLogout = {
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(AuthRoutes.MAIN) { inclusive = true }
                }
            })
        }
    }
}

// ── Main app with bottom nav (your existing screens) ─────────────────────────
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MainScreen(onLogout: () -> Unit) {
    val innerNavController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val stats by homeViewModel.stats.collectAsState()

    SharedTransitionLayout {
        Scaffold(
            bottomBar = {
                MovexaBottomNav(navController = innerNavController)
            }
        ) { innerPadding ->
            NavHost(
                navController = innerNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onNavigateToVitality = { innerNavController.navigate(DetailedRoutes.VITALITY) },
                        onNavigateToCalendar = { innerNavController.navigate(Screen.History.route) },
                        onNavigateToNotifications = { innerNavController.navigate(DetailedRoutes.NOTIFICATIONS) },
                        onNavigateToProfile = { innerNavController.navigate(Screen.Profile.route) },
                        onSeeAllActivities = { innerNavController.navigate(Screen.History.route) },
                        onStartActivity = { innerNavController.navigate(Screen.Activity.route) },
                        onNavigateToHeart = { innerNavController.navigate(DetailedRoutes.HEART) },
                        onNavigateToSleep = { innerNavController.navigate(DetailedRoutes.SLEEP) },
                        onNavigateToStress = { innerNavController.navigate(DetailedRoutes.STRESS) }
                    )
                }
                composable(DetailedRoutes.VITALITY) {
                    VitalityDetailScreen(
                        stats = stats,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onBack = { innerNavController.popBackStack() }
                    )
                }
                composable(DetailedRoutes.HEART) {
                    HeartDetailScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onBack = { innerNavController.popBackStack() }
                    )
                }
                composable(DetailedRoutes.SLEEP) {
                    SleepDetailScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onBack = { innerNavController.popBackStack() }
                    )
                }
                composable(DetailedRoutes.STRESS) {
                    StressDetailScreen(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable,
                        onBack = { innerNavController.popBackStack() }
                    )
                }
                composable(DetailedRoutes.NOTIFICATIONS) {
                    NotificationsScreen(onBack = { innerNavController.popBackStack() })
                }
                composable(Screen.Activity.route) {
                    ActivityScreen()
                }
                composable(Screen.History.route) {
                    HistoryScreen()
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}
