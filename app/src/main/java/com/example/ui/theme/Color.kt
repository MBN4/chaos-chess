package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Modern Chess UI & Dark Canvas (Chess.com Inspired)
// ==========================================
val ModernChessBg = Color(0xFF262522)
val ModernChessSurface = Color(0xFF302E2B)
val ModernChessCard = Color(0xFF3A3836)
val ModernChessCardElevated = Color(0xFF454441)
val ModernChessBorder = Color(0xFF504E4B)
val ModernChessBorderGilded = Color(0xFF739552)

// Backward-compatible Void & Midnight aliases tuned to clean Chess.com dark palette
val VoidDark = ModernChessBg
val VoidSurface = ModernChessSurface
val VoidCard = ModernChessCard
val VoidCardLight = ModernChessCardElevated
val VoidBorder = ModernChessBorder
val VoidBorderGilded = ModernChessBorderGilded

val MidnightNavyAbyss = ModernChessBg
val MidnightNavyVoid = ModernChessBg
val MidnightNavySurface = ModernChessSurface
val MidnightNavyCard = ModernChessCard
val MidnightNavyCardElevated = ModernChessCardElevated
val MidnightNavyBorder = ModernChessBorder
val MidnightNavyBorderGilded = ModernChessBorderGilded

// ==========================================
// Dark Fantasy - Royal Violets & Arcane Amethyst
// ==========================================
val RoyalVioletDeep = Color(0xFF283326)
val RoyalViolet = Color(0xFF739552)
val RoyalVioletGlow = Color(0xFF81A85B)
val RoyalVioletLight = Color(0xFFA9B5A0)
val RoyalVioletShimmer = Color(0xFFD7DDD2)
val ArcaneAmethyst = Color(0xFF9333EA)          // Deep Amethyst magic gem
val ArcaneVioletGaze = Color(0xFFC084FC)        // Bright violet rune marker

// Mystic Purple aliases tuned to Royal Violets
val MysticPurple = RoyalVioletLight
val MysticPurpleLight = RoyalVioletShimmer

// ==========================================
// Imperial & Radiant Gold Accents (Chess.com / Tournament)
// ==========================================
val ImperialGold = Color(0xFF739552)
val GildedGold = Color(0xFF81A85B)
val ShimmerGold = Color(0xFFF0F0EE)
val AntiqueGold = Color(0xFF5F7F43)
val DeepBronzeGold = Color(0xFF334628)

// Gold aliases
val GoldPrimary = GildedGold
val GoldAccent = ShimmerGold
val GoldMuted = AntiqueGold
val GoldDark = DeepBronzeGold

val SilverMetallic = Color(0xFFE2E8F0)          // Mithril Silver (Secondary badges & trims)
val BronzeMetallic = Color(0xFFB45309)          // Ancient Bronze

// ==========================================
// Elemental & Combat Accents
// ==========================================
val DragonCrimson = Color(0xFFEF4444)           // Dragon Fire / Lethal Check / Danger
val DragonFire = Color(0xFFF97316)              // Blazing Ember Orange
val EmeraldArcane = Color(0xFF10B981)           // Rune Life / Nature Affinity Emerald
val CelestialCyan = Color(0xFF38BDF8)           // Mana / Teleportation Rift Cyan

// ==========================================
// Event & Alert Tokens (Violet & Crimson Gothic)
// ==========================================
val EventAlertBg = Color(0xFF2A151A)            // Active Chaos event background container
val EventAlertText = Color(0xFFFFD6E7)          // Active Chaos event title
val EventAlertBorder = Color(0xFF991B38)        // Crimson-violet event outline
val EventAlertAccent = Color(0xFFFB7185)        // Event pulse badge accent

// ==========================================
// High-Visibility Chess Board Palettes (Chess.com & Real Tournament)
// ==========================================
// 1. Classic Chess.com Tournament Green (The Gold Standard for piece visibility)
val ChessComGreenLight = Color(0xFFEBECD0)
val ChessComGreenDark = Color(0xFF779556)

// 2. Warm Walnut & Natural Maple (Handcrafted Real Board)
val ChessWoodLight = Color(0xFFF0D9B5)           // Natural Maple light square
val ChessWoodDark = Color(0xFFB58863)            // Polished Walnut dark square

// 3. Nordic Ice & Slate Blue
val ChessSlateLight = Color(0xFFDEE3E6)          // Crisp Glacier light square
val ChessSlateDark = Color(0xFF8CA2AD)           // Slate Steel dark square

// 4. Arcane Twilight (Modern High Contrast)
val MysticLightSquare = Color(0xFFE2E8F0)       // Luminous Cloud light square
val MysticDarkSquare = Color(0xFF475569)        // Slate Granite dark square

// Classic aliases pointing to High-Visibility Chess.com Green by default
val ClassicLightSquare = ChessComGreenLight
val ClassicDarkSquare = ChessComGreenDark

// Chess.com style Highlights
val SquareSelected = Color(0xA6F4D35E)
val MoveHighlightDot = Color(0x44000000)        // Clean semi-transparent circle (Chess.com style)
val CaptureHighlightRing = Color(0x66EF4444)    // Clean translucent capture ring
val CheckWarningRed = Color(0xCCEF4444)         // Radiant King check alert
val LastMoveHighlight = Color(0x70F4D35E)
val TeleportHighlight = Color(0x8838BDF8)       // Celestial Cyan warp glow
val BurningHighlight = Color(0x99F97316)        // Fiery Amber inferno highlight
val MeteorHighlight = Color(0xBBEF4444)         // Meteor strike cataclysm highlight

// ==========================================
// Piece Rendering & Metallic Inlays (High Contrast)
// ==========================================
val WhitePieceColor = Color(0xFFF4F4F2)
val WhitePieceShadow = Color(0xFF9B9B96)
val WhitePieceGoldTrim = Color(0xFFCACAC4)

val BlackPieceColor = Color(0xFF454441)
val BlackPieceShadow = Color(0xFF171614)
val BlackPieceRimLight = Color(0xFF74726E)
val BlackPiecePurpleTrim = Color(0xFF242321)

