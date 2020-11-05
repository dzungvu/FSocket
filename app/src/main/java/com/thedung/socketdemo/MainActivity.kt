package com.thedung.socketdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.thedung.fsocket.builder.FSocketBuilder
import com.thedung.socketdemo.services.DemoService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fSocket = FSocketBuilder("wss://echo.websocket.org").build()
        val service = fSocket.create(DemoService::class.java)

        btnPush.setOnClickListener {
            service.sendData("19006600")
        }

        CoroutineScope(Dispatchers.IO).launch {
            service.receiveData().onEach {
                Log.d("MainActivity", "Response: $it")
            }
        }
    }
}