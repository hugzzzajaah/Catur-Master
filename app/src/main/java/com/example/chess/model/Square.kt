package com.example.chess.model

data class Square(
  val file: Int, // 0..7 (a..h)
  val rank: Int  // 0..7 (1..8)
) {
  init {
    require(isValid(file, rank)) { "Invalid square coordinates: file=$file, rank=$rank" }
  }

  val name: String
    get() = "${('a' + file)}${rank + 1}"

  val isLightSquare: Boolean
    get() = (file + rank) % 2 != 0

  companion object {
    fun isValid(file: Int, rank: Int): Boolean = file in 0..7 && rank in 0..7

    fun fromName(name: String): Square {
      require(name.length == 2) { "Invalid square name: $name" }
      val file = name[0].lowercaseChar() - 'a'
      val rank = name[1].digitToInt() - 1
      return Square(file, rank)
    }
  }
}
