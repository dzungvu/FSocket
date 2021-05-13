package com.thedung.fsocket.listeners

import com.thedung.fsocket.events.MessageEvent

interface OnMessageListener : BaseEventListener<MessageEvent> {
    fun onMessage(event: MessageEvent)

    companion object {
        inline operator fun invoke(crossinline block: (event: MessageEvent) -> Unit) =
            object : OnMessageListener {
                override fun onMessage(event: MessageEvent) {
                    block(event)
                }
            }
    }
}