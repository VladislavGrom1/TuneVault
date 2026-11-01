package com.example.tunevault.presentation.session

import androidx.lifecycle.ViewModel
import com.example.tunevault.domain.model.SessionState
import com.example.tunevault.domain.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Activity-scoped ViewModel: поставляет состояние сессии хосту навигации.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    sessionRepository: SessionRepository,
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = sessionRepository.sessionState
}
