package com.example.auctionarymobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.model.Auction
import com.example.auctionarymobile.model.toImageBitmap
import com.example.auctionarymobile.network.AuthManager
import com.example.auctionarymobile.ui.theme.*
import com.example.auctionarymobile.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAuctionsScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    val auctions by viewModel.auctions.collectAsState()
    val currentUsername = viewModel.currentUsername.ifEmpty {
        AuthManager.getUsername() ?: ""
    }

    val myAuctions = auctions.filter { it.sellerId == currentUsername }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Auctions", color = PrimaryGold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = PrimaryGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = DarkSurface,
                contentColor = PrimaryGold,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Sell a product")
            }
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (myAuctions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty Store!",
                        modifier = Modifier.size(100.dp),
                        tint = DividerColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "You have no item on sale right now.",
                        style = MaterialTheme.typography.titleLarge,
                        color = LightTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Let's start an auction and\nsell your stuff.",
                        color = LightTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(myAuctions) { auction ->
                        MyAuctionItemCard(auction = auction)
                    }
                }
            }
        }
    }
}

@Composable
fun MyAuctionItemCard(auction: Auction) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(DarkBackground),
                    contentAlignment = Alignment.Center
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
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = auction.productName,
                        color = LightTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "CURRENT PRİCE", color = LightTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${auction.currentPrice} ₺", color = PrimaryGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (auction.isActive) {
                    Text(text = "🟢 Live auction continues", color = StatusSuccess, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text(text = "🔴 Auction ended!", color = StatusError, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Text(text = "Manage", color = LightTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}