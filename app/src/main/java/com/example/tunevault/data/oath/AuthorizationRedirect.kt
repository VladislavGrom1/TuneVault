package com.example.tunevault.data.oath

data class AuthorizationRedirect (
    val code: String?,
    val state: String?,
    val error: String?,
    val errorDescription: String?,
)