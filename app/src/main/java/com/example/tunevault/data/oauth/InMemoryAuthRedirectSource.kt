package com.example.tunevault.data.oauth

import com.example.tunevault.domain.auth.AuthRedirect
import com.example.tunevault.domain.auth.AuthRedirectSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class InMemoryAuthRedirectSource @Inject constructor() : AuthRedirectSource {

    private val channel = Channel<AuthRedirect>(Channel.BUFFERED)

    override val redirects: Flow<AuthRedirect> = channel.receiveAsFlow()

    override fun submit(redirect: AuthRedirect) {
        channel.trySend(redirect)
    }
}
