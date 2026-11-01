package com.example.tunevault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tunevault.domain.auth.AuthRedirect
import com.example.tunevault.domain.auth.AuthRedirectSource
import com.example.tunevault.presentation.navigation.AppNavHost
import com.example.tunevault.presentation.theme.TuneVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRedirectSource: AuthRedirectSource
    
    private var handledRedirect: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handledRedirect = savedInstanceState?.getString(KEY_HANDLED_REDIRECT)

        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            TuneVaultTheme {
                AppNavHost()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_HANDLED_REDIRECT, handledRedirect)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (!data.isOAuthRedirect()) return

        val redirectKey = data.toString()
        if (redirectKey == handledRedirect) return
        handledRedirect = redirectKey

        authRedirectSource.submit(
            AuthRedirect(
                code = data.getQueryParameter(KEY_CODE),
                state = data.getQueryParameter(KEY_STATE),
                error = data.getQueryParameter(KEY_ERROR),
                errorDescription = data.getQueryParameter(KEY_ERROR_DESCRIPTION),
            )
        )
    }

    private fun Uri.isOAuthRedirect(): Boolean =
        scheme == OAUTH_SCHEME && host == OAUTH_HOST && path == OAUTH_PATH

    private companion object {
        const val OAUTH_SCHEME = "com.tunevault.app"
        const val OAUTH_HOST = "oauth"
        const val OAUTH_PATH = "/callback"

        const val KEY_CODE = "code"
        const val KEY_STATE = "state"
        const val KEY_ERROR = "error"
        const val KEY_ERROR_DESCRIPTION = "error_description"
        const val KEY_HANDLED_REDIRECT = "handled_redirect"
    }
}
