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
import kotlinx.coroutines.flow.SharedFlow

private const val HOME_SCREEN = "home"

@Composable
fun AppNavHost(
    authCallbackFlow: SharedFlow<Uri>
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HOME_SCREEN
    ) {
        composable(HOME_SCREEN){
            val viewModel: AuthViewModel = hiltViewModel()
            AuthScreen(viewModel = viewModel)
            LaunchedEffect(Unit) {
                authCallbackFlow.collect { uri ->
                    Log.d("OAuth", "AppNavHost получил uri: $uri")
                    viewModel.onAuthCodeReceived(uri)
                }
            }
        }
    }
}