package com.thedung.fsocket.builder

import com.thedung.fsocket.FSocket
import com.thedung.fsocket.utils.Constants
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class FSocketBuilder(private val url: String) {
    private var timeRetryInterval: Long = Constants.TIME_RETRY_SOCKET_MS
    private var maxCountRetry: Long = Constants.MAX_RETRY_COUNT

    private var connectionTimeout: Long = Constants.CONNECTION_TIMEOUT
    private var readTimeout: Long = Constants.READ_TIMEOUT
    private var pingInterval: Long = Constants.PING_INTERVAL
    private var headers: MutableMap<String, String> = mutableMapOf()

    init {
        if (!url.startsWith("ws:") && !url.startsWith("wss:")) {
            throw IllegalArgumentException("WebSocket must be started with ws or wss. Please re-check. Your url: $url")
        }
    }

    fun setRetryInterval(time: Long): FSocketBuilder {
        this.timeRetryInterval
        return this
    }

    fun setMaxCountRetry(maxCount: Long): FSocketBuilder {
        this.maxCountRetry
        return this
    }

    fun setConnectionTimeout(time: Long): FSocketBuilder {
        this.connectionTimeout = time
        return this
    }

    fun setReadTimeout(time: Long): FSocketBuilder {
        this.readTimeout = time
        return this
    }

    fun setPingInterval(time: Long): FSocketBuilder {
        this.pingInterval = time
        return this
    }

    fun addHeader(name: String, value: String): FSocketBuilder {
        headers[name] = value
        return this
    }

    fun addHeader(header: Map<String, String>): FSocketBuilder {
        headers.putAll(header)
        return this
    }

    fun build(): FSocket {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .pingInterval(pingInterval, TimeUnit.MILLISECONDS)
            .addInterceptor(logging)
            .build()

        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .build()

        return FSocket(request, httpClient).apply {
            setMaxCountRetry(maxCountRetry)
            setTimeRetryInterval(timeRetryInterval)
        }
    }
}