package com.thedung.fsocket

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.thedung.fsocket.events.*
import com.thedung.fsocket.listeners.BaseEventListener
import com.thedung.fsocket.utils.*
import okhttp3.*
import okhttp3.internal.ws.RealWebSocket
import okio.ByteString
import org.json.JSONException
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.math.pow
import kotlin.math.roundToLong

class FSocket(
    private val request: Request,
    private val client: OkHttpClient,
) : FSocketAPI {

    private val TAG = this::class.java.simpleName
    private val CLOSE_REASON = "end of session"
    private val MAX_COLLISION = 7

    private val gson = Gson()
    private val mSocketListener: WebSocketListener by lazy { createWebSocketListener() }

    private var mRealSocket: RealWebSocket? = null
    private var currentState: FSocketState = FSocketState.CLOSED
    private var isForceTerminate = false
    private var mHandler: Handler = Handler(Looper.getMainLooper())
    private var reconnectionAttempts = 0

    private var timeRetry: Long = Constants.TIME_RETRY_SOCKET_MS
    private var maxCountRetry: Long = Constants.MAX_RETRY_COUNT

    private val canSendSocket: Boolean
        get() = currentState == FSocketState.CONNECTED && mRealSocket != null

    private val mEventListener = CopyOnWriteArraySet<BaseEventListener<*>>()

    fun setTimeRetryInterval(timeMs: Long) {
        timeRetry = timeMs
    }

    fun setMaxCountRetry(max: Long) {
        maxCountRetry = max
    }

    override fun connect() {
        if (currentState == FSocketState.CLOSED || mRealSocket == null) {
            mRealSocket = client.newWebSocket(request, mSocketListener) as RealWebSocket
            changeState(FSocketState.CONNECTING)
        }
    }

    override fun send(event: String, data: Any): Boolean {
        if (canSendSocket) {
            return send(event, gson.toJson(data))
        }
        return false
    }

    override fun send(event: String, data: String): Boolean {
        if (canSendSocket) {
            try {
                LogUtil.d(TAG, "Data to send: $data")
                return mRealSocket?.send(data) ?: false
            } catch (ex: JSONException) {
                LogUtil.e(TAG, "Please check your data is in right json format")
            }
        }
        return false
    }

    override fun close() {
        mRealSocket?.close(1000, CLOSE_REASON)
    }

    override fun close(code: Int, reason: String) {
        mRealSocket?.close(code, reason)
    }

    override fun terminate() {
        isForceTerminate = true
        mRealSocket?.cancel()
        mRealSocket = null
        changeState(FSocketState.CLOSED)
    }

    override fun <T : BaseEventListener<*>> addEventListener(listener: T) {
        mEventListener.add(listener)
    }

    override fun <T : BaseEventListener<*>> removeEventListener(listener: T) {
        mEventListener.remove(listener)
    }

    override fun removeAllListener() {
        mEventListener.clear()
    }


    private fun reconnect() {
        if (currentState != FSocketState.CONNECT_ERROR) {
            return
        }
        changeState(FSocketState.RECONNECT_ATTEMPT)
        mRealSocket?.cancel()
        mRealSocket = null

        val collision =
            if (reconnectionAttempts > MAX_COLLISION) MAX_COLLISION
            else reconnectionAttempts

        val delayTime = ((2.0.pow(collision.toDouble()) - 1) / 2).roundToLong() * 1000

        if (reconnectionAttempts < maxCountRetry) {
            mHandler.removeCallbacksAndMessages(null)
            mHandler.postDelayed({
                LogUtil.e(TAG, "Reconnecttttttt")
                changeState(FSocketState.CONNECTING)
                reconnectionAttempts++
                connect()

            }, delayTime)
        }
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                LogUtil.d(TAG, "Socket connected success")
                reconnectionAttempts = 0
                changeState(FSocketState.CONNECTED)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                LogUtil.d(TAG, "Socket receive message with byteString: $bytes")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                LogUtil.d(TAG, "Socket receive message with string: $text")
                mEventListener.notifyMessage(MessageEvent(text))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                LogUtil.d(TAG, "Socket closing with reason: $reason")
                changeState(FSocketState.CLOSING)
                webSocket.close(1000, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                LogUtil.d(TAG, "Socket closed success with reason: $reason")
                changeState(FSocketState.CLOSED)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (!isForceTerminate) {
                    t.printStackTrace()
                    LogUtil.e(TAG, "Socket connect error with $t")
                    changeState(FSocketState.CONNECT_ERROR)
                    reconnect()
                }

            }
        }
    }

    private fun changeState(state: FSocketState) {
        currentState = state
        postEvent(state)
    }

    private fun postEvent(state: FSocketState) {
        when (state) {
            FSocketState.CONNECTING -> mEventListener.notifyConnecting(ConnectingEvent())
            FSocketState.CONNECTED -> mEventListener.notifyConnected(ConnectedEvent())
            FSocketState.RECONNECT_ATTEMPT -> mEventListener.notifyReconnecting(ReconnectingEvent())
            FSocketState.CLOSING -> mEventListener.notifyClosing(ClosingEvent())
            FSocketState.CLOSED -> mEventListener.notifyClosed(ClosedEvent())
            FSocketState.CONNECT_ERROR -> mEventListener.notifyConnectError(ConnectErrorEvent())
        }

    }

}