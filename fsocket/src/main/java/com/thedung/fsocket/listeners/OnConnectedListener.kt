package com.thedung.fsocket.listeners

import com.thedung.fsocket.events.ConnectedEvent

interface OnConnectedListener : BaseEventListener<ConnectedEvent> {
    fun onConnected(event: ConnectedEvent)

    companion object {
        inline operator fun invoke(crossinline block: (event: ConnectedEvent) -> Unit) =
            object : OnConnectedListener {
                override fun onConnected(event: ConnectedEvent) {
                    block(event)
                }
            }
    }
}