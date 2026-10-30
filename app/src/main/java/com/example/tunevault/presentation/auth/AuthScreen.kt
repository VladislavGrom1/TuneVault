package com.example.tunevault.presentation.auth

import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit
) {
    val state by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val processLifecycleOwner = ProcessLifecycleOwner.get()

    LaunchedEffect(state) {
        if (state is AuthState.Authenticated) onNavigateHome()
    }

    DisposableEffect(processLifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAppReturnedToForeground()
            }
        }
        processLifecycleOwner.lifecycle.addObserver(observer)
        onDispose { processLifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val onLoginClick: () -> Unit = {
        val url = viewModel.onLoginClick()
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(context, url.toUri())
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val currentState = state) {
                is AuthState.Initial -> InitialContent(
                    onLoginClick = onLoginClick
                )
                is AuthState.AwaitingRedirect -> AwaitingRedirectContent()
                is AuthState.Loading -> LoadingContent()
                is AuthState.Authenticated -> Unit
                is AuthState.Error -> ErrorContent(
                    state = currentState,
                    onLoginClick = onLoginClick
                )
            }
        }
    }
}

@Composable
private fun InitialContent(
    onLoginClick: () -> Unit
) {
    Text(
        text = "Авторизация",
        style = MaterialTheme.typography.headlineSmall
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = onLoginClick
    ) {
        Text("Войти")
    }
}

@Composable
private fun AwaitingRedirectContent() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "Ожидание входа в браузере",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun LoadingContent() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "Завершение входа",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ErrorContent(
    state: AuthState.Error,
    onLoginClick: () -> Unit
) {
    Text(
        text = state.error,
        modifier = Modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = onLoginClick
    ) {
        Text("Повторить")
    }
}
