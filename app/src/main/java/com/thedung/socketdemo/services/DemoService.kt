package com.thedung.socketdemo.services

import com.thedung.fsocket.annotation.Field
import com.thedung.fsocket.annotation.ReceiveEvent
import com.thedung.fsocket.annotation.SendEvent
import com.thedung.socketdemo.model.DemoResponse
import kotlinx.coroutines.flow.Flow

interface DemoService {
    @SendEvent("type1")
    fun sendData(
        @Field("phone") phone: String
    )

    @ReceiveEvent("type1")
    suspend fun receiveData(): Flow<DemoResponse>
}