package com.example.auctionarymobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.model.toImageBitmap
import com.example.auctionarymobile.network.AuthManager
import com.example.auctionarymobile.viewmodel.MainViewModel
import com.example.auctionarymobile.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailScreen(
    auctionId: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val auctions by viewModel.auctions.collectAsState()
    val auction = auctions.find { it.id == auctionId }

    val currentUsername = viewModel.currentUsername.ifEmpty {
        AuthManager.getUsername() ?: ""
    }

    var timeLeft by remember { mutableStateOf("Calculating...") }

    var customBidAmount by remember(auction?.currentPrice) {
        mutableStateOf((auction?.currentPrice?.plus(50) ?: 0.0).toString())
    }

    LaunchedEffect(auction) {
        while (true) {
            if (auction != null) {
                try {
                    val endInstant = Instant.parse(auction.endTime)
                    val nowInstant = Instant.now()
                    val diffMillis = endInstant.toEpochMilli() - nowInstant.toEpochMilli()

                    if (diffMillis <= 0) {
                        timeLeft = "ENDED"
                    } else {
                        val hours = diffMillis / (1000 * 60 * 60)
                        val mins = (diffMillis / (1000 * 60)) % 60
                        val secs = (diffMillis / 1000) % 60
                        timeLeft = String.format("%02d:%02d:%02d", hours, mins, secs)
                    }
                } catch (e: Exception) {
                    timeLeft = "Error"
                }
            }
            delay(1000)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = auction?.productName ?: "Item Details",
                        color = DarkTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add to favorites */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Save", tint = DarkTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (auction != null && timeLeft != "ENDED") {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Min. increment: 50 ₺", color = DarkTextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))

                                val maxBidText = if (auction.winnerId == currentUsername) "${auction.currentPrice} ₺" else "None"
                                Text("Your max bid: $maxBidText", color = DarkTextSecondary, fontSize = 12.sp)
                            }

                            Surface(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = "Time Left", tint = PrimaryGold, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = timeLeft,
                                        color = PrimaryGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customBidAmount,
                                onValueChange = { customBidAmount = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGold,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedTextColor = DarkTextPrimary,
                                    unfocusedTextColor = DarkTextPrimary,
                                    cursorColor = PrimaryGold
                                ),
                                singleLine = true,
                                leadingIcon = { Text("₺", color = DarkTextSecondary, modifier = Modifier.padding(start = 12.dp)) }
                            )

                            Button(
                                onClick = {
                                    val bidVal = customBidAmount.toDoubleOrNull()
                                    if (bidVal != null && bidVal > auction.currentPrice) {
                                        viewModel.placeBid(auction.id, bidVal)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Place Bid", color = LightTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (auction == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Item not found!", color = StatusError, fontWeight = FontWeight.Bold)
                    Text("Requested ID: '$auctionId'", color = DarkTextSecondary)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Görsel Alanı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.TopEnd
                ) {
                    val imageBitmap = auction.imageUrl.toImageBitmap()
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = "Time Left", tint = PrimaryGold, modifier = Modifier.size(16.dp))
                            Text(
                                text = timeLeft,
                                color = PrimaryGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // İçerik ve Detaylar
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AUCTION ITEM", color = PrimaryGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        if (auction.isActive && timeLeft != "ENDED") {
                            Surface(color = DarkSurface, shape = RoundedCornerShape(12.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).background(StatusError, CircleShape))
                                    Text("Live", color = DarkTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = auction.productName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = DarkTextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val (badgeText, badgeColor) = when {
                        timeLeft == "ENDED" || !auction.isActive -> "Closing Price" to StatusError
                        auction.currentPrice > auction.startingPrice -> "Active Bid" to StatusSuccess
                        else -> "Starting Price" to DarkTextSecondary
                    }

                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "${auction.currentPrice} ₺",
                            style = MaterialTheme.typography.headlineLarge,
                            color = DarkTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(color = DarkSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = auction.description.ifEmpty { "No description provided for this item." },
                        color = DarkTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                HorizontalDivider(color = DarkSurface, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                // Özellikler Alanı
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Specifications", style = MaterialTheme.typography.headlineMedium, color = DarkTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SpecItem("Category", auction.category, Modifier.weight(1f))
                        SpecItem("Starting Price", "${auction.startingPrice} ₺", Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SpecItem(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, color = DarkTextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = DarkTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}