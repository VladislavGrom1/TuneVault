package com.example.tunevault

import com.example.tunevault.data.dto.TokenResponseDto
import com.example.tunevault.data.oath.OAuthConfig
import com.example.tunevault.data.oath.TokenAuthenticator
import com.example.tunevault.data.remote_source.api.TokenService
import com.example.tunevault.domain.local_source.TokenLocalDataSource
import com.example.tunevault.domain.model.AuthToken
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

class TokenAuthenticatorTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var tokenLocalDataSource: TokenLocalDataSource
    private lateinit var tokenService: TokenService
    private lateinit var oauthConfig: OAuthConfig

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        tokenLocalDataSource = mockk(relaxed = true)
        tokenService = mockk()
        oauthConfig = mockk(relaxed = true)

        val authenticator = TokenAuthenticator(tokenLocalDataSource, oauthConfig, tokenService)
        okHttpClient = OkHttpClient.Builder()
            .authenticator(authenticator)
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `when 401 received, authenticator refreshes token and retries request`() = runBlocking {
        val oldToken = AuthToken(
            accessToken = "old_token",
            refreshToken = "refresh_123",
            expiresAt = 0, scope = "music", tokenType = "bearer"
        )

        coEvery { tokenLocalDataSource.getToken() } returns oldToken

        val newTokenDto = TokenResponseDto(
            accessToken = "new_token",
            refreshToken = "new_refresh",
            expiresIn = 7200,
            tokenType = "bearer",
            scope = "music"
        )

        val mockCall = mockk<Call<TokenResponseDto>>()
        every { mockCall.execute() } returns Response.success(newTokenDto)

        every {
            tokenService.refreshTokenSync(any(), any(), any(), any())
        } returns mockCall

        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("Success"))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .header("Authorization", "Bearer old_token")
            .build()

        val response = okHttpClient.newCall(request).execute()

        assertEquals(200, response.code)

        val request1 = mockWebServer.takeRequest()
        val request2 = mockWebServer.takeRequest()

        assertEquals("Bearer old_token", request1.getHeader("Authorization"))
        assertEquals("Bearer new_token", request2.getHeader("Authorization"))

        coVerify { tokenLocalDataSource.saveToken(match { it.accessToken == "new_token" }) }
    }
}