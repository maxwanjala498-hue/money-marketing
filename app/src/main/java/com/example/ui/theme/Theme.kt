package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BinanceYellow,
    onPrimary = BinanceBackground,
    primaryContainer = BinanceGold,
    onPrimaryContainer = BinanceBackground,
    secondary = BinanceYellow,
    onSecondary = BinanceBackground,
    background = BinanceBackground,
    onBackground = BinanceTextPrimary,
    surface = BinanceCard,
    onSurface = BinanceTextPrimary,
    surfaceVariant = BinanceCardElevated,
    onSurfaceVariant = BinanceTextSecondary,
    outline = BinanceBorder,
    error = BinanceRed,
    onError = BinanceTextPrimary
)

@Composable
fun BinanceConnectTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BinanceBackground.toArgb()
            window.navigationBarColor = BinanceBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
