package com.example.tunevault.presentation.auth

sealed interface AuthEvent {
    data class OpenBrowser(val url: String) : AuthEvent
}
