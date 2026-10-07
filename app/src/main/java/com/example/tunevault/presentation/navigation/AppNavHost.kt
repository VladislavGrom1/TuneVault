package com.example.tunevault.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tunevault.presentation.home.HomeScreen

private const val HOME_SCREEN = "home"

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HOME_SCREEN
    ) {
        composable(HOME_SCREEN){
            HomeScreen()
        }
    }
}