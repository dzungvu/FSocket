package com.thedung.fsocket.listeners

import com.thedung.fsocket.events.ConnectErrorEvent


interface OnConnectErrorListener : BaseEventListener<ConnectErrorEvent> {
    fun onConnectError(event: ConnectErrorEvent)

    companion object {
        inline operator fun invoke(crossinline block: (event: ConnectErrorEvent) -> Unit) =
            object : OnConnectErrorListener {
                override fun onConnectError(event: ConnectErrorEvent) {
                    block(event)
                }
            }
    }
}