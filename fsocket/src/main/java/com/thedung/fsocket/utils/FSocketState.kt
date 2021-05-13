package com.thedung.fsocket.utils

enum class FSocketState {
    CONNECTING,
    CONNECTED,
    CONNECT_ERROR,
    CLOSING,
    CLOSED,
    RECONNECT_ATTEMPT
}