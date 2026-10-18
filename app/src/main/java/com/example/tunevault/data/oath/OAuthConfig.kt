package com.example.tunevault.data.oath

import com.example.tunevault.BuildConfig
import javax.inject.Inject

class OAuthConfig @Inject constructor() {
    val clientId: String = BuildConfig.JAMENDO_CLIENT_ID
    val redirectUri: String = BuildConfig.JAMENDO_REDIRECT_URI
    val clientSecret: String = BuildConfig.JAMENDO_CLIENT_SECRET
}