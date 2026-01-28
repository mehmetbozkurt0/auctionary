package com.example.auctionarymobile.network

import com.example.auctionarymobile.model.Auction
import com.example.auctionarymobile.model.LoginRequest
import com.example.auctionarymobile.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("auctions")
    suspend fun getAuctions(): List<Auction>

    @POST("auctions")
    suspend fun createAuction(@Body auction: Auction): Map<String, Any>
}