package com.example.chaoschess.ai

import com.example.chaoschess.engine.models.BoardHazardType
import com.example.chaoschess.engine.models.ChampionType
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.Move
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.rules.ChaosEngine
import com.example.chaoschess.engine.rules.ChessBoard
import com.example.chaoschess.engine.rules.MoveGenerator
import kotlin.math.max
import kotlin.math.min

enum class AIDifficulty(val label: String, val maxDepth: Int, val description: String) {
    BEGINNER("Beginner", 1, "Learns the board and regularly leaves chances"),
    EASY("Easy", 1, "Casual play with simple captures and development"),
    MEDIUM("Medium", 2, "Balanced tactical and positional play"),
    HARD("Hard", 3, "Competitive calculation and king safety"),
    EXPERT("Master", 4, "Deep search, move ordering, and precise defense")
}

class ChaosAI(val difficulty: AIDifficulty = AIDifficulty.MEDIUM) {

    // Piece square tables (from White's perspective; inverted for Black)
    private val PAWN_TABLE = intArrayOf(
        0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5,  5, 10, 25, 25, 10,  5,  5,
        0,  0,  0, 20, 20,  0,  0,  0,
        5, -5,-10,  0,  0,-10, -5,  5,
        5, 10, 10,-20,-20, 10, 10,  5,
        0,  0,  0,  0,  0,  0,  0,  0
    )

    private val KNIGHT_TABLE = intArrayOf(
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    )

    private val BISHOP_TABLE = intArrayOf(
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    )

