package com.thedung.fsocket.listeners

import com.thedung.fsocket.events.ReconnectingEvent


interface OnReconnectingListener : BaseEventListener<ReconnectingEvent> {
    fun onReconnect(event: ReconnectingEvent)

    companion object {
        inline operator fun invoke(crossinline block: (event: ReconnectingEvent) -> Unit) =
            object : OnReconnectingListener {
                override fun onReconnect(event: ReconnectingEvent) {
                    block(event)
                }
            }
    }
}