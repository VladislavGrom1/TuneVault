package com.example.tunevault.data.oath

import com.example.tunevault.domain.model.AuthResult
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenRefresher: TokenRefresher
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ") ?: return null

        return when (val result = runBlocking { tokenRefresher.refresh(failedToken) }) {
            is AuthResult.Success -> response.request.newBuilder()
                .header("Authorization", "Bearer ${result.token.accessToken}")
                .build()
            is AuthResult.Failure -> null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }
}