package com.example.chaoschess

import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.Move
import com.example.chaoschess.engine.models.MoveType
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.rules.ChaosEngine
import com.example.chaoschess.engine.rules.ChessBoard
import com.example.chaoschess.engine.rules.MatchResultType
import com.example.chaoschess.engine.rules.MoveGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessEngineCoreTest {

    @Test
    fun testBoardInitialization() {
        val board = ChessBoard.initialBoard()
        assertEquals(32, board.pieces.size)
        assertEquals(16, board.findPieces(PieceColor.WHITE).size)
        assertEquals(16, board.findPieces(PieceColor.BLACK).size)

        // Verify Royal Kings
        val whiteKing = board.findRoyalKing(PieceColor.WHITE)
        val blackKing = board.findRoyalKing(PieceColor.BLACK)
        assertNotNull(whiteKing)
        assertNotNull(blackKing)
        assertEquals(Position(4, 0), whiteKing?.position)
        assertEquals(Position(4, 7), blackKing?.position)
    }

    @Test
    fun testPositionAlgebraicMapping() {
        val e4 = Position.fromAlgebraic("e4")
        assertEquals(4, e4.file)
        assertEquals(3, e4.rank)
        assertEquals("e4", e4.algebraic)

        val a1 = Position.fromAlgebraic("a1")
        assertEquals(0, a1.file)
        assertEquals(0, a1.rank)
        assertEquals("a1", a1.algebraic)

        val h8 = Position.fromAlgebraic("h8")
        assertEquals(7, h8.file)
        assertEquals(7, h8.rank)
        assertEquals("h8", h8.algebraic)
    }

    @Test
    fun testKingsideAndQueensideCastling() {
        var board = ChessBoard()
        val e1 = Position.fromAlgebraic("e1")
        val h1 = Position.fromAlgebraic("h1")
        val a1 = Position.fromAlgebraic("a1")
        val e8 = Position.fromAlgebraic("e8")

        val king = Piece(id = "wk", owner = PieceColor.WHITE, baseType = PieceType.KING, position = e1)
        val rookK = Piece(id = "wr_k", owner = PieceColor.WHITE, baseType = PieceType.ROOK, position = h1)
        val rookQ = Piece(id = "wr_q", owner = PieceColor.WHITE, baseType = PieceType.ROOK, position = a1)
        val enemyKing = Piece(id = "bk", owner = PieceColor.BLACK, baseType = PieceType.KING, position = e8)

        board = board.withPiecePlaced(king)
            .withPiecePlaced(rookK)
            .withPiecePlaced(rookQ)
            .withPiecePlaced(enemyKing)

        val legalMoves = MoveGenerator.generateLegalMoves(board, PieceColor.WHITE)
        val castleKingside = legalMoves.find { it.moveType == MoveType.CASTLE_KINGSIDE }
        val castleQueenside = legalMoves.find { it.moveType == MoveType.CASTLE_QUEENSIDE }

        assertNotNull("Kingside castling must be valid", castleKingside)
        assertNotNull("Queenside castling must be valid", castleQueenside)
        assertEquals(Position.fromAlgebraic("g1"), castleKingside?.to)
        assertEquals(Position.fromAlgebraic("c1"), castleQueenside?.to)

        // Apply kingside castling
        val afterCastle = MoveGenerator.applyMoveToBoard(board, castleKingside!!)
        assertEquals(PieceType.KING, afterCastle.pieceAt(Position.fromAlgebraic("g1"))?.currentType)
        assertEquals(PieceType.ROOK, afterCastle.pieceAt(Position.fromAlgebraic("f1"))?.currentType)
        assertNull(afterCastle.pieceAt(e1))
        assertNull(afterCastle.pieceAt(h1))
    }

    @Test
    fun testPawnPromotion() {
        var board = ChessBoard()
        val a7 = Position.fromAlgebraic("a7")
        val a8 = Position.fromAlgebraic("a8")
        val e1 = Position.fromAlgebraic("e1")
        val e8 = Position.fromAlgebraic("e8")

        val whitePawn = Piece(id = "wp", owner = PieceColor.WHITE, baseType = PieceType.PAWN, position = a7)
        val whiteKing = Piece(id = "wk", owner = PieceColor.WHITE, baseType = PieceType.KING, position = e1)
        val blackKing = Piece(id = "bk", owner = PieceColor.BLACK, baseType = PieceType.KING, position = e8)

        board = board.withPiecePlaced(whitePawn)
            .withPiecePlaced(whiteKing)
            .withPiecePlaced(blackKing)

        val legalMoves = MoveGenerator.generateLegalMoves(board, PieceColor.WHITE)
        val promoMoves = legalMoves.filter { it.from == a7 && it.to == a8 }

        assertEquals(4, promoMoves.size) // Queen, Rook, Bishop, Knight
        val promoQueen = promoMoves.find { it.promotionType == PieceType.QUEEN }
        assertNotNull(promoQueen)

        val promotedBoard = MoveGenerator.applyMoveToBoard(board, promoQueen!!)
        val newQueen = promotedBoard.pieceAt(a8)
        assertNotNull(newQueen)
        assertEquals(PieceType.QUEEN, newQueen?.currentType)
    }

    @Test
    fun testFoolsMateCheckmate() {
        val engine = ChaosEngine(mode = GameMode.CLASSIC)

        // Fool's mate sequence:
        // 1. f2-f3 e7-e5
        // 2. g2-g4 Qd8-h4#
        fun playMove(fromAlg: String, toAlg: String) {
            val from = Position.fromAlgebraic(fromAlg)
            val to = Position.fromAlgebraic(toAlg)
            val move = engine.getLegalMoves().find { it.from == from && it.to == to }
            assertNotNull("Move from $fromAlg to $toAlg should be legal", move)
            engine.executeMove(move!!)
        }

        playMove("f2", "f3")
        playMove("e7", "e5")
        playMove("g2", "g4")
        playMove("d8", "h4")

        assertTrue("Game must be over after Fool's Mate", engine.state.isGameOver)
        assertTrue("White King must be in check", engine.state.isCheck)
        assertEquals(PieceColor.BLACK, engine.state.matchResult?.winner)
        assertEquals(MatchResultType.CHECKMATE, engine.state.matchResult?.resultType)
    }

    @Test
    fun testStalemateDetection() {
        val engine = ChaosEngine(mode = GameMode.CLASSIC)

        // Setup classic stalemate: White Ka6, Qa7 / Black Ka8
        val a6 = Position.fromAlgebraic("a6")
        val c7 = Position.fromAlgebraic("c7")
        val a8 = Position.fromAlgebraic("a8")

        val whiteKing = Piece(id = "wk", owner = PieceColor.WHITE, baseType = PieceType.KING, position = a6)
        val whiteQueen = Piece(id = "wq", owner = PieceColor.WHITE, baseType = PieceType.QUEEN, position = c7)
        val blackKing = Piece(id = "bk", owner = PieceColor.BLACK, baseType = PieceType.KING, position = a8)

        val board = ChessBoard()
            .withPiecePlaced(whiteKing)
            .withPiecePlaced(whiteQueen)
            .withPiecePlaced(blackKing)

        engine.setBoardForTesting(board)

        // Make a queen move to c7->b7 or test stalemate when Black has to move
        // White plays Queen c7 -> c6? No, let's put Queen at c7, White king at a6, Black king at a8.
        // It is Black's turn:
        engine.executeMove(
            Move(
                from = a6,
                to = Position.fromAlgebraic("b6"),
                piece = whiteKing
            )
        )

        // Now Black King at a8 is not in check, but has no legal moves -> Stalemate
        val blackLegalMoves = engine.getLegalMoves()
        assertTrue("Black should have no legal moves", blackLegalMoves.isEmpty())
        assertFalse("Black King should not be in check", engine.state.isCheck)
    }

    @Test
    fun testInsufficientMaterialDraw() {
        val engine = ChaosEngine(mode = GameMode.CLASSIC)

        // King vs King
        val board = ChessBoard()
            .withPiecePlaced(Piece("wk", PieceColor.WHITE, PieceType.KING, position = Position.fromAlgebraic("e1")))
            .withPiecePlaced(Piece("bk", PieceColor.BLACK, PieceType.KING, position = Position.fromAlgebraic("e8")))

        engine.setBoardForTesting(board)
        val move = engine.getLegalMoves().first()
        engine.executeMove(move)

        assertTrue("King vs King must trigger insufficient material draw", engine.state.isGameOver)
        assertEquals(MatchResultType.INSUFFICIENT_MATERIAL, engine.state.matchResult?.resultType)
    }

    @Test
    fun testBoardTrackingAndPositionMethods() {
        val board = ChessBoard.initialBoard()
        assertEquals(64, board.getAllPositions().size)
        assertEquals(32, board.getAllPieces().size)

        // Retrieve occupant
        val occupantE1 = board.getOccupant(Position.fromAlgebraic("e1"))
        assertNotNull(occupantE1)
        assertEquals(PieceType.KING, occupantE1?.currentType)
        assertEquals(PieceColor.WHITE, occupantE1?.owner)

        val occupantE4 = board.getOccupant(Position.fromAlgebraic("e4"))
        assertNull(occupantE4)
        assertTrue(board.isEmpty(Position.fromAlgebraic("e4")))
        assertTrue(board.isEmpty(4, 3))
        assertTrue(board.isOccupied(Position.fromAlgebraic("e1")))
        assertTrue(board.isOccupied(4, 0))

        // Bounds checking
        assertTrue(board.isInside(0, 0))
        assertTrue(board.isInside(7, 7))
        assertFalse(board.isInside(8, 0))
        assertFalse(board.isInside(-1, 3))

        // Clear board
        val cleared = board.clear()
        assertEquals(0, cleared.getAllPieces().size)
        assertTrue(cleared.isEmpty(Position.fromAlgebraic("e1")))

        // Place piece
        val customQueen = Piece(
            id = "custom_queen",
            owner = PieceColor.WHITE,
            baseType = PieceType.QUEEN,
            position = Position.fromAlgebraic("d4")
        )
        val withPlaced = cleared.placePiece(customQueen)
        assertEquals(1, withPlaced.getAllPieces().size)
        assertEquals(customQueen, withPlaced.getOccupant(Position.fromAlgebraic("d4")))
        assertEquals(customQueen, withPlaced.getOccupant(3, 3))

        // Place piece at specific coordinate
        val withPlacedAt = withPlaced.placePieceAt(Position.fromAlgebraic("e5"), customQueen)
        assertEquals(2, withPlacedAt.getAllPieces().size)
        assertNotNull(withPlacedAt.getOccupant(4, 4))

        // Remove piece at coordinate
        val afterRemoval = withPlacedAt.removePieceAt(Position.fromAlgebraic("d4"))
        assertNull(afterRemoval.getOccupant(Position.fromAlgebraic("d4")))
        assertEquals(1, afterRemoval.getAllPieces().size)
    }
}
