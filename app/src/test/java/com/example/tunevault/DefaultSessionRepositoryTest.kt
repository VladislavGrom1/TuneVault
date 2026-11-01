package com.example.tunevault

import com.example.tunevault.data.local_source.TokenLocalDataSource
import com.example.tunevault.data.oauth.TokenRefresher
import com.example.tunevault.data.remote_source.TokenRemoteDataSource
import com.example.tunevault.data.session.DefaultSessionRepository
import com.example.tunevault.domain.model.AuthError
import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.model.AuthToken
import com.example.tunevault.domain.model.SessionState
import com.example.tunevault.domain.session.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class DefaultSessionRepositoryTest {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    private val tokenStore = FakeTokenLocalDataSource()
    private val tokenRemoteDataSource = mockk<TokenRemoteDataSource>()
    private val tokenRefresher = mockk<TokenRefresher>()

    @After
    fun tearDown() {
        applicationScope.cancel()
    }

    @Test
    fun `starts logged out when there is no saved token`() {
        val repository = createRepository()

        assertEquals(SessionState.LoggedOut, repository.awaitStableState())
    }

    @Test
    fun `valid saved token restores authenticated session without refresh`() {
        tokenStore.setToken(VALID_TOKEN)

        val repository = createRepository()

        assertEquals(SessionState.Authenticated, repository.awaitStableState())
        coVerify(exactly = 0) { tokenRefresher.refresh(any()) }
    }

    @Test
    fun `expired saved token is refreshed before session is exposed`() {
        val expiredToken = VALID_TOKEN.copy(expiresAt = 0L)
        tokenStore.setToken(expiredToken)
        coEvery { tokenRefresher.refresh(expiredToken.accessToken) } returns
            AuthResult.Success(VALID_TOKEN)

        val repository = createRepository()

        assertEquals(SessionState.Authenticated, repository.awaitStableState())
        coVerify(exactly = 1) { tokenRefresher.refresh(expiredToken.accessToken) }
    }

    @Test
    fun `revoked refresh token leads to logged out state`() {
        tokenStore.setToken(VALID_TOKEN.copy(expiresAt = 0L))
        coEvery { tokenRefresher.refresh(any()) } coAnswers {
            // Настоящий TokenRefresher очищает хранилище при отзыве доступа.
            tokenStore.setToken(null)
            AuthResult.Failure(AuthError.RefreshTokenRevoked)
        }

        val repository = createRepository()

        assertEquals(SessionState.LoggedOut, repository.awaitStableState())
    }

    @Test
    fun `login stores token and switches session to authenticated`() = runBlocking {
        coEvery { tokenRemoteDataSource.exchangeCode(CODE) } returns VALID_TOKEN
        val repository = createRepository()
        repository.awaitStableState()

        val result = repository.login(CODE)

        assertTrue(result is AuthResult.Success)
        assertEquals(VALID_TOKEN, tokenStore.getToken())
        assertEquals(SessionState.Authenticated, repository.sessionState.value)
    }

    @Test
    fun `login network failure is mapped to AuthError Network`() = runBlocking {
        coEvery { tokenRemoteDataSource.exchangeCode(any()) } throws IOException()
        val repository = createRepository()

        val result = repository.login(CODE)

        assertEquals(AuthResult.Failure(AuthError.Network), result)
        assertEquals(SessionState.LoggedOut, repository.sessionState.value)
    }

    @Test
    fun `logout clears token and switches session to logged out`() = runBlocking {
        tokenStore.setToken(VALID_TOKEN)
        val repository = createRepository()
        assertEquals(SessionState.Authenticated, repository.awaitStableState())

        repository.logout()

        assertEquals(SessionState.LoggedOut, repository.sessionState.value)
        assertEquals(null, tokenStore.getToken())
    }

    private fun createRepository(): SessionRepository = DefaultSessionRepository(
        tokenLocalDataSource = tokenStore,
        tokenRemoteDataSource = tokenRemoteDataSource,
        tokenRefresher = tokenRefresher,
        applicationScope = applicationScope,
    )

    private fun SessionRepository.awaitStableState(): SessionState = runBlocking {
        withTimeout(STATE_TIMEOUT_MS) {
            sessionState.first { it != SessionState.Loading }
        }
    }

    private class FakeTokenLocalDataSource : TokenLocalDataSource {

        private val state = MutableStateFlow<AuthToken?>(null)

        override val token: Flow<AuthToken?> = state.asStateFlow()

        override suspend fun getToken(): AuthToken? = state.value

        override suspend fun saveToken(token: AuthToken) {
            setToken(token)
        }

        override suspend fun clear() {
            setToken(null)
        }

        fun setToken(token: AuthToken?) {
            state.value = token
        }
    }

    private companion object {
        const val CODE = "authorization_code"
        const val STATE_TIMEOUT_MS = 1_000L

        val VALID_TOKEN = AuthToken(
            accessToken = "access_token",
            refreshToken = "refresh_token",
            expiresAt = Long.MAX_VALUE,
            scope = "music",
        )
    }
}
