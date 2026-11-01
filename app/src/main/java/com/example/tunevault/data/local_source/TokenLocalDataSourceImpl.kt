package com.example.tunevault.data.local_source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.tunevault.domain.model.AuthToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenLocalDataSourceImpl @Inject constructor(
    private val encryptedDataStore: DataStore<Preferences>
) : TokenLocalDataSource {

    override val token: Flow<AuthToken?> = encryptedDataStore.data
        .map { preferences -> preferences.toAuthToken() }
        .distinctUntilChanged()

    override suspend fun getToken(): AuthToken? = token.first()

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
