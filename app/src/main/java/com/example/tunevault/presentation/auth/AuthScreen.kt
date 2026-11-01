package com.example.tunevault.presentation.auth

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.OpenBrowser -> context.launchCustomTab(event.url)
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val state = uiState) {
                is AuthUiState.Idle -> LoginContent(
                    onLoginClick = viewModel::onLoginClicked,
                )

                is AuthUiState.AwaitingBrowser -> AwaitingBrowserContent(
                    onCancelClick = viewModel::onLoginCancelled,
                )

                is AuthUiState.Verifying -> VerifyingContent()

                is AuthUiState.Error -> ErrorContent(
                    message = state.error,
                    onRetryClick = viewModel::onLoginClicked,
                )
            }
        }
    }
}

@Composable
private fun LoginContent(onLoginClick: () -> Unit) {
    Text(
        text = "Авторизация",
        style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = onLoginClick) {
        Text("Войти")
    }
}

@Composable
private fun AwaitingBrowserContent(onCancelClick: () -> Unit) {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Ожидание входа в браузере",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(modifier = Modifier.height(12.dp))
    TextButton(onClick = onCancelClick) {
        Text("Отмена")
    }
}

@Composable
private fun VerifyingContent() {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Завершение входа",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = onRetryClick) {
        Text("Повторить")
    }
}

private fun Context.launchCustomTab(url: String) {
    val uri = url.toUri()
    try {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(this, uri)
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
