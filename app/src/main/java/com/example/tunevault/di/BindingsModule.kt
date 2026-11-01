package com.example.tunevault.di

import com.example.tunevault.data.local_source.TokenLocalDataSource
import com.example.tunevault.data.local_source.TokenLocalDataSourceImpl
import com.example.tunevault.data.oauth.InMemoryAuthRedirectSource
import com.example.tunevault.data.oauth.JamendoOAuthUrlProvider
import com.example.tunevault.data.remote_source.TokenRemoteDataSource
import com.example.tunevault.data.remote_source.TokenRemoteDataSourceImpl
import com.example.tunevault.data.session.DefaultSessionRepository
import com.example.tunevault.domain.auth.AuthRedirectSource
import com.example.tunevault.domain.auth.OAuthUrlProvider
import com.example.tunevault.domain.session.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindTokenLocalDataSource(
        impl: TokenLocalDataSourceImpl
    ): TokenLocalDataSource

    @Binds
    @Singleton
    abstract fun bindTokenRemoteDataSource(
        impl: TokenRemoteDataSourceImpl
    ): TokenRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: DefaultSessionRepository
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRedirectSource(
        impl: InMemoryAuthRedirectSource
    ): AuthRedirectSource

    @Binds
    abstract fun bindOAuthUrlProvider(
        impl: JamendoOAuthUrlProvider
    ): OAuthUrlProvider
}
