package com.example.chaoschess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.ui.components.ChessPieceView
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonFire
import com.example.ui.theme.EmeraldArcane
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurpleLight
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidCardLight
import com.example.ui.theme.VoidDark
import com.example.ui.theme.WhitePieceColor

data class TutorialGuide(
    val mode: GameMode,
    val pieceType: PieceType,
    val rules: List<String>,
    val proTip: String
)

@Composable
fun TutorialScreen(
    onBackClicked: () -> Unit
) {
    val guides = listOf(
        TutorialGuide(
            mode = GameMode.CLASSIC,
            pieceType = PieceType.KING,
            rules = listOf(
                "Standard 8x8 chess rules apply with all official conventions.",
                "Pawn movement (1 forward, 2 on first turn, diagonal capture, En Passant, Promotion).",
                "King safety strictly enforced: Check, Checkmate, Stalemate, 50-move rule, Threefold repetition."
            ),
            proTip = "Master opening piece development and control of the central squares."
        ),
        TutorialGuide(
            mode = GameMode.MUTATION,
            pieceType = PieceType.QUEEN,
            rules = listOf(
                "Every capture forces the capturing piece to instantly transform into a new random piece.",
                "Pawn: 35%, Knight: 20%, Bishop: 15%, Rook: 12%, Queen: 17.999%.",
                "★ Ultra-Rare King: Exactly 0.001% chance to mutate into a secondary King!",
                "Queens can mutate into Pawns — every capture is a strategic gamble!"
            ),
            proTip = "Be careful capturing with a Queen near enemy lines in case it devolves into a Pawn."
        ),
        TutorialGuide(
            mode = GameMode.DRAGON,
            pieceType = PieceType.DRAGON,
            rules = listOf(
                "Dragon combines Knight leaping with Bishop diagonal sliding.",
                "It can jump over pieces when performing Knight moves (8 L-shapes).",
                "It cannot jump over pieces when moving diagonally like a Bishop.",
                "Each player has 1 Dragon Charge to permanently awaken a Dragon from any non-King piece."
            ),
            proTip = "Transform a centralized Bishop or Knight into a Dragon to dominate both colors and angles."
        ),
        TutorialGuide(
            mode = GameMode.SPEED_MUTATION,
            pieceType = PieceType.KNIGHT,
            rules = listOf(
                "Every 5 turns, a random temporary chaotic rule activates for 2 turns.",
                "Rules include: Backward Pawns, Knight-Bishops, Diagonal Rooks, Frost Surges, and Chaos Swaps.",
                "Active rule countdown is clearly displayed in the HUD."
            ),
            proTip = "Anticipate upcoming turn intervals to take advantage of temporary movement boosts."
        ),
        TutorialGuide(
            mode = GameMode.KING_HUNT,
            pieceType = PieceType.KING,
            rules = listOf(
                "Check and checkmate are completely disabled!",
                "Kings can walk into attacked squares and stand adjacent to enemy Kings.",
                "Victory condition: You must directly capture the enemy Royal King."
            ),
            proTip = "Use long-range Rooks and Queens to hunt down the enemy King with direct attacks."
        ),
        TutorialGuide(
            mode = GameMode.MYSTERY_PIECE,
            pieceType = PieceType.PAWN,
            rules = listOf(
                "Pieces appear disguised as other piece types to your opponent.",
                "You can see your own pieces' true hidden identities with a '?' badge.",
                "Disguised pieces reveal their identity upon capturing or making unique moves."
            ),
            proTip = "Disguise a powerful Queen as a lowly Pawn to ambush unsuspecting attackers."
        ),
        TutorialGuide(
            mode = GameMode.LAST_STAND,
            pieceType = PieceType.ROOK,
            rules = listOf(
                "Select one piece as your Champion before the match begins.",
                "If your Champion is captured, you immediately LOSE!",
                "Each Champion type grants a one-time game-changing super ability (e.g. Queen Phoenix Rebirth, Knight Double Leap)."
            ),
            proTip = "Guard your Champion with a tight defensive perimeter while hunting the opponent's Champion."
        ),
        TutorialGuide(
            mode = GameMode.CHAOS_BOARD,
            pieceType = PieceType.BISHOP,
            rules = listOf(
                "The board itself dynamically mutates with hazard warnings.",
                "Meteors strike after a countdown destroying any non-King pieces on the square.",
                "Infernal Burning squares and Teleport portal pairs warp movement across the board."
            ),
            proTip = "Pay attention to hazard countdowns and herd enemy pieces into upcoming strike zones."
        ),
        TutorialGuide(
            mode = GameMode.CUSTOM_CHAOS,
            pieceType = PieceType.DRAGON,
            rules = listOf(
                "Mix and match any chaos rules in the Custom Chaos Builder sandbox.",
                "Configure mutation rates, Dragon availability, board event frequencies, and mystery percentages."
            ),
            proTip = "Combine Dragons with Last Stand and Speed Mutation for maximum strategic mayhem."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(16.dp)
            .testTag("tutorial_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClicked, modifier = Modifier.testTag("tutorial_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WhitePieceColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "HOW TO PLAY",
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Complete Chaos Chess Rulebook",
                    color = MysticPurpleLight,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(guides) { guide ->
                TutorialCardItem(guide = guide)
            }
        }
    }
}

@Composable
private fun TutorialCardItem(guide: TutorialGuide) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VoidCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VoidBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(VoidCardLight)
                        .border(1.5.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    ChessPieceView(
                        pieceType = guide.pieceType,
                        owner = PieceColor.WHITE,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = guide.mode.title,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = guide.mode.subtitle,
                        color = MysticPurpleLight,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            guide.rules.forEach { rule ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", color = CelestialCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = rule,
                        color = WhitePieceColor.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(VoidDark)
                    .border(1.dp, VoidBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "💡 Pro Tip: ${guide.proTip}",
                    color = GoldAccent.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
