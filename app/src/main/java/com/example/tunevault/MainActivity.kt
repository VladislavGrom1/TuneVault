package com.example.tunevault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tunevault.presentation.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authCallbackFlow = MutableSharedFlow<Uri>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            AppNavHost(authCallbackFlow = authCallbackFlow)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("OAuth", "onNewIntent called")
        setIntent(intent)
        handleIntent(intent)
        intent.data = null
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: run {
            Log.d("OAuth", "handleIntent: data = null")
            return
        }
        Log.d("OAuth", "handleIntent: data = $data")
        if (data.scheme == "com.tunevault.app" && data.host == "oauth") {
            Log.d("OAuth", "OAuth callback detected: code=${data.getQueryParameter("code")}")
            authCallbackFlow.tryEmit(data)
        }
    }
}