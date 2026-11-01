package com.example.tunevault

import com.example.tunevault.data.oauth.TokenAuthenticator
import com.example.tunevault.data.oauth.TokenRefresher
import com.example.tunevault.domain.model.AuthError
import com.example.tunevault.domain.model.AuthResult
import com.example.tunevault.domain.model.AuthToken
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenRefresher: TokenRefresher
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        tokenRefresher = mockk()
        client = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(tokenRefresher))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `401 refreshes token and retries request once`() = runBlocking {
        coEvery { tokenRefresher.refresh(OLD_TOKEN) } returns AuthResult.Success(NEW_TOKEN)

        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(200).setBody("OK"))

        val response = client.newCall(authenticatedRequest()).execute()

        assertEquals(200, response.code)
        assertEquals("Bearer $OLD_TOKEN", server.takeRequest().getHeader("Authorization"))
        assertEquals(
            "Bearer ${NEW_TOKEN.accessToken}",
            server.takeRequest().getHeader("Authorization"),
        )
        coVerify(exactly = 1) { tokenRefresher.refresh(OLD_TOKEN) }
    }

    @Test
    fun `repeated 401 does not loop refreshes`() = runBlocking {
        coEvery { tokenRefresher.refresh(any()) } returns AuthResult.Success(NEW_TOKEN)

        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))

        val response = client.newCall(authenticatedRequest()).execute()

        assertEquals(401, response.code)
        coVerify(exactly = 1) { tokenRefresher.refresh(any()) }
    }

    @Test
    fun `refresh failure returns the original 401`() = runBlocking {
        coEvery { tokenRefresher.refresh(any()) } returns
            AuthResult.Failure(AuthError.RefreshTokenRevoked)

        server.enqueue(MockResponse().setResponseCode(401))

        val response = client.newCall(authenticatedRequest()).execute()

        assertEquals(401, response.code)
        assertEquals(1, server.requestCount)
    }

    private fun authenticatedRequest(): Request =
        Request.Builder()
            .url(server.url("/tracks"))
            .header("Authorization", "Bearer $OLD_TOKEN")
            .build()

    private companion object {
        const val OLD_TOKEN = "old_token"
        val NEW_TOKEN = AuthToken(
            accessToken = "new_token",
            refreshToken = "new_refresh",
            expiresAt = Long.MAX_VALUE,
            scope = "music",
        )
    }
}
