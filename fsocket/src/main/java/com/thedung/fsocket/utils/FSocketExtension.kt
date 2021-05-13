package com.thedung.fsocket.utils

import com.thedung.fsocket.events.*
import com.thedung.fsocket.listeners.*

internal inline fun <reified T : BaseEventListener<*>> Iterable<BaseEventListener<*>>.forEach(
    action: T.() -> Unit
) {
    for (element in this) {
        if (element is T)
            action(element)
    }
}

internal fun MutableCollection<BaseEventListener<*>>.notifyClosed(event: ClosedEvent) {
    forEach<OnClosedListener> {
        onClosed(event)
    }
}

internal fun MutableCollection<BaseEventListener<*>>.notifyClosing(event: ClosingEvent) {
    forEach<OnClosingListener> {
        onClosing(event)
    }
}

internal fun MutableCollection<BaseEventListener<*>>.notifyConnected(event: ConnectedEvent) {
    forEach<OnConnectedListener> {
        onConnected(event)
    }
}

internal fun MutableCollection<BaseEventListener<*>>.notifyConnectError(event: ConnectErrorEvent) {
    forEach<OnConnectErrorListener> {
        onConnectError(event)
    }
}


internal fun MutableCollection<BaseEventListener<*>>.notifyConnecting(event: ConnectingEvent) {
    forEach<OnConnectingListener> {
        onConnecting(event)
    }
}

internal fun MutableCollection<BaseEventListener<*>>.notifyReconnecting(event: ReconnectingEvent) {
    forEach<OnReconnectingListener> {
        onReconnect(event)
    }
}

internal fun MutableCollection<BaseEventListener<*>>.notifyMessage(event: MessageEvent) {
    forEach<OnMessageListener> {
        onMessage(event)
    }
}