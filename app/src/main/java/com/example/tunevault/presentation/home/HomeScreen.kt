package com.example.tunevault.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateAuth: () -> Unit
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if(state is HomeState.LoggedOut) onNavigateAuth()
    }

    val logout: () -> Unit = {
        viewModel.logout()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when(val currentState = state) {
                is HomeState.Initial -> InitialContent(logout = logout)
                is HomeState.Loading -> LoadingContent()
                is HomeState.LoggedOut -> Unit
                is HomeState.Error -> Unit
            }
        }
    }
}

@Composable
fun InitialContent(
    logout: () -> Unit
) {
    Text(
        text = "Главный экран",
        style = MaterialTheme.typography.headlineSmall
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = logout
    ) {
        Text("Выйти")
    }
}

@Composable
fun LoadingContent(){
    CircularProgressIndicator()
}