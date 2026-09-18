package com.example.chaoschess

import com.example.chaoschess.engine.models.CustomChaosConfig
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.MutationProbabilities
import com.example.chaoschess.engine.models.Piece
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.chaoschess.engine.models.Position
import com.example.chaoschess.engine.random.ChaosRandom
import com.example.chaoschess.engine.rules.ChaosEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MutationTest {

    @Test
    fun testExactRareKingProbability() {
        val probs = MutationProbabilities()
        assertEquals(0.001, probs.kingPercent, 0.0000001) // 0.001%
    }

    @Test
    fun testQueenCanDevolveToPawn() {
        val probs = MutationProbabilities()
        val rng = ChaosRandom(42L)

        var pawnMutations = 0
        val totalRolls = 1000
        for (i in 0 until totalRolls) {
            val (result, _) = probs.selectPieceType(rng.nextDouble() * 100.0)
            if (result == PieceType.PAWN) {
                pawnMutations++
            }
        }

        // Expected pawn rate is 35% ~ 350 out of 1000
        assertTrue("Pawn mutations should occur frequently ($pawnMutations / $totalRolls)", pawnMutations > 250)
    }

    @Test
    fun testCaptureTriggersMutationInEngine() {
        val config = CustomChaosConfig.forMode(GameMode.MUTATION)
        val engine = ChaosEngine(mode = GameMode.MUTATION, config = config, seed = 12345L)

        // Setup a simple capture: White Knight takes Black Pawn
        val whiteKnightPos = Position.fromAlgebraic("b1")
        val blackPawnPos = Position.fromAlgebraic("c3")

        // Put a black pawn at c3
        val updatedBoard = engine.state.board.withPiecePlaced(
            Piece(id = "bp_c3", owner = PieceColor.BLACK, baseType = PieceType.PAWN, position = blackPawnPos)
        )
        engine.setBoardForTesting(updatedBoard)

        val captureMove = engine.getLegalMoves().find { it.from == whiteKnightPos && it.to == blackPawnPos }
        assertNotNull("Knight capture move should be available", captureMove)

        val executed = engine.executeMove(captureMove!!)
        assertNotNull("Mutation result should be generated on capture", executed.mutationResult)

        val pieceAtC3 = engine.state.board.pieceAt(blackPawnPos)
        assertNotNull(pieceAtC3)
        assertEquals("Piece at dest square must reflect the mutation result", executed.mutationResult, pieceAtC3?.currentType)
    }
}
