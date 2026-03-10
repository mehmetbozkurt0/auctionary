package com.example.auctionarymobile.network

import com.example.auctionarymobile.model.Auction
import com.example.auctionarymobile.model.CreateAuctionRequest
import com.example.auctionarymobile.model.LoginRequest
import com.example.auctionarymobile.model.LoginResponse
import com.example.auctionarymobile.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): retrofit2.Response<Unit>

    @GET("auctions")
    suspend fun getAuctions(): List<Auction>

    @POST("auctions")
    suspend fun createAuction(@Body request: CreateAuctionRequest) : Map<String, Any>
}