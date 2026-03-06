package com.example.auctionarymobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.model.Auction
import com.example.auctionarymobile.ui.theme.*
import com.example.auctionarymobile.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionListScreen(
    viewModel: MainViewModel,
    onAuctionClick: (String) -> Unit
) {
    val auctions by viewModel.auctions.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Sadece ikiye ayırıyoruz: Aktifler ve Bitmişler
    val activeAuctions = auctions.filter { it.isActive }
    val pastAuctions = auctions.filter { !it.isActive }

    Scaffold(
        containerColor = LightBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = DarkSurface,
                contentColor = PrimaryGold,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Ürün Ekle")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp)
        ) {
            // --- HEADER ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "WELCOME BACK", color = LightTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Discover", style = MaterialTheme.typography.headlineLarge, color = LightTextPrimary)
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = { /* Bildirimler */ }) {
                            Text("🔔", fontSize = 18.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- SEARCH BAR ---
            item {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search auctions...", color = LightTextSecondary) },
                    leadingIcon = { Text("🔍", fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = PrimaryGold,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    enabled = false
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- CATEGORIES ---
            item {
                val categories = listOf("All", "Watches", "Vehicles", "Art", "Jewelry")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == "All"
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) PrimaryGold else Color.White,
                            border = if (!isSelected) BorderStroke(1.dp, DividerColor) else null,
                            modifier = Modifier.clickable { /* Kategori Filtreleme */ }
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else LightTextPrimary,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (auctions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGold)
                    }
                }
            } else {
                // --- FEATURED LIVE SECTION (Yatay Kaydırılabilir) ---
                if (activeAuctions.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("Featured Live", style = MaterialTheme.typography.headlineMedium, color = LightTextPrimary, fontSize = 22.sp)
                            Text("${activeAuctions.size} Active", color = PrimaryGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Aktif müzayedeleri yan yana dizen LazyRow
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(activeAuctions) { auction ->
                                FeaturedLiveCard(
                                    auction = auction,
                                    onClick = { onAuctionClick(it) },
                                    modifier = Modifier.width(320.dp) // Kartın genişliğini sabitledik ki bir sonraki hafifçe görünsün
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // --- PAST AUCTIONS SECTION (Dikey Liste) ---
                if (pastAuctions.isNotEmpty()) {
                    item {
                        Text(
                            "Past Auctions",
                            style = MaterialTheme.typography.headlineMedium,
                            color = LightTextPrimary,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(pastAuctions) { auction ->
                        PastAuctionItem(
                            auction = auction,
                            onClick = { onAuctionClick(it) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    // --- CREATE AUCTION DIALOG ---
    if (showCreateDialog) {
        var productName by remember { mutableStateOf("") }
        var productPrice by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Color.White,
            title = { Text("Yeni Müzayede Başlat", fontWeight = FontWeight.Bold, color = LightTextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Ürün Adı") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it },
                        label = { Text("Başlangıç Fiyatı (₺)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = productPrice.toDoubleOrNull()
                        if (productName.isNotBlank() && price != null) {
                            viewModel.createAuction(productName, price)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text("Başlat", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("İptal", color = LightTextSecondary)
                }
            }
        )
    }
}

// Parametrelere modifier eklendi
@Composable
fun FeaturedLiveCard(auction: Auction, modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    var timeLeft by remember { mutableStateOf("...") }

    LaunchedEffect(auction.endTime) {
        while (true) {
            try {
                val endInstant = Instant.parse(auction.endTime)
                val diffMillis = endInstant.toEpochMilli() - Instant.now().toEpochMilli()
                if (diffMillis <= 0) {
                    timeLeft = "00:00:00"
                } else {
                    val hours = diffMillis / (1000 * 60 * 60)
                    val mins = (diffMillis / (1000 * 60)) % 60
                    val secs = (diffMillis / 1000) % 60
                    timeLeft = String.format("%02d:%02d:%02d", hours, mins, secs)
                }
            } catch (e: Exception) {
                timeLeft = "Hata"
            }
            delay(1000)
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.clickable { onClick(auction.id.toString()) }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(DarkBackground),
                contentAlignment = Alignment.BottomStart
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusError))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ends in $timeLeft", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(auction.productName, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontSize = 24.sp, maxLines = 1)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CURRENT BID", color = LightTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("${auction.currentPrice} ₺", color = PrimaryGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("STATUS", color = LightTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Active", color = StatusSuccess, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Parametrelere modifier eklendi
@Composable
fun PastAuctionItem(auction: Auction, modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(auction.id.toString()) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    color = StatusErrorBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CLOSED",
                        color = StatusError,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp).wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(auction.productName, color = LightTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Text("FINAL PRICE", color = LightTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${auction.currentPrice} ₺", color = DarkTextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Surface(
                color = DividerColor,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("View", color = LightTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}