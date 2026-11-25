package com.example.chess.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType

@Composable
fun ChessPieceView(
  piece: Piece,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val isWhite = piece.color == PieceColor.WHITE
      val fillColor = if (isWhite) Color(0xFFF8FAFC) else Color(0xFF1E293B)
      val strokeColor = if (isWhite) Color(0xFF1E293B) else Color(0xFFE2E8F0)
      val accentColor = if (isWhite) Color(0xFFCBD5E1) else Color(0xFF334155)

      when (piece.type) {
        PieceType.PAWN -> drawPawn(fillColor, strokeColor, accentColor)
        PieceType.KNIGHT -> drawKnight(fillColor, strokeColor, accentColor, isWhite)
        PieceType.BISHOP -> drawBishop(fillColor, strokeColor, accentColor)
        PieceType.ROOK -> drawRook(fillColor, strokeColor, accentColor)
        PieceType.QUEEN -> drawQueen(fillColor, strokeColor, accentColor)
        PieceType.KING -> drawKing(fillColor, strokeColor, accentColor)
      }
    }
  }
}

private fun DrawScope.drawPawn(fill: Color, stroke: Color, accent: Color) {
  val w = size.width
  val h = size.height
  val strokeWidth = (w * 0.045f).coerceAtLeast(1.5f)

  // Base
  val basePath = Path().apply {
    moveTo(w * 0.25f, h * 0.85f)
    lineTo(w * 0.75f, h * 0.85f)
    lineTo(w * 0.80f, h * 0.90f)
    lineTo(w * 0.20f, h * 0.90f)
    close()
  }
  drawPath(basePath, fill)
  drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Body
  val bodyPath = Path().apply {
    moveTo(w * 0.32f, h * 0.85f)
    quadraticTo(w * 0.36f, h * 0.55f, w * 0.40f, h * 0.45f)
    lineTo(w * 0.60f, h * 0.45f)
    quadraticTo(w * 0.64f, h * 0.55f, w * 0.68f, h * 0.85f)
    close()
  }
  drawPath(bodyPath, fill)
  drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Neck ring
  drawLine(
    color = stroke,
    start = Offset(w * 0.35f, h * 0.45f),
    end = Offset(w * 0.65f, h * 0.45f),
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round
  )

  // Head ball
  val headRadius = w * 0.16f
  drawCircle(fill, headRadius, Offset(w * 0.5f, h * 0.30f))
  drawCircle(stroke, headRadius, Offset(w * 0.5f, h * 0.30f), style = Stroke(strokeWidth))
}

