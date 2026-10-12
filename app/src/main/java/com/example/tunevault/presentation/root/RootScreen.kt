package com.example.tunevault.presentation.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tunevault.presentation.theme.TuneVaultTheme

@Composable
fun RootScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RootViewModel = hiltViewModel()
){
    val state by viewModel.rootState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when(state) {
            is RootState.NavigateToAuth -> onNavigateToAuth()
            is RootState.NavigateToHome -> onNavigateToHome()
            else -> Unit
        }
    }

    TuneVaultTheme{
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when(state) {
                    is RootState.Loading -> CircularProgressIndicator()
                    else -> Unit
                }
            }
        }
    }
}