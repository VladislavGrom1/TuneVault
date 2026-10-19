package com.example.tunevault.data.oath

import android.net.Uri
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRedirectBus @Inject constructor() {
    private val channel = Channel<Uri>(Channel.BUFFERED)
    val redirects: Flow<Uri> = channel.receiveAsFlow()

    fun post(uri: Uri) {
        channel.trySend(uri)
    }
}