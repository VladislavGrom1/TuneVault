package com.example.tunevault.di

import com.example.tunevault.BuildConfig
import com.example.tunevault.data.oauth.AuthInterceptor
import com.example.tunevault.data.oauth.TokenAuthenticator
import com.example.tunevault.data.remote_source.api.TokenService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.jamendo.com/v3.0/"

    @Provides
    @Singleton
    @Named("AuthClient")
    fun provideAuthOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .debugLogging()
        .build()

    @Provides
    @Singleton
    fun provideMainOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .debugLogging()
        .build()

    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(@Named("AuthClient") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideMainRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTokenService(@Named("AuthRetrofit") retrofit: Retrofit): TokenService =
        retrofit.create(TokenService::class.java)

    private fun OkHttpClient.Builder.debugLogging(): OkHttpClient.Builder = apply {
        if (BuildConfig.DEBUG) {
            addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
        }
    }
}