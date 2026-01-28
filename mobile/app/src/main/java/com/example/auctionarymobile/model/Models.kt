package com.example.auctionarymobile.model
import com.google.gson.annotations.SerializedName
import java.time.temporal.TemporalAmount

data class Auction(
    val id: String,

    @SerializedName("product_name")
    val productName: String,

    @SerializedName("current_price")
    val currentPrice: Double,

    @SerializedName("starting_price")
    val startingPrice: Double,

    @SerializedName("winner_id")
    val winnerId: String?,

    @SerializedName("end_time")
    val endTime: String,

    @SerializedName("is_active")
    val isActive: Boolean
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val username: String,

    @SerializedName("user_id")
    val userId: Int,
)

data class WebSocketEvent(
    val type: String,
    val payload: Any?
)

data class BidData(
    @SerializedName("auction_id")
    val auctionId: String,

    @SerializedName("user_id")
    val userId: String,

    val amount: Double
)

data class BidPayload(
    val type: String = "bid_placed",
    val payload: BidData
)

data class BidUpdate(
    @SerializedName("auction_id")
    val auctionId: String,

    @SerializedName("new_price")
    val newPrice: Double,

    @SerializedName("winner_id")
    val winnerId: String,

    @SerializedName("remaining_time_seconds")
    val remainingSeconds: Int
)
















