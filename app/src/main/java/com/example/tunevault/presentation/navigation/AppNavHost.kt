package com.example.tunevault.presentation.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tunevault.presentation.auth.AuthScreen
import com.example.tunevault.presentation.auth.AuthViewModel
import com.example.tunevault.presentation.home.HomeScreen
import com.example.tunevault.presentation.root.RootScreen
import kotlinx.coroutines.flow.SharedFlow

object Routes {
    const val ROOT_SCREEN = "root"
    const val AUTH_SCREEN = "auth"
    const val HOME_SCREEN = "home"
}

@Composable
fun AppNavHost(
    authCallbackFlow: SharedFlow<Uri>
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ROOT_SCREEN
    ) {
        composable(Routes.ROOT_SCREEN) {
            RootScreen(
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH_SCREEN) {
                        popUpTo(Routes.ROOT_SCREEN) {inclusive = true}
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME_SCREEN) {
                        popUpTo(Routes.ROOT_SCREEN) {inclusive = true}
                    }
                }
            )
        }
        composable(Routes.AUTH_SCREEN){
            val viewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                onNavigateHome = {
                    navController.navigate(Routes.HOME_SCREEN) {
                        popUpTo(Routes.AUTH_SCREEN) {inclusive = true}
                    }
                },
                viewModel = viewModel
            )
            LaunchedEffect(Unit) {
                authCallbackFlow.collect { uri ->
                    Log.d("OAuth", "AppNavHost получил uri: $uri")
                    viewModel.onAuthCodeReceived(uri)
                }
            }
        }
        composable(Routes.HOME_SCREEN){
            HomeScreen(
                onNavigateAuth = {
                    navController.navigate(Routes.AUTH_SCREEN) {
                        popUpTo(Routes.HOME_SCREEN) {inclusive = true}
                    }
                }
            )
        }
    }
}