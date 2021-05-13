package com.thedung.fsocket.helpers.data

import com.google.gson.annotations.SerializedName

open class BaseSocketData(
    @SerializedName("type")
    var type: String = "",
    @SerializedName("departure")
    val departure: String = "1"
)