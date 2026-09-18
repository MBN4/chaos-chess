package com.example.chaoschess.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.ModeCategory
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.ui.components.ChessPieceView
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonFire
import com.example.ui.theme.EmeraldArcane
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurple
import com.example.ui.theme.SilverMetallic
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.WhitePieceColor

@Composable
fun ModeSelectScreen(
    onModeSelected: (GameMode) -> Unit,
    onBackClicked: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<ModeCategory?>(null) }
    val allModes = GameMode.values()
    val displayedModes = remember(selectedCategory) {
        if (selectedCategory == null) allModes.toList()
        else allModes.filter { it.category == selectedCategory }
    }

    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isLoaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("mode_select_screen")
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(VoidCard)
                    .testTag("mode_select_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WhitePieceColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "SELECT GAME MODE",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${displayedModes.size} tactical arenas available",
                    color = WhitePieceColor.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("ALL (${allModes.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = VoidDark,
                        containerColor = VoidCard,
                        labelColor = WhitePieceColor
                    )
                )
            }
            items(ModeCategory.values().filter { it != ModeCategory.ALL }) { cat ->
                val count = allModes.count { it.category == cat }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    label = { Text("${cat.title.uppercase()} ($count)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = VoidDark,
                        containerColor = VoidCard,
                        labelColor = WhitePieceColor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mode Cards List with Staggered Entrance
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(displayedModes) { index, mode ->
                AnimatedVisibility(
                    visible = isLoaded,
                    enter = fadeIn(tween(300, delayMillis = (index * 25).coerceAtMost(300))) +
                            slideInVertically(initialOffsetY = { 20 }, animationSpec = spring())
                ) {
                    ModeCardItem(
                        mode = mode,
                        onPlay = { onModeSelected(mode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeCardItem(
    mode: GameMode,
    onPlay: () -> Unit
) {
    val pieceTypeForMode = when (mode) {
        GameMode.CLASSIC -> PieceType.KING
        GameMode.MUTATION -> PieceType.QUEEN
        GameMode.DRAGON -> PieceType.DRAGON
        GameMode.SPEED_MUTATION -> PieceType.KNIGHT
        GameMode.KING_HUNT -> PieceType.KING
        GameMode.MYSTERY_PIECE -> PieceType.PAWN
        GameMode.LAST_STAND -> PieceType.ROOK
        GameMode.CHAOS_BOARD -> PieceType.BISHOP
        GameMode.UNDEAD -> PieceType.PAWN
        GameMode.ELEMENTAL -> PieceType.BISHOP
        GameMode.PORTAL -> PieceType.KNIGHT
        GameMode.CLONE_WARS -> PieceType.ROOK
        GameMode.TIME_WARP -> PieceType.QUEEN
        GameMode.APOCALYPSE -> PieceType.DRAGON
        GameMode.CUSTOM_CHAOS -> PieceType.DRAGON
    }

    val accentColor = when (mode) {
        GameMode.CLASSIC -> SilverMetallic
        GameMode.MUTATION -> GoldAccent
        GameMode.DRAGON -> DragonFire
        GameMode.SPEED_MUTATION -> CelestialCyan
        GameMode.KING_HUNT -> GoldPrimary
        GameMode.MYSTERY_PIECE -> MysticPurple
        GameMode.LAST_STAND -> GoldAccent
        GameMode.CHAOS_BOARD -> DragonCrimson
        GameMode.UNDEAD -> MysticPurple
        GameMode.ELEMENTAL -> EmeraldArcane
        GameMode.PORTAL -> CelestialCyan
        GameMode.CLONE_WARS -> GoldAccent
        GameMode.TIME_WARP -> CelestialCyan
        GameMode.APOCALYPSE -> DragonCrimson
        GameMode.CUSTOM_CHAOS -> GoldPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
            .clickable { onPlay() }
            .testTag("mode_card_${mode.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VoidCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(VoidCardLight)
                    .border(1.5.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                ChessPieceView(
                    pieceType = pieceTypeForMode,
                    owner = PieceColor.WHITE,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mode.title,
                        color = WhitePieceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    // Chaos Rating Stars
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VoidCardLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(mode.chaosRating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                Text(
                    text = mode.subtitle,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = mode.description,
                    color = WhitePieceColor.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Play",
                tint = WhitePieceColor.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
