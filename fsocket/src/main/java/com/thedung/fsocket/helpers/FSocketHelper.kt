package com.thedung.fsocket.helpers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.thedung.fsocket.FSocket
import com.thedung.fsocket.annotation.ReceiveEvent
import com.thedung.fsocket.annotation.SendEvent
import com.thedung.fsocket.builder.FSocketBuilder
import com.thedung.fsocket.helpers.data.BaseSocketData
import com.thedung.fsocket.helpers.data.Receiver
import com.thedung.fsocket.listeners.BaseEventListener
import com.thedung.fsocket.listeners.OnMessageListener
import com.thedung.fsocket.utils.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import me.ibrahimsn.achilleslib.exception.InvalidAnnotationException
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy

class FSocketHelper {
    private val TAG = this::class.java.simpleName
    private var currentUrl: String = ""
    private var fSocket: FSocket? = null
    private val jsonParser by lazy { JsonParser() }

    private var gson: Gson = Gson()

    /**
     * Distributor receive data to filter subscriber
     */
    private val distributor = MutableLiveData<Receiver>()

    /**
     * Establish a connection with socket url.
     * @param url: Url of socket want to interact
     *
     * @NOTE:
     * If new url is the same as current url, this function will return false and NO connection
     * will be created. The old one keep running.
     *
     * @return true: Connect successfully / false: Connect failure
     */
    @Synchronized
    fun establishConnection(url: String?): Boolean {
        url?.let {
            return if (currentUrl.isNotEmpty() && currentUrl == url) {
                true
            } else if (url.isEmpty()) {
                LogUtil.d("AppSocket", "establishConnection false because url empty")
                false
            } else {
                LogUtil.d(
                    "AppSocket",
                    "establishConnection true with current: $currentUrl and new: $url"
                )
                fSocket?.removeAllListener()
                fSocket?.close()
                fSocket = null

                currentUrl = url
                fSocket = createFSocket(url)
                setListener()
                fSocket?.connect()
                true
            }
        } ?: LogUtil.e(
            TAG,
            "Can not establishConnection new connection with null url. Current url is $currentUrl"
        )

        return false
    }

    /**
     * Add listener for state change of socket - See [FSocket] FSocketState
     */
    fun <T : BaseEventListener<*>> addEventListener(listener: T) {
        LogUtil.d(TAG, "Add event listener $listener")
        fSocket?.addEventListener(listener)
    }

    fun getCurrentSocketUrl(): String {
        return currentUrl
    }

    /**
     * Remove listener for state change of socket
     */
    fun <T : BaseEventListener<*>> removeEventListener(listener: T) {
        LogUtil.d(TAG, "Remove event listener")
        fSocket?.removeEventListener(listener)
    }

    /**
     * Close current socket
     */
    fun closeConnection() {
        fSocket?.close()
        currentUrl = ""
    }

    /**
     * Create a socket service which can interact with [AppSocket] to execute send event.
     * All send event must be declared inside [serviceInterface]
     * @param serviceInterface: A Class contains functions. A function is considered as valid to call
     * must have annotation [SendEvent]
     *
     * @return instance of input service class
     */
    fun <T> create(serviceInterface: Class<T>): T {
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
                        return@newProxyInstance invokeReceiveMethod(ann, method, args)
                    }
                    else -> {
                        throw InvalidAnnotationException("Only SendEvent and ReceiveEvent annotation is allowed")
                    }
                }
            }
        )!!
    }

    private fun invokeSendMethod(ann: SendEvent?, method: Method, args: Array<out Any>) {
        if (args.size == 1 && args[0] is BaseSocketData) {
            (args[0] as BaseSocketData).type = ann?.value.toString()
            fSocket?.send("", gson.toJson(args[0]))
        } else {
            throw IllegalArgumentException("SendEvent only contain 1 parameter for send data and it must be instance of BaseSocketData")
        }
    }

    /**
     * Subscribe to Distributor with a service interface
     * Override receive data of input service. Create a [Flow] with [Any] type. And
     * subscribe it to the [distributor] for receive notify data change.
     *
     * @return A flow for streaming data
     */
    private fun invokeReceiveMethod(
        ann: ReceiveEvent?,
        method: Method,
        args: Array<out Any>
    ): Flow<Any> {
        return distributor.asFlow().filter { it.event.toInt() == ann?.value }
            .map {
                val json = jsonParser.parse(it.data).asJsonObject
                val typeArg = (method.genericReturnType as ParameterizedType).actualTypeArguments[0]
                gson.fromJson(json, typeArg as Class<*>)
            }
    }

    private fun sendData(data: BaseSocketData) {
        fSocket?.send("", data)
    }

    private fun createFSocket(url: String?): FSocket? {
        url?.run {
            currentUrl = url
            return FSocketBuilder(url).build()
        }
        LogUtil.e(TAG, "ws url can not be null")
        return null
    }

    private fun setListener() {
        fSocket?.run {
            addEventListener(OnMessageListener {
                try {
                    val json = jsonParser.parse(it.data).asJsonObject
                    if (json.has("type")
                        && json.has("departure")
                        && json["departure"].asString == "0"
                    ) {
                        val receiver = Receiver(
                            event = json.get("type").asString,
                            data = it.data
                        )

                        // Do not remove this log. Magic will make this postValue becomes useless.
                        LogUtil.d(
                            TAG,
                            "AppSocket receive a message and notify"
                        )
                        distributor.postValue(receiver)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            })
        }
    }
}