    fun findBestMove(engine: ChaosEngine): Move? {
        val legalMoves = engine.getLegalMoves()
        if (legalMoves.isEmpty()) return null

        if (difficulty == AIDifficulty.BEGINNER || difficulty == AIDifficulty.EASY) {
            val randomMoveChance = if (difficulty == AIDifficulty.BEGINNER) 0.55 else 0.22
            if (engine.randomEngine.nextDouble() < randomMoveChance) {
                return engine.randomEngine.pickRandom(legalMoves)
            }
        }

        // Check if King can be captured immediately in King Hunt mode
        if (engine.config.kingHuntEnabled) {
            val kingCaptureMove = legalMoves.find { it.capturedPiece?.isRoyalKing == true }
            if (kingCaptureMove != null) return kingCaptureMove
        }

        // Check if enemy champion can be captured in Last Stand mode
        if (engine.config.championEnabled) {
            val champCaptureMove = legalMoves.find { it.capturedPiece?.isChampion == true }
            if (champCaptureMove != null) return champCaptureMove
        }

        val aiColor = engine.state.turn
        var bestScore = Int.MIN_VALUE
        var bestMove: Move = legalMoves.first()

        // Order moves (captures and promotions evaluated first)
        val orderedMoves = legalMoves.sortedByDescending { moveScoreHeuristic(it) }

        val searchDepth = difficulty.maxDepth

        for (move in orderedMoves) {
            val nextBoard = MoveGenerator.applyMoveToBoard(engine.state.board, move)
            val score = minimax(
                board = nextBoard,
                depth = searchDepth - 1,
                alpha = Int.MIN_VALUE + 100,
                beta = Int.MAX_VALUE - 100,
                isMaximizing = false,
                currentColor = aiColor.opposite(),
                aiColor = aiColor,
                engine = engine
            )

            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
    }

    private fun minimax(
        board: ChessBoard,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        currentColor: PieceColor,
        aiColor: PieceColor,
        engine: ChaosEngine
    ): Int {
        val isKingHunt = engine.config.kingHuntEnabled
        val moves = MoveGenerator.generateLegalMoves(board, currentColor, isKingHunt, engine.state.activeSpeedRules)

        if (moves.isEmpty()) {
            val inCheck = if (isKingHunt) false else MoveGenerator.isKingInCheck(board, currentColor)
            return if (inCheck) {
                // Checkmate penalty/reward
                if (currentColor == aiColor) -100000 - depth else 100000 + depth
            } else {
                0 // Stalemate draw
            }
        }

        if (depth <= 0) {
            return evaluateBoard(board, aiColor, engine)
        }

        var currentAlpha = alpha
        var currentBeta = beta

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (m in moves.sortedByDescending { moveScoreHeuristic(it) }) {
                val nextBoard = MoveGenerator.applyMoveToBoard(board, m)
                val eval = minimax(nextBoard, depth - 1, currentAlpha, currentBeta, false, currentColor.opposite(), aiColor, engine)
                maxEval = max(maxEval, eval)
                currentAlpha = max(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (m in moves.sortedByDescending { moveScoreHeuristic(it) }) {
                val nextBoard = MoveGenerator.applyMoveToBoard(board, m)
                val eval = minimax(nextBoard, depth - 1, currentAlpha, currentBeta, true, currentColor.opposite(), aiColor, engine)
                minEval = min(minEval, eval)
                currentBeta = min(currentBeta, eval)
                if (currentBeta <= currentAlpha) break
            }
            return minEval
        }
    }

    private fun evaluateBoard(board: ChessBoard, aiColor: PieceColor, engine: ChaosEngine): Int {
        var score = 0

        val myPieces = board.findPieces(aiColor)
        val enemyPieces = board.findPieces(aiColor.opposite())

        // Material & Positional Evaluation
        for (p in myPieces) {
            score += pieceValue(p.currentType, p.isChampion, engine.config.championEnabled)
            score += positionalBonus(p.position, p.currentType, aiColor)

            // Hazard avoidance penalty
            for (hazard in board.hazards) {
                if (hazard.countdownTurns <= 1 && (hazard.type == BoardHazardType.METEOR_WARNING || hazard.type == BoardHazardType.BURNING_SQUARE)) {
                    if (p.position == hazard.primaryPosition) {
                        score -= 800 // High penalty for lingering on danger squares
                    }
                }
            }
        }

        for (p in enemyPieces) {
            score -= pieceValue(p.currentType, p.isChampion, engine.config.championEnabled)
            score -= positionalBonus(p.position, p.currentType, aiColor.opposite())

            // Bonus if enemy piece is caught on imminent hazard
            for (hazard in board.hazards) {
                if (hazard.countdownTurns <= 1 && (hazard.type == BoardHazardType.METEOR_WARNING || hazard.type == BoardHazardType.BURNING_SQUARE)) {
                    if (p.position == hazard.primaryPosition) {
                        score += 500
                    }
                }
            }
        }

        // Mode specific heuristics
        if (engine.config.kingHuntEnabled) {
            // In King Hunt, distance to enemy king is huge
            val enemyKing = board.findRoyalKing(aiColor.opposite())
            val myKing = board.findRoyalKing(aiColor)
            if (enemyKing != null) {
                // Closer pieces to enemy king = higher pressure
                for (p in myPieces) {
                    val dist = Math.abs(p.position.file - enemyKing.position.file) + Math.abs(p.position.rank - enemyKing.position.rank)
                    score += (14 - dist) * 15
                }
            }
        }

        if (engine.config.championEnabled) {
            val myChamp = myPieces.find { it.isChampion }
            if (myChamp == null) {
                score -= 50000 // Lost champion!
            }
            val enemyChamp = enemyPieces.find { it.isChampion }
            if (enemyChamp == null) {
                score += 50000 // Slain enemy champion!
            }
        }

        val myMobility = MoveGenerator.generateLegalMoves(
            board, aiColor, engine.config.kingHuntEnabled, engine.state.activeSpeedRules
        ).size
        val enemyMobility = MoveGenerator.generateLegalMoves(
            board, aiColor.opposite(), engine.config.kingHuntEnabled, engine.state.activeSpeedRules
        ).size
        score += (myMobility - enemyMobility) * 3

        if (!engine.config.kingHuntEnabled) {
            if (MoveGenerator.isKingInCheck(board, aiColor, engine.state.activeSpeedRules)) score -= 45
            if (MoveGenerator.isKingInCheck(board, aiColor.opposite(), engine.state.activeSpeedRules)) score += 45
        }

        return score
    }

    private fun pieceValue(type: PieceType, isChampion: Boolean, championMode: Boolean): Int {
        if (championMode && isChampion) return 25000
        return when (type) {
            PieceType.PAWN -> 100
            PieceType.KNIGHT -> 320
            PieceType.BISHOP -> 330
            PieceType.ROOK -> 500
            PieceType.QUEEN -> 900
            PieceType.KING -> 20000
            PieceType.DRAGON -> 650
        }
    }

    private fun positionalBonus(pos: Position, type: PieceType, color: PieceColor): Int {
        val index = if (color == PieceColor.WHITE) {
            (7 - pos.rank) * 8 + pos.file
        } else {
            pos.rank * 8 + pos.file
        }
        if (index !in 0..63) return 0
        return when (type) {
            PieceType.PAWN -> PAWN_TABLE[index]
            PieceType.KNIGHT -> KNIGHT_TABLE[index]
            PieceType.BISHOP -> BISHOP_TABLE[index]
            PieceType.DRAGON -> KNIGHT_TABLE[index] + BISHOP_TABLE[index] / 2
            else -> 0
        }
    }

    private fun moveScoreHeuristic(move: Move): Int {
        var score = 0
        if (move.capturedPiece != null) {
            score += 1000 + pieceValue(move.capturedPiece.currentType, move.capturedPiece.isChampion, false) -
                    pieceValue(move.piece.currentType, false, false) / 10
        }
        if (move.promotionType != null) {
            score += 800
        }
        return score
    }
}
