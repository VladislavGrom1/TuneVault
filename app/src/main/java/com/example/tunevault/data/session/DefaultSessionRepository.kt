package com.example.tunevault.data.session

import com.example.tunevault.data.local_source.TokenLocalDataSource
import com.example.tunevault.data.mappers.toAuthError
import com.example.tunevault.data.oauth.TokenRefresher
import com.example.tunevault.data.remote_source.TokenRemoteDataSource
import com.example.tunevault.di.ApplicationScope
import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.model.SessionState
import com.example.tunevault.domain.session.SessionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultSessionRepository @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val tokenRemoteDataSource: TokenRemoteDataSource,
    private val tokenRefresher: TokenRefresher,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : SessionRepository {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        applicationScope.launch { observeSession() }
    }

    override suspend fun login(authorizationCode: String): AuthResult {
        return try {
            val token = tokenRemoteDataSource.exchangeCode(authorizationCode)
            tokenLocalDataSource.saveToken(token)
            AuthResult.Success(token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }
    }

    override suspend fun logout() {
        tokenLocalDataSource.clear()
    }

    private suspend fun observeSession() {
        try {
            restoreSession()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Не смогли прочитать хранилище — считаем, что сессии нет.
            // Пользователь сможет войти заново.
            _sessionState.value = SessionState.LoggedOut
        }

        tokenLocalDataSource.token.collect { token ->
            _sessionState.value = if (token == null) {
                SessionState.LoggedOut
            } else {
                SessionState.Authenticated
            }
        }
    }

    private suspend fun restoreSession() {
        val token = tokenLocalDataSource.getToken() ?: return
        if (token.isExpired) {
            tokenRefresher.refresh(token.accessToken)
        }
    }
}
