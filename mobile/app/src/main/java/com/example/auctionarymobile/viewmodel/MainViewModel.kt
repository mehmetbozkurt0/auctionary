package com.example.auctionarymobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionarymobile.model.Auction
import com.example.auctionarymobile.model.BidData
import com.example.auctionarymobile.model.BidPayload
import com.example.auctionarymobile.model.BidUpdate
import com.example.auctionarymobile.model.CreateAuctionRequest
import com.example.auctionarymobile.model.LoginRequest
import com.example.auctionarymobile.model.RegisterRequest
import com.example.auctionarymobile.network.RetrofitClient
import com.example.auctionarymobile.network.WebSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

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

                currentUsername = response.username
                com.example.auctionarymobile.network.AuthManager.saveUser(response.username, response.token)
                _userToken.value = response.token
                Log.d("ViewModel", "Giriş Başarılı: ${response.username}")

                loadAuctions()
                connectToSocket()
            } catch (e: Exception) {
                Log.e("Login", "Hata: ${e.message}")
            }
        }
    }

    fun restoreSession(savedUsername: String) {
        currentUsername = savedUsername
        loadAuctions()
        connectToSocket()
    }

    fun logout() {
        com.example.auctionarymobile.network.AuthManager.clearUser()

        _userToken.value = null
        currentUsername = ""

        webSocketManager.disconnect()

        Log.d("ViewModel", "Kullanıcı çıkış yaptı ve oturum temizlendi.")
    }

    fun register(username: String, email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(username = username, email = email, password = pass)
                val response = RetrofitClient.api.register(request)

                if (response.isSuccessful) {
                    onResult(true,null)
                }else {
                    onResult(false, "Cannot sign in. Username or password is already in use.")
                }
            } catch (e: Exception) {
                Log.e("Register", "Error: ${e.message}")
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

    fun connectToSocket() {
        val wsUrl = "ws://10.68.6.136:8080/ws"

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
                else if (event.type == "auction_events" || event.type == "auction_created" || event.type == "auction_end") {
                    Log.d("Viewmodel", "There is some changes in the system: ${event.type}. List is updating now...")
                    loadAuctions()
                }
            }
        }
    }

    private fun handleBidUpdate(update: BidUpdate) {
        val currentList = _auctions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == update.auctionId }

        if (index != -1) {
            val oldItem = currentList[index]

            val newEndTime = Instant.now().plusSeconds(update.remainingSeconds.toLong()).toString()

            val newItem = oldItem.copy(
                currentPrice = update.newPrice,
                winnerId = update.winnerId,
                endTime = newEndTime
            )
            currentList[index] = newItem
            _auctions.value = currentList
            Log.d("ViewModel", "Ürün güncellendi: ${newItem.productName} -> Yeni Süre: $newEndTime")
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

    fun createAuction(name: String, description:String, category: String, imageUrl:String , startingPrice: Double){
        viewModelScope.launch {
            try {
                val generatedId = "item" + (10000..99999).random()
                val request = CreateAuctionRequest(
                    id = generatedId,
                    productName = name,
                    description = description,
                    category = category,
                    imageUrl = imageUrl,
                    startingPrice = startingPrice,
                    sellerId = currentUsername
                )
                RetrofitClient.api.createAuction(request)
                Log.d("ViewModel", "Product added successfully: $name")
                loadAuctions()
            } catch (e: Exception) {
                Log.e("ViewModel", "Cannot add this product: ${e.message}")
            }
        }
    }
}