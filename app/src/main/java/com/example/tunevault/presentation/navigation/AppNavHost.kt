package com.example.tunevault.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tunevault.domain.model.SessionState
import com.example.tunevault.presentation.auth.AuthScreen
import com.example.tunevault.presentation.home.HomeScreen
import com.example.tunevault.presentation.session.SessionViewModel
import com.example.tunevault.presentation.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val HOME = "home"
}

/**
 * Единственное место, где состояние сессии превращается в навигацию.
 * Экраны больше не принимают решений о том, куда идти.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.sessionState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) { SplashScreen() }
        composable(Routes.AUTH) { AuthScreen() }
        composable(Routes.HOME) { HomeScreen() }
    }

    LaunchedEffect(sessionState) {
        // Loading не трогаем: на холодном старте уже показан Splash,
        // а после смерти процесса это позволяет сохранить восстановленный экран
        // (например, Auth с незавершённым OAuth-входом).
        val target = when (sessionState) {
            SessionState.Loading -> return@LaunchedEffect
            SessionState.LoggedOut -> Routes.AUTH
            SessionState.Authenticated -> Routes.HOME
        }
        navController.navigateToSessionDestination(target)
    }
}

private fun NavHostController.navigateToSessionDestination(route: String) {
    if (currentDestination?.route == route) return

    navigate(route) {
        // Сессионные экраны не должны оставаться в back stack друг за другом.
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
