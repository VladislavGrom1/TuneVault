package com.example.tunevault.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tunevault.domain.local_source.TokenLocalDataSource
import com.example.tunevault.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel() {
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Initial)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _homeState.value = HomeState.Loading
            try{
                authRepository.logout()
                _homeState.value = HomeState.LoggedOut
            } catch (e: Exception) {
                _homeState.value = HomeState.Error(e.message ?: "Ошибка вывода")
            }
        }
    }
}