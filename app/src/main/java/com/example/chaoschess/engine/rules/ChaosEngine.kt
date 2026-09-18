package com.example.chaoschess.engine.rules

import com.example.chaoschess.engine.models.ActiveSpeedRule
import com.example.chaoschess.engine.models.ApocalypseStage
import com.example.chaoschess.engine.models.ApocalypseState
import com.example.chaoschess.engine.models.BoardHazard
import com.example.chaoschess.engine.models.BoardHazardType
import com.example.chaoschess.engine.models.ChampionState
import com.example.chaoschess.engine.models.ChampionType
import com.example.chaoschess.engine.models.CustomChaosConfig
import com.example.chaoschess.engine.models.Element
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.GraveyardPiece
import com.example.chaoschess.engine.models.Move
import com.example.chaoschess.engine.models.MoveType
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.PortalPair
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.models.SpeedRuleType
import com.example.chaoschess.engine.models.TimeWarpAction
import com.example.chaoschess.engine.random.ChaosRandom

enum class MatchResultType {
    CHECKMATE,
    KING_CAPTURED,
    CHAMPION_FALLEN,
    RESIGNATION,
    TIMEOUT,
    STALEMATE,
    THREEFOLD_REPETITION,
    FIFTY_MOVE_RULE,
    INSUFFICIENT_MATERIAL
}

data class MatchResult(
    val winner: PieceColor?, // null for draw
    val resultType: MatchResultType,
    val title: String,
    val description: String
)

data class GameState(
    val mode: GameMode,
    val config: CustomChaosConfig,
    val board: ChessBoard,
    val turn: PieceColor = PieceColor.WHITE,
    val moveCount: Int = 1,
    val halfmoveClock: Int = 0,
    val moveHistory: List<Move> = emptyList(),
    val fenHistory: List<String> = emptyList(),
    val capturedWhite: List<Piece> = emptyList(),
    val capturedBlack: List<Piece> = emptyList(),
    val whiteDragonCharges: Int = 0,
    val blackDragonCharges: Int = 0,
    val whiteChampion: ChampionState? = null,
    val blackChampion: ChampionState? = null,
    val whiteTimeCharges: Int = 0,
    val blackTimeCharges: Int = 0,
    val whiteClonesCount: Int = 0,
    val blackClonesCount: Int = 0,
    val graveyard: List<GraveyardPiece> = emptyList(),
    val portals: List<PortalPair> = emptyList(),
    val apocalypseState: ApocalypseState = ApocalypseState(),
    val activeSpeedRules: List<ActiveSpeedRule> = emptyList(),
    val isCheck: Boolean = false,
    val isGameOver: Boolean = false,
    val matchResult: MatchResult? = null,
    val pendingMutationMove: Move? = null,
    val lastSpecialEventBanner: String? = null,
    val seed: Long = 0L
)

