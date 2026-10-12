package com.example.tunevault.presentation.home

sealed interface HomeState {
    data object Initial : HomeState
    data object Loading : HomeState
    data object LoggedOut : HomeState
    data class Error(val message: String) : HomeState
}