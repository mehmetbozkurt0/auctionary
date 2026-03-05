package com.example.auctionarymobile.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Sadece Dark Color Scheme kullanıyoruz
private val DarkColorScheme = darkColorScheme(
    primary = AccentGold,
    secondary = AccentOrange,
    tertiary = PrimaryNeonBlue,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkBackground,
    onSecondary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    error = DangerRed
)

@Composable
fun AuctionaryMobileTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    // Üstteki bildirim çubuğunun (Status Bar) rengini ayarlıyoruz
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Type.kt içindeki ayarları kullanır
        content = content
    )
}