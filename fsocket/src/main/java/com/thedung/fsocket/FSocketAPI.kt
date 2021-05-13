package com.thedung.fsocket

import com.thedung.fsocket.listeners.BaseEventListener

interface FSocketAPI {
    fun connect()

    fun send(event: String, data: Any): Boolean

    fun send(event: String, data: String): Boolean

    fun close()

    fun close(code: Int, reason: String)

    fun terminate()

    fun <T : BaseEventListener<*>> addEventListener(listener: T)

    fun <T : BaseEventListener<*>> removeEventListener(listener: T)

    fun removeAllListener()
}