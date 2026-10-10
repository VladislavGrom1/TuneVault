package com.example.tunevault.domain.model

sealed interface AuthResult {
    data class Success(val token: AuthToken) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}