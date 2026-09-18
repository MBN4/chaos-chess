package com.example.chaoschess.ui.components

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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.engine.models.ChampionState
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurple
import com.example.ui.theme.MysticPurpleLight
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.WhitePieceColor

@Composable
fun PlayerHud(
    color: PieceColor,
    playerName: String,
    isCurrentTurn: Boolean,
    capturedPieces: List<Piece>,
    materialAdvantage: Int,
    dragonCharges: Int,
    championState: ChampionState?,
    onDragonChargeClicked: (() -> Unit)? = null,
    onChampionAbilityClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isCurrentTurn) GoldPrimary else VoidBorder
    val bgBrush = if (isCurrentTurn) {
        Brush.horizontalGradient(listOf(VoidCardLight, VoidCard))
    } else {
        Brush.horizontalGradient(listOf(VoidCard, VoidSurface))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(if (isCurrentTurn) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgBrush)
            .testTag("player_hud_${color.name.lowercase()}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player Avatar & Identity
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (color == PieceColor.WHITE) GoldPrimary else VoidCard)
                        .border(1.5.dp, if (color == PieceColor.WHITE) GoldAccent else VoidBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (color == PieceColor.WHITE) "W" else "B",
                        color = if (color == PieceColor.WHITE) VoidSurface else WhitePieceColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = playerName,
                            color = WhitePieceColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        if (isCurrentTurn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldPrimary)
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = "TURN",
                                    color = VoidSurface,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    // Captured pieces display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        if (capturedPieces.isNotEmpty()) {
                            val pieceSymbols = capturedPieces.map {
                                when (it.currentType) {
                                    PieceType.PAWN -> "♙"
                                    PieceType.KNIGHT -> "♘"
                                    PieceType.BISHOP -> "♗"
                                    PieceType.ROOK -> "♖"
                                    PieceType.QUEEN -> "♕"
                                    PieceType.DRAGON -> "🐉"
                                    PieceType.KING -> "♔"
                                }
                            }.joinToString(" ")
                            Text(
                                text = pieceSymbols,
                                color = MysticPurpleLight,
                                fontSize = 12.sp
                            )
                        }
                        if (materialAdvantage > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+$materialAdvantage",
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Action Buttons / Chaos Badges (Dragon charge, Champion ability)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (dragonCharges > 0 && onDragonChargeClicked != null) {
                    ElevatedButton(
                        onClick = onDragonChargeClicked,
                        enabled = isCurrentTurn,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = DragonCrimson,
                            contentColor = WhitePieceColor
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("dragon_charge_button")
                    ) {
                        Text("🐉 $dragonCharges", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (championState != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (championState.abilityAvailable) GoldPrimary.copy(alpha = 0.2f) else VoidDark)
                            .border(1.dp, if (championState.abilityAvailable) GoldPrimary else VoidBorder, RoundedCornerShape(10.dp))
                            .clickable(enabled = championState.abilityAvailable && isCurrentTurn) {
                                onChampionAbilityClicked?.invoke()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("champion_hud_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Champion",
                                tint = if (championState.abilityAvailable) GoldPrimary else Color.Gray,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = championState.championType.pieceType.letter,
                                color = if (championState.abilityAvailable) GoldAccent else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
