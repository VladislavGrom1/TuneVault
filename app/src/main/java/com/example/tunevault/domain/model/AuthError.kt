package com.example.tunevault.domain.model

sealed interface AuthError {
    data object AccessDenied : AuthError
    data object StateMismatch : AuthError
    data object AuthorizationCodeExpired : AuthError
    data object InvalidClient : AuthError
    data object RefreshTokenRevoked : AuthError
    data object Network : AuthError
    data class Unknown(val message: String) : AuthError
}