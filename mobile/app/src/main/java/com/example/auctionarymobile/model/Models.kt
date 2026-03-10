package com.example.auctionarymobile.model
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.gson.annotations.SerializedName
import java.time.temporal.TemporalAmount

data class Auction(
    val id: String,

    @SerializedName("product_name")
    val productName: String,

    val description: String = "",

    val category: String = "Other",

    @SerializedName("image_url")
    val imageUrl: String = "",

    @SerializedName("current_price")
    val currentPrice: Double,

    @SerializedName("starting_price")
    val startingPrice: Double,

    @SerializedName("winner_id")
    val winnerId: String?,


    @SerializedName("seller_id")
    var sellerId: String,


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

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
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

data class CreateAuctionRequest (
    val id: String,
    @SerializedName("product_name")
    val productName: String,

    val description: String,

    val category: String,

    @SerializedName("image_url")
    val imageUrl: String,

    @SerializedName("starting_price")
    val startingPrice: Double,

    @SerializedName("seller_id")
    val sellerId: String
)

fun String.toImageBitmap(): ImageBitmap? {
    if(this.isBlank()) return null
    return try {
        val bytes = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }catch (e: Exception) {
        null
    }
}
















