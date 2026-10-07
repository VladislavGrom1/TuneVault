package com.example.tunevault.domain.model

data class AuthToken (
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val scope: String,
    val tokenType: String = "bearer"
) {
    val isExpired: Boolean get() = System.currentTimeMillis() >= expiresAt - 60_000

    val authorizationHeader: String get() = "$tokenType $accessToken"
}