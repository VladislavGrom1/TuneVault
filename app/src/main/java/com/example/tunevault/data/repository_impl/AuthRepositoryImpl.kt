package com.example.tunevault.data.repository_impl

import com.example.tunevault.domain.local_source.TokenLocalDataSource
import com.example.tunevault.domain.model.AuthToken
import com.example.tunevault.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource
) : AuthRepository {
    override suspend fun getToken(): AuthToken? {
        return tokenLocalDataSource.getToken()
    }

    override suspend fun saveToken(token: AuthToken) {
        tokenLocalDataSource.saveToken(token)
    }

    override suspend fun hasValidToken(): Boolean {
        return tokenLocalDataSource.hasToken()
    }

    override suspend fun logout() {
        tokenLocalDataSource.clear()
    }
}