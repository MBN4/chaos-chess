package com.example.chaoschess

import com.example.chaoschess.ai.AIDifficulty
import com.example.chaoschess.ai.ChaosAI
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.rules.ChaosEngine
import com.example.chaoschess.engine.rules.ChessBoard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ChaosAITest {

    @Test
    fun hardAiPrefersWinningAnExposedQueen() {
        val whiteKing = Piece("wk", PieceColor.WHITE, PieceType.KING, position = Position.fromAlgebraic("h1"))
        val whiteRook = Piece("wr", PieceColor.WHITE, PieceType.ROOK, position = Position.fromAlgebraic("a1"))
        val blackKing = Piece("bk", PieceColor.BLACK, PieceType.KING, position = Position.fromAlgebraic("h8"))
        val blackQueen = Piece("bq", PieceColor.BLACK, PieceType.QUEEN, position = Position.fromAlgebraic("a8"))
        val board = ChessBoard()
            .withPiecePlaced(whiteKing)
            .withPiecePlaced(whiteRook)
            .withPiecePlaced(blackKing)
            .withPiecePlaced(blackQueen)

        val engine = ChaosEngine(mode = GameMode.CLASSIC, seed = 7L)
        engine.setBoardForTesting(board)

        val move = ChaosAI(AIDifficulty.HARD).findBestMove(engine)

        assertNotNull(move)
        assertEquals(Position.fromAlgebraic("a1"), move?.from)
        assertEquals(Position.fromAlgebraic("a8"), move?.to)
        assertEquals(PieceType.QUEEN, move?.capturedPiece?.currentType)
    }
}
