package com.example.tunevault.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tunevault.domain.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onLogoutClicked() {
        if (_uiState.value.isLoggingOut) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, error = null) }
            try {
                // После очистки токенов SessionState станет LoggedOut,
                // и AppNavHost сам уведёт на экран авторизации.
                sessionRepository.logout()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        error = "Не удалось выйти. Попробуйте ещё раз",
                    )
                }
            }
        }
    }
}
