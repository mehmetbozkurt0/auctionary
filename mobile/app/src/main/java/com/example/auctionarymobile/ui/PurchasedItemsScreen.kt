package com.example.auctionarymobile.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
fun PurchasedItemsScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val auctions by viewModel.auctions.collectAsState()
    val currentUsername = viewModel.currentUsername.ifEmpty {
        AuthManager.getUsername() ?: ""
    }
    val purchasedItems = auctions.filter { !it.isActive && it.winnerId == currentUsername}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Purchased Items", color = PrimaryGold, fontWeight = FontWeight.Bold)},
                navigationIcon = {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            if (purchasedItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Empty Cart", modifier = Modifier.size(100.dp), tint = DividerColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "You haven't win an auction yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = LightTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Join an auction to\nexpand your collection",
                        color = LightTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(purchasedItems){auction ->
                        PurchasedItemCard(auction = auction)
                    }
                }
            }
        }
    }
}

@Composable
fun PurchasedItemCard(auction: Auction) {
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
                    Text(text = "Price Paid", color = LightTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = StatusSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Payment Received - Preparing to Shipment", color = StatusSuccess, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Text(text = "Detail", color = LightTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}













