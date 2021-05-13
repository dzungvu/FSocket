package com.thedung.fsocket.listeners

import com.thedung.fsocket.events.ClosingEvent

interface OnClosingListener : BaseEventListener<ClosingEvent> {
    fun onClosing(event: ClosingEvent)

    companion object {
        inline operator fun invoke(crossinline block: (event: ClosingEvent) -> Unit) =
            object : OnClosingListener {
                override fun onClosing(event: ClosingEvent) {
                    block(event)
                }
            }
    }
}