package com.example.tunevault.presentation.root

sealed interface RootState {
    data object Initial: RootState
    data object Loading: RootState
    data object NavigateToAuth: RootState
    data object NavigateToHome: RootState
}