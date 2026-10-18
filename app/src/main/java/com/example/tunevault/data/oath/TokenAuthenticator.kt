package com.example.tunevault.data.oath

import com.example.tunevault.BuildConfig
import com.example.tunevault.data.mappers.toDomain
import com.example.tunevault.data.remote_source.api.TokenService
import com.example.tunevault.domain.local_source.TokenLocalDataSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val oauthConfig: OAuthConfig,
    private val tokenService: TokenService
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        return runBlocking {
            mutex.withLock {
                val currentToken = tokenLocalDataSource.getToken()
                val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

                if (currentToken != null && currentToken.accessToken != failedToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer ${currentToken.accessToken}")
                        .build()
                }

                val refreshToken = currentToken?.refreshToken ?: return@withLock null

                try {
                    val newTokenDto = tokenService.refreshTokenSync(
                        clientId = oauthConfig.clientId,
                        clientSecret = oauthConfig.clientSecret,
                        refreshToken = refreshToken
                    ).execute().body() ?: return@withLock null

                    val newToken = newTokenDto.toDomain()
                    tokenLocalDataSource.saveToken(newToken)

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newToken.accessToken}")
                        .build()
                } catch (e: Exception) {
                    tokenLocalDataSource.clear()
                    null
                }
            }
        }

    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

}