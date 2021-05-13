package com.thedung.fsocket.listeners

import com.thedung.fsocket.events.ConnectingEvent

interface OnConnectingListener : BaseEventListener<ConnectingEvent> {
    fun onConnecting(event: ConnectingEvent)

    companion object {
        inline operator fun invoke(crossinline block: (event: ConnectingEvent) -> Unit) =
            object : OnConnectingListener {
                override fun onConnecting(event: ConnectingEvent) {
                    block(event)
                }
            }
    }
}