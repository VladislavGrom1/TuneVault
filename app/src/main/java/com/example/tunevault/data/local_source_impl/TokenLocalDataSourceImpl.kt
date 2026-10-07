package com.example.tunevault.data.local_source_impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.tunevault.domain.local_source.TokenLocalDataSource
import com.example.tunevault.domain.model.AuthToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenLocalDataSourceImpl @Inject constructor(
    private val encryptedDataStore: DataStore<Preferences>
) : TokenLocalDataSource {

    override suspend fun getToken() : AuthToken? {
        return encryptedDataStore.data
            .map { prefs -> prefs.toAuthToken()}
            .first()
    }

    override suspend fun saveToken(token: AuthToken) {
        encryptedDataStore.edit { prefs ->
            prefs[TokenKeys.ACCESS_TOKEN_KEY] = token.accessToken
            prefs[TokenKeys.REFRESH_TOKEN_KEY] = token.refreshToken
            prefs[TokenKeys.EXPIRES_AT_KEY] = token.expiresAt
            prefs[TokenKeys.SCOPE_KEY] = token.scope
            prefs[TokenKeys.TOKEN_TYPE_KEY] = token.tokenType
        }
    }

    override suspend fun clear() {
        encryptedDataStore.edit { it.clear() }
    }

    override suspend fun hasToken(): Boolean = getToken() != null

    private fun Preferences.toAuthToken(): AuthToken? {
        val access = this[TokenKeys.ACCESS_TOKEN_KEY] ?: return null
        val refresh = this[TokenKeys.REFRESH_TOKEN_KEY] ?: return null
        val expiresAt = this[TokenKeys.EXPIRES_AT_KEY] ?: return null

        return AuthToken(
            accessToken = access,
            refreshToken = refresh,
            expiresAt = expiresAt,
            scope = this[TokenKeys.SCOPE_KEY] ?: "",
            tokenType = this[TokenKeys.TOKEN_TYPE_KEY] ?: "bearer"
        )
    }
}