package com.example.chess.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.chess.ai.DifficultyLevel
import com.example.chess.ai.PlayerStats
import com.example.chess.model.PieceColor
import kotlin.random.Random

@Composable
fun HomeScreen(
  stats: PlayerStats,
  onStartVsAi: (PieceColor, DifficultyLevel, Long?) -> Unit,
  onStartVsHuman: (Long?) -> Unit,
  onOpenSettings: () -> Unit,
  onOpenStats: () -> Unit
) {
  var selectedDifficulty by remember { mutableStateOf(DifficultyLevel.ADAPTIF) }
  var selectedColorOption by remember { mutableStateOf("WHITE") } // "WHITE", "BLACK", "RANDOM"
  var selectedTimerOption by remember { mutableStateOf<Long?>(600L) } // 600L (10m), 300L (5m), 180L (3m), null (santai)

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top header with stats & settings buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Player ELO pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier
            .clickable { onOpenStats() }
            .testTag("home_stats_pill")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.Leaderboard,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${stats.elo} ELO",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (stats.winStreak > 0) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${stats.winStreak}🔥",
                fontSize = 12.sp
              )
            }
          }
        }

        // Settings Button
        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.testTag("home_settings_btn")
        ) {
          Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Hero Banner with App Icon
      Box(
        modifier = Modifier
          .size(104.dp)
          .shadow(elevation = 16.dp, shape = RoundedCornerShape(26.dp))
          .clip(RoundedCornerShape(26.dp))
          .background(Color(0xFF1E293B))
          .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(26.dp))
      ) {
        Image(
          painter = painterResource(id = R.drawable.chess_app_icon_1793779423675),
          contentDescription = "Catur Master Icon",
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Catur Master",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Lawan AI dengan Kesulitan Otomatis & Main 2 Pemain",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // CARD 1: Lawan Komputer (AI)
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("vs_computer_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Computer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Bermain dengan Komputer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Tingkat kesulitan menyesuaikan otomatis",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Difficulty Selector
          Text(
            text = "Tingkat Kesulitan AI:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (level in DifficultyLevel.entries) {
              val isSelected = selectedDifficulty == level
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedDifficulty = level }
                  .testTag("difficulty_opt_${level.name.lowercase()}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (level == DifficultyLevel.ADAPTIF) {
                      Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = level.displayName,
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        if (level == DifficultyLevel.ADAPTIF) {
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF59E0B)
                          ) {
                            Text(
                              text = "OTOMATIS",
                              fontSize = 9.sp,
                              fontWeight = FontWeight.ExtraBold,
                              color = Color.Black,
                              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                          }
                        }
                      }
                      Text(
                        text = level.description,
                        fontSize = 11.sp,
                        color = (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f),
                        maxLines = 1
                      )
                    }
                  }

                  Text(
                    text = "${level.estimatedElo} ELO",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Color Choice (Putih, Hitam, Acak)
          Text(
            text = "Pilih Sisi Bidak:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val colorOptions = listOf(
              "WHITE" to "Putih (Jalan 1)",
              "RANDOM" to "Acak",
              "BLACK" to "Hitam"
            )

            for ((opt, label) in colorOptions) {
              val isSelected = selectedColorOption == opt
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                  .weight(1f)
                  .clickable { selectedColorOption = opt }
                  .testTag("color_opt_${opt.lowercase()}")
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.padding(vertical = 10.dp)
                ) {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Timer Options
          Text(
            text = "Jam Waktu Pertandingan:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val timerOptions = listOf(
              600L to "10 Min",
              300L to "5 Min",
              180L to "3 Min",
              -1L to "Santai"
            )

            for ((seconds, label) in timerOptions) {
              val realVal = if (seconds == -1L) null else seconds
              val isSelected = (selectedTimerOption == realVal)
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier
                  .weight(1f)
                  .clickable { selectedTimerOption = realVal }
                  .testTag("timer_opt_${label.lowercase().replace(" ", "_")}")
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.padding(vertical = 8.dp)
                ) {
                  Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Start vs AI Button
          Button(
            onClick = {
              val chosenColor = when (selectedColorOption) {
                "WHITE" -> PieceColor.WHITE
                "BLACK" -> PieceColor.BLACK
                else -> if (Random.nextBoolean()) PieceColor.WHITE else PieceColor.BLACK
              }
              onStartVsAi(chosenColor, selectedDifficulty, selectedTimerOption)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("start_vs_ai_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mulai Main Lawan AI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // CARD 2: 2 Pemain (Pass & Play)
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("two_player_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Bermain 2 Pemain",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Bermain berdua di satu perangkat (Pass & Play)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedButton(
            onClick = { onStartVsHuman(selectedTimerOption) },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("start_vs_human_btn")
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mulai Pertandingan 2 Pemain", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
