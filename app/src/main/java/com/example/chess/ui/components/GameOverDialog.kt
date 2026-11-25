package com.example.chess.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chess.model.PieceColor

@Composable
fun GameOverDialog(
  title: String,
  subtitle: String,
  winner: PieceColor?,
  isVsAi: Boolean,
  eloChange: Pair<Int, Int>?, // (newElo, deltaElo)
  adaptiveFeedback: String?,
  onRematch: () -> Unit,
  onReviewBoard: () -> Unit,
  onHome: () -> Unit
) {
  Dialog(onDismissRequest = onReviewBoard) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
      elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("game_over_dialog")
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Trophy icon
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color(0xFFFEF3C7)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFD97706),
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = title,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        // Rating change section if played vs AI
        if (isVsAi && eloChange != null) {
          val (newElo, delta) = eloChange
          val deltaText = if (delta > 0) "+$delta" else "$delta"
          val deltaColor = if (delta >= 0) Color(0xFF10B981) else Color(0xFFEF4444)

          Spacer(modifier = Modifier.height(16.dp))

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = Color(0xFFF59E0B),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Rating Pemain Anda",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = "$newElo ELO",
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "($deltaText)",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = deltaColor
                )
              }
              if (!adaptiveFeedback.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = adaptiveFeedback,
                  fontSize = 11.sp,
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions
        Button(
          onClick = onRematch,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rematch_button"),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Main Lagi", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onReviewBoard,
            modifier = Modifier
              .weight(1f)
              .testTag("review_board_button")
          ) {
            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Lihat Papan", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = onHome,
            modifier = Modifier
              .weight(1f)
              .testTag("home_button")
          ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Menu Utama", fontSize = 12.sp)
          }
        }
      }
    }
  }
}
