package com.example.auctionarymobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.model.Auction
import com.example.auctionarymobile.model.toImageBitmap
import com.example.auctionarymobile.ui.theme.*
import com.example.auctionarymobile.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionListScreen(
    viewModel: MainViewModel,
    onMenuClick: () -> Unit,
    onAuctionClick: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    val auctions by viewModel.auctions.collectAsState()

    // --- FİLTRELEME (STATES) ---
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // --- ANLIK FİLTRELEME ---
    val filteredAuctions = auctions.filter { auction ->
        val matchesSearch = auction.productName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || auction.category.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    val activeAuctions = filteredAuctions.filter { it.isActive }
    val pastAuctions = filteredAuctions.filter { !it.isActive }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("You have no new notifications at the moment.", color = LightTextSecondary) },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Close", color = PrimaryGold, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = PrimaryGold,
                contentColor = DarkBackground,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Item")
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = DarkSurface,
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(onClick = onMenuClick) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Menu",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = "WELCOME BACK", color = PrimaryGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Discover", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = DarkSurface,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = { showNotificationDialog = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- ARAMA KUTUSU ---
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search auctions...", color = LightTextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryGold) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = PrimaryGold,
                        unfocusedContainerColor = DarkSurface,
                        focusedContainerColor = DarkSurface,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PrimaryGold
                    ),
                    singleLine = true,
                    enabled = true
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- KATEGORİLER ---
            item {
                val categories = listOf("All", "Watches", "Vehicles", "Art", "Jewelry", "Other")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) PrimaryGold else DarkSurface,
                            border = if (!isSelected) BorderStroke(1.dp, DividerColor.copy(alpha = 0.3f)) else null,
                            modifier = Modifier.clickable { selectedCategory = category } // Kategori seçimi aktif
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) DarkBackground else Color.White,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- LİSTELEME ---
            if (filteredAuctions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        if (auctions.isEmpty()) {
                            CircularProgressIndicator(color = PrimaryGold)
                        } else {
                            Text("No items found.", color = LightTextSecondary)
                        }
                    }
                }
            } else {
                if (activeAuctions.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("Featured Live", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontSize = 22.sp)
                            Text("${activeAuctions.size} Active", color = PrimaryGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(activeAuctions) { auction ->
                                FeaturedLiveCard(
                                    auction = auction,
                                    onClick = { onAuctionClick(it) },
                                    modifier = Modifier.width(320.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                if (pastAuctions.isNotEmpty()) {
                    item {
                        Text(
                            "Past Auctions",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
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
}

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
                timeLeft = "Error"
            }
            delay(1000)
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface), // Karanlık Kart
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.clickable { onClick(auction.id) }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.BottomStart
            ) {
                val imageBitmap = auction.imageUrl.toImageBitmap()
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = auction.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
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
                    Text("Ends in $timeLeft", color = PrimaryGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                    Text("${auction.currentPrice} ₺", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("STATUS", color = LightTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Active", color = StatusSuccess, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PastAuctionItem(auction: Auction, modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(auction.id) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.BottomCenter
            ) {
                val imageBitmap = auction.imageUrl.toImageBitmap()
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = auction.productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    color = StatusError.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CLOSED",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp).wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(auction.productName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Text("FINAL PRICE", color = LightTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${auction.currentPrice} ₺", color = PrimaryGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Surface(
                color = DarkBackground,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("View", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}