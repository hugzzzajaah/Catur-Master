package com.example.chess.model

data class Move(
  val from: Square,
  val to: Square,
  val promotion: PieceType? = null,
  val isEnPassant: Boolean = false,
  val isCastling: Boolean = false,
  val capturedPiece: Piece? = null,
  var san: String = ""
) {
  val isCapture: Boolean
    get() = capturedPiece != null || isEnPassant
}

data class CastlingRights(
  val whiteKingside: Boolean = true,
  val whiteQueenside: Boolean = true,
  val blackKingside: Boolean = true,
  val blackQueenside: Boolean = true
) {
  fun copyForColor(color: PieceColor, kingside: Boolean, queenside: Boolean): CastlingRights {
    return if (color == PieceColor.WHITE) {
      this.copy(whiteKingside = kingside, whiteQueenside = queenside)
    } else {
      this.copy(blackKingside = kingside, blackQueenside = queenside)
    }
  }
}
