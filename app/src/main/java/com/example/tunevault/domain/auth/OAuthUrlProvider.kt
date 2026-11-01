package com.example.tunevault.domain.auth

interface OAuthUrlProvider {

    fun generateState(): String

    fun buildAuthorizationUrl(state: String): String
}
