package com.example.tunevault.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _rootState = MutableStateFlow<RootState>(RootState.Initial)
    val rootState: StateFlow<RootState> = _rootState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _rootState.value = RootState.Loading
            val token = authRepository.getToken()

            if(token == null || token.refreshToken.isBlank()){
                _rootState.value = RootState.NavigateToAuth
                return@launch
            }

            if(!token.isExpired){
                _rootState.value = RootState.NavigateToHome
                return@launch
            }

            when (val result = authRepository.refreshToken()) {
                is AuthResult.Success -> {
                    _rootState.value = RootState.NavigateToHome
                }
                is AuthResult.Failure -> {
                    _rootState.value = RootState.NavigateToAuth
                }
            }
        }
    }
}