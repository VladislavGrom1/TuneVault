package com.example.tunevault.domain.session

import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.model.SessionState
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {

    val sessionState: StateFlow<SessionState>

    suspend fun login(authorizationCode: String): AuthResult

    suspend fun logout()
}
