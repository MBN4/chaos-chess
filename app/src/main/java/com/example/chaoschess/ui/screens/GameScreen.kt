package com.example.chaoschess.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.ai.AIDifficulty
import com.example.chaoschess.ai.ChaosAI
import com.example.chaoschess.audio.SoundEffect
import com.example.chaoschess.audio.SoundEngine
import com.example.chaoschess.data.GameSettings
import com.example.chaoschess.data.ReplayData
import com.example.chaoschess.data.SettingsRepository
import com.example.chaoschess.engine.models.ChampionType
import com.example.chaoschess.engine.models.CustomChaosConfig
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.Move
import com.example.chaoschess.engine.models.MoveType
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.rules.ChaosEngine
import com.example.chaoschess.ui.components.ChampionSelectionModal
import com.example.chaoschess.ui.components.ChessBoardView
import com.example.chaoschess.ui.components.DragonTransformModal
import com.example.chaoschess.ui.components.GameResultDialog
import com.example.chaoschess.ui.components.GraveyardModal
import com.example.chaoschess.ui.components.MutationBannerModal
import com.example.chaoschess.ui.components.PauseMenuDialog
import com.example.chaoschess.ui.components.PawnPromotionDialog
import com.example.chaoschess.ui.components.PlayerHud
import com.example.chaoschess.ui.components.TimeWarpModal
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.theme.EventAlertAccent
import com.example.ui.theme.EventAlertBg
import com.example.ui.theme.EventAlertBorder
import com.example.ui.theme.EventAlertText
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurpleLight
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidCard
import com.example.ui.theme.VoidDark
import com.example.ui.theme.WhitePieceColor
import com.example.ui.theme.WhitePieceShadow
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GameScreen(
    mode: GameMode,
    customConfig: CustomChaosConfig? = null,
    opponentType: MatchOpponentType = MatchOpponentType.AI,
    aiDifficulty: AIDifficulty = AIDifficulty.MEDIUM,
    playerColor: PieceColor = PieceColor.WHITE,
    settings: GameSettings,
    repository: SettingsRepository,
    onNavigateBackToMenu: () -> Unit,
    onNavigateToModeSelect: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val engine = remember {
        val cfg = customConfig ?: CustomChaosConfig.forMode(mode)
        ChaosEngine(mode = mode, config = cfg)
    }
    val ai = remember { ChaosAI(aiDifficulty) }
    var stateRevision by remember { mutableStateOf(0) }
    stateRevision // Observe imperative engine changes that do not also close a modal.

    var selectedPosition by remember { mutableStateOf<Position?>(null) }
    var legalMovesForSelected by remember { mutableStateOf<List<Move>>(emptyList()) }
    var lastExecutedMove by remember { mutableStateOf<Move?>(null) }

    // Dialog & Modal States
    var showPauseDialog by remember { mutableStateOf(false) }
    var showPromotionDialogForMove by remember { mutableStateOf<Move?>(null) }
    var activeMutationBannerMove by remember { mutableStateOf<Move?>(null) }
    var showDragonTransformModal by remember { mutableStateOf(false) }
    var showTimeWarpModal by remember { mutableStateOf(false) }
    var showGraveyardModal by remember { mutableStateOf(false) }
    var pendingDragonTargetPos by remember { mutableStateOf<Position?>(null) }
    var showWhiteChampionSelect by remember { mutableStateOf(engine.config.championEnabled && engine.state.whiteChampion == null) }
    var showBlackChampionSelect by remember { mutableStateOf(false) }
    var whiteSelectedChamp by remember { mutableStateOf<ChampionType?>(null) }
    var isAIThinking by remember { mutableStateOf(false) }

    // Local Pass and Play veil state (for Mystery Piece mode)
    var showPassDeviceNotice by remember { mutableStateOf(false) }

    // Calculate material advantage
    fun calculateMaterialAdvantage(): Pair<Int, Int> {
        val whiteVal = engine.state.board.findPieces(PieceColor.WHITE).sumOf { it.currentType.baseValue }
        val blackVal = engine.state.board.findPieces(PieceColor.BLACK).sumOf { it.currentType.baseValue }
        val diff = (whiteVal - blackVal) / 100
        return if (diff > 0) Pair(diff, 0) else Pair(0, -diff)
    }

    val (whiteAdvantage, blackAdvantage) = calculateMaterialAdvantage()

    // Trigger Move Execution
    fun performMove(move: Move) {
        val isCapture = (move.capturedPiece != null || move.moveType == MoveType.EN_PASSANT)
        val isCastle = (move.moveType == MoveType.CASTLE_KINGSIDE || move.moveType == MoveType.CASTLE_QUEENSIDE)
        val isPromotion = (move.moveType == MoveType.PROMOTION)
        val executed = engine.executeMove(move)
        lastExecutedMove = executed
        selectedPosition = null
        legalMovesForSelected = emptyList()

        // Sound FX - Realistic Board Piece Acoustics
        if (executed.isRareKingMutation) {
            SoundEngine.playSound(SoundEffect.RARE_KING_MUTATION)
        } else if (executed.mutationResult != null) {
            SoundEngine.playSound(SoundEffect.MUTATION)
        } else if (engine.state.isGameOver && engine.state.matchResult?.winner != null) {
            SoundEngine.playSound(SoundEffect.CHECKMATE)
        } else if (engine.state.isCheck) {
            SoundEngine.playSound(SoundEffect.CHECK)
        } else if (isCastle) {
            SoundEngine.playSound(SoundEffect.CASTLE)
        } else if (isPromotion) {
            SoundEngine.playSound(SoundEffect.PROMOTION)
        } else if (isCapture) {
            val capturedType = move.capturedPiece?.currentType
            if (capturedType == PieceType.QUEEN || capturedType == PieceType.ROOK || capturedType == PieceType.DRAGON) {
                SoundEngine.playSound(SoundEffect.CAPTURE_HEAVY)
            } else {
                SoundEngine.playSound(SoundEffect.CAPTURE)
            }
        } else {
            SoundEngine.playPieceMove(move.piece.currentType)
        }

        // Show mutation popup if captured & mutation enabled
        if (executed.mutationResult != null && !settings.reducedMotion) {
            activeMutationBannerMove = executed
        }

        // Game over sound & auto save replay
        if (engine.state.isGameOver) {
            if (engine.state.matchResult?.winner == playerColor) {
                SoundEngine.playSound(SoundEffect.VICTORY)
            } else if (engine.state.matchResult?.winner != null) {
                SoundEngine.playSound(SoundEffect.DEFEAT)
            }

            // Save match replay
            repository.saveReplay(
                ReplayData(
                    id = "match_${System.currentTimeMillis()}",
                    title = "${engine.mode.title} vs ${if (opponentType == MatchOpponentType.AI) "AI (${aiDifficulty.label})" else "Player 2"}",
                    mode = engine.mode,
                    dateEpoch = System.currentTimeMillis(),
                    winner = engine.state.matchResult?.winner,
                    seed = engine.state.seed,
                    moveNotations = engine.state.moveHistory.map { "${it.from.algebraic}->${it.to.algebraic}" },
                    totalMoves = engine.state.moveCount
                )
            )
        } else if (opponentType == MatchOpponentType.LOCAL_PASS_AND_PLAY && engine.config.mysteryEnabled) {
            showPassDeviceNotice = true
        }
    }

    // AI Turn Handler
    LaunchedEffect(engine.state.turn, engine.state.isGameOver, showWhiteChampionSelect, showBlackChampionSelect) {
        if (!engine.state.isGameOver &&
            opponentType == MatchOpponentType.AI &&
            engine.state.turn != playerColor &&
            !showWhiteChampionSelect && !showBlackChampionSelect
        ) {
            isAIThinking = true
            delay(350) // Natural thinking delay

            // Check if AI wants to transform dragon
            val charges = if (engine.state.turn == PieceColor.WHITE) engine.state.whiteDragonCharges else engine.state.blackDragonCharges
            if (charges > 0 && engine.state.moveCount >= 3) {
                val eligible = engine.state.board.findPieces(engine.state.turn).filter { !it.isRoyalKing && !it.isDragon }
                val knightOrBishop = eligible.find { it.currentType == PieceType.KNIGHT || it.currentType == PieceType.BISHOP }
                if (knightOrBishop != null) {
                    engine.transformPieceToDragon(knightOrBishop.position)
                    SoundEngine.playSound(SoundEffect.DRAGON_ROAR)
                }
            }

            val bestMove = withContext(Dispatchers.Default) {
                ai.findBestMove(engine)
            }
            isAIThinking = false
            if (bestMove != null) {
                performMove(bestMove)
            }
        }
    }

    // Board Square Click Handler
    fun handleSquareClick(pos: Position) {
        if (engine.state.isGameOver || isAIThinking) return
        if (opponentType == MatchOpponentType.AI && engine.state.turn != playerColor) return

        val currentTurn = engine.state.turn
        val clickedPiece = engine.state.board.pieceAt(pos)

        if (selectedPosition == null) {
            // Select piece
            if (clickedPiece != null && clickedPiece.owner == currentTurn) {
                selectedPosition = pos
                legalMovesForSelected = engine.getLegalMoves().filter { it.from == pos }
                SoundEngine.playSound(SoundEffect.PIECE_SELECT)
            }
        } else {
            val fromPos = selectedPosition!!
            if (pos == fromPos) {
                // Deselect
                selectedPosition = null
                legalMovesForSelected = emptyList()
                SoundEngine.playSound(SoundEffect.PIECE_DESELECT)
                return
            }

            // Check if user clicked another own piece to switch selection
            if (clickedPiece != null && clickedPiece.owner == currentTurn) {
                selectedPosition = pos
                legalMovesForSelected = engine.getLegalMoves().filter { it.from == pos }
                SoundEngine.playSound(SoundEffect.PIECE_SELECT)
                return
            }

            // Check if move is legal
            val matchingMove = legalMovesForSelected.find { it.to == pos }
            if (matchingMove != null) {
                if (matchingMove.moveType == MoveType.PROMOTION && matchingMove.promotionType == PieceType.QUEEN) {
                    // Open promotion dialog
                    showPromotionDialogForMove = matchingMove
                } else {
                    performMove(matchingMove)
                }
            } else {
                selectedPosition = null
                legalMovesForSelected = emptyList()
            }
        }
    }

    // Main Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(12.dp)
            .testTag("game_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Game Mode & Status Pill & Pause Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = engine.mode.title.uppercase(),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                            .background(GoldPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (engine.state.activeSpeedRules.isNotEmpty()) {
                                val active = engine.state.activeSpeedRules.first()
                                "${active.type.title} (${active.turnsRemaining}t)"
                            } else {
                                if (mode == GameMode.CLASSIC) "Standard game" else mode.subtitle
                            },
                            color = WhitePieceColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Turn / Move Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VoidCard)
                            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MOVE",
                                color = WhitePieceShadow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "#${engine.state.moveCount}",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showPauseDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VoidCard)
                            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                            .testTag("game_pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = WhitePieceColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Opponent Player HUD (Top)
            val topColor = if (playerColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            val topName = if (opponentType == MatchOpponentType.AI) "AI (${aiDifficulty.label})" else "Player 2"
            PlayerHud(
                color = topColor,
                playerName = topName,
                isCurrentTurn = (engine.state.turn == topColor),
                capturedPieces = if (topColor == PieceColor.WHITE) engine.state.capturedBlack else engine.state.capturedWhite,
                materialAdvantage = if (topColor == PieceColor.WHITE) whiteAdvantage else blackAdvantage,
                dragonCharges = if (topColor == PieceColor.WHITE) engine.state.whiteDragonCharges else engine.state.blackDragonCharges,
                championState = if (topColor == PieceColor.WHITE) engine.state.whiteChampion else engine.state.blackChampion,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Apocalypse Cataclysm Progress Bar (if Apocalypse mode)
            if (engine.config.apocalypseEnabled) {
                val stage = engine.state.apocalypseState.stage
                val meter = engine.state.apocalypseState.meter
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, DragonCrimson.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .background(VoidCard)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌋 APOCALYPSE: ${stage.title.uppercase()}",
                            color = DragonCrimson,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Collapse Meter: $meter%",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Special Event Banner (floating)
            if (engine.state.lastSpecialEventBanner != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, EventAlertBorder, RoundedCornerShape(12.dp))
                        .background(EventAlertBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "EVENT: ",
                            color = EventAlertAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = engine.state.lastSpecialEventBanner ?: "",
                            color = EventAlertText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Chess Board
            ChessBoardView(
                board = engine.state.board,
                selectedPosition = selectedPosition,
                legalMovesForSelected = if (settings.showLegalMoves) legalMovesForSelected else emptyList(),
                lastMove = lastExecutedMove,
                isCheck = engine.state.isCheck,
                turn = engine.state.turn,
                perspective = playerColor,
                showCoordinates = settings.showCoordinates,
                highContrast = settings.highContrastMode,
                viewerColor = if (opponentType == MatchOpponentType.LOCAL_PASS_AND_PLAY) engine.state.turn else playerColor,
                portals = engine.state.portals,
                collapsedSquares = engine.state.apocalypseState.collapsedSquares,
                onSquareClicked = { pos ->
                    if (pendingDragonTargetPos != null) {
                        // Transform this piece
                        val success = engine.transformPieceToDragon(pos)
                        if (success) {
                            SoundEngine.playSound(SoundEffect.DRAGON_ROAR)
                        }
                        pendingDragonTargetPos = null
                    } else {
                        handleSquareClick(pos)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Local Player HUD (Bottom)
            val bottomColor = playerColor
            val bottomName = if (opponentType == MatchOpponentType.AI) "You" else "Player 1"
            PlayerHud(
                color = bottomColor,
                playerName = bottomName,
                isCurrentTurn = (engine.state.turn == bottomColor),
                capturedPieces = if (bottomColor == PieceColor.WHITE) engine.state.capturedBlack else engine.state.capturedWhite,
                materialAdvantage = if (bottomColor == PieceColor.WHITE) whiteAdvantage else blackAdvantage,
                dragonCharges = if (bottomColor == PieceColor.WHITE) engine.state.whiteDragonCharges else engine.state.blackDragonCharges,
                championState = if (bottomColor == PieceColor.WHITE) engine.state.whiteChampion else engine.state.blackChampion,
                onDragonChargeClicked = {
                    showDragonTransformModal = true
                },
                onChampionAbilityClicked = {
                    val champ = if (bottomColor == PieceColor.WHITE) engine.state.whiteChampion else engine.state.blackChampion
                    if (champ != null) {
                        val piece = engine.state.board.pieces.values.find { it.id == champ.pieceId }
                        if (piece != null) {
                            val success = engine.useChampionAbility(piece.position)
                            if (success) {
                                stateRevision++
                                SoundEngine.playSound(SoundEffect.CHAMPION_ABILITY)
                            }
                        }
                    }
                },
                modifier = Modifier.padding(top = 4.dp)
            )

            // Action / Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VoidCard)
                            .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isAIThinking) "AI Thinking..." else if (engine.state.turn == playerColor) "Your Move" else "${engine.state.turn.name}'s Move",
                            color = if (engine.state.turn == playerColor) GoldPrimary else MysticPurpleLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (engine.config.undeadEnabled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { showGraveyardModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VoidCard, contentColor = MysticPurpleLight),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.border(1.dp, VoidBorder, RoundedCornerShape(10.dp)).height(36.dp)
                        ) {
                            Text("⚰️ ${engine.state.graveyard.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (engine.config.timeWarpEnabled) {
                        val charges = if (playerColor == PieceColor.WHITE) engine.state.whiteTimeCharges else engine.state.blackTimeCharges
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { showTimeWarpModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VoidCard, contentColor = CelestialCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.border(1.dp, VoidBorder, RoundedCornerShape(10.dp)).height(36.dp)
                        ) {
                            Text("⏳ $charges", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        engine.resign(playerColor)
                        stateRevision++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoidCard,
                        contentColor = DragonCrimson
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .border(1.dp, VoidBorder, RoundedCornerShape(12.dp))
                        .testTag("resign_button")
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESIGN", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }

        // Modals & Dialogs

        // 1. Champion Selection (White)
        if (showWhiteChampionSelect) {
            ChampionSelectionModal(
                color = PieceColor.WHITE,
                onChampionSelected = { champ ->
                    whiteSelectedChamp = champ
                    showWhiteChampionSelect = false
                    if (opponentType == MatchOpponentType.AI) {
                        // AI selects knight or queen champion
                        engine.setChampions(champ, ChampionType.KNIGHT)
                    } else {
                        showBlackChampionSelect = true
                    }
                }
            )
        }

        // 2. Champion Selection (Black for local 2P)
        if (showBlackChampionSelect) {
            ChampionSelectionModal(
                color = PieceColor.BLACK,
                onChampionSelected = { blackChamp ->
                    showBlackChampionSelect = false
                    if (whiteSelectedChamp != null) {
                        engine.setChampions(whiteSelectedChamp!!, blackChamp)
                    }
                }
            )
        }

        // 3. Mutation Banner Modal
        if (activeMutationBannerMove != null) {
            MutationBannerModal(
                move = activeMutationBannerMove!!,
                onDismiss = { activeMutationBannerMove = null }
            )
        }

        // 4. Dragon Transform Modal
        if (showDragonTransformModal) {
            DragonTransformModal(
                onConfirm = {
                    showDragonTransformModal = false
                    // Next click on a piece transforms it
                    pendingDragonTargetPos = Position(0, 0)
                },
                onCancel = { showDragonTransformModal = false }
            )
        }

        // Time Warp Modal
        if (showTimeWarpModal) {
            val charges = if (playerColor == PieceColor.WHITE) engine.state.whiteTimeCharges else engine.state.blackTimeCharges
            TimeWarpModal(
                charges = charges,
                onActionSelected = { action ->
                    showTimeWarpModal = false
                    val success = engine.executeTimeWarp(action)
                    if (success) {
                        SoundEngine.playSound(SoundEffect.BOARD_EVENT_WARNING)
                        selectedPosition = null
                        legalMovesForSelected = emptyList()
                    }
                },
                onDismiss = { showTimeWarpModal = false }
            )
        }

        // Graveyard Modal
        if (showGraveyardModal) {
            GraveyardModal(
                graveyard = engine.state.graveyard,
                onDismiss = { showGraveyardModal = false }
            )
        }

        // 5. Pawn Promotion Dialog
        if (showPromotionDialogForMove != null) {
            PawnPromotionDialog(
                color = engine.state.turn,
                onPieceChosen = { promoType ->
                    val move = showPromotionDialogForMove!!.copy(promotionType = promoType)
                    showPromotionDialogForMove = null
                    performMove(move)
                }
            )
        }

        // 6. Pause Dialog
        if (showPauseDialog) {
            PauseMenuDialog(
                onResume = { showPauseDialog = false },
                onRestart = {
                    showPauseDialog = false
                    engine.reset()
                    selectedPosition = null
                    legalMovesForSelected = emptyList()
                    lastExecutedMove = null
                },
                onHowToPlay = {
                    showPauseDialog = false
                    onNavigateToTutorial()
                },
                onSettings = {
                    showPauseDialog = false
                    onNavigateToSettings()
                },
                onQuit = {
                    showPauseDialog = false
                    onNavigateBackToMenu()
                }
            )
        }

        // 7. Match Over Result Dialog
        if (engine.state.isGameOver && engine.state.matchResult != null) {
            GameResultDialog(
                result = engine.state.matchResult!!,
                moveCount = engine.state.moveCount,
                onRematch = {
                    engine.reset()
                    selectedPosition = null
                    legalMovesForSelected = emptyList()
                    lastExecutedMove = null
                },
                onModeSelect = onNavigateToModeSelect,
                onMainMenu = onNavigateBackToMenu
            )
        }
    }
}
