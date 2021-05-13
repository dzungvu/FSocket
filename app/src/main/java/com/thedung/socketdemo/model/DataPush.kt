package com.thedung.socketdemo.model

import com.thedung.fsocket.helpers.data.BaseSocketData

data class DataPush(
    val customdata1: String
) : BaseSocketData()

data class DataResponse(
    val customdata2: String
) : BaseSocketData()