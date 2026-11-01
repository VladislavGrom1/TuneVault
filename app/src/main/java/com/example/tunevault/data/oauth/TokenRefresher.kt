package com.example.tunevault.data.oauth

import com.example.tunevault.data.remote_source.TokenRemoteDataSource
import com.example.tunevault.data.local_source.TokenLocalDataSource
import com.example.tunevault.domain.model.AuthError
import com.example.tunevault.domain.model.AuthResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class TokenRefresher @Inject constructor(
    private val localSource: TokenLocalDataSource,
    private val remoteSource: TokenRemoteDataSource
) {
    private val mutex = Mutex()

    suspend fun refresh(failedAccessToken: String? = null): AuthResult = mutex.withLock {
        val current = localSource.getToken() ?: return@withLock AuthResult.Failure(AuthError.RefreshTokenRevoked)

        if (failedAccessToken != null && current.accessToken != failedAccessToken) {
            return@withLock AuthResult.Success(current)
        }

        try {
            val newToken = remoteSource.refreshToken(current.refreshToken)
            localSource.saveToken(newToken)
            AuthResult.Success(newToken)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            AuthResult.Failure(AuthError.Network)
        } catch (e: HttpException) {
            val error = when (e.code()) {
                400, 401 -> AuthError.RefreshTokenRevoked
                403 -> AuthError.AccessDenied
                else -> AuthError.Unknown(e.message())
            }
            if (error == AuthError.RefreshTokenRevoked) localSource.clear()
            AuthResult.Failure(error)
        } catch (e: Exception) {
            AuthResult.Failure(AuthError.Unknown(e.message ?: "Unknown"))
        }
    }

}