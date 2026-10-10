package com.example.tunevault.presentation.auth

sealed interface AuthState{
    data object Initial : AuthState
    data object Loading : AuthState
    data class Authenticated(
        val accessToken: String,
        val refreshToken: String
    ) : AuthState
    data class Error(val error: String) : AuthState
}