package com.example.tunevault.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tunevault.presentation.theme.TuneVaultTheme

@Composable
fun HomeScreen(
    onNavigateAuth: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()

    LaunchedEffect(homeState) {
        if(homeState is HomeState.LoggedOut){
            onNavigateAuth()
        }
    }

    when(homeState) {
        is HomeState.Initial -> Content(
            logout = { viewModel.logout() }
        )
        is HomeState.Loading -> CircularProgressIndicator()
        is HomeState.LoggedOut -> Unit
        is HomeState.Error -> Text(
            text = (homeState as HomeState.Error).message,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun Content(
    logout: () -> Unit
) {
    TuneVaultTheme() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Главный экран",
                    style = MaterialTheme.typography.headlineSmall
                )
                Button(
                    onClick = {
                        logout()
                    }
                ) {
                    Text("Выйти")
                }
            }
        }
    }
}