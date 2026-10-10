package com.example.tunevault.di

import com.example.tunevault.data.local_source.TokenLocalDataSourceImpl
import com.example.tunevault.data.remote_source.TokenRemoteDataSource
import com.example.tunevault.data.remote_source.TokenRemoteDataSourceImpl
import com.example.tunevault.data.repository_impl.AuthRepositoryImpl
import com.example.tunevault.domain.local_source.TokenLocalDataSource
import com.example.tunevault.domain.repository.AuthRepository
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
    ) : TokenLocalDataSource

    @Binds
    @Singleton
    abstract fun bindTokenRemoteDataSource(
        impl: TokenRemoteDataSourceImpl
    ) : TokenRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ) : AuthRepository
}