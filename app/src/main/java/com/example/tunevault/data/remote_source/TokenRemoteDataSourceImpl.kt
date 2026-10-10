package com.example.tunevault.data.remote_source

import com.example.tunevault.BuildConfig
import com.example.tunevault.data.mappers.toDomain
import com.example.tunevault.data.remote_source.api.JamendoApi
import com.example.tunevault.domain.model.AuthToken
import javax.inject.Inject

class TokenRemoteDataSourceImpl @Inject constructor(
    private val api: JamendoApi
) : TokenRemoteDataSource {

    private val clientId: String = BuildConfig.JAMENDO_CLIENT_ID
    private val clientSecret: String = BuildConfig.JAMENDO_CLIENT_SECRET
    private val redirectUri: String = BuildConfig.JAMENDO_REDIRECT_URI

    override suspend fun exchangeCode(code: String): AuthToken {
        val dto = api.exchangeCode(
            clientId = clientId,
            clientSecret = clientSecret,
            grantType = "authorization_code",
            code = code,
            redirectUri = redirectUri
        )
        return dto.toDomain()
    }

    override suspend fun refreshToken(refreshToken: String): AuthToken {
        val dto = api.refreshToken(
            clientId = clientId,
            clientSecret = clientSecret,
            grantType = "refresh_token",
            refreshToken = refreshToken
        )
        return dto.toDomain()
    }

}