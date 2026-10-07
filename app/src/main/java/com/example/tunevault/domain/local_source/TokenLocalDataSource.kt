package com.example.tunevault.domain.local_source
import com.example.tunevault.domain.model.AuthToken

interface TokenLocalDataSource {
    suspend fun saveToken(token: AuthToken)
    suspend fun getToken() : AuthToken?
    suspend fun clear()
    suspend fun hasToken() : Boolean
}