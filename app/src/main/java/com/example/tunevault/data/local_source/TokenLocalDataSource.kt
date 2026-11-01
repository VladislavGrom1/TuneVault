package com.example.tunevault.data.local_source

import com.example.tunevault.domain.model.AuthToken
import kotlinx.coroutines.flow.Flow

interface TokenLocalDataSource {

    val token: Flow<AuthToken?>

    suspend fun getToken(): AuthToken?

    suspend fun saveToken(token: AuthToken)

    suspend fun clear()
}
