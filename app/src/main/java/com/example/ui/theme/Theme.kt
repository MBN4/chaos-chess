package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark Fantasy Color Scheme
 * Centered on Deep Midnight Navies, Royal Violets, and Radiant Gold Accents
 */
private val DarkFantasyColorScheme = darkColorScheme(
    // Primary: Radiant Imperial Gold
    primary = GildedGold,
    onPrimary = MidnightNavyAbyss,
    primaryContainer = MidnightNavyCardElevated,
    onPrimaryContainer = ShimmerGold,

    // Secondary: Royal Violet & Arcane Amethyst
    secondary = RoyalViolet,
    onSecondary = WhitePieceColor,
    secondaryContainer = RoyalVioletDeep,
    onSecondaryContainer = RoyalVioletShimmer,

    // Tertiary: Crimson Dragonfire & Chaos Sparks
    tertiary = DragonCrimson,
    onTertiary = Color.White,
    tertiaryContainer = EventAlertBg,
    onTertiaryContainer = EventAlertAccent,

    // Canvas & Midnight Navy Surfaces
    background = MidnightNavyVoid,
    onBackground = WhitePieceColor,
    surface = MidnightNavySurface,
    onSurface = WhitePieceColor,
    surfaceVariant = MidnightNavyCard,
    onSurfaceVariant = ShimmerGold,

    // Borders & Metallic Outlines
    outline = MidnightNavyBorder,
    outlineVariant = MidnightNavyBorderGilded,

    // Errors & Fatal States
    error = CheckWarningRed,
    onError = Color.White
)

@Composable
fun ChaosChessTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MidnightNavyVoid.toArgb()
            window.navigationBarColor = MidnightNavyVoid.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkFantasyColorScheme,
        typography = DarkFantasyTypography,
        content = content
    )
}

