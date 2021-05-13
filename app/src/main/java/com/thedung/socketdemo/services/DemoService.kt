package com.thedung.socketdemo.services

import com.thedung.fsocket.annotation.ReceiveEvent
import com.thedung.fsocket.annotation.SendEvent
import com.thedung.socketdemo.model.DataPush
import com.thedung.socketdemo.model.DemoResponse
import kotlinx.coroutines.flow.Flow

interface DemoService {
    @SendEvent(1)
    fun sendData(data: DataPush)

    @ReceiveEvent(1)
    suspend fun receiveData(): Flow<DemoResponse>
}