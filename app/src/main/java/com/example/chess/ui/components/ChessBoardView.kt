package com.example.chess.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.Square
import com.example.chess.ui.theme.BoardTheme

@Composable
fun ChessBoardView(
  state: GameState,
  selectedSquare: Square?,
  legalMovesForSelected: List<Move>,
  lastMove: Move?,
  hintMove: Move?,
  isBoardFlipped: Boolean,
  theme: BoardTheme,
  onSquareClicked: (Square) -> Unit,
  modifier: Modifier = Modifier
) {
  val destinationSquares = legalMovesForSelected.map { it.to }.toSet()
  val checkedKingSquare = if (state.isCheck) state.board.findKing(state.activeColor) else null

  val checkPulseColor by animateColorAsState(
    targetValue = if (state.isCheck) theme.checkColor else Color.Transparent,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 600),
      repeatMode = RepeatMode.Reverse
    ),
    label = "check_pulse"
  )

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp))
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF1E293B))
      .padding(4.dp)
      .testTag("chessboard_view")
  ) {
    val ranks = if (isBoardFlipped) 0..7 else 7 downTo 0
    val files = if (isBoardFlipped) 7 downTo 0 else 0..7

    Column(modifier = Modifier.fillMaxSize()) {
      for (r in ranks) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
          for (f in files) {
            val square = Square(f, r)
            val piece = state.board[square]
            val isSelected = selectedSquare == square
            val isLastMove = lastMove?.from == square || lastMove?.to == square
            val isHint = hintMove?.from == square || hintMove?.to == square
            val isLegalDest = destinationSquares.contains(square)
            val isKingInCheckSquare = checkedKingSquare == square

            val baseColor = if (square.isLightSquare) theme.lightSquare else theme.darkSquare
            val labelColor = if (square.isLightSquare) theme.darkSquare else theme.lightSquare

            Box(
              modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(baseColor)
                .clickable { onSquareClicked(square) }
                .testTag("square_${square.name}")
            ) {
              // 1. Last move highlight
              if (isLastMove) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(theme.lastMoveColor)
                )
              }

              // 2. Selected square highlight
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(theme.selectedColor)
                )
              }

              // 3. Hint move highlight
              if (isHint) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x7706B6D4))
                )
              }

              // 4. King under check pulse
              if (isKingInCheckSquare) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(checkPulseColor)
                )
              }

              // 5. Piece rendering
              if (piece != null) {
                ChessPieceView(
                  piece = piece,
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                )
              }

              // 6. Legal move indicator
              if (isLegalDest) {
                if (piece != null || (state.enPassantSquare == square && selectedSquare != null && state.board[selectedSquare]?.type == com.example.chess.model.PieceType.PAWN)) {
                  // Capture target: outer ring / crosshairs
                  Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension * 0.44f
                    drawCircle(
                      color = theme.captureColor,
                      radius = radius,
                      center = center,
                      style = Stroke(width = size.minDimension * 0.08f)
                    )
                  }
                } else {
                  // Move target: clean center dot
                  Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                      color = theme.legalMoveColor,
                      radius = size.minDimension * 0.16f,
                      center = center
                    )
                  }
                }
              }

              // 7. Coordinate labels
              // Show rank on the first column of the view
              val isFirstCol = f == files.first
              if (isFirstCol) {
                Text(
                  text = (r + 1).toString(),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = labelColor.copy(alpha = 0.85f),
                  modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 1.dp)
                )
              }
              // Show file on the bottom row of the view
              val isBottomRow = r == ranks.last
              if (isBottomRow) {
                Text(
                  text = ('a' + f).toString(),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = labelColor.copy(alpha = 0.85f),
                  modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 1.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
