package com.example.chaoschess.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.ui.components.ChessPieceView
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SilverMetallic
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.WhitePieceColor
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1800)
        onSplashFinished()
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700)) + slideInVertically(initialOffsetY = { 80 }, animationSpec = spring())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // A native vector mark stays sharp at every screen density.
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(VoidCard)
                        .border(1.dp, VoidBorder, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ChessPieceView(
                        pieceType = PieceType.KNIGHT,
                        owner = PieceColor.WHITE,
                        modifier = Modifier.size(88.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "CHAOS CHESS",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(VoidCard)
                        .border(1.dp, VoidBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TACTICAL CHESS EVOLVED",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    onPlayClassic: () -> Unit,
    onGameModes: () -> Unit,
    onCustomChaos: () -> Unit,
    onHowToPlay: () -> Unit,
    onReplays: () -> Unit,
    onSettings: () -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isLoaded = true
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("main_menu_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Hero Banner Section
            AnimatedVisibility(
                visible = isLoaded,
                enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { -40 }, animationSpec = spring())
            ) {
                HeroHeaderSection()
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main "Play Modes" Feature Card
            AnimatedVisibility(
                visible = isLoaded,
                enter = fadeIn(tween(700, delayMillis = 100)) + slideInVertically(initialOffsetY = { 40 }, animationSpec = spring())
            ) {
                PlayModesHeroCard(onClick = onGameModes)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Play 2x2 Action Deck
            AnimatedVisibility(
                visible = isLoaded,
                enter = fadeIn(tween(800, delayMillis = 200)) + slideInVertically(initialOffsetY = { 40 }, animationSpec = spring())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickModeCard(
                            title = "Classic Chess",
                            subtitle = "Standard Rules",
                            icon = Icons.Default.PlayArrow,
                            accentColor = SilverMetallic,
                            modifier = Modifier.weight(1f),
                            onClick = onPlayClassic,
                            tag = "play_classic_card"
                        )
                        QuickModeCard(
                            title = "Custom Chaos",
                            subtitle = "Rules Sandbox",
                            icon = Icons.Default.Tune,
                            accentColor = GoldPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = onCustomChaos,
                            tag = "custom_chaos_card"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickModeCard(
                            title = "How to Play",
                            subtitle = "Rulebook & Tips",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            accentColor = GoldAccent,
                            modifier = Modifier.weight(1f),
                            onClick = onHowToPlay,
                            tag = "how_to_play_card"
                        )
                        QuickModeCard(
                            title = "Match Replays",
                            subtitle = "Watch History",
                            icon = Icons.Default.History,
                            accentColor = DragonCrimson,
                            modifier = Modifier.weight(1f),
                            onClick = onReplays,
                            tag = "replays_card"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Settings Card
            AnimatedVisibility(
                visible = isLoaded,
                enter = fadeIn(tween(900, delayMillis = 300)) + slideInVertically(initialOffsetY = { 30 }, animationSpec = spring())
            ) {
                Card(
                    onClick = onSettings,
                    colors = CardDefaults.cardColors(containerColor = VoidCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
                        .testTag("settings_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = WhitePieceColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Settings, Audio & Accessibility",
                                color = WhitePieceColor.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = WhitePieceColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroHeaderSection() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, VoidBorder, RoundedCornerShape(20.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldPrimary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PLAY. IMPROVE. COMPETE.",
                            color = VoidDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "CHAOS CHESS",
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "Classic chess plus 14 original variants",
                        color = WhitePieceColor.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    ChessPieceView(
                        pieceType = PieceType.ROOK,
                        owner = PieceColor.BLACK,
                        modifier = Modifier.size(48.dp)
                    )
                    ChessPieceView(
                        pieceType = PieceType.KING,
                        owner = PieceColor.WHITE,
                        modifier = Modifier.size(66.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayModesHeroCard(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = VoidCard),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, GoldPrimary, RoundedCornerShape(18.dp))
            .testTag("play_game_modes_button")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = VoidDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "PLAY A GAME",
                            color = WhitePieceColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Choose classic chess or a custom variant",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Feature Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModeFeatureChip(text = "🧬 Mutations", modifier = Modifier.weight(1f))
                ModeFeatureChip(text = "🐉 Dragon", modifier = Modifier.weight(1f))
                ModeFeatureChip(text = "👑 King Hunt", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ModeFeatureChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(VoidCardLight)
            .border(1.dp, VoidBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = WhitePieceColor.copy(alpha = 0.9f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun QuickModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = VoidCard),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .height(84.dp)
            .border(1.dp, VoidBorder, RoundedCornerShape(14.dp))
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VoidCardLight)
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = WhitePieceColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = WhitePieceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = WhitePieceColor.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
