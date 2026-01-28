package com.example.auctionarymobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionarymobile.model.Auction
import com.example.auctionarymobile.model.BidData
import com.example.auctionarymobile.model.BidPayload
import com.example.auctionarymobile.model.BidUpdate
import com.example.auctionarymobile.model.LoginRequest
import com.example.auctionarymobile.network.RetrofitClient
import com.example.auctionarymobile.network.WebSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val webSocketManager = WebSocketManager()
    private val gson = Gson()

    private val _auctions = MutableStateFlow<List<Auction>>(emptyList())
    val auctions = _auctions.asStateFlow()

    private val _userToken = MutableStateFlow<String?>(null)
    val userToken = _userToken.asStateFlow()

    var currentUsername: String = ""

    fun login(email: String, pass: String){
        viewModelScope.launch{
            try {
                Log.d("ViewModel", "Giriş deneniyor: $email")
                val response = RetrofitClient.api.login(LoginRequest(email, pass))

                _userToken.value = response.token
                currentUsername = response.username
                Log.d("ViewModel", "Giriş Başarılı: ${response.username}")

                loadAuctions()
                connectToSocket()
            } catch (e: Exception) {
                Log.e("Login", "Hata: ${e.message}")
            }
        }
    }

    fun loadAuctions() {
        viewModelScope.launch {
            try {
                val list = RetrofitClient.api.getAuctions()
                _auctions.value = list
                Log.d("ViewModel", "Liste çekildi: ${list.size} ürün")
            } catch (e: Exception) {
                Log.e("API", "Liste çekilemedi: ${e.message}")
            }
        }
    }

    private fun connectToSocket() {
        val wsUrl = "ws://192.168.1.12:8080/ws"

        webSocketManager.connect(wsUrl)

        viewModelScope.launch {
            webSocketManager.events.collect { event ->
                if (event.type == "bid_accepted") {
                    try {
                        val jsonStr = gson.toJson(event.payload)
                        val update = gson.fromJson(jsonStr, BidUpdate::class.java)

                        handleBidUpdate(update)
                    } catch (e: Exception) {
                        Log.e("ViewModel", "Parse Hatası: ${e.message}")
                    }
                }
            }
        }
    }

    private fun handleBidUpdate(update: BidUpdate) {
        val currentList = _auctions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == update.auctionId }

        if (index != -1) {
            val oldItem = currentList[index]
            val newItem = oldItem.copy(
                currentPrice = update.newPrice,
                winnerId = update.winnerId
            )
            currentList[index] = newItem
            _auctions.value = currentList
            Log.d("ViewModel", "Ürün güncellendi: ${newItem.productName} -> ${newItem.currentPrice}")
        }
    }

    fun placeBid(auctionId: String, amount: Double) {
        val payload = BidPayload(
            payload = BidData(
                auctionId = auctionId,
                userId = currentUsername,
                amount = amount
            )
        )
        webSocketManager.sendMessage(payload)
    }
}