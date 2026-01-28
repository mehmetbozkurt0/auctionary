package com.example.auctionarymobile.network

import android.util.Log
import com.example.auctionarymobile.model.WebSocketEvent
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class WebSocketManager {
    private var webSocket : WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    private val _events = MutableSharedFlow<WebSocketEvent>()
    val events = _events.asSharedFlow()

    fun connect(url: String){
        if(webSocket != null){
            return
        }

        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("Websocket", "Connected to websocket!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("Websocket","Message arrived: $text")
                try {
                    val event = gson.fromJson(text, WebSocketEvent::class.java)
                    if (event != null){
                        _events.tryEmit(event)
                    }
                } catch (e: Exception){
                    Log.e("Websocket","Parsing error: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("Websocket","Closing: $reason")
                webSocket.close(1000,null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("Websocket","Error: ${t.message}")
                disconnect()
            }
        })
    }

    fun sendMessage(data: Any){
        try{
            val json = gson.toJson(data)
            webSocket?.send(json)
            Log.d("Websocket","Sent: $json")
        }catch (e: Exception) {
            Log.e("Websocket","Sending error: ${e.message}")
        }
    }

    fun disconnect(){
        webSocket?.close(1000,"User has closed the app!")
        webSocket = null
    }

}












