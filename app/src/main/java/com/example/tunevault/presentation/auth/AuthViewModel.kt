package com.example.tunevault.presentation.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tunevault.data.oath.AuthManager
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun onLoginClick(): String{
        _authState.value = AuthState.Loading
        return authManager.buildAuthUrl()
    }

    fun onAuthCodeReceived(uri: Uri) {
        _authState.value = AuthState.Loading
        val redirect = authManager.parseRedirect(uri)
        if (!authManager.validateState(redirect.state)) {
            _authState.value = AuthState.Error("Ошибка при редиректе")
            return
        }
        println("Code: $redirect")

        val code = redirect.code
        if (code.isNullOrBlank()) {
            _authState.value = AuthState.Error("Код авторизации пустой")
            return
        }

        viewModelScope.launch {
            when (val result = authRepository.exchangeCode(redirect.code)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Authenticated(
                        accessToken = result.token.accessToken,
                        refreshToken = result.token.refreshToken
                    )
                }
                is AuthResult.Failure -> {
                    _authState.value = AuthState.Error(mapErrorToMessage(result.error))
                }
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
}