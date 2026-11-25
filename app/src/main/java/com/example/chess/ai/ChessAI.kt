package com.example.chess.ai

import com.example.chess.model.Board
import com.example.chess.model.ChessEngine
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import kotlin.random.Random

object ChessAI {

  // Piece-Square Tables (PST) from White's perspective [rank 0..7][file 0..7]
  private val PAWN_TABLE = arrayOf(
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(50, 50, 50, 50, 50, 50, 50, 50),
    intArrayOf(10, 10, 20, 30, 30, 20, 10, 10),
    intArrayOf(5, 5, 10, 25, 25, 10, 5, 5),
    intArrayOf(0, 0, 0, 20, 20, 0, 0, 0),
    intArrayOf(5, -5, -10, 0, 0, -10, -5, 5),
    intArrayOf(5, 10, 10, -20, -20, 10, 10, 5),
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
  )

  private val KNIGHT_TABLE = arrayOf(
    intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50),
    intArrayOf(-40, -20, 0, 0, 0, 0, -20, -40),
    intArrayOf(-30, 0, 10, 15, 15, 10, 0, -30),
    intArrayOf(-30, 5, 15, 20, 20, 15, 5, -30),
    intArrayOf(-30, 0, 15, 20, 20, 15, 0, -30),
    intArrayOf(-30, 5, 10, 15, 15, 10, 5, -30),
    intArrayOf(-40, -20, 0, 5, 5, 0, -20, -40),
    intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50)
  )

  private val BISHOP_TABLE = arrayOf(
    intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20),
    intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
    intArrayOf(-10, 0, 5, 10, 10, 5, 0, -10),
    intArrayOf(-10, 5, 5, 10, 10, 5, 5, -10),
    intArrayOf(-10, 0, 10, 10, 10, 10, 0, -10),
    intArrayOf(-10, 10, 10, 10, 10, 10, 10, -10),
    intArrayOf(-10, 5, 0, 0, 0, 0, 5, -10),
    intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20)
  )

  private val ROOK_TABLE = arrayOf(
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(5, 10, 10, 10, 10, 10, 10, 5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
    intArrayOf(0, 0, 0, 5, 5, 0, 0, 0)
  )

  private val QUEEN_TABLE = arrayOf(
    intArrayOf(-20, -10, -10, -5, -5, -10, -10, -20),
    intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
    intArrayOf(-10, 0, 5, 5, 5, 5, 0, -10),
    intArrayOf(-5, 0, 5, 5, 5, 5, 0, -5),
    intArrayOf(0, 0, 5, 5, 5, 5, 0, -5),
    intArrayOf(-10, 5, 5, 5, 5, 5, 0, -10),
    intArrayOf(-10, 0, 5, 0, 0, 0, 0, -10),
    intArrayOf(-20, -10, -10, -5, -5, -10, -10, -20)
  )

  private val KING_MID_TABLE = arrayOf(
    intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
    intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
    intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
    intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
    intArrayOf(-20, -30, -30, -40, -40, -30, -30, -20),
    intArrayOf(-10, -20, -20, -20, -20, -20, -20, -10),
    intArrayOf(20, 20, 0, 0, 0, 0, 20, 20),
    intArrayOf(20, 30, 10, 0, 0, 10, 30, 20)
  )

  fun evaluate(state: GameState): Int {
    if (state.isCheckmate) {
      return if (state.winner == PieceColor.WHITE) 100000 else -100000
    }
    if (state.isDraw) return 0

    var score = 0
    val board = state.board

    for (r in 0..7) {
      for (f in 0..7) {
        val p = board[f, r] ?: continue
        val baseVal = p.type.baseValue
        val positionalVal = getPositionalValue(p, f, r)
        val pieceScore = baseVal + positionalVal

        if (p.isWhite) {
          score += pieceScore
        } else {
          score -= pieceScore
        }
      }
    }
    return score
  }

  private fun getPositionalValue(piece: Piece, file: Int, rank: Int): Int {
    val tableRank = if (piece.isWhite) 7 - rank else rank
    val tableFile = file
    return when (piece.type) {
      PieceType.PAWN -> PAWN_TABLE[tableRank][tableFile]
      PieceType.KNIGHT -> KNIGHT_TABLE[tableRank][tableFile]
      PieceType.BISHOP -> BISHOP_TABLE[tableRank][tableFile]
      PieceType.ROOK -> ROOK_TABLE[tableRank][tableFile]
      PieceType.QUEEN -> QUEEN_TABLE[tableRank][tableFile]
      PieceType.KING -> KING_MID_TABLE[tableRank][tableFile]
    }
  }

  fun findBestMove(
    state: GameState,
    depth: Int,
    randomnessChance: Float
  ): Move? {
    val legalMoves = ChessEngine.generateLegalMoves(state)
    if (legalMoves.isEmpty()) return null

    // Inject deliberate randomness if random threshold met (for lower difficulties)
    if (randomnessChance > 0f && Random.nextFloat() < randomnessChance) {
      val sortedMoves = legalMoves.shuffled()
      // Pick among top 3 or random reasonable moves
      return sortedMoves.first()
    }

    val isMaximizing = state.activeColor == PieceColor.WHITE
    var bestMove: Move? = null
    var bestEval = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE

    // Sort moves: captures & checks first for faster pruning
    val orderedMoves = orderMoves(legalMoves)

    for (move in orderedMoves) {
      val nextState = ChessEngine.applyMove(state, move)
      val eval = alphaBeta(
        state = nextState,
        depth = depth - 1,
        alpha = if (isMaximizing) bestEval else Int.MIN_VALUE,
        beta = if (isMaximizing) Int.MAX_VALUE else bestEval,
        isMaximizing = !isMaximizing
      )

      if (isMaximizing) {
        if (eval > bestEval) {
          bestEval = eval
          bestMove = move
        }
      } else {
        if (eval < bestEval) {
          bestEval = eval
          bestMove = move
        }
      }
    }

    return bestMove ?: legalMoves.random()
  }

  private fun alphaBeta(
    state: GameState,
    depth: Int,
    alpha: Int,
    beta: Int,
    isMaximizing: Boolean
  ): Int {
    if (depth <= 0 || state.isGameOver) {
      return evaluate(state)
    }

    val legalMoves = ChessEngine.generateLegalMoves(state)
    if (legalMoves.isEmpty()) {
      return evaluate(state)
    }

    var currentAlpha = alpha
    var currentBeta = beta
    val orderedMoves = orderMoves(legalMoves)

    if (isMaximizing) {
      var maxEval = Int.MIN_VALUE
      for (move in orderedMoves) {
        val nextState = ChessEngine.applyMove(state, move)
        val eval = alphaBeta(nextState, depth - 1, currentAlpha, currentBeta, false)
        maxEval = Math.max(maxEval, eval)
        currentAlpha = Math.max(currentAlpha, eval)
        if (currentBeta <= currentAlpha) break
      }
      return maxEval
    } else {
      var minEval = Int.MAX_VALUE
      for (move in orderedMoves) {
        val nextState = ChessEngine.applyMove(state, move)
        val eval = alphaBeta(nextState, depth - 1, currentAlpha, currentBeta, true)
        minEval = Math.min(minEval, eval)
        currentBeta = Math.min(currentBeta, eval)
        if (currentBeta <= currentAlpha) break
      }
      return minEval
    }
  }

  private fun orderMoves(moves: List<Move>): List<Move> {
    return moves.sortedByDescending { move ->
      var score = 0
      if (move.capturedPiece != null) {
        // MVV-LVA: Most Valuable Victim - Least Valuable Attacker
        val victimVal = move.capturedPiece.type.baseValue
        score += victimVal * 10
      }
      if (move.promotion != null) {
        score += move.promotion.baseValue * 5
      }
      if (move.isCastling) {
        score += 50
      }
      score
    }
  }
}
