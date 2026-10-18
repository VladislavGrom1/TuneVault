package com.example.tunevault.data.remote_source.api

import com.example.tunevault.data.dto.TokenResponseDto
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TokenService {
    @FormUrlEncoded
    @POST("oauth/grant")
    fun refreshTokenSync(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Call<TokenResponseDto>
}