package com.example.tunevault.data.remote_source

import com.example.tunevault.domain.model.AuthToken

interface TokenRemoteDataSource {
    suspend fun exchangeCode(code: String): AuthToken
    suspend fun refreshToken(refreshToken: String): AuthToken
}