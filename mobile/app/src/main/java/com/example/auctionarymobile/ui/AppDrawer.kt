package com.example.auctionarymobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.ui.theme.*

@Composable
fun AppDrawerContent(
    username: String,
    onNavigate: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.75f),
        drawerContainerColor = DarkBackground,
        drawerContentColor = Color.White
    ) {
        Spacer(Modifier.height(48.dp))

        // --- PROFİL BAŞLIK KISMI ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Surface(
                shape = CircleShape,
                color = DarkSurface,
                border = BorderStroke(2.dp, PrimaryGold),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = if (username.isNotEmpty()) username.take(1).uppercase() else "?"
                    Text(
                        text = initial,
                        color = PrimaryGold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "Welcome back,", color = LightTextSecondary, fontSize = 14.sp)
            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = DividerColor.copy(alpha = 0.2f))
        Spacer(Modifier.height(16.dp))

        // --- MENÜ SEÇENEKLERİ ---
        DrawerMenuItem(icon = "👤", label = "Profil", onClick = { onNavigate("profile") })
        DrawerMenuItem(icon = "🛍️", label = "Satın Aldıklarım", onClick = { onNavigate("purchased_items") })
        DrawerMenuItem(icon = "📦", label = "Eklediğim Ürünler", onClick = { onNavigate("my_auctions") })

        Spacer(Modifier.weight(1f))

        HorizontalDivider(color = DividerColor.copy(alpha = 0.2f))
        Spacer(Modifier.height(16.dp))

        // --- ÇIKIŞ BUTONU ---
        DrawerMenuItem(
            icon = "🚪",
            label = "Çıkış Yap",
            onClick = onLogoutClick,
            textColor = StatusError
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun DrawerMenuItem(icon: String, label: String, onClick: () -> Unit, textColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}