private fun DrawScope.drawKnight(fill: Color, stroke: Color, accent: Color, isWhite: Boolean) {
  val w = size.width
  val h = size.height
  val strokeWidth = (w * 0.045f).coerceAtLeast(1.5f)

  // Base
  val basePath = Path().apply {
    moveTo(w * 0.20f, h * 0.85f)
    lineTo(w * 0.80f, h * 0.85f)
    lineTo(w * 0.84f, h * 0.90f)
    lineTo(w * 0.16f, h * 0.90f)
    close()
  }
  drawPath(basePath, fill)
  drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Horse Head & Body
  val horsePath = Path().apply {
    moveTo(w * 0.24f, h * 0.85f)
    // Chest
    quadraticTo(w * 0.22f, h * 0.60f, w * 0.28f, h * 0.45f)
    // Muzzle / Jaw
    lineTo(w * 0.22f, h * 0.42f)
    quadraticTo(w * 0.20f, h * 0.36f, w * 0.26f, h * 0.33f)
    // Snout
    lineTo(w * 0.38f, h * 0.35f)
    // Forehead to Ear
    lineTo(w * 0.46f, h * 0.20f)
    lineTo(w * 0.52f, h * 0.24f)
    // Mane / Back of neck
    quadraticTo(w * 0.58f, h * 0.35f, w * 0.76f, h * 0.52f)
    quadraticTo(w * 0.80f, h * 0.68f, w * 0.74f, h * 0.85f)
    close()
  }
  drawPath(horsePath, fill)
  drawPath(horsePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Eye
  drawCircle(
    color = stroke,
    radius = w * 0.035f,
    center = Offset(w * 0.36f, h * 0.33f)
  )

  // Nostril / Muzzle line
  drawLine(
    color = stroke,
    start = Offset(w * 0.25f, h * 0.39f),
    end = Offset(w * 0.30f, h * 0.41f),
    strokeWidth = strokeWidth * 0.8f,
    cap = StrokeCap.Round
  )

  // Mane cut
  drawLine(
    color = stroke,
    start = Offset(w * 0.55f, h * 0.38f),
    end = Offset(w * 0.65f, h * 0.48f),
    strokeWidth = strokeWidth * 0.8f,
    cap = StrokeCap.Round
  )
}

private fun DrawScope.drawBishop(fill: Color, stroke: Color, accent: Color) {
  val w = size.width
  val h = size.height
  val strokeWidth = (w * 0.045f).coerceAtLeast(1.5f)

  // Base
  val basePath = Path().apply {
    moveTo(w * 0.22f, h * 0.85f)
    lineTo(w * 0.78f, h * 0.85f)
    lineTo(w * 0.82f, h * 0.90f)
    lineTo(w * 0.18f, h * 0.90f)
    close()
  }
  drawPath(basePath, fill)
  drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Mitre / Oval Body
  val mitrePath = Path().apply {
    moveTo(w * 0.32f, h * 0.85f)
    quadraticTo(w * 0.22f, h * 0.50f, w * 0.48f, h * 0.25f)
    lineTo(w * 0.52f, h * 0.25f)
    quadraticTo(w * 0.78f, h * 0.50f, w * 0.68f, h * 0.85f)
    close()
  }
  drawPath(mitrePath, fill)
  drawPath(mitrePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Top ball
  drawCircle(fill, w * 0.06f, Offset(w * 0.5f, h * 0.20f))
  drawCircle(stroke, w * 0.06f, Offset(w * 0.5f, h * 0.20f), style = Stroke(strokeWidth))

  // Bishop slash
  drawLine(
    color = stroke,
    start = Offset(w * 0.42f, h * 0.38f),
    end = Offset(w * 0.58f, h * 0.48f),
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round
  )

  // Mid collar
  drawLine(
    color = stroke,
    start = Offset(w * 0.30f, h * 0.72f),
    end = Offset(w * 0.70f, h * 0.72f),
    strokeWidth = strokeWidth * 0.8f,
    cap = StrokeCap.Round
  )
}

private fun DrawScope.drawRook(fill: Color, stroke: Color, accent: Color) {
  val w = size.width
  val h = size.height
  val strokeWidth = (w * 0.045f).coerceAtLeast(1.5f)

  // Base
  val basePath = Path().apply {
    moveTo(w * 0.20f, h * 0.85f)
    lineTo(w * 0.80f, h * 0.85f)
    lineTo(w * 0.84f, h * 0.90f)
    lineTo(w * 0.16f, h * 0.90f)
    close()
  }
  drawPath(basePath, fill)
  drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Tower body
  val bodyPath = Path().apply {
    moveTo(w * 0.30f, h * 0.85f)
    lineTo(w * 0.32f, h * 0.45f)
    lineTo(w * 0.68f, h * 0.45f)
    lineTo(w * 0.70f, h * 0.85f)
    close()
  }
  drawPath(bodyPath, fill)
  drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Castle Crenels (Battlements)
  val topPath = Path().apply {
    moveTo(w * 0.22f, h * 0.45f)
    lineTo(w * 0.22f, h * 0.26f)
    lineTo(w * 0.34f, h * 0.26f)
    lineTo(w * 0.34f, h * 0.34f)
    lineTo(w * 0.44f, h * 0.34f)
    lineTo(w * 0.44f, h * 0.26f)
    lineTo(w * 0.56f, h * 0.26f)
    lineTo(w * 0.56f, h * 0.34f)
    lineTo(w * 0.66f, h * 0.34f)
    lineTo(w * 0.66f, h * 0.26f)
    lineTo(w * 0.78f, h * 0.26f)
    lineTo(w * 0.78f, h * 0.45f)
    close()
  }
  drawPath(topPath, fill)
  drawPath(topPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))
}

private fun DrawScope.drawQueen(fill: Color, stroke: Color, accent: Color) {
  val w = size.width
  val h = size.height
  val strokeWidth = (w * 0.045f).coerceAtLeast(1.5f)

  // Base
  val basePath = Path().apply {
    moveTo(w * 0.18f, h * 0.85f)
    lineTo(w * 0.82f, h * 0.85f)
    lineTo(w * 0.86f, h * 0.90f)
    lineTo(w * 0.14f, h * 0.90f)
    close()
  }
  drawPath(basePath, fill)
  drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Body
  val bodyPath = Path().apply {
    moveTo(w * 0.28f, h * 0.85f)
    quadraticTo(w * 0.32f, h * 0.60f, w * 0.36f, h * 0.50f)
    lineTo(w * 0.64f, h * 0.50f)
    quadraticTo(w * 0.68f, h * 0.60f, w * 0.72f, h * 0.85f)
    close()
  }
  drawPath(bodyPath, fill)
  drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Crown
  val crownPath = Path().apply {
    moveTo(w * 0.34f, h * 0.50f)
    lineTo(w * 0.20f, h * 0.30f)
    lineTo(w * 0.35f, h * 0.38f)
    lineTo(w * 0.50f, h * 0.24f)
    lineTo(w * 0.65f, h * 0.38f)
    lineTo(w * 0.80f, h * 0.30f)
    lineTo(w * 0.66f, h * 0.50f)
    close()
  }
  drawPath(crownPath, fill)
  drawPath(crownPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Crown jewels
  val tipOrbs = listOf(
    Offset(w * 0.20f, h * 0.28f),
    Offset(w * 0.50f, h * 0.22f),
    Offset(w * 0.80f, h * 0.28f)
  )
  for (orb in tipOrbs) {
    drawCircle(fill, w * 0.038f, orb)
    drawCircle(stroke, w * 0.038f, orb, style = Stroke(strokeWidth * 0.9f))
  }
}

private fun DrawScope.drawKing(fill: Color, stroke: Color, accent: Color) {
  val w = size.width
  val h = size.height
  val strokeWidth = (w * 0.045f).coerceAtLeast(1.5f)

  // Base
  val basePath = Path().apply {
    moveTo(w * 0.18f, h * 0.85f)
    lineTo(w * 0.82f, h * 0.85f)
    lineTo(w * 0.86f, h * 0.90f)
    lineTo(w * 0.14f, h * 0.90f)
    close()
  }
  drawPath(basePath, fill)
  drawPath(basePath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Robe / Body
  val bodyPath = Path().apply {
    moveTo(w * 0.26f, h * 0.85f)
    quadraticTo(w * 0.30f, h * 0.60f, w * 0.34f, h * 0.46f)
    lineTo(w * 0.66f, h * 0.46f)
    quadraticTo(w * 0.70f, h * 0.60f, w * 0.74f, h * 0.85f)
    close()
  }
  drawPath(bodyPath, fill)
  drawPath(bodyPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Crown arched top
  val crownPath = Path().apply {
    moveTo(w * 0.32f, h * 0.46f)
    quadraticTo(w * 0.20f, h * 0.32f, w * 0.35f, h * 0.26f)
    lineTo(w * 0.65f, h * 0.26f)
    quadraticTo(w * 0.80f, h * 0.32f, w * 0.68f, h * 0.46f)
    close()
  }
  drawPath(crownPath, fill)
  drawPath(crownPath, stroke, style = Stroke(strokeWidth, join = StrokeJoin.Round))

  // Cross on top
  val crossVertical = Path().apply {
    moveTo(w * 0.5f, h * 0.13f)
    lineTo(w * 0.5f, h * 0.26f)
  }
  drawLine(
    color = stroke,
    start = Offset(w * 0.5f, h * 0.14f),
    end = Offset(w * 0.5f, h * 0.26f),
    strokeWidth = strokeWidth * 1.3f,
    cap = StrokeCap.Square
  )
  drawLine(
    color = stroke,
    start = Offset(w * 0.42f, h * 0.18f),
    end = Offset(w * 0.58f, h * 0.18f),
    strokeWidth = strokeWidth * 1.3f,
    cap = StrokeCap.Square
  )
}
