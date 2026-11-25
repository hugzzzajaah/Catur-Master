package com.example.chess.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.model.Move
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor

@Composable
fun EvaluationBar(
  centipawns: Int,
  modifier: Modifier = Modifier
) {
  // Clamp evaluation between -1500 and +1500
  val clamped = centipawns.coerceIn(-1500, 1500)
  // Fraction for White: 0.0 (all Black) to 1.0 (all White), 0.5 is even
  val targetFraction = ((clamped + 1500f) / 3000f).coerceIn(0.05f, 0.95f)
  val animatedFraction by animateFloatAsState(targetValue = targetFraction, label = "eval_fraction")

  val scoreText = when {
    clamped > 90000 -> "+M"
    clamped < -90000 -> "-M"
    clamped >= 0 -> "+${String.format("%.1f", clamped / 100.0)}"
    else -> String.format("%.1f", clamped / 100.0)
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .height(18.dp)
      .clip(RoundedCornerShape(9.dp))
      .background(Color(0xFF334155))
      .border(1.dp, Color(0xFF475569), RoundedCornerShape(9.dp))
  ) {
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(animatedFraction)
        .background(Color(0xFFF8FAFC))
    )
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .background(Color(0xFF1E293B))
    )
  }
}

@Composable
fun PlayerHeaderCard(
  name: String,
  subtitle: String,
  color: PieceColor,
  isActiveTurn: Boolean,
  capturedPieces: List<Piece>,
  materialAdvantage: Int,
  remainingSeconds: Long?,
  isComputer: Boolean,
  modifier: Modifier = Modifier
) {
  val borderColor by animateColorAsState(
    targetValue = if (isActiveTurn) Color(0xFFF59E0B) else Color.Transparent,
    label = "player_turn_border"
  )

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isActiveTurn) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    modifier = modifier
      .fillMaxWidth()
      .border(2.dp, borderColor, RoundedCornerShape(12.dp))
      .testTag("player_card_${color.name.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Avatar + Names
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (color == PieceColor.WHITE) Color(0xFFE2E8F0) else Color(0xFF0F172A))
            .border(1.dp, Color(0xFF94A3B8), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isComputer) Icons.Default.Computer else Icons.Default.Person,
            contentDescription = null,
            tint = if (color == PieceColor.WHITE) Color(0xFF0F172A) else Color(0xFFF8FAFC),
            modifier = Modifier.size(22.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = name,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (color == PieceColor.WHITE) Color.White else Color.Black)
                .border(1.dp, Color.Gray, CircleShape)
            )
          }
          Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
          )

          // Captured pieces preview
          if (capturedPieces.isNotEmpty()) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              val grouped = capturedPieces.groupBy { it.type }
              for ((type, list) in grouped) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(modifier = Modifier.size(14.dp)) {
                    ChessPieceView(piece = Piece(type, color.opposite()))
                  }
                  if (list.size > 1) {
                    Text(
                      text = "${list.size}",
                      fontSize = 10.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
                Spacer(modifier = Modifier.width(2.dp))
              }
              if (materialAdvantage > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "+$materialAdvantage",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF10B981)
                )
              }
            }
          }
        }
      }

      // Right: Timer display
      if (remainingSeconds != null) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val isLowTime = remainingSeconds < 30

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLowTime) Color(0xFFEF4444) else if (isActiveTurn) Color(0xFF1E293B) else Color(0xFF334155))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = String.format("%02d:%02d", minutes, seconds),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
fun MoveHistoryTicker(
  moves: List<Move>,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()

  LaunchedEffect(moves.size) {
    if (moves.isNotEmpty()) {
      listState.animateScrollToItem(moves.size - 1)
    }
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    modifier = modifier
      .fillMaxWidth()
      .height(36.dp)
  ) {
    if (moves.isEmpty()) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Belum ada langkah. Mulai permainan!",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
      }
    } else {
      LazyRow(
        state = listState,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
      ) {
        val pairs = moves.chunked(2)
        itemsIndexed(pairs) { idx, turnMoves ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${idx + 1}.",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(3.dp))
            // White move
            Text(
              text = turnMoves[0].san,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Black move if available
            if (turnMoves.size > 1) {
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = turnMoves[1].san,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
          }
        }
      }
    }
  }
}
