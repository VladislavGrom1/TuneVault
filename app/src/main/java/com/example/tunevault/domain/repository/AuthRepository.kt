package com.example.tunevault.domain.repository

import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.model.AuthToken

interface AuthRepository {
    suspend fun exchangeCode(code: String): AuthResult
    suspend fun refreshToken(): AuthResult
    suspend fun saveToken(token: AuthToken)
    suspend fun getToken(): AuthToken?
    suspend fun hasValidToken(): Boolean
    suspend fun logout()
}