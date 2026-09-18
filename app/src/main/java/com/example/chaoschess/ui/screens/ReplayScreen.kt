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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.chaoschess.audio.SoundEffect
import com.example.chaoschess.audio.SoundEngine
import com.example.chaoschess.data.ReplayData
import com.example.chaoschess.data.SettingsRepository
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.rules.ChaosEngine
import com.example.chaoschess.ui.components.ChessBoardView
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonFire
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurpleLight
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.WhitePieceColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReplayScreen(
    repository: SettingsRepository,
    onBackClicked: () -> Unit
) {
    val replays = remember { repository.loadReplays() }
    var activeReplay by remember { mutableStateOf<ReplayData?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(16.dp)
            .testTag("replay_screen")
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    if (activeReplay != null) {
                        activeReplay = null
                    } else {
                        onBackClicked()
                    }
                },
                modifier = Modifier.testTag("replay_back")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhitePieceColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (activeReplay != null) "REPLAY VIEWER" else "MATCH REPLAYS",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = if (activeReplay != null) activeReplay!!.title else "${replays.size} Recorded Matches",
                    color = MysticPurpleLight,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeReplay != null) {
            // Replay Player
            ReplayPlayerView(
                replay = activeReplay!!,
                onClose = { activeReplay = null }
            )
        } else {
            // Replay List
            if (replays.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved match replays yet.\nPlay a match to record games!",
                        color = WhitePieceColor.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(replays) { item ->
                        ReplayCardItem(
                            replay = item,
                            onClick = { activeReplay = item }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplayCardItem(
    replay: ReplayData,
    onClick: () -> Unit
) {
    val dateStr = remember(replay.dateEpoch) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(replay.dateEpoch))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = VoidCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("replay_item_${replay.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = replay.title,
                    color = WhitePieceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = dateStr,
                    color = MysticPurpleLight,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${replay.totalMoves} moves • Result: ${if (replay.winner != null) "${replay.winner.name} Victory" else "Draw"}",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.2f))
                    .border(1.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Watch",
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ReplayPlayerView(
    replay: ReplayData,
    onClose: () -> Unit
) {
    var currentMoveIndex by remember { mutableStateOf(0) }

    // Replay engine instance
    val replayEngine = remember(replay.id) {
        ChaosEngine(mode = replay.mode, seed = replay.seed)
    }

    // Step to index
    fun stepTo(index: Int) {
        val previousIndex = currentMoveIndex
        val target = index.coerceIn(0, replay.moveNotations.size)
        replayEngine.reset()
        var lastMovedPieceType: com.example.chaoschess.engine.models.PieceType? = null
        var isLastCapture = false
        for (i in 0 until target) {
            val notation = replay.moveNotations[i] // e.g. "e2->e4"
            val parts = notation.split("->")
            if (parts.size == 2) {
                val from = Position.fromAlgebraic(parts[0])
                val to = Position.fromAlgebraic(parts[1])
                val matching = replayEngine.getLegalMoves().find { it.from == from && it.to == to }
                if (matching != null) {
                    if (i == target - 1) {
                        lastMovedPieceType = matching.piece.currentType
                        isLastCapture = (matching.capturedPiece != null)
                    }
                    replayEngine.executeMove(matching)
                }
            }
        }
        if (target > previousIndex && lastMovedPieceType != null) {
            if (isLastCapture) {
                SoundEngine.playSound(SoundEffect.CAPTURE)
            } else {
                SoundEngine.playPieceMove(lastMovedPieceType)
            }
        }
        currentMoveIndex = target
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Board
        ChessBoardView(
            board = replayEngine.state.board,
            selectedPosition = null,
            legalMovesForSelected = emptyList(),
            lastMove = replayEngine.state.moveHistory.lastOrNull(),
            isCheck = replayEngine.state.isCheck,
            turn = replayEngine.state.turn,
            perspective = PieceColor.WHITE,
            onSquareClicked = {},
            modifier = Modifier.fillMaxWidth()
        )

        // Replay Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Move $currentMoveIndex of ${replay.moveNotations.size}",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { stepTo(0) },
                    modifier = Modifier.clip(CircleShape).background(VoidCard)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = WhitePieceColor)
                }

                IconButton(
                    onClick = { stepTo(currentMoveIndex - 1) },
                    enabled = currentMoveIndex > 0,
                    modifier = Modifier.clip(CircleShape).background(VoidCard)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = if (currentMoveIndex > 0) WhitePieceColor else Color.Gray)
                }

                IconButton(
                    onClick = { stepTo(currentMoveIndex + 1) },
                    enabled = currentMoveIndex < replay.moveNotations.size,
                    modifier = Modifier.clip(CircleShape).background(VoidCard)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = if (currentMoveIndex < replay.moveNotations.size) WhitePieceColor else Color.Gray)
                }
            }
        }
    }
}
