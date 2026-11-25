package com.example.chess.model

enum class PieceType(val symbol: String, val baseValue: Int) {
  PAWN("P", 100),
  KNIGHT("N", 320),
  BISHOP("B", 330),
  ROOK("R", 500),
  QUEEN("Q", 900),
  KING("K", 20000)
}

enum class PieceColor {
  WHITE,
  BLACK;

  fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}

data class Piece(
  val type: PieceType,
  val color: PieceColor
) {
  val isWhite: Boolean get() = color == PieceColor.WHITE
  val isBlack: Boolean get() = color == PieceColor.BLACK

  fun charRepresentation(): Char {
    val char = when (type) {
      PieceType.PAWN -> 'p'
      PieceType.KNIGHT -> 'n'
      PieceType.BISHOP -> 'b'
      PieceType.ROOK -> 'r'
      PieceType.QUEEN -> 'q'
      PieceType.KING -> 'k'
    }
    return if (isWhite) char.uppercaseChar() else char
  }
}
