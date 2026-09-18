package com.example.chaoschess.engine.rules

import com.example.chaoschess.engine.models.BoardHazard
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position

data class ChessBoard(
    val pieces: Map<Position, Piece> = emptyMap(),
    val enPassantTarget: Position? = null,
    val hazards: List<BoardHazard> = emptyList()
) {
    /**
     * Retrieve the occupant piece at the given coordinate position.
     */
    fun pieceAt(position: Position): Piece? = pieces[position]

    /**
     * Retrieve the occupant piece at the given (file, rank) coordinates.
     */
    fun pieceAt(file: Int, rank: Int): Piece? {
        if (!isInside(file, rank)) return null
        return pieces[Position(file, rank)]
    }

    /**
     * Alias method for occupant retrieval at a position.
     */
    fun getOccupant(position: Position): Piece? = pieceAt(position)

    /**
     * Alias method for occupant retrieval at (file, rank).
     */
    fun getOccupant(file: Int, rank: Int): Piece? = pieceAt(file, rank)

    /**
     * Checks if a square is occupied by any piece.
     */
    fun isOccupied(position: Position): Boolean = pieces.containsKey(position)

    /**
     * Checks if a square at (file, rank) is occupied by any piece.
     */
    fun isOccupied(file: Int, rank: Int): Boolean = isInside(file, rank) && pieces.containsKey(Position(file, rank))

    /**
     * Checks if a square is empty.
     */
    fun isEmpty(position: Position): Boolean = !pieces.containsKey(position)

    /**
     * Checks if a square at (file, rank) is empty.
     */
    fun isEmpty(file: Int, rank: Int): Boolean = isInside(file, rank) && !pieces.containsKey(Position(file, rank))

    /**
     * Checks if the coordinate is within the valid 8x8 chessboard boundary.
     */
    fun isInside(file: Int, rank: Int): Boolean = file in 0..7 && rank in 0..7

    /**
     * Checks if the Position is within the valid 8x8 chessboard boundary.
     */
    fun isInside(position: Position): Boolean = position.file in 0..7 && position.rank in 0..7

    /**
     * Returns a list of all currently active pieces on the board.
     */
    fun getAllPieces(): List<Piece> = pieces.values.toList()

    /**
     * Returns all 64 square positions on the 8x8 grid.
     */
    fun getAllPositions(): List<Position> {
        return (0..7).flatMap { f -> (0..7).map { r -> Position(f, r) } }
    }

    /**
     * Clears all pieces and hazards from the board.
     */
    fun clear(): ChessBoard = ChessBoard()

    /**
     * Returns a new board with all pieces cleared.
     */
    fun withClearedBoard(): ChessBoard = copy(pieces = emptyMap(), enPassantTarget = null, hazards = emptyList())

    /**
     * Returns the royal King for the specified player color.
     */
    fun findRoyalKing(color: PieceColor): Piece? {
        return pieces.values.find { it.owner == color && it.isRoyalKing }
    }

    /**
     * Returns all pieces owned by the specified player color.
     */
    fun findPieces(color: PieceColor): List<Piece> {
        return pieces.values.filter { it.owner == color }
    }

    /**
     * Returns a new board with the piece moved from [from] to [to].
     */
    fun withPieceMoved(from: Position, to: Position): ChessBoard {
        val movingPiece = pieces[from] ?: return this
        val newMap = pieces.toMutableMap()
        newMap.remove(from)
        newMap[to] = movingPiece.moveTo(to)
        return copy(pieces = newMap)
    }

    /**
     * Places a piece at its designated position on the board.
     */
    fun placePiece(piece: Piece): ChessBoard = withPiecePlaced(piece)

    /**
     * Returns a new board with the specified piece placed.
     */
    fun withPiecePlaced(piece: Piece): ChessBoard {
        val newMap = pieces.toMutableMap()
        newMap[piece.position] = piece
        return copy(pieces = newMap)
    }

    /**
     * Places a piece at a specific position coordinate on the board.
     */
    fun placePieceAt(position: Position, piece: Piece): ChessBoard {
        return withPiecePlaced(piece.copy(position = position))
    }

    /**
     * Places a piece at a specific (file, rank) coordinate on the board.
     */
    fun placePieceAt(file: Int, rank: Int, piece: Piece): ChessBoard {
        if (!isInside(file, rank)) return this
        return placePieceAt(Position(file, rank), piece)
    }

    /**
     * Returns a new board with the piece at the specified position removed.
     */
    fun withPieceRemoved(position: Position): ChessBoard {
        val newMap = pieces.toMutableMap()
        newMap.remove(position)
        return copy(pieces = newMap)
    }

    /**
     * Alias for removing a piece from a coordinate.
     */
    fun removePieceAt(position: Position): ChessBoard = withPieceRemoved(position)

    /**
     * Alias for removing a piece from (file, rank).
     */
    fun removePieceAt(file: Int, rank: Int): ChessBoard = withPieceRemoved(Position(file, rank))

    fun withEnPassant(target: Position?): ChessBoard {
        return copy(enPassantTarget = target)
    }

    fun withHazards(newHazards: List<BoardHazard>): ChessBoard {
        return copy(hazards = newHazards)
    }

    fun toFenString(turn: PieceColor, castlingRights: String, halfmove: Int, fullmove: Int): String {
        val sb = StringBuilder()
        for (r in 7 downTo 0) {
            var emptyCount = 0
            for (f in 0..7) {
                val p = pieceAt(f, r)
                if (p == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    val char = when (p.currentType) {
                        PieceType.PAWN -> 'p'
                        PieceType.KNIGHT -> 'n'
                        PieceType.BISHOP -> 'b'
                        PieceType.ROOK -> 'r'
                        PieceType.QUEEN -> 'q'
                        PieceType.KING -> 'k'
                        PieceType.DRAGON -> 'd'
                    }
                    sb.append(if (p.owner == PieceColor.WHITE) char.uppercaseChar() else char)
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount)
            }
            if (r > 0) sb.append('/')
        }
        sb.append(" ").append(if (turn == PieceColor.WHITE) "w" else "b")
        sb.append(" ").append(if (castlingRights.isEmpty()) "-" else castlingRights)
        sb.append(" ").append(enPassantTarget?.algebraic ?: "-")
        sb.append(" ").append(halfmove)
        sb.append(" ").append(fullmove)
        return sb.toString()
    }

    companion object {
        fun initialBoard(): ChessBoard {
            val pieces = mutableMapOf<Position, Piece>()
            var idCounter = 1

            fun addPiece(file: Int, rank: Int, color: PieceColor, type: PieceType) {
                val pos = Position(file, rank)
                val id = "${color.name.first()}_${type.letter}_${idCounter++}"
                pieces[pos] = Piece(
                    id = id,
                    owner = color,
                    baseType = type,
                    currentType = type,
                    position = pos,
                    isRoyalKing = (type == PieceType.KING)
                )
            }

            // White pieces (ranks 0 and 1)
            addPiece(0, 0, PieceColor.WHITE, PieceType.ROOK)
            addPiece(1, 0, PieceColor.WHITE, PieceType.KNIGHT)
            addPiece(2, 0, PieceColor.WHITE, PieceType.BISHOP)
            addPiece(3, 0, PieceColor.WHITE, PieceType.QUEEN)
            addPiece(4, 0, PieceColor.WHITE, PieceType.KING)
            addPiece(5, 0, PieceColor.WHITE, PieceType.BISHOP)
            addPiece(6, 0, PieceColor.WHITE, PieceType.KNIGHT)
            addPiece(7, 0, PieceColor.WHITE, PieceType.ROOK)
            for (f in 0..7) {
                addPiece(f, 1, PieceColor.WHITE, PieceType.PAWN)
            }

            // Black pieces (ranks 7 and 6)
            addPiece(0, 7, PieceColor.BLACK, PieceType.ROOK)
            addPiece(1, 7, PieceColor.BLACK, PieceType.KNIGHT)
            addPiece(2, 7, PieceColor.BLACK, PieceType.BISHOP)
            addPiece(3, 7, PieceColor.BLACK, PieceType.QUEEN)
            addPiece(4, 7, PieceColor.BLACK, PieceType.KING)
            addPiece(5, 7, PieceColor.BLACK, PieceType.BISHOP)
            addPiece(6, 7, PieceColor.BLACK, PieceType.KNIGHT)
            addPiece(7, 7, PieceColor.BLACK, PieceType.ROOK)
            for (f in 0..7) {
                addPiece(f, 6, PieceColor.BLACK, PieceType.PAWN)
            }

            return ChessBoard(pieces = pieces)
        }
    }
}
