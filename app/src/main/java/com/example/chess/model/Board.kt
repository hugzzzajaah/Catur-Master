package com.example.chess.model

class Board private constructor(
  private val grid: Array<Array<Piece?>>
) {
  constructor() : this(Array(8) { arrayOfNulls<Piece>(8) })

  operator fun get(file: Int, rank: Int): Piece? = grid[rank][file]
  operator fun get(square: Square): Piece? = grid[square.rank][square.file]

  operator fun set(file: Int, rank: Int, piece: Piece?) {
    grid[rank][file] = piece
  }

  operator fun set(square: Square, piece: Piece?) {
    grid[square.rank][square.file] = piece
  }

  fun copy(): Board {
    val newGrid = Array(8) { r ->
      Array(8) { f ->
        grid[r][f]?.copy()
      }
    }
    return Board(newGrid)
  }

  fun findKing(color: PieceColor): Square? {
    for (r in 0..7) {
      for (f in 0..7) {
        val p = grid[r][f]
        if (p?.type == PieceType.KING && p.color == color) {
          return Square(f, r)
        }
      }
    }
    return null
  }

  fun getAllPieces(color: PieceColor): List<Pair<Square, Piece>> {
    val result = mutableListOf<Pair<Square, Piece>>()
    for (r in 0..7) {
      for (f in 0..7) {
        val p = grid[r][f]
        if (p != null && p.color == color) {
          result.add(Square(f, r) to p)
        }
      }
    }
    return result
  }

  fun getAllSquares(): List<Pair<Square, Piece?>> {
    val result = mutableListOf<Pair<Square, Piece?>>()
    for (r in 0..7) {
      for (f in 0..7) {
        result.add(Square(f, r) to grid[r][f])
      }
    }
    return result
  }

  companion object {
    fun initialBoard(): Board {
      val board = Board()

      // White Pawns & Major pieces
      for (f in 0..7) {
        board[f, 1] = Piece(PieceType.PAWN, PieceColor.WHITE)
      }
      board[0, 0] = Piece(PieceType.ROOK, PieceColor.WHITE)
      board[1, 0] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
      board[2, 0] = Piece(PieceType.BISHOP, PieceColor.WHITE)
      board[3, 0] = Piece(PieceType.QUEEN, PieceColor.WHITE)
      board[4, 0] = Piece(PieceType.KING, PieceColor.WHITE)
      board[5, 0] = Piece(PieceType.BISHOP, PieceColor.WHITE)
      board[6, 0] = Piece(PieceType.KNIGHT, PieceColor.WHITE)
      board[7, 0] = Piece(PieceType.ROOK, PieceColor.WHITE)

      // Black Pawns & Major pieces
      for (f in 0..7) {
        board[f, 6] = Piece(PieceType.PAWN, PieceColor.BLACK)
      }
      board[0, 7] = Piece(PieceType.ROOK, PieceColor.BLACK)
      board[1, 7] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
      board[2, 7] = Piece(PieceType.BISHOP, PieceColor.BLACK)
      board[3, 7] = Piece(PieceType.QUEEN, PieceColor.BLACK)
      board[4, 7] = Piece(PieceType.KING, PieceColor.BLACK)
      board[5, 7] = Piece(PieceType.BISHOP, PieceColor.BLACK)
      board[6, 7] = Piece(PieceType.KNIGHT, PieceColor.BLACK)
      board[7, 7] = Piece(PieceType.ROOK, PieceColor.BLACK)

      return board
    }
  }
}
