package com.example.tunevault.presentation.auth

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tunevault.data.oath.AuthManager
import com.example.tunevault.data.oath.AuthRedirectBus
import com.example.tunevault.domain.model.AuthError
import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val authRepository: AuthRepository,
    redirectBus: AuthRedirectBus,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            redirectBus.redirects.collect { onAuthRedirect(it) }
        }
    }

    private companion object {
        const val KEY_PENDING_STATE = "pending_state"
    }

    fun onLoginClick(): String{
        val state = authManager.generateState()
        savedStateHandle[KEY_PENDING_STATE] = state
        _authState.value = AuthState.Loading
        return authManager.buildAuthUrl(state)
    }

    private fun onAuthRedirect(uri: Uri) {
        val redirect = authManager.parseRedirect(uri)
        val expectedState = savedStateHandle.remove<String>(KEY_PENDING_STATE)

        if (expectedState == null || redirect.state != expectedState) {
            _authState.value = AuthState.Error(mapErrorToMessage(AuthError.StateMismatch))
            return
        }

        if (redirect.error != null) {
            _authState.value = AuthState.Error(mapErrorToMessage(AuthError.AccessDenied))
            return
        }

        val code = redirect.code
        if (code.isNullOrBlank()) {
            _authState.value = AuthState.Error("Код авторизации пустой")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            _authState.value = authRepository.exchangeCode(code).toState()
        }
    }

    fun refreshToken() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            _authState.value = authRepository.refreshToken().toState()
        }
    }

    private fun AuthResult.toState(): AuthState = when (this) {
        is AuthResult.Success -> AuthState.Authenticated(token.accessToken, token.refreshToken)
        is AuthResult.Failure -> AuthState.Error(mapErrorToMessage(error))
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
}