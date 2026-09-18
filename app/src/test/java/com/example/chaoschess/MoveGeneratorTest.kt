package com.example.chaoschess

import com.example.chaoschess.engine.models.MoveType
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.rules.ChessBoard
import com.example.chaoschess.engine.rules.MoveGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveGeneratorTest {

    @Test
    fun testInitialBoardLegalMoves() {
        val board = ChessBoard.initialBoard()
        val whiteMoves = MoveGenerator.generateLegalMoves(board, PieceColor.WHITE)
        // In standard chess, White has 16 pawn moves (8 single, 8 double) + 4 knight moves = 20 legal moves
        assertEquals(20, whiteMoves.size)
    }

    @Test
    fun testPawnDoubleMoveAndEnPassant() {
        var board = ChessBoard()
        val e2 = Position.fromAlgebraic("e2")
        val d4 = Position.fromAlgebraic("d4")
        val e1 = Position.fromAlgebraic("e1")
        val e8 = Position.fromAlgebraic("e8")

        val whitePawn = Piece(id = "wp_e2", owner = PieceColor.WHITE, baseType = PieceType.PAWN, position = e2)
        val blackPawn = Piece(id = "bp_d4", owner = PieceColor.BLACK, baseType = PieceType.PAWN, position = d4)
        val whiteKing = Piece(id = "wk", owner = PieceColor.WHITE, baseType = PieceType.KING, position = e1)
        val blackKing = Piece(id = "bk", owner = PieceColor.BLACK, baseType = PieceType.KING, position = e8)

        board = board.withPiecePlaced(whitePawn)
            .withPiecePlaced(blackPawn)
            .withPiecePlaced(whiteKing)
            .withPiecePlaced(blackKing)

        // White plays e2 -> e4 (double push)
        val doublePush = MoveGenerator.generateLegalMoves(board, PieceColor.WHITE)
            .find { it.from == e2 && it.to == Position.fromAlgebraic("e4") }
        assertNotNull(doublePush)
        assertEquals(MoveType.DOUBLE_PAWN_PUSH, doublePush?.moveType)

        // Apply e2->e4
        board = board.withPieceRemoved(e2)
            .withPiecePlaced(whitePawn.copy(position = Position.fromAlgebraic("e4"), hasMoved = true))
            .withEnPassant(Position.fromAlgebraic("e3"))

        // Now Black at d4 should have en passant move to e3
        val blackMoves = MoveGenerator.generateLegalMoves(board, PieceColor.BLACK)
        val epMove = blackMoves.find { it.from == d4 && it.to == Position.fromAlgebraic("e3") }
        assertNotNull("En Passant move should be available", epMove)
        assertEquals(MoveType.EN_PASSANT, epMove?.moveType)
    }

    @Test
    fun testDragonMovement() {
        var board = ChessBoard()
        val d4 = Position.fromAlgebraic("d4")
        val e1 = Position.fromAlgebraic("e1")
        val e8 = Position.fromAlgebraic("e8")

        val dragon = Piece(id = "w_drag", owner = PieceColor.WHITE, baseType = PieceType.DRAGON, position = d4)
        val whiteKing = Piece(id = "wk", owner = PieceColor.WHITE, baseType = PieceType.KING, position = e1)
        val blackKing = Piece(id = "bk", owner = PieceColor.BLACK, baseType = PieceType.KING, position = e8)

        board = board.withPiecePlaced(dragon)
            .withPiecePlaced(whiteKing)
            .withPiecePlaced(blackKing)

        val moves = MoveGenerator.generateLegalMoves(board, PieceColor.WHITE)
            .filter { it.from == d4 }

        // Knight leaps from d4: b3, b5, c2, c6, e2, e6, f3, f5 (8 destinations)
        // Bishop diagonals from d4 on open board:
        // up-right: e5, f6, g7, h8 (4)
        // up-left: c5, b6, a7 (3)
        // down-right: e3, f2, g1 (3)
        // down-left: c3, b2, a1 (3)
        // Total diagonals = 13.
        // Total moves = 8 + 13 = 21 moves
        assertEquals(21, moves.size)

        // Check specific knight leap and bishop diagonal
        assertTrue(moves.any { it.to == Position.fromAlgebraic("e6") }) // knight leap
        assertTrue(moves.any { it.to == Position.fromAlgebraic("h8") }) // bishop slide
        assertTrue(moves.any { it.to == Position.fromAlgebraic("a1") }) // bishop slide
    }

    @Test
    fun testKingInCheckValidation() {
        var board = ChessBoard()
        val e1 = Position.fromAlgebraic("e1")
        val e8 = Position.fromAlgebraic("e8")
        val a2 = Position.fromAlgebraic("a2")
        val a8 = Position.fromAlgebraic("a8")

        val whiteKing = Piece(id = "wk", owner = PieceColor.WHITE, baseType = PieceType.KING, position = e1)
        val blackRook = Piece(id = "br", owner = PieceColor.BLACK, baseType = PieceType.ROOK, position = e8)
        val whitePawn = Piece(id = "wp", owner = PieceColor.WHITE, baseType = PieceType.PAWN, position = a2)
        val blackKing = Piece(id = "bk", owner = PieceColor.BLACK, baseType = PieceType.KING, position = a8)

        board = board.withPiecePlaced(whiteKing)
            .withPiecePlaced(blackRook)

        assertTrue("White King should be in check", MoveGenerator.isSquareAttacked(board, e1, PieceColor.BLACK))

        board = board.withPiecePlaced(whitePawn).withPiecePlaced(blackKing)

        val legalMoves = MoveGenerator.generateLegalMoves(board, PieceColor.WHITE)
        // Moving pawn a2->a3 does not resolve check, so it must not be in legalMoves
        val pawnMove = legalMoves.find { it.from == a2 }
        assertEquals(null, pawnMove)
    }
}
