package com.example.auctionarymobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.ui.theme.*
import com.example.auctionarymobile.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val username = viewModel.currentUsername.ifEmpty {
        com.example.auctionarymobile.network.AuthManager.getUsername() ?: "User"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = PrimaryGold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- PROFİL AVATARI VE BİLGİLER ---
            Surface(
                shape = CircleShape,
                color = DarkSurface,
                border = BorderStroke(2.dp, PrimaryGold),
                modifier = Modifier.size(100.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = if (username.isNotEmpty()) username.take(1).uppercase() else "?"
                    Text(text = initial, color = PrimaryGold, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = username, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "Premium Member", color = PrimaryGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(24.dp))

            // Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BadgeItem(icon = Icons.Default.Star, label = "First Bid", color = PrimaryGold)
                BadgeItem(icon = Icons.Default.CheckCircle, label = "Collector", color = Color(0xFFC0C0C0))
                BadgeItem(icon = Icons.Default.Favorite, label = "Fast Bidder", color = Color(0xFFCD7F32))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Wallet
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Current Balance", color = DarkTextSecondary, fontSize = 12.sp)
                        Text(text = "12.450 ₺", color = PrimaryGold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { /* Add Money */ },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Money", tint = DarkSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Interests
            AlignLeftTitle("Interests")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InterestChip("Acoustic Guitars")
                InterestChip("Movie Posters")
                InterestChip("Sports Cards")
                InterestChip("Tarot Decks")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Activities
            AlignLeftTitle("Last Activities")
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                TimelineItem(
                    title = "You placed a 4,500 ₺ bid on Vintage Acoustic Guitar.",
                    time = "2 hours ago",
                    isLast = false
                )
                TimelineItem(
                    title = "You followed the Signed Galatasaray Jersey auction.",
                    time = "Yesterday",
                    isLast = false
                )
                TimelineItem(
                    title = "You won the Special Edition Tarot Deck auction!",
                    time = "3 days ago",
                    isLast = true
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Settings
            Button(
                onClick = { /* Goes to settings */ },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Account Settings", color = PrimaryGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AlignLeftTitle(title: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BadgeItem(icon: ImageVector, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = DarkSurface,
            border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
            modifier = Modifier.size(50.dp)
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.padding(12.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = DarkTextSecondary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun InterestChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, PrimaryGold),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = text,
            color = PrimaryGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun TimelineItem(title: String, time: String, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(PrimaryGold)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(DividerColor.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = time, color = DarkTextSecondary, fontSize = 12.sp)
        }
    }
}