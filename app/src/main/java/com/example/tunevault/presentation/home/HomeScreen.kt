package com.example.tunevault.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tunevault.presentation.theme.TuneVaultTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.homeState.collectAsStateWithLifecycle()

    TuneVaultTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Тест DataStore + Tink",
                    style = MaterialTheme.typography.headlineSmall
                )

                Button(
                    onClick = { viewModel.saveTestToken() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить тестовый токен")
                }

                Button(
                    onClick = { viewModel.loadToken() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Загрузить токен")
                }

                OutlinedButton(
                    onClick = { viewModel.clearToken() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Очистить хранилище")
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (val currentState = state) {
                    is HomeState.Initial -> {
                        Text(
                            text = "Initial",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is HomeState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Загрузка...")
                        }
                    }

                    is HomeState.Loaded -> {
                        Content(homeState = currentState)
                    }

                    is HomeState.Error -> {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = currentState.error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Content(homeState: HomeState.Loaded) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (homeState.savedToken != null) {
                Text(
                    text = "Токен в хранилище:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = homeState.savedToken,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    text = "Токен не найден",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}