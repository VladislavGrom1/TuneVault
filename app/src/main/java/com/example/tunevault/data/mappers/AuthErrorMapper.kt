package com.example.tunevault.data.mappers

import com.example.tunevault.domain.model.AuthError
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toAuthError(): AuthError = when (this) {
    is IOException -> AuthError.Network
    is HttpException -> when (code()) {
        400 -> AuthError.AuthorizationCodeExpired
        401 -> AuthError.InvalidClient
        else -> AuthError.Unknown(message())
    }
    else -> AuthError.Unknown(message ?: "Unknown")
}
