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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.chaoschess.ai.AIDifficulty
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.PieceColor
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurpleLight
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.WhitePieceColor

enum class MatchOpponentType {
    AI,
    LOCAL_PASS_AND_PLAY
}

@Composable
fun MatchSetupScreen(
    mode: GameMode,
    onStartMatch: (opponentType: MatchOpponentType, difficulty: AIDifficulty, playerColor: PieceColor) -> Unit,
    onBackClicked: () -> Unit
) {
    var opponentType by remember { mutableStateOf(MatchOpponentType.AI) }
    var difficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }
    var selectedColor by remember { mutableStateOf(PieceColor.WHITE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(20.dp)
            .testTag("match_setup_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClicked, modifier = Modifier.testTag("setup_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhitePieceColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "MATCH SETUP",
                        color = GoldAccent,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = mode.title,
                        color = MysticPurpleLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Match Type Selection (AI vs Local 2P)
            Text(
                text = "OPPONENT",
                color = WhitePieceColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChoicePill(
                    title = "🤖 Versus AI",
                    isSelected = opponentType == MatchOpponentType.AI,
                    modifier = Modifier.weight(1f),
                    onClick = { opponentType = MatchOpponentType.AI }
                )
                ChoicePill(
                    title = "👥 Pass & Play",
                    isSelected = opponentType == MatchOpponentType.LOCAL_PASS_AND_PLAY,
                    modifier = Modifier.weight(1f),
                    onClick = { opponentType = MatchOpponentType.LOCAL_PASS_AND_PLAY }
                )
            }

            // AI Difficulty selector (if vs AI)
            if (opponentType == MatchOpponentType.AI) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "AI DIFFICULTY",
                    color = WhitePieceColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AIDifficulty.values().toList().chunked(3).forEach { rowDifficulties ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowDifficulties.forEach { diff ->
                                ChoicePill(
                                    title = diff.label,
                                    isSelected = difficulty == diff,
                                    modifier = Modifier.weight(1f),
                                    onClick = { difficulty = diff }
                                )
                            }
                            repeat(3 - rowDifficulties.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = difficulty.description,
                    color = MysticPurpleLight,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Play As Color
            Text(
                text = "PLAY AS",
                color = WhitePieceColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChoicePill(
                    title = "♔ White (First)",
                    isSelected = selectedColor == PieceColor.WHITE,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedColor = PieceColor.WHITE }
                )
                ChoicePill(
                    title = "♚ Black",
                    isSelected = selectedColor == PieceColor.BLACK,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedColor = PieceColor.BLACK }
                )
            }
        }

        // Start Match Button
        Button(
            onClick = { onStartMatch(opponentType, difficulty, selectedColor) },
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = VoidDark
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("start_match_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "START MATCH",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
private fun ChoicePill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) GoldPrimary.copy(alpha = 0.25f) else VoidCard)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) GoldPrimary else VoidBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) GoldAccent else WhitePieceColor.copy(alpha = 0.8f),
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
