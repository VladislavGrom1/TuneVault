package com.example.tunevault.data.oath

import android.net.Uri
import android.util.Base64
import javax.inject.Inject
import androidx.core.net.toUri
import com.example.tunevault.BuildConfig
import java.security.SecureRandom

class AuthManager @Inject constructor() {
    private val clientId: String = BuildConfig.JAMENDO_CLIENT_ID
    private val redirectUri: String = BuildConfig.JAMENDO_REDIRECT_URI
    private var pendingState: String? = null

    fun buildAuthUrl(): String {
        val state = generateState()
        pendingState = state
        return "https://api.jamendo.com/v3.0/oauth/authorize".toUri()
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("scope", "music")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("state", state)
            .build()
            .toString()
    }

    fun parseRedirect(uri: Uri): AuthorizationRedirect {
        return AuthorizationRedirect(
            code = uri.getQueryParameter("code"),
            state = uri.getQueryParameter("state"),
            error = uri.getQueryParameter("error"),
            errorDescription = uri.getQueryParameter("error_description")
        )
    }

    fun validateState(received: String?): Boolean {
        val isValid = received != null && received == pendingState
        pendingState = null
        return isValid
    }

    private fun generateState(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)
    }
}