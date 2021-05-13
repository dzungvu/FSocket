package com.thedung.fsocket.listeners


import com.thedung.fsocket.events.ClosedEvent

interface OnClosedListener : BaseEventListener<ClosedEvent> {
    fun onClosed(event: ClosedEvent)

    companion object {
        inline operator fun invoke(crossinline block: (event: ClosedEvent) -> Unit) =
            object : OnClosedListener {
                override fun onClosed(event: ClosedEvent) {
                    block(event)
                }
            }
    }
}