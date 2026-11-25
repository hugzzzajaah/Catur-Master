package com.example.chess.ui.theme

import androidx.compose.ui.graphics.Color

enum class BoardTheme(
  val displayName: String,
  val lightSquare: Color,
  val darkSquare: Color,
  val selectedColor: Color = Color(0x80F59E0B),
  val lastMoveColor: Color = Color(0x60FBBF24),
  val legalMoveColor: Color = Color(0x6610B981),
  val captureColor: Color = Color(0x80EF4444),
  val checkColor: Color = Color(0x99DC2626)
) {
  KAYU_KLASIK(
    displayName = "Kayu Klasik",
    lightSquare = Color(0xFFF0D9B5),
    darkSquare = Color(0xFFB58863),
    selectedColor = Color(0x99F59E0B),
    lastMoveColor = Color(0x66FCD34D)
  ),
  ZAMRUD_TURNAMEN(
    displayName = "Zamrud Turnamen",
    lightSquare = Color(0xFFEEEED2),
    darkSquare = Color(0xFF769656),
    selectedColor = Color(0x99FACC15),
    lastMoveColor = Color(0x66FDE047)
  ),
  BIRU_MODERN(
    displayName = "Biru Modern",
    lightSquare = Color(0xFFDEE3E6),
    darkSquare = Color(0xFF5B84A6),
    selectedColor = Color(0x9938BDF8),
    lastMoveColor = Color(0x667DD3FC)
  ),
  MIDNIGHT_SLATE(
    displayName = "Midnight Slate",
    lightSquare = Color(0xFF94A3B8),
    darkSquare = Color(0xFF334155),
    selectedColor = Color(0x99A855F7),
    lastMoveColor = Color(0x66C084FC)
  )
}
