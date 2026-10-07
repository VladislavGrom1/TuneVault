package com.example.tunevault.presentation.home

sealed interface HomeState{
    data object Initial: HomeState
    data object Loading: HomeState
    data class Error(val error: String) : HomeState
    data class Loaded(val savedToken: String?) : HomeState
}