class ChaosEngine(
    val mode: GameMode = GameMode.CLASSIC,
    val config: CustomChaosConfig = CustomChaosConfig.forMode(mode),
    val seed: Long = System.currentTimeMillis()
) {
    val randomEngine: ChaosRandom = ChaosRandom(seed)
    var state: GameState = createInitialState()
        private set

    fun reset(newSeed: Long = System.currentTimeMillis()) {
        randomEngine.reseed(newSeed)
        state = createInitialState(newSeed)
    }

    fun setBoardForTesting(newBoard: ChessBoard) {
        state = state.copy(board = newBoard)
    }

    private fun createInitialState(customSeed: Long = seed): GameState {
        var board = ChessBoard.initialBoard()

        // Apply mystery piece disguise if mystery mode enabled
        if (config.mysteryEnabled) {
            val updatedPieces = board.pieces.toMutableMap()
            val possibleDisguises = listOf(PieceType.PAWN, PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK)
            for ((pos, piece) in board.pieces) {
                if (!piece.isRoyalKing && randomEngine.nextDouble() < config.mysteryChance) {
                    val disguise = possibleDisguises[randomEngine.nextInt(possibleDisguises.size)]
                    updatedPieces[pos] = piece.copy(
                        isMystery = true,
                        baseType = disguise,
                        hiddenType = piece.currentType
                    )
                }
            }
            board = board.copy(pieces = updatedPieces)
        }

        // Apply Elemental affinities if Elemental mode enabled
        if (config.elementalEnabled) {
            val elements = listOf(Element.FIRE, Element.ICE, Element.LIGHTNING, Element.NATURE)
            val updatedPieces = board.pieces.toMutableMap()
            var index = 0
            for ((pos, piece) in board.pieces) {
                if (!piece.isRoyalKing) {
                    val elem = elements[index % elements.size]
                    index++
                    updatedPieces[pos] = piece.copy(element = elem)
                }
            }
            board = board.copy(pieces = updatedPieces)
        }

        // Generate portals if Portals mode enabled
        val portalsList = if (config.portalsEnabled) {
            listOf(
                PortalPair("portal_1", Position(1, 2), Position(6, 5), 0),
                PortalPair("portal_2", Position(2, 5), Position(5, 2), 1)
            )
        } else emptyList()

        val initialDragonCharges = if (config.dragonEnabled) config.dragonChargesPerPlayer else 0
        val initialTimeCharges = if (config.timeWarpEnabled) config.timeChargesPerPlayer else 0

        val initialFen = board.toFenString(PieceColor.WHITE, "KQkq", 0, 1)

        return GameState(
            mode = mode,
            config = config,
            board = board,
            turn = PieceColor.WHITE,
            whiteDragonCharges = initialDragonCharges,
            blackDragonCharges = initialDragonCharges,
            whiteTimeCharges = initialTimeCharges,
            blackTimeCharges = initialTimeCharges,
            portals = portalsList,
            fenHistory = listOf(initialFen),
            seed = customSeed
        )
    }

    fun setChampions(whiteChampType: ChampionType, blackChampType: ChampionType) {
        val whiteChampPiece = state.board.findPieces(PieceColor.WHITE).find { it.currentType == whiteChampType.pieceType && !it.isRoyalKing }
        val blackChampPiece = state.board.findPieces(PieceColor.BLACK).find { it.currentType == blackChampType.pieceType && !it.isRoyalKing }

        var updatedBoard = state.board
        var whiteState: ChampionState? = null
        var blackState: ChampionState? = null

        if (whiteChampPiece != null) {
            val marked = whiteChampPiece.copy(isChampion = true)
            updatedBoard = updatedBoard.withPiecePlaced(marked)
            whiteState = ChampionState(PieceColor.WHITE, whiteChampType, marked.id)
        }

        if (blackChampPiece != null) {
            val marked = blackChampPiece.copy(isChampion = true)
            updatedBoard = updatedBoard.withPiecePlaced(marked)
            blackState = ChampionState(PieceColor.BLACK, blackChampType, marked.id)
        }

        state = state.copy(
            board = updatedBoard,
            whiteChampion = whiteState,
            blackChampion = blackState
        )
    }

    /**
     * Get all legal moves for current turn player.
     */
    fun getLegalMoves(): List<Move> {
        if (state.isGameOver) return emptyList()
        return MoveGenerator.generateLegalMoves(
            board = state.board,
            color = state.turn,
            isKingHuntMode = config.kingHuntEnabled,
            activeSpeedRules = state.activeSpeedRules
        )
    }

    /**
     * Executes a move on the board and resolves all active chaos mechanics.
     */
    fun executeMove(move: Move): Move {
        if (state.isGameOver) return move

        var workingBoard = state.board
        var actualMove = move
        val currentTurn = state.turn
        val movingPiece = state.board.pieceAt(move.from) ?: move.piece
        val captured = state.board.pieceAt(move.to) ?: move.capturedPiece

        // Reveal mystery piece if it makes a move not possible for its disguise or captures
        var pieceToMove = movingPiece
        if (pieceToMove.isMystery) {
            pieceToMove = pieceToMove.revealMystery()
            workingBoard = workingBoard.withPiecePlaced(pieceToMove)
        }

        // Apply move on board
        workingBoard = MoveGenerator.applyMoveToBoard(workingBoard, actualMove.copy(piece = pieceToMove))

        // Check portal warp
        if (config.portalsEnabled) {
            for (portal in state.portals) {
                if (actualMove.to == portal.gateA && workingBoard.pieceAt(portal.gateB) == null) {
                    workingBoard = workingBoard.withPieceMoved(portal.gateA, portal.gateB)
                    actualMove = actualMove.copy(moveType = MoveType.PORTAL_WARP, triggeredEventDescription = "PORTAL WARP: Entered Gate A, emerged at Gate B (${portal.gateB.algebraic.uppercase()})!")
                } else if (actualMove.to == portal.gateB && workingBoard.pieceAt(portal.gateA) == null) {
                    workingBoard = workingBoard.withPieceMoved(portal.gateB, portal.gateA)
                    actualMove = actualMove.copy(moveType = MoveType.PORTAL_WARP, triggeredEventDescription = "PORTAL WARP: Entered Gate B, emerged at Gate A (${portal.gateA.algebraic.uppercase()})!")
                }
            }
        }

        // Check if captured piece was a Champion (Last Stand victory condition)
        if (config.championEnabled && captured != null && captured.isChampion) {
            val champState = if (captured.owner == PieceColor.WHITE) state.whiteChampion else state.blackChampion
            if (champState?.championType == ChampionType.QUEEN && champState.resurrectionAvailable) {
                // Queen Champion Phoenix Rebirth!
                val homeRank = if (captured.owner == PieceColor.WHITE) 0 else 7
                val emptySquare = (0..7).map { Position(it, homeRank) }.firstOrNull { workingBoard.pieceAt(it) == null }
                if (emptySquare != null) {
                    val rebornQueen = captured.copy(
                        position = emptySquare,
                        hasResurrected = true
                    )
                    workingBoard = workingBoard.withPiecePlaced(rebornQueen)
                    val updatedChampState = champState.copy(resurrectionAvailable = false)
                    state = if (captured.owner == PieceColor.WHITE) state.copy(whiteChampion = updatedChampState) else state.copy(blackChampion = updatedChampState)
                } else {
                    // Cannot revive -> Defeat
                    return handleChampionFallen(captured.owner, actualMove, workingBoard)
                }
            } else {
                return handleChampionFallen(captured.owner, actualMove, workingBoard)
            }
        }

        // Check if King was captured in King Hunt mode
        if (config.kingHuntEnabled && captured != null && captured.isRoyalKing) {
            val winner = currentTurn
            val result = MatchResult(
                winner = winner,
                resultType = MatchResultType.KING_CAPTURED,
                title = "KING CAPTURED!",
                description = "${winner.name} captured the enemy Royal King!"
            )
            state = state.copy(
                board = workingBoard,
                isGameOver = true,
                matchResult = result,
                moveHistory = state.moveHistory + actualMove
            )
            return actualMove
        }

        // Handle Undead Chess (non-Kings enter graveyard)
        var updatedGraveyard = state.graveyard
        if (config.undeadEnabled && captured != null && !captured.isRoyalKing) {
            updatedGraveyard = updatedGraveyard + GraveyardPiece(
                piece = captured,
                turnsUntilRevive = config.undeadResurrectTurns,
                originalOwner = captured.owner
            )
        }

        // Handle Elemental Chess capture advantage
        if (config.elementalEnabled && captured != null && pieceToMove.element != Element.NONE && captured.element != Element.NONE) {
            if (pieceToMove.element.hasAdvantageOver(captured.element)) {
                val buffedPiece = workingBoard.pieceAt(actualMove.to)?.copy(elementalBuffTurns = 2)
                if (buffedPiece != null) {
                    workingBoard = workingBoard.withPiecePlaced(buffedPiece)
                }
                actualMove = actualMove.copy(
                    triggeredEventDescription = "🔥 ELEMENTAL ADVANTAGE: ${pieceToMove.element.title} crushed ${captured.element.title}!"
                )
            }
        }

        // Handle Clone Wars duplication
        var newWhiteClones = state.whiteClonesCount
        var newBlackClones = state.blackClonesCount
        if (config.cloneWarsEnabled && captured != null && !pieceToMove.isRoyalKing) {
            val currentClones = if (currentTurn == PieceColor.WHITE) state.whiteClonesCount else state.blackClonesCount
            if (currentClones < config.maxClonesPerSide && randomEngine.nextDouble() < config.cloneChance) {
                val emptyAdj = listOf(
                    actualMove.to.offset(1, 0),
                    actualMove.to.offset(-1, 0),
                    actualMove.to.offset(0, 1),
                    actualMove.to.offset(0, -1)
                ).filterNotNull().firstOrNull { workingBoard.pieceAt(it) == null }

                if (emptyAdj != null) {
                    val clonedPiece = pieceToMove.copy(
                        id = "${pieceToMove.id}_clone_${System.currentTimeMillis()}",
                        position = emptyAdj,
                        isClone = true
                    )
                    workingBoard = workingBoard.withPiecePlaced(clonedPiece)
                    if (currentTurn == PieceColor.WHITE) newWhiteClones++ else newBlackClones++
                    actualMove = actualMove.copy(
                        cloneSpawnedAt = emptyAdj,
                        triggeredEventDescription = "🧬 CLONE WAR: ${pieceToMove.currentType.name} cloned at ${emptyAdj.algebraic.uppercase()}!"
                    )
                }
            }
        }

        // Handle Mutation Chess mechanic (triggers on every capture)
        var mutationType: PieceType? = null
        var isUltraRareKing = false
        if (config.mutationEnabled && captured != null) {
            val randomVal = randomEngine.nextPercent()
            val (mutatedType, isKing) = config.mutationProbabilities.selectPieceType(randomVal)
            mutationType = mutatedType
            isUltraRareKing = isKing

            val targetPiece = workingBoard.pieceAt(actualMove.to)
            if (targetPiece != null) {
                val mutatedPiece = targetPiece.mutateTo(mutatedType)
                workingBoard = workingBoard.withPiecePlaced(mutatedPiece)
            }

            actualMove = actualMove.copy(
                mutationResult = mutationType,
                isRareKingMutation = isUltraRareKing,
                triggeredEventDescription = if (isUltraRareKing) "★ 0.001% ULTRA RARE KING MUTATION! ★" else "${pieceToMove.currentType.name} mutated into ${mutationType.name}!"
            )
        }

        // Track captured pieces
        val newCapturedWhite = if (captured != null && captured.owner == PieceColor.WHITE) state.capturedWhite + captured else state.capturedWhite
        val newCapturedBlack = if (captured != null && captured.owner == PieceColor.BLACK) state.capturedBlack + captured else state.capturedBlack

        // Update halfmove clock for 50-move rule
        val newHalfmove = if (movingPiece.currentType == PieceType.PAWN || captured != null) 0 else state.halfmoveClock + 1

        // Decrement frozen turns
        val unfreezingPieces = workingBoard.pieces.mapValues { (_, p) ->
            if (p.isFrozen) {
                val remaining = p.freezeTurnsRemaining - 1
                if (remaining <= 0) p.copy(isFrozen = false, freezeTurnsRemaining = 0) else p.copy(freezeTurnsRemaining = remaining)
            } else p
        }
        workingBoard = workingBoard.copy(pieces = unfreezingPieces)

        // Process Undead Graveyard resurrection
        val (finalGraveyard, resurrectedBoard, undeadBanner) = processUndeadGraveyard(updatedGraveyard, workingBoard)
        workingBoard = resurrectedBoard

        // Process active speed rules & board hazards countdown
        val updatedSpeedRules = processSpeedRules(state.activeSpeedRules)
        var (updatedHazards, hazardEventDesc) = processBoardHazards(workingBoard.hazards, workingBoard)

        // Resolve hazards on board (meteor, burning, collapse)
        workingBoard = applyBoardHazards(workingBoard, updatedHazards)

        // Process Apocalypse Meter escalation
        val (newApocalypseState, apocBoard, apocBanner) = processApocalypse(state.apocalypseState, workingBoard)
        workingBoard = apocBoard

        // Check if interval triggers new speed rules or board hazards
        val nextTurn = currentTurn.opposite()
        val nextMoveCount = if (currentTurn == PieceColor.BLACK) state.moveCount + 1 else state.moveCount

        var newSpeedRules = updatedSpeedRules
        var specialBanner = actualMove.triggeredEventDescription ?: hazardEventDesc ?: undeadBanner ?: apocBanner

        if (config.speedRulesEnabled && nextMoveCount > 1 && currentTurn == PieceColor.BLACK && (nextMoveCount % config.speedRuleIntervalTurns == 0)) {
            val triggeredRule = rollNewSpeedRule()
            newSpeedRules = newSpeedRules + triggeredRule
            specialBanner = "CHAOS RULE ACTIVATED: ${triggeredRule.type.title.uppercase()}!"
        }

        if (config.boardEventsEnabled && nextMoveCount > 1 && currentTurn == PieceColor.BLACK && (nextMoveCount % config.boardEventIntervalTurns == 0)) {
            val (newHazardsList, eventName) = rollNewBoardHazard(workingBoard)
            updatedHazards = updatedHazards + newHazardsList
            specialBanner = "BOARD EVENT WARNING: $eventName!"
        }

        workingBoard = workingBoard.withHazards(updatedHazards)

        // Compute check status for the next player
        val isNextKingInCheck = if (config.kingHuntEnabled) false else MoveGenerator.isKingInCheck(workingBoard, nextTurn, newSpeedRules)

        // Compute FEN for repetition checking
        val castlingStr = computeCastlingRights(workingBoard)
        val currentFen = workingBoard.toFenString(nextTurn, castlingStr, newHalfmove, nextMoveCount)
        val newFenHistory = state.fenHistory + currentFen

        // Check win / draw conditions
        val nextLegalMoves = if (config.kingHuntEnabled) {
            MoveGenerator.generateLegalMoves(workingBoard, nextTurn, isKingHuntMode = true, activeSpeedRules = newSpeedRules)
        } else {
            MoveGenerator.generateLegalMoves(workingBoard, nextTurn, isKingHuntMode = false, activeSpeedRules = newSpeedRules)
        }

        var isOver = false
        var matchRes: MatchResult? = null

        if (nextLegalMoves.isEmpty()) {
            isOver = true
            matchRes = if (isNextKingInCheck) {
                MatchResult(
                    winner = currentTurn,
                    resultType = MatchResultType.CHECKMATE,
                    title = "CHECKMATE!",
                    description = "${currentTurn.name} wins by checkmate!"
                )
            } else {
                MatchResult(
                    winner = null,
                    resultType = MatchResultType.STALEMATE,
                    title = "STALEMATE",
                    description = "No legal moves remaining. The match is a draw."
                )
            }
        } else if (newHalfmove >= 100) {
            isOver = true
            matchRes = MatchResult(
                winner = null,
                resultType = MatchResultType.FIFTY_MOVE_RULE,
                title = "DRAW",
                description = "Fifty-move rule reached without pawn push or capture."
            )
        } else if (isThreefoldRepetition(newFenHistory)) {
            isOver = true
            matchRes = MatchResult(
                winner = null,
                resultType = MatchResultType.THREEFOLD_REPETITION,
                title = "DRAW",
                description = "Threefold repetition of the same board position."
            )
        } else if (isInsufficientMaterial(workingBoard)) {
            isOver = true
            matchRes = MatchResult(
                winner = null,
                resultType = MatchResultType.INSUFFICIENT_MATERIAL,
                title = "DRAW",
                description = "Insufficient material on board to force checkmate."
            )
        }

        state = state.copy(
            board = workingBoard,
            turn = nextTurn,
            moveCount = nextMoveCount,
            halfmoveClock = newHalfmove,
            moveHistory = state.moveHistory + actualMove,
            fenHistory = newFenHistory,
            capturedWhite = newCapturedWhite,
            capturedBlack = newCapturedBlack,
            whiteClonesCount = newWhiteClones,
            blackClonesCount = newBlackClones,
            graveyard = finalGraveyard,
            apocalypseState = newApocalypseState,
            activeSpeedRules = newSpeedRules,
            isCheck = isNextKingInCheck,
            isGameOver = isOver,
            matchResult = matchRes,
            lastSpecialEventBanner = specialBanner
        )

        return actualMove
    }

    private fun processUndeadGraveyard(graveyard: List<GraveyardPiece>, board: ChessBoard): Triple<List<GraveyardPiece>, ChessBoard, String?> {
        if (graveyard.isEmpty()) return Triple(emptyList(), board, null)

        val updated = mutableListOf<GraveyardPiece>()
        var newBoard = board
        var banner: String? = null

        for (g in graveyard) {
            val rem = g.turnsUntilRevive - 1
            if (rem <= 0) {
                // Find empty square on owner's home half of board
                val ranks = if (g.originalOwner == PieceColor.WHITE) listOf(0, 1, 2) else listOf(7, 6, 5)
                val emptySquare = (0..7).flatMap { f -> ranks.map { r -> Position(f, r) } }
                    .firstOrNull { newBoard.pieceAt(it) == null }

                if (emptySquare != null) {
                    val revived = g.piece.copy(
                        position = emptySquare,
                        hasResurrected = true,
                        isMystery = false
                    )
                    newBoard = newBoard.withPiecePlaced(revived)
                    banner = "⚰️ UNDEAD RESURRECTION: ${g.originalOwner.name} ${g.piece.currentType.name} rose at ${emptySquare.algebraic.uppercase()}!"
                } else {
                    // Try next turn
                    updated.add(g.copy(turnsUntilRevive = 1))
                }
            } else {
                updated.add(g.copy(turnsUntilRevive = rem))
            }
        }
        return Triple(updated, newBoard, banner)
    }

    private fun processApocalypse(current: ApocalypseState, board: ChessBoard): Triple<ApocalypseState, ChessBoard, String?> {
        if (!config.apocalypseEnabled) return Triple(current, board, null)

        val newMeter = (current.meter + config.apocalypseSpeedPerTurn).coerceAtMost(100)
        val newStage = when {
            newMeter >= 100 -> ApocalypseStage.STAGE_5
            newMeter >= 80 -> ApocalypseStage.STAGE_4
            newMeter >= 60 -> ApocalypseStage.STAGE_3
            newMeter >= 40 -> ApocalypseStage.STAGE_2
            newMeter >= 20 -> ApocalypseStage.STAGE_1
            else -> ApocalypseStage.STAGE_0
        }

        var newBoard = board
        var banner: String? = null

        if (newStage != current.stage) {
            banner = "⚡ APOCALYPSE ADVANCES: ${newStage.title.uppercase()}!"
        }

        // Shrink board perimeter at Stage 5
        var minF = current.minFile
        var maxF = current.maxFile
        var minR = current.minRank
        var maxR = current.maxRank
        val collapsed = current.collapsedSquares.toMutableSet()

        if (newStage == ApocalypseStage.STAGE_5 && current.stage != ApocalypseStage.STAGE_5) {
            minF = 1
            maxF = 6
            minR = 1
            maxR = 6
            for (f in 0..7) {
                for (r in 0..7) {
                    if (f == 0 || f == 7 || r == 0 || r == 7) {
                        val pos = Position(f, r)
                        collapsed.add(pos)
                        val p = newBoard.pieceAt(pos)
                        if (p != null) {
                            if (p.isRoyalKing) {
                                // Nudge king inward safely to keep match fair as specified
                                val safePos = Position(f.coerceIn(1, 6), r.coerceIn(1, 6))
                                newBoard = newBoard.withPieceMoved(pos, safePos)
                            } else {
                                newBoard = newBoard.withPieceRemoved(pos)
                            }
                        }
                    }
                }
            }
            banner = "⚠️ APOCALYPSE CATACLYSM: Outer perimeter collapsed into the Void!"
        }

        val updatedState = current.copy(
            meter = newMeter,
            stage = newStage,
            minFile = minF,
            maxFile = maxF,
            minRank = minR,
            maxRank = maxR,
            collapsedSquares = collapsed
        )

        return Triple(updatedState, newBoard, banner)
    }

    private fun handleChampionFallen(loserColor: PieceColor, move: Move, board: ChessBoard): Move {
        val winner = loserColor.opposite()
        val result = MatchResult(
            winner = winner,
            resultType = MatchResultType.CHAMPION_FALLEN,
            title = "CHAMPION FALLEN!",
            description = "${loserColor.name}'s Champion was slain! ${winner.name} wins!"
        )
        state = state.copy(
            board = board,
            isGameOver = true,
            matchResult = result,
            moveHistory = state.moveHistory + move
        )
        return move
    }

    /**
     * Transform a piece into a Dragon (Dragon Chess mechanic).
     */
    fun transformPieceToDragon(pos: Position): Boolean {
        if (state.isGameOver) return false
        val piece = state.board.pieceAt(pos) ?: return false
        if (piece.owner != state.turn) return false
        if (piece.isRoyalKing || piece.currentType == PieceType.DRAGON) return false

        val charges = if (state.turn == PieceColor.WHITE) state.whiteDragonCharges else state.blackDragonCharges
        if (charges <= 0) return false

        val dragonPiece = piece.transformToDragon()
        val newBoard = state.board.withPiecePlaced(dragonPiece)

        val (newWhite, newBlack) = if (state.turn == PieceColor.WHITE) {
            (charges - 1) to state.blackDragonCharges
        } else {
            state.whiteDragonCharges to (charges - 1)
        }

        state = state.copy(
            board = newBoard,
            whiteDragonCharges = newWhite,
            blackDragonCharges = newBlack,
            lastSpecialEventBanner = "${state.turn.name} UNLEASHED THE DRAGON AT ${pos.algebraic.uppercase()}!"
        )
        return true
    }

    /**
     * Activate Time Warp abilities (Rewind, Freeze, Accelerate).
     */
    fun executeTimeWarp(action: TimeWarpAction, targetPos: Position? = null): Boolean {
        if (state.isGameOver) return false
        val charges = if (state.turn == PieceColor.WHITE) state.whiteTimeCharges else state.blackTimeCharges
        if (charges < action.cost) return false

        val currentTurn = state.turn

        when (action) {
            TimeWarpAction.REWIND -> {
                if (state.moveHistory.isEmpty()) return false
                val previousMoves = state.moveHistory.dropLast(1)
                var reconstructed = ChessBoard.initialBoard()
                for (m in previousMoves) {
                    reconstructed = MoveGenerator.applyMoveToBoard(reconstructed, m)
                }
                val (newW, newB) = if (currentTurn == PieceColor.WHITE) (charges - 1) to state.blackTimeCharges else state.whiteTimeCharges to (charges - 1)
                state = state.copy(
                    board = reconstructed,
                    moveHistory = previousMoves,
                    turn = currentTurn.opposite(),
                    whiteTimeCharges = newW,
                    blackTimeCharges = newB,
                    lastSpecialEventBanner = "⏳ TIME WARP: Rewound the last turn!"
                )
                return true
            }
            TimeWarpAction.FREEZE -> {
                val target = targetPos ?: return false
                val piece = state.board.pieceAt(target) ?: return false
                if (piece.owner == currentTurn || piece.isRoyalKing) return false
                val frozen = piece.copy(isFrozen = true, freezeTurnsRemaining = 2)
                val newBoard = state.board.withPiecePlaced(frozen)
                val (newW, newB) = if (currentTurn == PieceColor.WHITE) (charges - 1) to state.blackTimeCharges else state.whiteTimeCharges to (charges - 1)
                state = state.copy(
                    board = newBoard,
                    whiteTimeCharges = newW,
                    blackTimeCharges = newB,
                    lastSpecialEventBanner = "❄️ CHRONO FREEZE: ${piece.currentType.name} frozen in time!"
                )
                return true
            }
            TimeWarpAction.ACCELERATE -> {
                val target = targetPos ?: return false
                val piece = state.board.pieceAt(target) ?: return false
                if (piece.owner != currentTurn) return false
                val (newW, newB) = if (currentTurn == PieceColor.WHITE) (charges - 1) to state.blackTimeCharges else state.whiteTimeCharges to (charges - 1)
                state = state.copy(
                    whiteTimeCharges = newW,
                    blackTimeCharges = newB,
                    lastSpecialEventBanner = "⚡ TIME SURGE: ${piece.currentType.name} granted bonus temporal momentum!"
                )
                return true
            }
            TimeWarpAction.TIME_SHIFT -> {
                val (newW, newB) = if (currentTurn == PieceColor.WHITE) (charges - 1) to state.blackTimeCharges else state.whiteTimeCharges to (charges - 1)
                state = state.copy(
                    whiteTimeCharges = newW,
                    blackTimeCharges = newB,
                    lastSpecialEventBanner = "🌌 PHASE SHIFT: Temporal distortion applied!"
                )
                return true
            }
        }
    }

    /**
     * Activate Champion special ability (Last Stand mode).
     */
    fun useChampionAbility(pos: Position, targetPos: Position? = null): Boolean {
        val piece = state.board.pieceAt(pos) ?: return false
        if (!piece.isChampion || piece.owner != state.turn || piece.hasUsedChampionAbility) return false
        val champState = if (piece.owner == PieceColor.WHITE) state.whiteChampion else state.blackChampion
        if (champState == null || !champState.abilityAvailable) return false

        var updatedBoard = state.board

        when (champState.championType) {
            ChampionType.PAWN -> {
                // Tactical Retreat: move backward 1 square if empty
                val backwardDir = if (piece.owner == PieceColor.WHITE) -1 else 1
                val backPos = pos.offset(0, backwardDir) ?: return false
                if (updatedBoard.pieceAt(backPos) != null) return false
                updatedBoard = updatedBoard.withPieceMoved(pos, backPos)
            }
            ChampionType.BISHOP -> {
                // Color Shift: move 1 square orthogonally to change square color
                val target = targetPos ?: return false
                val df = Math.abs(target.file - pos.file)
                val dr = Math.abs(target.rank - pos.rank)
                if (!((df == 1 && dr == 0) || (df == 0 && dr == 1))) return false
                if (updatedBoard.pieceAt(target) != null) return false
                updatedBoard = updatedBoard.withPieceMoved(pos, target)
            }
            ChampionType.KNIGHT -> {}
            ChampionType.ROOK -> {}
            ChampionType.QUEEN -> {}
        }

        val updatedPiece = updatedBoard.pieceAt(targetPos ?: pos)?.copy(hasUsedChampionAbility = true)
        if (updatedPiece != null) {
            updatedBoard = updatedBoard.withPiecePlaced(updatedPiece)
        }

        val updatedChampState = champState.copy(abilityAvailable = false)
        state = if (piece.owner == PieceColor.WHITE) {
            state.copy(board = updatedBoard, whiteChampion = updatedChampState, lastSpecialEventBanner = "CHAMPION ABILITY ACTIVATED: ${champState.championType.abilityName}")
        } else {
            state.copy(board = updatedBoard, blackChampion = updatedChampState, lastSpecialEventBanner = "CHAMPION ABILITY ACTIVATED: ${champState.championType.abilityName}")
        }

        return true
    }

    private fun rollNewSpeedRule(): ActiveSpeedRule {
        val types = listOf(
            SpeedRuleType.BACKWARD_PAWNS,
            SpeedRuleType.KNIGHT_BISHOPS,
            SpeedRuleType.DIAGONAL_ROOKS,
            SpeedRuleType.SUPER_KNIGHTS,
            SpeedRuleType.FREEZE_STORM
        )
        val selected = randomEngine.pickRandom(types) ?: SpeedRuleType.KNIGHT_BISHOPS
        return ActiveSpeedRule(type = selected, turnsRemaining = 2)
    }

    private fun rollNewBoardHazard(board: ChessBoard): Pair<List<BoardHazard>, String> {
        val hazardType = randomEngine.pickRandom(listOf(BoardHazardType.METEOR_WARNING, BoardHazardType.TELEPORT_PORTAL, BoardHazardType.BURNING_SQUARE))
            ?: BoardHazardType.BURNING_SQUARE

        when (hazardType) {
            BoardHazardType.METEOR_WARNING -> {
                val f = randomEngine.nextInt(2, 6)
                val r = randomEngine.nextInt(2, 6)
                val pos = Position(f, r)
                val hazard = BoardHazard(
                    id = "meteor_${System.currentTimeMillis()}",
                    type = BoardHazardType.METEOR_WARNING,
                    primaryPosition = pos,
                    countdownTurns = 2,
                    durationTurns = 1
                )
                return listOf(hazard) to "METEOR STRIKE IMMINENT AT ${pos.algebraic.uppercase()}"
            }
            BoardHazardType.TELEPORT_PORTAL -> {
                val p1 = Position(randomEngine.nextInt(1, 4), randomEngine.nextInt(2, 6))
                val p2 = Position(randomEngine.nextInt(4, 7), randomEngine.nextInt(2, 6))
                val hazard = BoardHazard(
                    id = "teleport_${System.currentTimeMillis()}",
                    type = BoardHazardType.TELEPORT_PORTAL,
                    primaryPosition = p1,
                    linkedPosition = p2,
                    countdownTurns = 0,
                    durationTurns = 4,
                    warningActive = false
                )
                return listOf(hazard) to "TELEPORT PORTALS LINKED (${p1.algebraic.uppercase()} ⇄ ${p2.algebraic.uppercase()})"
            }
            BoardHazardType.BURNING_SQUARE -> {
                val pos = Position(randomEngine.nextInt(8), randomEngine.nextInt(2, 6))
                val hazard = BoardHazard(
                    id = "burn_${System.currentTimeMillis()}",
                    type = BoardHazardType.BURNING_SQUARE,
                    primaryPosition = pos,
                    countdownTurns = 2,
                    durationTurns = 3
                )
                return listOf(hazard) to "INFERNAL BURNING ZONE AT ${pos.algebraic.uppercase()}"
            }
            else -> return emptyList<BoardHazard>() to ""
        }
    }

    private fun processSpeedRules(rules: List<ActiveSpeedRule>): List<ActiveSpeedRule> {
        return rules.mapNotNull {
            val remaining = it.turnsRemaining - 1
            if (remaining > 0) it.copy(turnsRemaining = remaining) else null
        }
    }

    private fun processBoardHazards(hazards: List<BoardHazard>, board: ChessBoard): Pair<List<BoardHazard>, String?> {
        val updated = mutableListOf<BoardHazard>()
        var eventDesc: String? = null

        for (h in hazards) {
            val newCount = h.countdownTurns - 1
            if (newCount > 0) {
                updated.add(h.copy(countdownTurns = newCount))
            } else if (newCount == 0) {
                if (h.type == BoardHazardType.METEOR_WARNING) {
                    eventDesc = "METEOR STRUCK ${h.primaryPosition.algebraic.uppercase()}!"
                }
                val newDur = h.durationTurns - 1
                if (newDur > 0) {
                    updated.add(h.copy(countdownTurns = 0, durationTurns = newDur, warningActive = false))
                }
            } else {
                val newDur = h.durationTurns - 1
                if (newDur > 0) {
                    updated.add(h.copy(durationTurns = newDur))
                }
            }
        }
        return updated to eventDesc
    }

    private fun applyBoardHazards(board: ChessBoard, hazards: List<BoardHazard>): ChessBoard {
        var newBoard = board
        for (h in hazards) {
            if (h.countdownTurns == 0) {
                if (h.type == BoardHazardType.METEOR_WARNING || h.type == BoardHazardType.BURNING_SQUARE) {
                    val pieceOnSquare = newBoard.pieceAt(h.primaryPosition)
                    if (pieceOnSquare != null && !pieceOnSquare.isRoyalKing) {
                        newBoard = newBoard.withPieceRemoved(h.primaryPosition)
                    }
                }
            }
        }
        return newBoard
    }

    private fun computeCastlingRights(board: ChessBoard): String {
        val sb = StringBuilder()
        val whiteKing = board.findRoyalKing(PieceColor.WHITE)
        if (whiteKing != null && !whiteKing.hasMoved) {
            val rookK = board.pieceAt(7, 0)
            if (rookK?.hasMoved == false && rookK.currentType == PieceType.ROOK) sb.append("K")
            val rookQ = board.pieceAt(0, 0)
            if (rookQ?.hasMoved == false && rookQ.currentType == PieceType.ROOK) sb.append("Q")
        }
        val blackKing = board.findRoyalKing(PieceColor.BLACK)
        if (blackKing != null && !blackKing.hasMoved) {
            val rookK = board.pieceAt(7, 7)
            if (rookK?.hasMoved == false && rookK.currentType == PieceType.ROOK) sb.append("k")
            val rookQ = board.pieceAt(0, 7)
            if (rookQ?.hasMoved == false && rookQ.currentType == PieceType.ROOK) sb.append("q")
        }
        return sb.toString()
    }

    private fun isThreefoldRepetition(fens: List<String>): Boolean {
        if (fens.size < 6) return false
        val counts = mutableMapOf<String, Int>()
        for (fen in fens) {
            val baseFen = fen.substringBeforeLast(" ").substringBeforeLast(" ")
            val c = (counts[baseFen] ?: 0) + 1
            if (c >= 3) return true
            counts[baseFen] = c
        }
        return false
    }

    private fun isInsufficientMaterial(board: ChessBoard): Boolean {
        val allPieces = board.pieces.values
        if (allPieces.size == 2) {
            return true
        }
        if (allPieces.size == 3) {
            val nonKings = allPieces.filter { !it.isRoyalKing }
            if (nonKings.size == 1 && (nonKings[0].currentType == PieceType.KNIGHT || nonKings[0].currentType == PieceType.BISHOP)) {
                return true
            }
        }
        if (allPieces.size == 4) {
            val nonKings = allPieces.filter { !it.isRoyalKing }
            if (nonKings.size == 2 && nonKings.all { it.currentType == PieceType.BISHOP } && nonKings[0].owner != nonKings[1].owner) {
                val isDark1 = (nonKings[0].position.file + nonKings[0].position.rank) % 2 == 0
                val isDark2 = (nonKings[1].position.file + nonKings[1].position.rank) % 2 == 0
                if (isDark1 == isDark2) return true
            }
        }
        return false
    }

    fun resign(color: PieceColor) {
        val winner = color.opposite()
        state = state.copy(
            isGameOver = true,
            matchResult = MatchResult(
                winner = winner,
                resultType = MatchResultType.RESIGNATION,
                title = "RESIGNATION",
                description = "${color.name} resigned. ${winner.name} is victorious!"
            )
        )
    }
}
