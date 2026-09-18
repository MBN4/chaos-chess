package com.example.chaoschess

import com.example.chaoschess.engine.models.ChampionType
import com.example.chaoschess.engine.models.CustomChaosConfig
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.rules.ChaosEngine
import com.example.chaoschess.engine.rules.ChessBoard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameModesTest {

    @Test
    fun testKingHuntCaptureWinsMatch() {
        val config = CustomChaosConfig.forMode(GameMode.KING_HUNT)
        val engine = ChaosEngine(mode = GameMode.KING_HUNT, config = config)

        val e2 = Position.fromAlgebraic("e2")
        val e7 = Position.fromAlgebraic("e7")
        val e1 = Position.fromAlgebraic("e1")

        val whiteQueen = Piece(id = "wq", owner = PieceColor.WHITE, baseType = PieceType.QUEEN, position = e2)
        val blackKing = Piece(id = "bk", owner = PieceColor.BLACK, baseType = PieceType.KING, position = e7, isRoyalKing = true)
        val whiteKing = Piece(id = "wk", owner = PieceColor.WHITE, baseType = PieceType.KING, position = e1, isRoyalKing = true)

        val board = ChessBoard()
            .withPiecePlaced(whiteQueen)
            .withPiecePlaced(blackKing)
            .withPiecePlaced(whiteKing)
        engine.setBoardForTesting(board)

        val captureKingMove = engine.getLegalMoves().find { it.from == e2 && it.to == e7 }
        assertNotNull(captureKingMove)

        engine.executeMove(captureKingMove!!)
        assertTrue("Game should be over when King is captured in King Hunt", engine.state.isGameOver)
        assertEquals("White should win", PieceColor.WHITE, engine.state.matchResult?.winner)
    }

    @Test
    fun testLastStandChampionLossEndsGame() {
        val config = CustomChaosConfig.forMode(GameMode.LAST_STAND)
        val engine = ChaosEngine(mode = GameMode.LAST_STAND, config = config)

        // Set champions
        engine.setChampions(ChampionType.KNIGHT, ChampionType.BISHOP)

        val whiteChamp = engine.state.whiteChampion
        val blackChamp = engine.state.blackChampion
        assertNotNull(whiteChamp)
        assertNotNull(blackChamp)

        // Find Black Champion's piece
        val blackChampPiece = engine.state.board.pieces.values.find { it.id == blackChamp?.pieceId }
        assertNotNull(blackChampPiece)

        // Place a White Rook to capture Black's Champion with clear path
        val targetPos = blackChampPiece!!.position
        val attackFromPos = Position(targetPos.file, 2)
        val whiteRook = Piece(id = "attack_rook", owner = PieceColor.WHITE, baseType = PieceType.ROOK, position = attackFromPos)
        val updatedBoard = engine.state.board
            .withPieceRemoved(Position(targetPos.file, 6)) // remove blocking pawn
            .withPiecePlaced(whiteRook)
        engine.setBoardForTesting(updatedBoard)

        val captureChampMove = engine.getLegalMoves().find { it.from == attackFromPos && it.to == targetPos }
        assertNotNull(captureChampMove)

        engine.executeMove(captureChampMove!!)
        assertTrue("Match should terminate upon Champion capture", engine.state.isGameOver)
        assertEquals("White should be victorious", PieceColor.WHITE, engine.state.matchResult?.winner)
    }

    @Test
    fun testDragonChargeTransformation() {
        val config = CustomChaosConfig.forMode(GameMode.DRAGON)
        val engine = ChaosEngine(mode = GameMode.DRAGON, config = config)

        assertEquals(1, engine.state.whiteDragonCharges)

        // Transform White Bishop at c1 into a Dragon
        val bishopPos = Position.fromAlgebraic("c1")
        val success = engine.transformPieceToDragon(bishopPos)
        assertTrue("Dragon transformation should succeed", success)
        assertEquals(0, engine.state.whiteDragonCharges)

        val piece = engine.state.board.pieceAt(bishopPos)
        assertNotNull(piece)
        assertEquals(PieceType.DRAGON, piece?.currentType)
        assertTrue(piece?.isDragon == true)
    }
}
