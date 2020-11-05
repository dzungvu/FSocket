package com.thedung.fsocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.thedung.fsocket.annotation.Field
import com.thedung.fsocket.annotation.ReceiveEvent
import com.thedung.fsocket.annotation.SendEvent
import com.thedung.fsocket.models.DataPushTest
import com.thedung.fsocket.models.Receiver
import com.thedung.fsocket.utils.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import me.ibrahimsn.achilleslib.exception.InvalidAnnotationException
import me.ibrahimsn.achilleslib.exception.InvalidReturnTypeException
import okhttp3.*
import okio.ByteString
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy

/**
 *
 */
@ExperimentalCoroutinesApi
class FSocket internal constructor(
    val url: String,
    client: OkHttpClient
) : FSocketAPI, WebSocketListener() {
    private val socket: WebSocket
    private val socketListener: WebSocketListener
    private var logTraffic = true

    private val distributor = ConflatedBroadcastChannel<Receiver>()

    init {

        val request = Request.Builder().url(url).build()
        socketListener = createWebSocketListener()
        socket = client.newWebSocket(request, socketListener)
        client.dispatcher.executorService.shutdown()
    }

    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                log("Open websocket $webSocket")
                super.onOpen(webSocket, response)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                log("onMessage websocket byte $webSocket")
                super.onMessage(webSocket, bytes)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                log("onMessage websocket String $webSocket")
                super.onMessage(webSocket, text)
                val json = JsonParser.parseString(text).asJsonObject
                log("Receive: $json")

                if (json.has(Constants.ATTR_EVENT) && json.has(Constants.ATTR_DATA)) {
                    log("Notify: $json")
                    distributor.offer(Gson().fromJson(json, Receiver::class.java))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                log("closing websocket $webSocket")
                super.onClosing(webSocket, code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                log("Closed websocket $webSocket")
                super.onClosed(webSocket, code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                log("onFailure websocket byte $webSocket")
                super.onFailure(webSocket, t, response)
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @Throws(InvalidReturnTypeException::class)
    override fun <T> create(serviceInterface: Class<T>): T {
        return serviceInterface.cast(
            Proxy.newProxyInstance(
                serviceInterface.classLoader, arrayOf(serviceInterface)
            ) { _, method, args ->
                when {
                    method.isAnnotationPresent(SendEvent::class.java) -> {
                        val ann = method.getAnnotation(SendEvent::class.java)
                        return@newProxyInstance invokeSendMethod(ann, method, args)
                    }
                    method.isAnnotationPresent(ReceiveEvent::class.java) -> {
                        val ann = method.getAnnotation(ReceiveEvent::class.java)
                        return@newProxyInstance invokeReceiverMethod(ann, method)
                    }
                    else -> throw InvalidAnnotationException("Only SendEvent and ReceiveEvent are allowed.")
                }
            }
        )!!
    }

    private fun invokeSendMethod(ann: SendEvent?, method: Method, args: Array<out Any>) {
        val data = mutableMapOf<String, Any>()

        for ((i, par) in method.parameterAnnotations.withIndex()) {
            if (par[0] is Field) {
                data[(par[0] as Field).value] = args[i]
            }
        }

//        val payload = Gson().toJson(
//            mapOf(
//                Constants.ATTR_EVENT to ann?.value,
//                Constants.ATTR_DATA to data
//            )
//        )
        //test
        val temp = DataPushTest("14", "1", "up")
        val payload = Gson().toJson(temp)
        log("Send to: $url: $payload")
        socket.send(payload)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Throws(InvalidReturnTypeException::class)
    private fun invokeReceiverMethod(ann: ReceiveEvent?, method: Method): Flow<Any> {
        return distributor
            .asFlow().filter { it.event == ann?.value }
            .map {
                val json = JsonParser.parseString(it.data.toString()).asJsonObject
                val typeArg = (method.genericReturnType as ParameterizedType).actualTypeArguments[0]
                Gson().fromJson(json, typeArg as Class<*>)
            }
    }

    private fun log(message: String) {
        if (logTraffic) {
            Log.d(Constants.LOG_TAG, message)
        }
    }
}