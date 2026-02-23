package com.example.auctionarymobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailScreen(
    auctionId: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val auctions by viewModel.auctions.collectAsState()
    println("ARANAN ID: $auctionId")
    auctions.forEach {
        println("LİSTEDEKİ ÜRÜN - İsim: ${it.productName}, ID: ${it.id}, ID Tipi: ${it.id::class.simpleName}")
    }

    val auction = auctions.find{ it.id.toString() == auctionId}

    val timeLeft by remember { mutableStateOf("00:15:30") }

    LaunchedEffect(auction) {
        while (true) {
            delay(1000)
        }
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Müzayede Detayı", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (auction == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ürün eşleşmedi!", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text("Aranan ID: '$auctionId'", color = Color.Gray)
                    Text("Listedeki Ürün Sayısı: ${auctions.size}", color = Color.Gray)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ürün İsmi
                Text(
                    text = auction.productName,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Bu eşsiz ürün için teklifler kızışıyor! 🔥", color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))

                // Fiyat ve Sayaç Kartı
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("GÜNCEL FİYAT", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${auction.currentPrice} ₺",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("KALAN SÜRE", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = timeLeft,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // TEKLİF BUTONLARI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // +50 TL Arttır
                    Button(
                        onClick = { viewModel.placeBid(auction.id, auction.currentPrice + 50) },
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)) // Turuncu
                    ) {
                        Text("+50 ₺", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    // +100 TL Arttır
                    Button(
                        onClick = { viewModel.placeBid(auction.id, auction.currentPrice + 100) },
                        modifier = Modifier.weight(1f).height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("+100 ₺", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}















