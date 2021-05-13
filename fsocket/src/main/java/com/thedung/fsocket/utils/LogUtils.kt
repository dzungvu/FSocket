package com.thedung.fsocket.utils

import android.util.Log

@Suppress("unused")
object LogUtil {
    fun v(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }
}