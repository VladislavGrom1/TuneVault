package com.example.tunevault.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tunevault.domain.model.AuthToken
import com.example.tunevault.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Initial)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun saveTestToken() {
        _homeState.value = HomeState.Loading
        viewModelScope.launch {
            try {
                val testToken = AuthToken(
                    accessToken = "test_access_${System.currentTimeMillis()}",
                    refreshToken = "test_refresh_${System.currentTimeMillis()}",
                    expiresAt = System.currentTimeMillis() + 7200_000L,
                    scope = "music",
                    tokenType = "bearer"
                )
                authRepository.saveToken(testToken)
                _homeState.value = HomeState.Initial
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(
                    error = e.message ?: "Неизвестная ошибка сохранения"
                )
            }
        }
    }

    fun loadToken() {
        _homeState.value = HomeState.Loading
        viewModelScope.launch {
            try {
                val saved = authRepository.getToken()?.accessToken
                _homeState.value = HomeState.Loaded(savedToken = saved)
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(
                    error = e.message ?: "Неизвестная ошибка загрузки"
                )
            }
        }
    }

    fun clearToken() {
        _homeState.value = HomeState.Loading
        viewModelScope.launch {
            try {
                authRepository.logout()
                _homeState.value = HomeState.Loaded(savedToken = null)
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(
                    error = e.message ?: "Неизвестная ошибка очистки"
                )
            }
        }
    }
}