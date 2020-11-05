package com.thedung.fsocket.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ReceiveEvent (val value: String)