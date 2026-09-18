package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark Fantasy Colors - Deep Midnight Navies, Royal Violets & Gold Accents
 */
object DarkFantasyColors {
    // Deep Midnight Navy Canvas & Surfaces
    val MidnightAbyss = MidnightNavyAbyss               // 0xFF070A12 Deepest void canvas
    val MidnightCanvas = MidnightNavyVoid               // 0xFF0B101E Primary dark canvas
    val MidnightSurface = MidnightNavySurface           // 0xFF12182B Elevated app bars & dialogs
    val MidnightCard = MidnightNavyCard                 // 0xFF18223C Tactical HUD & cards
    val MidnightCardElevated = MidnightNavyCardElevated // 0xFF222F52 Hover / focused card
    val MidnightBorder = MidnightNavyBorder             // 0xFF2D3C66 Metallic navy outline
    val GildedBorder = MidnightNavyBorderGilded         // 0xFF6B541E Burnished antique gold border

    // Royal Violets & Arcane Amethyst
    val RoyalViolet = com.example.ui.theme.RoyalViolet   // 0xFF7C3AED Vibrant Royal Violet
    val RoyalVioletGlow = com.example.ui.theme.RoyalVioletGlow // 0xFF8B5CF6 Arcane Violet
    val RoyalVioletLight = com.example.ui.theme.RoyalVioletLight // 0xFFA78BFA Soft Violet
    val RoyalVioletShimmer = com.example.ui.theme.RoyalVioletShimmer // 0xFFDDD6FE Luminous Violet
    val RoyalVioletDeep = com.example.ui.theme.RoyalVioletDeep // 0xFF241242 Deep container violet

    // Metallic Royal Gold & Luminous Accents
    val ImperialGold = com.example.ui.theme.ImperialGold // 0xFFF59E0B Primary Imperial Gold
    val GildedGold = com.example.ui.theme.GildedGold     // 0xFFFBBF24 Radiant Gold CTA & Highlight
    val ShimmerGold = com.example.ui.theme.ShimmerGold   // 0xFFFDE68A Luminous pale gold text
    val BurnishedGold = com.example.ui.theme.AntiqueGold // 0xFFD97706 Antique gold accent
    val DeepGold = com.example.ui.theme.DeepBronzeGold   // 0xFF78350F Bronze gold shadows

    // Metallics & Inlays
    val MithrilSilver = SilverMetallic                  // 0xFFE2E8F0 Polished mithril
    val BronzeAccent = BronzeMetallic                   // 0xFFB45309 Warm bronze
    val PlatinumWhite = WhitePieceColor                 // 0xFFF8FAFC High-contrast text & ivory

    // Fantasy Arcane & Danger Accents
    val CrimsonHazard = DragonCrimson                   // 0xFFEF4444 Dragon fire / Check warning
    val AmberBlaze = DragonFire                         // 0xFFF97316 Fiery ember
    val ArcanePurple = com.example.ui.theme.RoyalVioletLight // 0xFFA78BFA Mystic spell lavender
    val ArcanePurpleLight = com.example.ui.theme.RoyalVioletShimmer // 0xFFDDD6FE Arcane glow
    val CelestialMana = com.example.ui.theme.CelestialCyan // 0xFF38BDF8 Mana / teleport rift
    val EmeraldRune = com.example.ui.theme.EmeraldArcane   // 0xFF10B981 Vitality & success emerald
}

// Backward-compatibility alias
val ElegantDarkColors = DarkFantasyColors

/**
 * Material 3 ColorScheme for Dark Fantasy
 */
val DarkFantasyM3ColorScheme: ColorScheme = darkColorScheme(
    primary = DarkFantasyColors.GildedGold,
    onPrimary = DarkFantasyColors.MidnightAbyss,
    primaryContainer = DarkFantasyColors.MidnightCardElevated,
    onPrimaryContainer = DarkFantasyColors.ShimmerGold,
    
    secondary = DarkFantasyColors.RoyalViolet,
    onSecondary = DarkFantasyColors.PlatinumWhite,
    secondaryContainer = DarkFantasyColors.RoyalVioletDeep,
    onSecondaryContainer = DarkFantasyColors.RoyalVioletShimmer,
    
    tertiary = DarkFantasyColors.CrimsonHazard,
    onTertiary = Color.White,
    tertiaryContainer = EventAlertBg,
    onTertiaryContainer = EventAlertAccent,
    
    background = DarkFantasyColors.MidnightCanvas,
    onBackground = DarkFantasyColors.PlatinumWhite,
    
    surface = DarkFantasyColors.MidnightSurface,
    onSurface = DarkFantasyColors.PlatinumWhite,
    surfaceVariant = DarkFantasyColors.MidnightCard,
    onSurfaceVariant = DarkFantasyColors.ShimmerGold,
    
    outline = DarkFantasyColors.MidnightBorder,
    outlineVariant = DarkFantasyColors.GildedBorder,
    
    error = DarkFantasyColors.CrimsonHazard,
    onError = Color.White,
    errorContainer = Color(0xFF3B151E),
    onErrorContainer = Color(0xFFFFDAD6)
)

val ElegantDarkColorScheme = DarkFantasyM3ColorScheme

/**
 * Extended palette properties for dark fantasy UI elements
 */
@Immutable
data class ExtendedFantasyColors(
    val midnightCanvas: Color = DarkFantasyColors.MidnightCanvas,
    val midnightSurface: Color = DarkFantasyColors.MidnightSurface,
    val midnightCard: Color = DarkFantasyColors.MidnightCard,
    val royalViolet: Color = DarkFantasyColors.RoyalViolet,
    val royalVioletLight: Color = DarkFantasyColors.RoyalVioletLight,
    val royalVioletDeep: Color = DarkFantasyColors.RoyalVioletDeep,
    val gildedGold: Color = DarkFantasyColors.GildedGold,
    val shimmerGold: Color = DarkFantasyColors.ShimmerGold,
    val burnishedGold: Color = DarkFantasyColors.BurnishedGold,
    val mithrilSilver: Color = DarkFantasyColors.MithrilSilver,
    val bronzeAccent: Color = DarkFantasyColors.BronzeAccent,
    val crimsonHazard: Color = DarkFantasyColors.CrimsonHazard,
    val celestialMana: Color = DarkFantasyColors.CelestialMana,
    val emeraldRune: Color = DarkFantasyColors.EmeraldRune,
    val gildedBorder: Color = DarkFantasyColors.GildedBorder
)

val LocalExtendedFantasyColors = staticCompositionLocalOf { ExtendedFantasyColors() }

/**
 * Dark Fantasy Material3 Theme Composable
 */
@Composable
fun DarkFantasyTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkFantasyColors.MidnightCanvas.toArgb()
            window.navigationBarColor = DarkFantasyColors.MidnightCanvas.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(
        LocalExtendedFantasyColors provides ExtendedFantasyColors()
    ) {
        MaterialTheme(
            colorScheme = DarkFantasyM3ColorScheme,
            typography = DarkFantasyTypography,
            content = content
        )
    }
}

// Backward-compatibility alias
@Composable
fun ElegantDarkTheme(
    content: @Composable () -> Unit
) = DarkFantasyTheme(content = content)

