package com.example.chaoschess.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.engine.models.CustomChaosConfig
import com.example.chaoschess.engine.models.MutationProbabilities
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonFire
import com.example.ui.theme.EmeraldArcane
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurple
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.WhitePieceColor

@Composable
fun CustomChaosBuilderScreen(
    onStartCustomMatch: (CustomChaosConfig) -> Unit,
    onBackClicked: () -> Unit
) {
    var mutationEnabled by remember { mutableStateOf(true) }
    var dragonEnabled by remember { mutableStateOf(true) }
    var dragonCharges by remember { mutableStateOf(1) }
    var mysteryEnabled by remember { mutableStateOf(false) }
    var mysteryChance by remember { mutableStateOf(0.25) }
    var kingHuntEnabled by remember { mutableStateOf(false) }
    var championEnabled by remember { mutableStateOf(false) }
    var boardEventsEnabled by remember { mutableStateOf(true) }
    var speedRulesEnabled by remember { mutableStateOf(false) }
    var undeadGraveyardEnabled by remember { mutableStateOf(false) }
    var elementalAffinitiesEnabled by remember { mutableStateOf(false) }
    var portalsEnabled by remember { mutableStateOf(false) }
    var cloneDuplicationEnabled by remember { mutableStateOf(false) }
    var timeWarpEnabled by remember { mutableStateOf(false) }
    var apocalypseEnabled by remember { mutableStateOf(false) }

    val currentConfig = CustomChaosConfig(
        mutationEnabled = mutationEnabled,
        mutationProbabilities = MutationProbabilities(),
        dragonEnabled = dragonEnabled,
        dragonChargesPerPlayer = dragonCharges,
        mysteryEnabled = mysteryEnabled,
        mysteryChance = mysteryChance,
        kingHuntEnabled = kingHuntEnabled,
        championEnabled = championEnabled,
        boardEventsEnabled = boardEventsEnabled,
        speedRulesEnabled = speedRulesEnabled,
        undeadEnabled = undeadGraveyardEnabled,
        elementalEnabled = elementalAffinitiesEnabled,
        portalsEnabled = portalsEnabled,
        cloneWarsEnabled = cloneDuplicationEnabled,
        timeWarpEnabled = timeWarpEnabled,
        apocalypseEnabled = apocalypseEnabled
    )

    val activeChaosCount = listOf(
        mutationEnabled, dragonEnabled, mysteryEnabled, kingHuntEnabled,
        championEnabled, boardEventsEnabled, speedRulesEnabled, undeadGraveyardEnabled,
        elementalAffinitiesEnabled, portalsEnabled, cloneDuplicationEnabled, timeWarpEnabled, apocalypseEnabled
    ).count { it }

    val (computedChaosRating, chaosIntensityLabel) = currentConfig.calculateChaosRating()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(16.dp)
            .testTag("custom_chaos_builder_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClicked, modifier = Modifier.testTag("custom_chaos_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhitePieceColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "CUSTOM CHAOS BUILDER",
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.2.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Active: $activeChaosCount/13  •  Chaos Rating: ",
                            color = EmeraldArcane,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        repeat(computedChaosRating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rule Configuration List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    RuleToggleCard(
                        title = "🧬 Piece Mutation",
                        description = "Every capture triggers piece transformation (0.001% King).",
                        isEnabled = mutationEnabled,
                        onToggle = { mutationEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🐉 Dragon Transformation",
                        description = "Enable Dragon pieces (Knight leaps + Bishop slides).",
                        isEnabled = dragonEnabled,
                        onToggle = { dragonEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🎭 Mystery Disguise",
                        description = "Secret hidden piece identities revealed upon movement/capture.",
                        isEnabled = mysteryEnabled,
                        onToggle = { mysteryEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "👑 King Hunt Mode",
                        description = "Check/checkmate disabled. Directly capture enemy King to win.",
                        isEnabled = kingHuntEnabled,
                        onToggle = { kingHuntEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "★ Last Stand Champions",
                        description = "Choose a Champion with super-abilities; losing Champion = defeat.",
                        isEnabled = championEnabled,
                        onToggle = { championEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🔥 Dynamic Board Hazards",
                        description = "Meteors, burning tiles, teleport portals, and collapse events.",
                        isEnabled = boardEventsEnabled,
                        onToggle = { boardEventsEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "⚡ Speed Chaos Rules",
                        description = "Periodic temporary rules (backward pawns, knight-bishops).",
                        isEnabled = speedRulesEnabled,
                        onToggle = { speedRulesEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "⚰️ Undead Graveyard",
                        description = "Captured non-royal pieces resurrect 3 turns later on back ranks.",
                        isEnabled = undeadGraveyardEnabled,
                        onToggle = { undeadGraveyardEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🔥 Elemental Affinities",
                        description = "Pieces have elements (Fire, Ice, Lightning, Nature) with combat multipliers.",
                        isEnabled = elementalAffinitiesEnabled,
                        onToggle = { elementalAffinitiesEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🌀 Teleportation Portals",
                        description = "Dual connected rift gates instantly warp moving pieces across the board.",
                        isEnabled = portalsEnabled,
                        onToggle = { portalsEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🧬 Clone Wars",
                        description = "Pieces reaching rank 4/5 split into an adjacent clone duplicate.",
                        isEnabled = cloneDuplicationEnabled,
                        onToggle = { cloneDuplicationEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "⏳ Time Warp Mechanics",
                        description = "Temporal charges allow turn rewind, square freezing, and accelerated double turns.",
                        isEnabled = timeWarpEnabled,
                        onToggle = { timeWarpEnabled = it }
                    )
                }

                item {
                    RuleToggleCard(
                        title = "🌋 Apocalypse Cataclysm",
                        description = "Cataclysm meter rises with strikes; outer board tiles collapse into the void.",
                        isEnabled = apocalypseEnabled,
                        onToggle = { apocalypseEnabled = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Start Custom Match CTA
        Button(
            onClick = { onStartCustomMatch(currentConfig) },
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = VoidDark
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_custom_chaos_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LAUNCH CUSTOM CHAOS MATCH",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun RuleToggleCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VoidCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isEnabled) GoldPrimary.copy(alpha = 0.5f) else VoidBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isEnabled) GoldAccent else WhitePieceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = description,
                    color = WhitePieceColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VoidDark,
                    checkedTrackColor = GoldPrimary,
                    uncheckedThumbColor = WhitePieceColor.copy(alpha = 0.6f),
                    uncheckedTrackColor = VoidCardLight
                )
            )
        }
    }
}
