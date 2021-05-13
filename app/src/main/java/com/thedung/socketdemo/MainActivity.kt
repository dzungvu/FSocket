package com.thedung.socketdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.thedung.fsocket.builder.FSocketBuilder
import com.thedung.fsocket.helpers.FSocketHelper
import com.thedung.fsocket.listeners.*
import com.thedung.socketdemo.model.DataPush
import com.thedung.socketdemo.services.DemoService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    val tag: String = this::class.java.simpleName
    val socket = FSocketHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val fSocket = FSocketBuilder("wss://echo.websocket.org")
//            .setConnectionTimeout(30_000L)
//            .setReadTimeout(30_000L)
//            .setPingInterval(2_000L)
//            .build()
//        fSocket.run {
//            addEventListener(OnConnectingListener {})
//            addEventListener(OnConnectedListener {})
//            addEventListener(OnConnectErrorListener {})
//            addEventListener(OnMessageListener {})
//            addEventListener(OnClosingListener {})
//            addEventListener(OnClosedListener {})
//            addEventListener(OnReconnectingListener {})
//        }
//        fSocket.connect()
//        fSocket.terminate()

        socket.establishConnection("wss://echo.websocket.org")
        val service = socket.create(DemoService::class.java)

        btnPush.setOnClickListener {
//            fSocket.send("event", "data")
            service.sendData(DataPush("Name here"))
        }

//        fSocket.addEventListener(OnMessageListener {
//            Log.d(tag, it.data)
//        })

        CoroutineScope(Dispatchers.IO).launch {
            service.receiveData().collect {
                Log.d("MainActivity", "Response: $it")
            }
        }
    }
}