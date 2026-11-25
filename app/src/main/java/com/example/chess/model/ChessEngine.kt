package com.example.chess.model

data class GameState(
  val board: Board,
  val activeColor: PieceColor = PieceColor.WHITE,
  val castlingRights: CastlingRights = CastlingRights(),
  val enPassantSquare: Square? = null,
  val halfmoveClock: Int = 0,
  val fullmoveNumber: Int = 1,
  val moveHistory: List<Move> = emptyList(),
  val capturedByWhite: List<Piece> = emptyList(),
  val capturedByBlack: List<Piece> = emptyList(),
  val isCheck: Boolean = false,
  val isCheckmate: Boolean = false,
  val isStalemate: Boolean = false,
  val isDraw: Boolean = false,
  val isGameOver: Boolean = false,
  val winner: PieceColor? = null
)

class ChessEngine(
  initialState: GameState? = null
) {
  var currentState: GameState = initialState ?: GameState(board = Board.initialBoard())
    private set

  private val undoStack = mutableListOf<GameState>()
  private val redoStack = mutableListOf<GameState>()

  fun reset(initialState: GameState? = null) {
    currentState = initialState ?: GameState(board = Board.initialBoard())
    undoStack.clear()
    redoStack.clear()
  }

  fun getLegalMoves(): List<Move> {
    return generateLegalMoves(currentState)
  }

  fun getLegalMovesForSquare(square: Square): List<Move> {
    val piece = currentState.board[square] ?: return emptyList()
    if (piece.color != currentState.activeColor) return emptyList()
    return getLegalMoves().filter { it.from == square }
  }

  fun makeMove(move: Move): Boolean {
    val legalMoves = getLegalMoves()
    val matched = legalMoves.find {
      it.from == move.from && it.to == move.to &&
        (it.promotion == null || it.promotion == move.promotion)
    } ?: return false

    // Save previous state for undo
    undoStack.add(currentState)
    redoStack.clear()

    currentState = applyMove(currentState, matched)
    return true
  }

  fun canUndo(): Boolean = undoStack.isNotEmpty()
  fun canRedo(): Boolean = redoStack.isNotEmpty()

  fun undo(): Boolean {
    if (undoStack.isEmpty()) return false
    redoStack.add(currentState)
    currentState = undoStack.removeAt(undoStack.lastIndex)
    return true
  }

  fun redo(): Boolean {
    if (redoStack.isEmpty()) return false
    undoStack.add(currentState)
    currentState = redoStack.removeAt(redoStack.lastIndex)
    return true
  }

  companion object {
    fun generateLegalMoves(state: GameState): List<Move> {
      if (state.isGameOver) return emptyList()
      val pseudoMoves = generatePseudoLegalMoves(state)
      val legalMoves = mutableListOf<Move>()

      for (move in pseudoMoves) {
        val nextBoard = state.board.copy()
        applyMoveToBoard(nextBoard, move, state.enPassantSquare)
        val kingSq = nextBoard.findKing(state.activeColor)
        if (kingSq != null && !isSquareAttacked(nextBoard, kingSq, state.activeColor.opposite())) {
          legalMoves.add(move)
        }
      }

      // Compute SAN notation for legal moves
      for (move in legalMoves) {
        if (move.san.isEmpty()) {
          move.san = generateSan(state, move, legalMoves)
        }
      }

      return legalMoves
    }

    fun isKingInCheck(board: Board, color: PieceColor): Boolean {
      val kingSq = board.findKing(color) ?: return false
      return isSquareAttacked(board, kingSq, color.opposite())
    }

    fun isSquareAttacked(board: Board, target: Square, byColor: PieceColor): Boolean {
      // 1. Pawn attacks
      val pawnRank = if (byColor == PieceColor.WHITE) target.rank - 1 else target.rank + 1
      if (pawnRank in 0..7) {
        for (f in listOf(target.file - 1, target.file + 1)) {
          if (f in 0..7) {
            val p = board[f, pawnRank]
            if (p?.type == PieceType.PAWN && p.color == byColor) return true
          }
        }
      }

      // 2. Knight attacks
      val knightOffsets = listOf(
        -2 to -1, -2 to 1, -1 to -2, -1 to 2,
        1 to -2, 1 to 2, 2 to -1, 2 to 1
      )
      for ((df, dr) in knightOffsets) {
        val f = target.file + df
        val r = target.rank + dr
        if (Square.isValid(f, r)) {
          val p = board[f, r]
          if (p?.type == PieceType.KNIGHT && p.color == byColor) return true
        }
      }

      // 3. Bishop / Queen diagonal attacks
      val diagDirs = listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)
      for ((df, dr) in diagDirs) {
        var f = target.file + df
        var r = target.rank + dr
        while (Square.isValid(f, r)) {
          val p = board[f, r]
          if (p != null) {
            if (p.color == byColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) {
              return true
            }
            break
          }
          f += df
          r += dr
        }
      }

      // 4. Rook / Queen straight attacks
      val straightDirs = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
      for ((df, dr) in straightDirs) {
        var f = target.file + df
        var r = target.rank + dr
        while (Square.isValid(f, r)) {
          val p = board[f, r]
          if (p != null) {
            if (p.color == byColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) {
              return true
            }
            break
          }
          f += df
          r += dr
        }
      }

      // 5. King attacks
      for (df in -1..1) {
        for (dr in -1..1) {
          if (df == 0 && dr == 0) continue
          val f = target.file + df
          val r = target.rank + dr
          if (Square.isValid(f, r)) {
            val p = board[f, r]
            if (p?.type == PieceType.KING && p.color == byColor) return true
          }
        }
      }

      return false
    }

    private fun generatePseudoLegalMoves(state: GameState): List<Move> {
      val moves = mutableListOf<Move>()
      val board = state.board
      val color = state.activeColor
      val pieces = board.getAllPieces(color)

      for ((square, piece) in pieces) {
        when (piece.type) {
          PieceType.PAWN -> generatePawnMoves(state, square, piece, moves)
          PieceType.KNIGHT -> generateKnightMoves(board, square, piece, moves)
          PieceType.BISHOP -> generateSlidingMoves(board, square, piece, listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1), moves)
          PieceType.ROOK -> generateSlidingMoves(board, square, piece, listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1), moves)
          PieceType.QUEEN -> generateSlidingMoves(board, square, piece, listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1, 1 to 0, -1 to 0, 0 to 1, 0 to -1), moves)
          PieceType.KING -> generateKingMoves(state, square, piece, moves)
        }
      }
      return moves
    }

    private fun generatePawnMoves(state: GameState, sq: Square, piece: Piece, out: MutableList<Move>) {
      val dir = if (piece.isWhite) 1 else -1
      val startRank = if (piece.isWhite) 1 else 6
      val promoRank = if (piece.isWhite) 7 else 0
      val board = state.board

      // 1 step forward
      val fwdRank = sq.rank + dir
      if (Square.isValid(sq.file, fwdRank) && board[sq.file, fwdRank] == null) {
        val dest = Square(sq.file, fwdRank)
        if (fwdRank == promoRank) {
          listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT).forEach { promo ->
            out.add(Move(sq, dest, promotion = promo))
          }
        } else {
          out.add(Move(sq, dest))

          // 2 steps forward from start rank
          if (sq.rank == startRank) {
            val doubleRank = sq.rank + 2 * dir
            if (board[sq.file, doubleRank] == null) {
              out.add(Move(sq, Square(sq.file, doubleRank)))
            }
          }
        }
      }

      // Diagonal captures
      for (df in listOf(-1, 1)) {
        val capFile = sq.file + df
        val capRank = sq.rank + dir
        if (Square.isValid(capFile, capRank)) {
          val dest = Square(capFile, capRank)
          val targetPiece = board[dest]
          if (targetPiece != null && targetPiece.color != piece.color) {
            if (capRank == promoRank) {
              listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT).forEach { promo ->
                out.add(Move(sq, dest, promotion = promo, capturedPiece = targetPiece))
              }
            } else {
              out.add(Move(sq, dest, capturedPiece = targetPiece))
            }
          } else if (targetPiece == null && state.enPassantSquare == dest) {
            // En passant
            val epCapturedPiece = board[capFile, sq.rank]
            out.add(Move(sq, dest, isEnPassant = true, capturedPiece = epCapturedPiece))
          }
        }
      }
    }

    private fun generateKnightMoves(board: Board, sq: Square, piece: Piece, out: MutableList<Move>) {
      val offsets = listOf(
        -2 to -1, -2 to 1, -1 to -2, -1 to 2,
        1 to -2, 1 to 2, 2 to -1, 2 to 1
      )
      for ((df, dr) in offsets) {
        val f = sq.file + df
        val r = sq.rank + dr
        if (Square.isValid(f, r)) {
          val target = board[f, r]
          if (target == null) {
            out.add(Move(sq, Square(f, r)))
          } else if (target.color != piece.color) {
            out.add(Move(sq, Square(f, r), capturedPiece = target))
          }
        }
      }
    }

    private fun generateSlidingMoves(
      board: Board,
      sq: Square,
      piece: Piece,
      directions: List<Pair<Int, Int>>,
      out: MutableList<Move>
    ) {
      for ((df, dr) in directions) {
        var f = sq.file + df
        var r = sq.rank + dr
        while (Square.isValid(f, r)) {
          val target = board[f, r]
          if (target == null) {
            out.add(Move(sq, Square(f, r)))
          } else {
            if (target.color != piece.color) {
              out.add(Move(sq, Square(f, r), capturedPiece = target))
            }
            break
          }
          f += df
          r += dr
        }
      }
    }

    private fun generateKingMoves(state: GameState, sq: Square, piece: Piece, out: MutableList<Move>) {
      val board = state.board
      for (df in -1..1) {
        for (dr in -1..1) {
          if (df == 0 && dr == 0) continue
          val f = sq.file + df
          val r = sq.rank + dr
          if (Square.isValid(f, r)) {
            val target = board[f, r]
            if (target == null) {
              out.add(Move(sq, Square(f, r)))
            } else if (target.color != piece.color) {
              out.add(Move(sq, Square(f, r), capturedPiece = target))
            }
          }
        }
      }

      // Castling
      if (piece.isWhite && sq == Square(4, 0)) {
        if (!isKingInCheck(board, PieceColor.WHITE)) {
          // Kingside: e1 -> g1 (f1, g1 empty, f1, g1 not attacked)
          if (state.castlingRights.whiteKingside &&
            board[5, 0] == null && board[6, 0] == null &&
            board[7, 0]?.type == PieceType.ROOK && board[7, 0]?.color == PieceColor.WHITE &&
            !isSquareAttacked(board, Square(5, 0), PieceColor.BLACK) &&
            !isSquareAttacked(board, Square(6, 0), PieceColor.BLACK)
          ) {
            out.add(Move(sq, Square(6, 0), isCastling = true))
          }
          // Queenside: e1 -> c1 (d1, c1, b1 empty, d1, c1 not attacked)
          if (state.castlingRights.whiteQueenside &&
            board[1, 0] == null && board[2, 0] == null && board[3, 0] == null &&
            board[0, 0]?.type == PieceType.ROOK && board[0, 0]?.color == PieceColor.WHITE &&
            !isSquareAttacked(board, Square(3, 0), PieceColor.BLACK) &&
            !isSquareAttacked(board, Square(2, 0), PieceColor.BLACK)
          ) {
            out.add(Move(sq, Square(2, 0), isCastling = true))
          }
        }
      } else if (piece.isBlack && sq == Square(4, 7)) {
        if (!isKingInCheck(board, PieceColor.BLACK)) {
          // Kingside: e8 -> g8
          if (state.castlingRights.blackKingside &&
            board[5, 7] == null && board[6, 7] == null &&
            board[7, 7]?.type == PieceType.ROOK && board[7, 7]?.color == PieceColor.BLACK &&
            !isSquareAttacked(board, Square(5, 7), PieceColor.WHITE) &&
            !isSquareAttacked(board, Square(6, 7), PieceColor.WHITE)
          ) {
            out.add(Move(sq, Square(6, 7), isCastling = true))
          }
          // Queenside: e8 -> c8
          if (state.castlingRights.blackQueenside &&
            board[1, 7] == null && board[2, 7] == null && board[3, 7] == null &&
            board[0, 7]?.type == PieceType.ROOK && board[0, 7]?.color == PieceColor.BLACK &&
            !isSquareAttacked(board, Square(3, 7), PieceColor.WHITE) &&
            !isSquareAttacked(board, Square(2, 7), PieceColor.WHITE)
          ) {
            out.add(Move(sq, Square(2, 7), isCastling = true))
          }
        }
      }
    }

    fun applyMoveToBoard(board: Board, move: Move, enPassantSquare: Square?) {
      val movingPiece = board[move.from] ?: return

      // Handle Castling
      if (move.isCastling) {
        board[move.from] = null
        board[move.to] = movingPiece
        val rank = move.from.rank
        if (move.to.file == 6) { // Kingside
          val rook = board[7, rank]
          board[7, rank] = null
          board[5, rank] = rook
        } else if (move.to.file == 2) { // Queenside
          val rook = board[0, rank]
          board[0, rank] = null
          board[3, rank] = rook
        }
        return
      }

      // Handle En Passant capture
      if (move.isEnPassant) {
        board[move.from] = null
        board[move.to] = movingPiece
        // Captured pawn is at [move.to.file, move.from.rank]
        board[move.to.file, move.from.rank] = null
        return
      }

      // Standard move / Promotion
      board[move.from] = null
      if (move.promotion != null) {
        board[move.to] = Piece(move.promotion, movingPiece.color)
      } else {
        board[move.to] = movingPiece
      }
    }

    fun applyMove(state: GameState, move: Move): GameState {
      val newBoard = state.board.copy()
      val movingPiece = state.board[move.from] ?: return state
      val captured = move.capturedPiece

      applyMoveToBoard(newBoard, move, state.enPassantSquare)

      // Update captured piece lists
      val newCapByWhite = state.capturedByWhite.toMutableList()
      val newCapByBlack = state.capturedByBlack.toMutableList()
      if (captured != null) {
        if (movingPiece.isWhite) {
          newCapByWhite.add(captured)
        } else {
          newCapByBlack.add(captured)
        }
      }

      // Castling rights update
      var castling = state.castlingRights
      if (movingPiece.type == PieceType.KING) {
        castling = if (movingPiece.isWhite) {
          castling.copy(whiteKingside = false, whiteQueenside = false)
        } else {
          castling.copy(blackKingside = false, blackQueenside = false)
        }
      } else if (movingPiece.type == PieceType.ROOK) {
        if (move.from == Square(0, 0)) castling = castling.copy(whiteQueenside = false)
        if (move.from == Square(7, 0)) castling = castling.copy(whiteKingside = false)
        if (move.from == Square(0, 7)) castling = castling.copy(blackQueenside = false)
        if (move.from == Square(7, 7)) castling = castling.copy(blackKingside = false)
      }
      // If a rook was captured on original square
      if (move.to == Square(0, 0)) castling = castling.copy(whiteQueenside = false)
      if (move.to == Square(7, 0)) castling = castling.copy(whiteKingside = false)
      if (move.to == Square(0, 7)) castling = castling.copy(blackQueenside = false)
      if (move.to == Square(7, 7)) castling = castling.copy(blackKingside = false)

      // En passant target square calculation
      val newEnPassant = if (movingPiece.type == PieceType.PAWN && Math.abs(move.to.rank - move.from.rank) == 2) {
        Square(move.from.file, (move.from.rank + move.to.rank) / 2)
      } else {
        null
      }

      // Halfmove clock for 50-move rule
      val newHalfmove = if (movingPiece.type == PieceType.PAWN || captured != null) 0 else state.halfmoveClock + 1

      // Next active color
      val nextColor = state.activeColor.opposite()
      val newFullmove = if (state.activeColor == PieceColor.BLACK) state.fullmoveNumber + 1 else state.fullmoveNumber

      // Check next state legal moves and check status
      val nextKingInCheck = isKingInCheck(newBoard, nextColor)

      val intermediateState = GameState(
        board = newBoard,
        activeColor = nextColor,
        castlingRights = castling,
        enPassantSquare = newEnPassant,
        halfmoveClock = newHalfmove,
        fullmoveNumber = newFullmove,
        moveHistory = state.moveHistory + move,
        capturedByWhite = newCapByWhite,
        capturedByBlack = newCapByBlack,
        isCheck = nextKingInCheck
      )

      val legalMovesForNext = generateLegalMoves(intermediateState)
      val hasNoLegalMoves = legalMovesForNext.isEmpty()

      val isCheckmate = hasNoLegalMoves && nextKingInCheck
      val isStalemate = hasNoLegalMoves && !nextKingInCheck
      val is50MoveRule = newHalfmove >= 100
      val isInsufficientMaterial = checkInsufficientMaterial(newBoard)

      val isGameOver = isCheckmate || isStalemate || is50MoveRule || isInsufficientMaterial
      val winner = if (isCheckmate) state.activeColor else null

      return intermediateState.copy(
        isCheckmate = isCheckmate,
        isStalemate = isStalemate,
        isDraw = isStalemate || is50MoveRule || isInsufficientMaterial,
        isGameOver = isGameOver,
        winner = winner
      )
    }

    private fun checkInsufficientMaterial(board: Board): Boolean {
      val whitePieces = board.getAllPieces(PieceColor.WHITE).map { it.second.type }
      val blackPieces = board.getAllPieces(PieceColor.BLACK).map { it.second.type }

      // King vs King
      if (whitePieces == listOf(PieceType.KING) && blackPieces == listOf(PieceType.KING)) return true

      // King + Minor vs King
      if (whitePieces == listOf(PieceType.KING) &&
        (blackPieces == listOf(PieceType.KING, PieceType.BISHOP) || blackPieces == listOf(PieceType.BISHOP, PieceType.KING) ||
         blackPieces == listOf(PieceType.KING, PieceType.KNIGHT) || blackPieces == listOf(PieceType.KNIGHT, PieceType.KING))
      ) return true

      if (blackPieces == listOf(PieceType.KING) &&
        (whitePieces == listOf(PieceType.KING, PieceType.BISHOP) || whitePieces == listOf(PieceType.BISHOP, PieceType.KING) ||
         whitePieces == listOf(PieceType.KING, PieceType.KNIGHT) || whitePieces == listOf(PieceType.KNIGHT, PieceType.KING))
      ) return true

      return false
    }

    private fun generateSan(state: GameState, move: Move, allLegalMoves: List<Move>): String {
      if (move.isCastling) {
        return if (move.to.file == 6) "O-O" else "O-O-O"
      }

      val piece = state.board[move.from] ?: return "${move.from.name}${move.to.name}"
      val sb = StringBuilder()

      if (piece.type == PieceType.PAWN) {
        if (move.isCapture) {
          sb.append(('a' + move.from.file))
          sb.append("x")
        }
        sb.append(move.to.name)
        if (move.promotion != null) {
          sb.append("=").append(move.promotion.symbol)
        }
      } else {
        sb.append(piece.type.symbol)

        // Disambiguation if multiple pieces of same type can move to same square
        val conflicts = allLegalMoves.filter {
          it != move && it.to == move.to && state.board[it.from]?.type == piece.type && state.board[it.from]?.color == piece.color
        }
        if (conflicts.isNotEmpty()) {
          val sameFile = conflicts.any { it.from.file == move.from.file }
          val sameRank = conflicts.any { it.from.rank == move.from.rank }
          if (!sameFile) {
            sb.append(('a' + move.from.file))
          } else if (!sameRank) {
            sb.append((move.from.rank + 1))
          } else {
            sb.append(move.from.name)
          }
        }

        if (move.isCapture) {
          sb.append("x")
        }
        sb.append(move.to.name)
      }

      // Check or checkmate indicator
      val testBoard = state.board.copy()
      applyMoveToBoard(testBoard, move, state.enPassantSquare)
      val oppColor = state.activeColor.opposite()
      if (isKingInCheck(testBoard, oppColor)) {
        // Test if checkmate
        val tempState = GameState(
          board = testBoard,
          activeColor = oppColor,
          enPassantSquare = null
        )
        val replies = generateLegalMoves(tempState)
        if (replies.isEmpty()) {
          sb.append("#")
        } else {
          sb.append("+")
        }
      }

      return sb.toString()
    }
  }
}
