package com.example.tunevault.domain.auth

import kotlinx.coroutines.flow.Flow

interface AuthRedirectSource {

    val redirects: Flow<AuthRedirect>

    fun submit(redirect: AuthRedirect)
}
