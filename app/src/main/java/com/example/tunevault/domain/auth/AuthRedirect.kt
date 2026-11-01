package com.example.tunevault.domain.auth


data class AuthRedirect(
    val code: String?,
    val state: String?,
    val error: String?,
    val errorDescription: String?,
)
