package com.example.tunevault.data.oauth

import android.util.Base64
import androidx.core.net.toUri
import com.example.tunevault.domain.auth.OAuthUrlProvider
import java.security.SecureRandom
import javax.inject.Inject

class JamendoOAuthUrlProvider @Inject constructor(
    private val oauthConfig: OAuthConfig,
) : OAuthUrlProvider {

    override fun generateState(): String {
        val bytes = ByteArray(STATE_SIZE_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)
    }

    override fun buildAuthorizationUrl(state: String): String =
        AUTHORIZE_URL.toUri()
            .buildUpon()
            .appendQueryParameter("client_id", oauthConfig.clientId)
            .appendQueryParameter("redirect_uri", oauthConfig.redirectUri)
            .appendQueryParameter("scope", "music")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("state", state)
            .build()
            .toString()

    private companion object {
        const val AUTHORIZE_URL = "https://api.jamendo.com/v3.0/oauth/authorize"
        const val STATE_SIZE_BYTES = 32
    }
}
