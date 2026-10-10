package com.example.tunevault.data.local_source

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object TokenKeys {
    val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    val EXPIRES_AT_KEY = longPreferencesKey("expires_at")
    val SCOPE_KEY = stringPreferencesKey("scope")
    val TOKEN_TYPE_KEY = stringPreferencesKey("token_type")
}