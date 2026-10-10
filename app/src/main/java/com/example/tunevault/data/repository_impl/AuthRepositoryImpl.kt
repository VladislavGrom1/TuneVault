package com.example.tunevault.data.repository_impl

import com.example.tunevault.data.remote_source.TokenRemoteDataSource
import com.example.tunevault.domain.local_source.TokenLocalDataSource
import com.example.tunevault.domain.model.AuthError
import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.model.AuthToken
import com.example.tunevault.domain.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val tokenRemoteDataSource: TokenRemoteDataSource
) : AuthRepository {

    override suspend fun exchangeCode(code: String): AuthResult {
        return try {
            val token = tokenRemoteDataSource.exchangeCode(code)
            tokenLocalDataSource.saveToken(token)
            AuthResult.Success(token)
        } catch (e: IOException) {
            AuthResult.Failure(AuthError.Network)
        } catch (e: HttpException) {
            val error = when (e.code()) {
                400 -> AuthError.AuthorizationCodeExpired
                401 -> AuthError.InvalidClient
                else -> AuthError.Unknown(e.message())
            }
            AuthResult.Failure(error)
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Unknown"))
        }
    }

    override suspend fun saveToken(token: AuthToken) {
        tokenLocalDataSource.saveToken(token)
    }

    override suspend fun refreshToken(): AuthResult {
        TODO("Not yet implemented")
    }

    override suspend fun getToken(): AuthToken? {
        return tokenLocalDataSource.getToken()
    }

    override suspend fun hasValidToken(): Boolean {
        val token = tokenLocalDataSource.getToken() ?: return false
        return !token.isExpired
    }

    override suspend fun logout() {
        tokenLocalDataSource.clear()
    }
}