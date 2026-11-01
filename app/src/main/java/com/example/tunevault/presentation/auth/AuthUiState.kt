package com.example.tunevault.presentation.auth

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object AwaitingBrowser : AuthUiState
    data object Verifying : AuthUiState
    data class Error(val error: String) : AuthUiState
}
