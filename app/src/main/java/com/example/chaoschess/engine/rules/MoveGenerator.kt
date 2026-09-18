package com.example.chaoschess.engine.rules

import com.example.chaoschess.engine.models.ActiveSpeedRule
import com.example.chaoschess.engine.models.BoardHazardType
import com.example.chaoschess.engine.models.Move
import com.example.chaoschess.engine.models.MoveType
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.models.SpeedRuleType

object MoveGenerator {

    private val KNIGHT_OFFSETS = listOf(
        Pair(1, 2), Pair(2, 1), Pair(2, -1), Pair(1, -2),
        Pair(-1, -2), Pair(-2, -1), Pair(-2, 1), Pair(-1, 2)
    )

    private val BISHOP_DIRECTIONS = listOf(
        Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)
    )

    private val ROOK_DIRECTIONS = listOf(
        Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
    )

    private val KING_OFFSETS = listOf(
        Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1),
        Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)
    )

    /**
     * Computes all strictly legal moves for the given player.
     */
    fun generateLegalMoves(
        board: ChessBoard,
        color: PieceColor,
        isKingHuntMode: Boolean = false,
        activeSpeedRules: List<ActiveSpeedRule> = emptyList()
    ): List<Move> {
        val pieces = board.findPieces(color)
        val allPseudoLegal = mutableListOf<Move>()

        for (piece in pieces) {
            if (piece.isFrozen) continue
            allPseudoLegal.addAll(generatePseudoLegalMoves(board, piece, activeSpeedRules, isKingHuntMode))
        }

        if (isKingHuntMode) {
            // In King Hunt mode, check/checkmate rules and king safety are completely disabled!
            return allPseudoLegal
        }

        // Filter out moves that leave the Royal King in check
        return allPseudoLegal.filter { move ->
            val simulatedBoard = applyMoveToBoard(board, move)
            !isKingInCheck(simulatedBoard, color, activeSpeedRules)
        }
    }

    /**
     * Checks if the Royal King of the given color is currently under attack.
     */
    fun isKingInCheck(
        board: ChessBoard,
        color: PieceColor,
        activeSpeedRules: List<ActiveSpeedRule> = emptyList()
    ): Boolean {
        val royalKing = board.findRoyalKing(color) ?: return false
        return isSquareAttacked(board, royalKing.position, color.opposite(), activeSpeedRules)
    }

    /**
     * Checks whether a given square is attacked by any piece of attackerColor.
     */
    fun isSquareAttacked(
        board: ChessBoard,
        targetPos: Position,
        attackerColor: PieceColor,
        activeSpeedRules: List<ActiveSpeedRule> = emptyList()
    ): Boolean {
        val enemyPieces = board.findPieces(attackerColor)
        for (enemy in enemyPieces) {
            if (enemy.isFrozen) continue
            val attacks = generateAttackSquares(board, enemy, activeSpeedRules)
            if (targetPos in attacks) {
                return true
            }
        }
        return false
    }

    /**
     * Generates all squares attacked/threatened by a piece.
     */
    fun generateAttackSquares(
        board: ChessBoard,
        piece: Piece,
        activeSpeedRules: List<ActiveSpeedRule> = emptyList()
    ): Set<Position> {
        val attacked = mutableSetOf<Position>()
        val pos = piece.position
        val forwardDir = if (piece.owner == PieceColor.WHITE) 1 else -1

        // Speed rule checks
        val hasKnightBishops = activeSpeedRules.any { it.type == SpeedRuleType.KNIGHT_BISHOPS }
        val hasDiagonalRooks = activeSpeedRules.any { it.type == SpeedRuleType.DIAGONAL_ROOKS }
        val hasBackwardPawns = activeSpeedRules.any { it.type == SpeedRuleType.BACKWARD_PAWNS }

        when (piece.currentType) {
            PieceType.PAWN -> {
                // Diagonal capture attacks
                pos.offset(1, forwardDir)?.let { attacked.add(it) }
                pos.offset(-1, forwardDir)?.let { attacked.add(it) }

                if (hasBackwardPawns) {
                    pos.offset(1, -forwardDir)?.let { attacked.add(it) }
                    pos.offset(-1, -forwardDir)?.let { attacked.add(it) }
                }
            }

            PieceType.KNIGHT -> {
                for ((df, dr) in KNIGHT_OFFSETS) {
                    pos.offset(df, dr)?.let { attacked.add(it) }
                }
            }

            PieceType.BISHOP -> {
                addRayAttacks(board, pos, BISHOP_DIRECTIONS, attacked)
                if (hasKnightBishops) {
                    for ((df, dr) in KNIGHT_OFFSETS) {
                        pos.offset(df, dr)?.let { attacked.add(it) }
                    }
                }
            }

            PieceType.ROOK -> {
                addRayAttacks(board, pos, ROOK_DIRECTIONS, attacked)
                if (hasDiagonalRooks) {
                    addRayAttacks(board, pos, BISHOP_DIRECTIONS, attacked)
                }
            }

            PieceType.QUEEN -> {
                addRayAttacks(board, pos, BISHOP_DIRECTIONS, attacked)
                addRayAttacks(board, pos, ROOK_DIRECTIONS, attacked)
            }

            PieceType.KING -> {
                for ((df, dr) in KING_OFFSETS) {
                    pos.offset(df, dr)?.let { attacked.add(it) }
                }
            }

            PieceType.DRAGON -> {
                // Dragon = Knight + Bishop (leaps like Knight, slides like Bishop)
                for ((df, dr) in KNIGHT_OFFSETS) {
                    pos.offset(df, dr)?.let { attacked.add(it) }
                }
                addRayAttacks(board, pos, BISHOP_DIRECTIONS, attacked)
            }
        }

        return attacked
    }

    private fun addRayAttacks(
        board: ChessBoard,
        from: Position,
        directions: List<Pair<Int, Int>>,
        dest: MutableSet<Position>
    ) {
        for ((df, dr) in directions) {
            var curr = from.offset(df, dr)
            while (curr != null) {
                dest.add(curr)
                if (board.pieceAt(curr) != null) {
                    // Ray blocked by a piece
                    break
                }
                curr = curr.offset(df, dr)
            }
        }
    }

    /**
     * Generates pseudo-legal moves for a specific piece.
     */
    fun generatePseudoLegalMoves(
        board: ChessBoard,
        piece: Piece,
        activeSpeedRules: List<ActiveSpeedRule> = emptyList(),
        isKingHuntMode: Boolean = false
    ): List<Move> {
        val moves = mutableListOf<Move>()
        val pos = piece.position
        val owner = piece.owner
        val forward = if (owner == PieceColor.WHITE) 1 else -1
        val startRank = if (owner == PieceColor.WHITE) 1 else 6
        val promoRank = if (owner == PieceColor.WHITE) 7 else 0

        // Collapsed squares cannot be moved onto
        val collapsedPositions = board.hazards
            .filter { it.type == BoardHazardType.COLLAPSED_SQUARE && it.durationTurns > 0 }
            .map { it.primaryPosition }
            .toSet()

        fun canMoveTo(dest: Position): Boolean {
            if (dest in collapsedPositions) return false
            val target = board.pieceAt(dest)
            return target == null || target.owner != owner
        }

        val hasKnightBishops = activeSpeedRules.any { it.type == SpeedRuleType.KNIGHT_BISHOPS }
        val hasDiagonalRooks = activeSpeedRules.any { it.type == SpeedRuleType.DIAGONAL_ROOKS }
        val hasBackwardPawns = activeSpeedRules.any { it.type == SpeedRuleType.BACKWARD_PAWNS }

        when (piece.currentType) {
            PieceType.PAWN -> {
                // 1 square forward
                val oneStep = pos.offset(0, forward)
                if (oneStep != null && board.pieceAt(oneStep) == null && oneStep !in collapsedPositions) {
                    if (oneStep.rank == promoRank) {
                        // Promotion
                        for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                            moves.add(Move(pos, oneStep, piece, moveType = MoveType.PROMOTION, promotionType = promo))
                        }
                    } else {
                        moves.add(Move(pos, oneStep, piece, moveType = MoveType.NORMAL))

                        // 2 squares forward from start rank
                        if (pos.rank == startRank) {
                            val twoStep = pos.offset(0, forward * 2)
                            if (twoStep != null && board.pieceAt(twoStep) == null && twoStep !in collapsedPositions) {
                                moves.add(Move(pos, twoStep, piece, moveType = MoveType.DOUBLE_PAWN_PUSH))
                            }
                        }
                    }
                }

                // Diagonal Captures
                for (df in listOf(-1, 1)) {
                    val capPos = pos.offset(df, forward)
                    if (capPos != null && capPos !in collapsedPositions) {
                        val target = board.pieceAt(capPos)
                        if (target != null && target.owner != owner) {
                            if (capPos.rank == promoRank) {
                                for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                                    moves.add(Move(pos, capPos, piece, capturedPiece = target, moveType = MoveType.PROMOTION, promotionType = promo))
                                }
                            } else {
                                moves.add(Move(pos, capPos, piece, capturedPiece = target, moveType = MoveType.NORMAL))
                            }
                        } else if (capPos == board.enPassantTarget) {
                            // En Passant
                            val epCapturedPawnPos = Position(capPos.file, pos.rank)
                            val epPawn = board.pieceAt(epCapturedPawnPos)
                            if (epPawn != null && epPawn.owner != owner && epPawn.currentType == PieceType.PAWN) {
                                moves.add(Move(pos, capPos, piece, capturedPiece = epPawn, moveType = MoveType.EN_PASSANT))
                            }
                        }
                    }
                }

                // Speed rule: Backward Pawns
                if (hasBackwardPawns) {
                    val backStep = pos.offset(0, -forward)
                    if (backStep != null && board.pieceAt(backStep) == null && backStep !in collapsedPositions) {
                        moves.add(Move(pos, backStep, piece, moveType = MoveType.NORMAL))
                    }
                }
            }

            PieceType.KNIGHT -> {
                for ((df, dr) in KNIGHT_OFFSETS) {
                    val dest = pos.offset(df, dr) ?: continue
                    if (canMoveTo(dest)) {
                        moves.add(Move(pos, dest, piece, capturedPiece = board.pieceAt(dest)))
                    }
                }
            }

            PieceType.BISHOP -> {
                addRayMoves(board, piece, pos, BISHOP_DIRECTIONS, collapsedPositions, moves)
                if (hasKnightBishops) {
                    for ((df, dr) in KNIGHT_OFFSETS) {
                        val dest = pos.offset(df, dr) ?: continue
                        if (canMoveTo(dest)) {
                            moves.add(Move(pos, dest, piece, capturedPiece = board.pieceAt(dest)))
                        }
                    }
                }
            }

            PieceType.ROOK -> {
                addRayMoves(board, piece, pos, ROOK_DIRECTIONS, collapsedPositions, moves)
                if (hasDiagonalRooks) {
                    addRayMoves(board, piece, pos, BISHOP_DIRECTIONS, collapsedPositions, moves)
                }
            }

            PieceType.QUEEN -> {
                addRayMoves(board, piece, pos, BISHOP_DIRECTIONS, collapsedPositions, moves)
                addRayMoves(board, piece, pos, ROOK_DIRECTIONS, collapsedPositions, moves)
            }

            PieceType.KING -> {
                for ((df, dr) in KING_OFFSETS) {
                    val dest = pos.offset(df, dr) ?: continue
                    if (canMoveTo(dest)) {
                        moves.add(Move(pos, dest, piece, capturedPiece = board.pieceAt(dest)))
                    }
                }

                // Castling (allowed for Royal King if not moved, not in check, path clear and safe)
                if (piece.isRoyalKing && !piece.hasMoved && !isKingHuntMode) {
                    val rank = if (owner == PieceColor.WHITE) 0 else 7

                    // Kingside castling (King e->g, Rook h->f)
                    val rookKingside = board.pieceAt(7, rank)
                    if (rookKingside != null && rookKingside.owner == owner && rookKingside.currentType == PieceType.ROOK && !rookKingside.hasMoved) {
                        val fSquare = Position(5, rank)
                        val gSquare = Position(6, rank)
                        if (board.pieceAt(fSquare) == null && board.pieceAt(gSquare) == null &&
                            fSquare !in collapsedPositions && gSquare !in collapsedPositions
                        ) {
                            // Check squares safety
                            if (!isSquareAttacked(board, pos, owner.opposite(), activeSpeedRules) &&
                                !isSquareAttacked(board, fSquare, owner.opposite(), activeSpeedRules) &&
                                !isSquareAttacked(board, gSquare, owner.opposite(), activeSpeedRules)
                            ) {
                                moves.add(Move(pos, gSquare, piece, moveType = MoveType.CASTLE_KINGSIDE))
                            }
                        }
                    }

                    // Queenside castling (King e->c, Rook a->d)
                    val rookQueenside = board.pieceAt(0, rank)
                    if (rookQueenside != null && rookQueenside.owner == owner && rookQueenside.currentType == PieceType.ROOK && !rookQueenside.hasMoved) {
                        val bSquare = Position(1, rank)
                        val cSquare = Position(2, rank)
                        val dSquare = Position(3, rank)
                        if (board.pieceAt(bSquare) == null && board.pieceAt(cSquare) == null && board.pieceAt(dSquare) == null &&
                            bSquare !in collapsedPositions && cSquare !in collapsedPositions && dSquare !in collapsedPositions
                        ) {
                            if (!isSquareAttacked(board, pos, owner.opposite(), activeSpeedRules) &&
                                !isSquareAttacked(board, dSquare, owner.opposite(), activeSpeedRules) &&
                                !isSquareAttacked(board, cSquare, owner.opposite(), activeSpeedRules)
                            ) {
                                moves.add(Move(pos, cSquare, piece, moveType = MoveType.CASTLE_QUEENSIDE))
                            }
                        }
                    }
                }
            }

            PieceType.DRAGON -> {
                // Dragon = Knight (jumps) + Bishop (diagonal continuous slides)
                for ((df, dr) in KNIGHT_OFFSETS) {
                    val dest = pos.offset(df, dr) ?: continue
                    if (canMoveTo(dest)) {
                        moves.add(Move(pos, dest, piece, capturedPiece = board.pieceAt(dest), moveType = MoveType.DRAGON_LEAP))
                    }
                }
                addRayMoves(board, piece, pos, BISHOP_DIRECTIONS, collapsedPositions, moves)
            }
        }

        return moves
    }

    private fun addRayMoves(
        board: ChessBoard,
        piece: Piece,
        from: Position,
        directions: List<Pair<Int, Int>>,
        collapsed: Set<Position>,
        moves: MutableList<Move>
    ) {
        for ((df, dr) in directions) {
            var curr = from.offset(df, dr)
            while (curr != null) {
                if (curr in collapsed) break
                val target = board.pieceAt(curr)
                if (target == null) {
                    moves.add(Move(from, curr, piece))
                } else {
                    if (target.owner != piece.owner) {
                        moves.add(Move(from, curr, piece, capturedPiece = target))
                    }
                    break // Ray is blocked
                }
                curr = curr.offset(df, dr)
            }
        }
    }

    /**
     * Applies a move to the board and returns the new board state.
     */
    fun applyMoveToBoard(board: ChessBoard, move: Move): ChessBoard {
        var newBoard = board

        when (move.moveType) {
            MoveType.NORMAL, MoveType.DRAGON_LEAP -> {
                newBoard = newBoard.withPieceMoved(move.from, move.to)
            }

            MoveType.DOUBLE_PAWN_PUSH -> {
                newBoard = newBoard.withPieceMoved(move.from, move.to)
                val epRank = (move.from.rank + move.to.rank) / 2
                newBoard = newBoard.withEnPassant(Position(move.from.file, epRank))
                return newBoard
            }

            MoveType.EN_PASSANT -> {
                newBoard = newBoard.withPieceMoved(move.from, move.to)
                // Remove en-passant captured pawn
                val epPawnPos = Position(move.to.file, move.from.rank)
                newBoard = newBoard.withPieceRemoved(epPawnPos)
            }

            MoveType.CASTLE_KINGSIDE -> {
                val rank = move.from.rank
                // Move King e->g
                newBoard = newBoard.withPieceMoved(move.from, Position(6, rank))
                // Move Rook h->f
                newBoard = newBoard.withPieceMoved(Position(7, rank), Position(5, rank))
            }

            MoveType.CASTLE_QUEENSIDE -> {
                val rank = move.from.rank
                // Move King e->c
                newBoard = newBoard.withPieceMoved(move.from, Position(2, rank))
                // Move Rook a->d
                newBoard = newBoard.withPieceMoved(Position(0, rank), Position(3, rank))
            }

            MoveType.PROMOTION -> {
                newBoard = newBoard.withPieceMoved(move.from, move.to)
                val promoType = move.promotionType ?: PieceType.QUEEN
                val movedPiece = newBoard.pieceAt(move.to)
                if (movedPiece != null) {
                    newBoard = newBoard.withPiecePlaced(movedPiece.mutateTo(promoType))
                }
            }

            MoveType.CHAMPION_ABILITY, MoveType.PORTAL_WARP, MoveType.TIME_WARP_ACCEL -> {
                newBoard = newBoard.withPieceMoved(move.from, move.to)
            }
        }

        // Clear en passant target unless set by DOUBLE_PAWN_PUSH above
        if (move.moveType != MoveType.DOUBLE_PAWN_PUSH) {
            newBoard = newBoard.withEnPassant(null)
        }

        // Check if destination is a teleport portal hazard
        val portal = newBoard.hazards.find { it.type == BoardHazardType.TELEPORT_PORTAL && it.primaryPosition == move.to }
        if (portal?.linkedPosition != null && newBoard.pieceAt(portal.linkedPosition) == null) {
            newBoard = newBoard.withPieceMoved(move.to, portal.linkedPosition)
        }

        return newBoard
    }
}
