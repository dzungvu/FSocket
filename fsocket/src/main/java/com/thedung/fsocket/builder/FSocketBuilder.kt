package com.thedung.fsocket.builder

import com.thedung.fsocket.FSocket
import com.thedung.fsocket.FSocketAPI
import com.thedung.fsocket.utils.Constants
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class FSocketBuilder(val url: String) {
    var connectionTimeout: Long = Constants.CONNECTION_TIMEOUT
    var readTimeout: Long = Constants.READ_TIMEOUT
    var isAutoReconnect: Boolean = true

    fun build(): FSocketAPI {
        val client = OkHttpClient().newBuilder()
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
            .build()
        return FSocket(
            url, client
        )
    }
}