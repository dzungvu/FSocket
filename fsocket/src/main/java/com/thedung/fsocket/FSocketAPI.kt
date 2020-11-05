package com.thedung.fsocket

interface FSocketAPI {
    fun <T> create(serviceInterface: Class<T>): T
}