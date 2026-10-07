package com.example.tunevault.domain.repository

import com.example.tunevault.domain.model.AuthToken

interface AuthRepository {
    suspend fun saveToken(token: AuthToken)
    suspend fun getToken(): AuthToken?
    suspend fun hasValidToken(): Boolean
    suspend fun logout()
}