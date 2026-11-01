package com.example.tunevault.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tunevault.domain.auth.AuthRedirect
import com.example.tunevault.domain.auth.AuthRedirectSource
import com.example.tunevault.domain.auth.OAuthUrlProvider
import com.example.tunevault.domain.model.AuthError
import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val oauthUrlProvider: OAuthUrlProvider,
    private val authRedirectSource: AuthRedirectSource,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            authRedirectSource.redirects.collect(::handleRedirect)
        }
    }

    fun onLoginClicked() {
        when (_uiState.value) {
            is AuthUiState.AwaitingBrowser, is AuthUiState.Verifying -> return
            else -> Unit
        }

        val state = oauthUrlProvider.generateState()
        savedStateHandle[KEY_PENDING_STATE] = state
        _uiState.value = AuthUiState.AwaitingBrowser
        _events.trySend(AuthEvent.OpenBrowser(oauthUrlProvider.buildAuthorizationUrl(state)))
    }

    fun onLoginCancelled() {
        savedStateHandle.remove<String>(KEY_PENDING_STATE)
        _uiState.value = AuthUiState.Idle
    }

    private suspend fun handleRedirect(redirect: AuthRedirect) {
        // Redirect без начатого входа игнорируем: это может быть запоздавшее событие.
        val expectedState = savedStateHandle.remove<String>(KEY_PENDING_STATE) ?: return

        if (redirect.state != expectedState) {
            _uiState.value = AuthUiState.Error(mapErrorToMessage(AuthError.StateMismatch))
            return
        }

        if (redirect.error != null) {
            _uiState.value = AuthUiState.Error(mapErrorToMessage(AuthError.AccessDenied))
            return
        }

        val code = redirect.code
        if (code.isNullOrBlank()) {
            _uiState.value = AuthUiState.Error("Код авторизации пустой")
            return
        }

        _uiState.value = AuthUiState.Verifying

        // Успех навигации обрабатывает AppNavHost через SessionState.Authenticated.
        when (val result = sessionRepository.login(code)) {
            is AuthResult.Success -> Unit
            is AuthResult.Failure -> {
                _uiState.value = AuthUiState.Error(mapErrorToMessage(result.error))
            }
        }
    }

    private fun mapErrorToMessage(error: AuthError): String = when (error) {
        AuthError.AccessDenied -> "Вы отклонили доступ"
        AuthError.StateMismatch -> "Ошибка безопасности: state не совпал"
        AuthError.AuthorizationCodeExpired -> "Код авторизации истёк, попробуйте снова"
        AuthError.InvalidClient -> "Неверный client_id или client_secret"
        AuthError.RefreshTokenRevoked -> "Доступ отозван, войдите заново"
        AuthError.Network -> "Нет подключения к интернету"
        is AuthError.Unknown -> "Ошибка: ${error.message}"
    }

    private companion object {
        const val KEY_PENDING_STATE = "pending_state"
    }
}
