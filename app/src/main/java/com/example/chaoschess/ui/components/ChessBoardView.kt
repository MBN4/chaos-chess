package com.example.chaoschess.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.engine.models.BoardHazard
import com.example.chaoschess.engine.models.BoardHazardType
import com.example.chaoschess.engine.models.Move
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PortalPair
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.rules.ChessBoard
import com.example.ui.theme.BurningHighlight
import com.example.ui.theme.CaptureHighlightRing
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.CheckWarningRed
import com.example.ui.theme.ChessComGreenDark
import com.example.ui.theme.ChessComGreenLight
import com.example.ui.theme.ChessWoodDark
import com.example.ui.theme.ChessWoodLight
import com.example.ui.theme.ClassicDarkSquare
import com.example.ui.theme.ClassicLightSquare
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonFire
import com.example.ui.theme.EmeraldArcane
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LastMoveHighlight
import com.example.ui.theme.MeteorHighlight
import com.example.ui.theme.MoveHighlightDot
import com.example.ui.theme.MysticDarkSquare
import com.example.ui.theme.MysticLightSquare
import com.example.ui.theme.MysticPurple
import com.example.ui.theme.SquareSelected
import com.example.ui.theme.TeleportHighlight
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidDark

@Composable
fun ChessBoardView(
    board: ChessBoard,
    selectedPosition: Position?,
    legalMovesForSelected: List<Move>,
    lastMove: Move?,
    isCheck: Boolean,
    turn: PieceColor,
    perspective: PieceColor = PieceColor.WHITE,
    showCoordinates: Boolean = true,
    highContrast: Boolean = false,
    viewerColor: PieceColor? = null,
    portals: List<PortalPair> = emptyList(),
    collapsedSquares: Set<Position> = emptySet(),
    onSquareClicked: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val lightSquareColor = if (highContrast) ChessWoodLight else ChessComGreenLight
    val darkSquareColor = if (highContrast) ChessWoodDark else ChessComGreenDark

    val royalKingInCheckPos = if (isCheck) board.findRoyalKing(turn)?.position else null

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF262421))
            .border(3.dp, Color(0xFF383531), RoundedCornerShape(12.dp))
            .testTag("chess_board_view")
    ) {
        val squareSize = maxWidth / 8

        Column(modifier = Modifier.fillMaxSize()) {
            val rankRange = if (perspective == PieceColor.WHITE) (7 downTo 0) else (0..7)
            val fileRange = if (perspective == PieceColor.WHITE) (0..7) else (7 downTo 0)

            for (r in rankRange) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    for (f in fileRange) {
                        val pos = Position(f, r)
                        val piece = board.pieceAt(pos)
                        val isLight = (f + r) % 2 != 0
                        val isCollapsed = collapsedSquares.contains(pos)
                        val baseColor = if (isCollapsed) VoidDark else if (isLight) lightSquareColor else darkSquareColor

                        val isSelected = (pos == selectedPosition)
                        val isLastMoveSrc = (pos == lastMove?.from)
                        val isLastMoveDst = (pos == lastMove?.to)
                        val isKingInCheckSquare = (pos == royalKingInCheckPos)

                        val legalMoveToSquare = legalMovesForSelected.find { it.to == pos }
                        val isLegalDest = legalMoveToSquare != null
                        val isCaptureDest = isLegalDest && (piece != null || legalMoveToSquare?.capturedPiece != null)

                        // Hazards on this square
                        val hazard = board.hazards.find { it.primaryPosition == pos }
                        val portalOnSquare = portals.find { it.gateA == pos || it.gateB == pos }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(baseColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = GoldPrimary)
                                ) {
                                    if (!isCollapsed) {
                                        onSquareClicked(pos)
                                    }
                                }
                                .testTag("square_${pos.algebraic}"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Collapsed square void styling
                            if (isCollapsed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(VoidDark)
                                        .border(1.dp, DragonCrimson.copy(alpha = 0.3f))
                                )
                                Text(
                                    text = "VOID",
                                    color = DragonCrimson.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            // Portal Gate Rendering
                            if (portalOnSquare != null && !isCollapsed) {
                                val isGateA = portalOnSquare.gateA == pos
                                val portalColor = if (portalOnSquare.colorIndex == 0) CelestialCyan else MysticPurple
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(portalColor.copy(alpha = 0.25f))
                                        .border(1.5.dp, portalColor.copy(alpha = pulseAlpha), RoundedCornerShape(8.dp))
                                )
                                Text(
                                    text = if (isGateA) "🌀A" else "🌀B",
                                    color = portalColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.BottomStart).padding(2.dp)
                                )
                            }

                            // Last Move Highlight
                            if (isLastMoveSrc || isLastMoveDst) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(LastMoveHighlight)
                                )
                            }

                            // Selection Highlight
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(SquareSelected)
                                        .border(2.5.dp, GoldPrimary)
                                )
                            }

                            // Check Warning Highlight
                            if (isKingInCheckSquare) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(CheckWarningRed.copy(alpha = pulseAlpha * 0.7f))
                                        .border(3.dp, CheckWarningRed)
                                )
                            }

                            // Hazard Overlays
                            if (hazard != null) {
                                when (hazard.type) {
                                    BoardHazardType.BURNING_SQUARE -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(BurningHighlight.copy(alpha = if (hazard.countdownTurns == 0) 0.6f else 0.3f))
                                        )
                                        Text(
                                            text = if (hazard.countdownTurns > 0) "🔥${hazard.countdownTurns}" else "🔥",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.BottomStart)
                                        )
                                    }
                                    BoardHazardType.METEOR_WARNING -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MeteorHighlight.copy(alpha = pulseAlpha * 0.5f))
                                                .border(2.dp, MeteorHighlight)
                                        )
                                        Text(
                                            text = "☄${hazard.countdownTurns}",
                                            color = MeteorHighlight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                    BoardHazardType.TELEPORT_PORTAL -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(TeleportHighlight.copy(alpha = 0.35f))
                                                .border(2.dp, CelestialCyan)
                                        )
                                        Text(
                                            text = "🌀",
                                            fontSize = 13.sp,
                                            modifier = Modifier.align(Alignment.BottomStart)
                                        )
                                    }
                                    BoardHazardType.COLLAPSED_SQUARE -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(VoidDark.copy(alpha = 0.9f))
                                        )
                                        Text(
                                            text = "✖",
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }

                            // Piece
                            if (piece != null && !isCollapsed) {
                                val visibleType = piece.getVisibleType(viewerColor)
                                ChessPieceView(
                                    pieceType = visibleType,
                                    owner = piece.owner,
                                    isSelected = isSelected,
                                    modifier = Modifier.fillMaxSize(0.92f),
                                    isChampion = piece.isChampion,
                                    isMystery = piece.isMystery && (viewerColor == null || viewerColor == piece.owner),
                                    isFrozen = piece.isFrozen,
                                    isDragon = piece.isDragon,
                                    element = piece.element,
                                    isClone = piece.isClone,
                                    hasResurrected = piece.hasResurrected
                                )
                            }

                            // Legal Move Indicators (Chess.com Style Minimalist and Smooth)
                            if (isLegalDest && !isCollapsed) {
                                if (isCaptureDest) {
                                    // Clean outer capture reticle ring
                                    Canvas(modifier = Modifier.fillMaxSize(0.88f)) {
                                        val strokeW = size.minDimension * 0.085f
                                        drawCircle(
                                            color = Color(0x55000000),
                                            radius = (size.minDimension - strokeW) / 2f,
                                            style = Stroke(width = strokeW)
                                        )
                                    }
                                } else {
                                    // Minimal, elegant destination circle
                                    Canvas(modifier = Modifier.size(squareSize * 0.32f)) {
                                        drawCircle(
                                            color = Color(0x38000000),
                                            radius = size.minDimension / 2f
                                        )
                                    }
                                }
                            }

                            // Coordinate Labels (on outer edges of the board)
                            if (showCoordinates) {
                                if (f == (if (perspective == PieceColor.WHITE) 0 else 7)) {
                                    Text(
                                        text = "${r + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isLight) darkSquareColor.copy(alpha = 0.8f) else lightSquareColor.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(start = 3.dp, top = 2.dp)
                                    )
                                }
                                if (r == (if (perspective == PieceColor.WHITE) 0 else 7)) {
                                    Text(
                                        text = "${('a' + f)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isLight) darkSquareColor.copy(alpha = 0.8f) else lightSquareColor.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(end = 3.dp, bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
