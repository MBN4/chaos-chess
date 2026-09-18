package com.example.chaoschess.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chaoschess.engine.models.ChampionType
import com.example.chaoschess.engine.models.Move
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.rules.MatchResult
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonFire
import com.example.ui.theme.EmeraldArcane
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurple
import com.example.ui.theme.MysticPurpleLight
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.WhitePieceColor

@Composable
fun MutationBannerModal(
    move: Move,
    onDismiss: () -> Unit
) {
    val isUltraRare = move.isRareKingMutation
    val fromType = move.piece.currentType
    val toType = move.mutationResult ?: PieceType.QUEEN

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .border(
                    3.dp,
                    if (isUltraRare) GoldPrimary else MysticPurpleLight,
                    RoundedCornerShape(20.dp)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isUltraRare) Color(0xFF332200) else Color(0xFF200E3D),
                            VoidDark
                        )
                    )
                )
                .padding(24.dp)
                .testTag("mutation_banner_modal"),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isUltraRare) {
                    Text(
                        text = "★ ULTRA RARE EVENT ★",
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "0.001% ROYAL MUTATION",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                } else {
                    Text(
                        text = "CAPTURE MUTATION!",
                        color = MysticPurpleLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${fromType.name} MUTATED",
                        color = WhitePieceColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(VoidCardLight)
                            .border(1.5.dp, VoidBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ChessPieceView(
                            pieceType = fromType,
                            owner = move.piece.owner,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "➔",
                        color = if (isUltraRare) GoldPrimary else MysticPurpleLight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isUltraRare) GoldPrimary.copy(alpha = 0.3f) else MysticPurple.copy(alpha = 0.3f))
                            .border(2.5.dp, if (isUltraRare) GoldPrimary else MysticPurpleLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ChessPieceView(
                            pieceType = toType,
                            owner = move.piece.owner,
                            modifier = Modifier.size(58.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isUltraRare) {
                        "A legendary Secondary King has risen on the board!"
                    } else if (fromType == PieceType.QUEEN && toType == PieceType.PAWN) {
                        "Unlucky fate! The Queen devolved into a Pawn."
                    } else {
                        "The capturing unit transformed into a ${toType.name}!"
                    },
                    color = WhitePieceColor.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isUltraRare) GoldPrimary else MysticPurple,
                        contentColor = VoidDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.7f).testTag("mutation_continue_button")
                ) {
                    Text("CONTINUE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DragonTransformModal(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(20.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .border(2.5.dp, DragonCrimson, RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF380A1E), VoidDark)))
                .padding(20.dp)
                .testTag("dragon_transform_modal"),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🐉 UNLEASH THE DRAGON",
                    color = DragonFire,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select a non-King piece to permanently mutate into a Dragon (Knight leaps + Bishop diagonal slide)!",
                    color = WhitePieceColor.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CANCEL", color = WhitePieceColor)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DragonCrimson,
                            contentColor = WhitePieceColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("TRANSFORM", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChampionSelectionModal(
    color: PieceColor,
    onChampionSelected: (ChampionType) -> Unit
) {
    val champions = ChampionType.values()

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .border(2.5.dp, GoldPrimary, RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(VoidCardLight, VoidDark)))
                .padding(20.dp)
                .testTag("champion_selection_modal"),
            color = Color.Transparent
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CHOOSE YOUR CHAMPION",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "${color.name} PLAYER",
                    color = MysticPurpleLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "If your Champion falls, you immediately lose the match!",
                    color = DragonCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    champions.forEach { champ ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoidCard)
                                .border(1.dp, VoidBorder, RoundedCornerShape(10.dp))
                                .clickable { onChampionSelected(champ) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(VoidDark)
                                    .border(1.5.dp, GoldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                ChessPieceView(
                                    pieceType = champ.pieceType,
                                    owner = color,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = champ.title,
                                        color = WhitePieceColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "• ${champ.abilityName}",
                                        color = GoldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = champ.abilityDescription,
                                    color = WhitePieceColor.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PawnPromotionDialog(
    color: PieceColor,
    onPieceChosen: (PieceType) -> Unit
) {
    val choices = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(20.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, GoldPrimary, RoundedCornerShape(16.dp))
                .background(VoidCard)
                .padding(20.dp)
                .testTag("pawn_promotion_dialog"),
            color = Color.Transparent
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PROMOTE PAWN",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    choices.forEach { type ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoidDark)
                                .border(1.5.dp, VoidBorder, RoundedCornerShape(10.dp))
                                .clickable { onPieceChosen(type) }
                                .padding(4.dp)
                                .testTag("promo_${type.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            ChessPieceView(
                                pieceType = type,
                                owner = color,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameResultDialog(
    result: MatchResult,
    moveCount: Int,
    onRematch: () -> Unit,
    onModeSelect: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(28.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .border(3.dp, GoldPrimary, RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(VoidCardLight, VoidDark)))
                .padding(24.dp)
                .testTag("game_result_dialog"),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (result.winner != null) "VICTORY" else "MATCH OVER",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = result.title,
                    color = if (result.winner != null) CelestialCyan else MysticPurpleLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = result.description,
                    color = WhitePieceColor.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Total Moves: $moveCount",
                    color = WhitePieceColor.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRematch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = VoidDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("rematch_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PLAY AGAIN", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onModeSelect,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("mode_select_button")
                ) {
                    Text("CHANGE MODE", color = WhitePieceColor)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onMainMenu,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("main_menu_button")
                ) {
                    Text("MAIN MENU", color = WhitePieceColor.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun PauseMenuDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, VoidBorder, RoundedCornerShape(20.dp))
                .background(VoidCard)
                .padding(24.dp)
                .testTag("pause_menu_dialog"),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PAUSED",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = VoidDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("RESUME", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("RESTART MATCH", color = WhitePieceColor)
                }

                OutlinedButton(
                    onClick = onHowToPlay,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("HOW TO PLAY", color = WhitePieceColor)
                }

                OutlinedButton(
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SETTINGS", color = WhitePieceColor)
                }

                OutlinedButton(
                    onClick = onQuit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DragonCrimson),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("QUIT TO MENU", color = DragonCrimson)
                }
            }
        }
    }
}

@Composable
fun TimeWarpModal(
    charges: Int,
    onActionSelected: (com.example.chaoschess.engine.models.TimeWarpAction) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, CelestialCyan, RoundedCornerShape(20.dp))
                .background(VoidDark)
                .padding(20.dp)
                .testTag("time_warp_modal"),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "⏳ TEMPORAL CONTROL",
                    color = CelestialCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Temporal Charges Remaining: $charges",
                    color = WhitePieceColor.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                com.example.chaoschess.engine.models.TimeWarpAction.values().forEach { action ->
                    Button(
                        onClick = { onActionSelected(action) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoidCardLight,
                            contentColor = WhitePieceColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(action.title, color = CelestialCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(action.description, color = WhitePieceColor.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CANCEL", color = WhitePieceColor.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun GraveyardModal(
    graveyard: List<com.example.chaoschess.engine.models.GraveyardPiece>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, MysticPurple, RoundedCornerShape(20.dp))
                .background(VoidDark)
                .padding(20.dp)
                .testTag("graveyard_modal"),
            color = Color.Transparent
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "⚰️ UNDEAD GRAVEYARD",
                    color = MysticPurpleLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Fallen souls returning in 3 turns",
                    color = WhitePieceColor.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (graveyard.isEmpty()) {
                    Text(
                        text = "Graveyard is currently empty.",
                        color = WhitePieceColor.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    graveyard.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoidCard)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (entry.piece.owner == PieceColor.WHITE) "⚪" else "⚫",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = entry.piece.currentType.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = WhitePieceColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "Resurrects in ${entry.turnsUntilRevive} turn(s)",
                                color = EmeraldArcane,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VoidCardLight, contentColor = WhitePieceColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CLOSE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
