package com.example

import com.example.chess.ai.ChessAI
import com.example.chess.model.ChessEngine
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun initialChessPositionHas20LegalMoves() {
    val engine = ChessEngine()
    val legalMoves = engine.getLegalMoves()
    // 8 pawns (1 or 2 steps = 16) + 2 knights (2 moves each = 4) = 20 legal moves
    assertEquals(20, legalMoves.size)
  }

  @Test
  fun chessAiFindsValidOpeningMove() {
    val engine = ChessEngine()
    val move = ChessAI.findBestMove(engine.currentState, depth = 1, randomnessChance = 0f)
    assertNotNull(move)
    assertTrue(engine.getLegalMoves().any { it.from == move!!.from && it.to == move.to })
  }

  @Test
  fun moveExecutionUpdatesActiveTurn() {
    val engine = ChessEngine()
    val e2 = Square.fromName("e2")
    val e4 = Square.fromName("e4")
    val move = Move(from = e2, to = e4)
    val success = engine.makeMove(move)
    assertTrue("Move should succeed", success)
    assertEquals(PieceColor.BLACK, engine.currentState.activeColor)
    assertEquals(1, engine.currentState.moveHistory.size)
  }
}
