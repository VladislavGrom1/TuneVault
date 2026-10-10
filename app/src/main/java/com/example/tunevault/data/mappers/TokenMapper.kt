package com.example.tunevault.data.mappers

import com.example.tunevault.domain.model.AuthToken
import com.example.tunevault.data.dto.TokenResponseDto

fun TokenResponseDto.toDomain(): AuthToken = AuthToken(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresAt = System.currentTimeMillis() + expiresIn * 1000L,
    scope = scope,
    tokenType = tokenType
)