package com.example.tunevault.domain.model

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data object Authenticated : SessionState
}
