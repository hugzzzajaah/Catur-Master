package com.example.chess.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chess.ai.DifficultyLevel
import com.example.chess.model.PieceColor
import com.example.chess.ui.ChessViewModel
import com.example.chess.ui.components.ChessBoardView
import com.example.chess.ui.components.EvaluationBar
import com.example.chess.ui.components.GameOverDialog
import com.example.chess.ui.components.MoveHistoryTicker
import com.example.chess.ui.components.PlayerHeaderCard
import com.example.chess.ui.components.PromotionDialog
import com.example.chess.ui.components.SettingsSheet
import com.example.chess.ui.components.StatsDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen(
  viewModel: ChessViewModel,
  onNavigateHome: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val haptic = LocalHapticFeedback.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var showSettingsSheet by remember { mutableStateOf(false) }
  var showStatsDialog by remember { mutableStateOf(false) }

  BackHandler {
    onNavigateHome()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = if (uiState.isVsAi) "Lawan Komputer" else "Catur 2 Pemain",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            if (uiState.isVsAi) {
              Text(
                text = if (uiState.difficultyLevel == DifficultyLevel.ADAPTIF)
                  uiState.adaptiveSettings.dynamicLabel
                else
                  "Level: ${uiState.difficultyLevel.displayName} (${uiState.difficultyLevel.estimatedElo} ELO)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateHome,
            modifier = Modifier.testTag("game_back_btn")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke Menu")
          }
        },
        actions = {
          IconButton(
            onClick = { viewModel.toggleBoardFlip() },
            modifier = Modifier.testTag("game_flip_btn")
          ) {
            Icon(Icons.Default.SwapVert, contentDescription = "Putar Papan")
          }
          IconButton(
            onClick = { showStatsDialog = true },
            modifier = Modifier.testTag("game_stats_btn")
          ) {
            Icon(Icons.Default.Leaderboard, contentDescription = "Statistik")
          }
          IconButton(
            onClick = { showSettingsSheet = true },
            modifier = Modifier.testTag("game_settings_btn")
          ) {
            Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { paddingValues ->
    val scrollState = rememberScrollState()

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(scrollState)
        .padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. Adaptive AI Feedback Banner (visible when vs AI and adaptive)
      if (uiState.isVsAi && uiState.difficultyLevel == DifficultyLevel.ADAPTIF) {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color(0xFFF59E0B),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = uiState.adaptiveSettings.feedbackHint,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              maxLines = 1
            )
          }
        }
      }

      // Top Player Card (Top side of board)
      val topColor = if (uiState.isBoardFlipped) PieceColor.WHITE else PieceColor.BLACK
      val bottomColor = topColor.opposite()

      val isTopComputer = uiState.isVsAi && (topColor != uiState.playerColor)
      val isBottomComputer = uiState.isVsAi && (bottomColor != uiState.playerColor)

      val topName = if (isTopComputer) "Komputer (AI)" else if (uiState.isVsAi) "Anda" else "Pemain ${if (topColor == PieceColor.WHITE) "Putih" else "Hitam"}"
      val bottomName = if (isBottomComputer) "Komputer (AI)" else if (uiState.isVsAi) "Anda" else "Pemain ${if (bottomColor == PieceColor.WHITE) "Putih" else "Hitam"}"

      val topSubtitle = if (isTopComputer) {
        if (uiState.isAiThinking) "Sedang berpikir..." else "${uiState.difficultyLevel.displayName}"
      } else {
        "${uiState.stats.elo} ELO"
      }

      val bottomSubtitle = if (isBottomComputer) {
        if (uiState.isAiThinking) "Sedang berpikir..." else "${uiState.difficultyLevel.displayName}"
      } else {
        "${uiState.stats.elo} ELO"
      }

      val topCaptured = if (topColor == PieceColor.WHITE) uiState.gameState.capturedByWhite else uiState.gameState.capturedByBlack
      val bottomCaptured = if (bottomColor == PieceColor.WHITE) uiState.gameState.capturedByWhite else uiState.gameState.capturedByBlack

      val topAdv = if (topColor == PieceColor.WHITE) uiState.whiteMaterialAdvantage else uiState.blackMaterialAdvantage
      val bottomAdv = if (bottomColor == PieceColor.WHITE) uiState.whiteMaterialAdvantage else uiState.blackMaterialAdvantage

      val topTimer = if (topColor == PieceColor.WHITE) uiState.whiteTimeRemaining else uiState.blackTimeRemaining
      val bottomTimer = if (bottomColor == PieceColor.WHITE) uiState.whiteTimeRemaining else uiState.blackTimeRemaining

      PlayerHeaderCard(
        name = topName,
        subtitle = topSubtitle,
        color = topColor,
        isActiveTurn = uiState.gameState.activeColor == topColor,
        capturedPieces = topCaptured,
        materialAdvantage = topAdv,
        remainingSeconds = topTimer,
        isComputer = isTopComputer
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Evaluation Bar
      EvaluationBar(
        centipawns = uiState.currentEvalCentipawns,
        modifier = Modifier.padding(horizontal = 4.dp)
      )

      Spacer(modifier = Modifier.height(6.dp))

      // The Chessboard
      Box(modifier = Modifier.fillMaxWidth()) {
        ChessBoardView(
          state = uiState.gameState,
          selectedSquare = uiState.selectedSquare,
          legalMovesForSelected = uiState.legalMovesForSelected,
          lastMove = uiState.lastMove,
          hintMove = uiState.hintMove,
          isBoardFlipped = uiState.isBoardFlipped,
          theme = uiState.boardTheme,
          onSquareClicked = { sq ->
            if (uiState.isHapticsEnabled) {
              haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            }
            viewModel.onSquareClicked(sq)
          }
        )

        // AI thinking overlay spinner
        if (uiState.isAiThinking) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier.align(Alignment.Center)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              CircularProgressIndicator(
                color = Color(0xFFF59E0B),
                strokeWidth = 3.dp,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "AI Berpikir...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Bottom Player Card
      PlayerHeaderCard(
        name = bottomName,
        subtitle = bottomSubtitle,
        color = bottomColor,
        isActiveTurn = uiState.gameState.activeColor == bottomColor,
        capturedPieces = bottomCaptured,
        materialAdvantage = bottomAdv,
        remainingSeconds = bottomTimer,
        isComputer = isBottomComputer
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Move History Ticker
      MoveHistoryTicker(moves = uiState.gameState.moveHistory)

      Spacer(modifier = Modifier.height(10.dp))

      // Hint Move Banner if active
      AnimatedVisibility(visible = uiState.hintMove != null) {
        val hint = uiState.hintMove
        if (hint != null) {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Saran Langkah Terbaik: ${hint.san} (${hint.from.name} ➔ ${hint.to.name})",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp
              )
            }
          }
        }
      }

      // Action Control Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Undo Button
        OutlinedButton(
          onClick = { viewModel.undoMove() },
          enabled = uiState.gameState.moveHistory.isNotEmpty() && !uiState.isAiThinking,
          modifier = Modifier
            .weight(1f)
            .testTag("undo_move_btn")
        ) {
          Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Urungkan", fontSize = 11.sp, maxLines = 1)
        }

        // Hint Button
        OutlinedButton(
          onClick = {
            viewModel.requestHint()
            scope.launch {
              snackbarHostState.showSnackbar("Petunjuk langkah terbaik disorot warna biru di papan!")
            }
          },
          enabled = !uiState.gameState.isGameOver && !uiState.isAiThinking,
          modifier = Modifier
            .weight(1f)
            .testTag("hint_move_btn")
        ) {
          Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Petunjuk", fontSize = 11.sp, maxLines = 1)
        }

        // Restart Game Button
        OutlinedButton(
          onClick = {
            viewModel.startNewGame(
              isVsAi = uiState.isVsAi,
              playerColor = uiState.playerColor,
              difficulty = uiState.difficultyLevel,
              timerSeconds = uiState.timerOptionSeconds
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("restart_game_btn")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Ulang", fontSize = 11.sp, maxLines = 1)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Pawn Promotion Dialog
  if (uiState.pendingPromotionMove != null) {
    PromotionDialog(
      color = uiState.gameState.activeColor,
      onPieceSelected = { promo -> viewModel.completePromotion(promo) },
      onDismiss = { viewModel.cancelPromotion() }
    )
  }

  // Game Over Dialog
  if (uiState.showGameOverDialog) {
    GameOverDialog(
      title = uiState.gameOverTitle,
      subtitle = uiState.gameOverSubtitle,
      winner = uiState.gameState.winner,
      isVsAi = uiState.isVsAi,
      eloChange = uiState.gameOverEloChange,
      adaptiveFeedback = uiState.adaptiveSettings.dynamicLabel,
      onRematch = {
        viewModel.startNewGame(
          isVsAi = uiState.isVsAi,
          playerColor = uiState.playerColor,
          difficulty = uiState.difficultyLevel,
          timerSeconds = uiState.timerOptionSeconds
        )
      },
      onReviewBoard = { viewModel.dismissGameOverDialog() },
      onHome = onNavigateHome
    )
  }

  // Settings Bottom Sheet
  if (showSettingsSheet) {
    SettingsSheet(
      currentTheme = uiState.boardTheme,
      onThemeSelected = { viewModel.setBoardTheme(it) },
      isSoundEnabled = uiState.isSoundEnabled,
      onToggleSound = { viewModel.toggleSound(it) },
      isHapticsEnabled = uiState.isHapticsEnabled,
      onToggleHaptics = { viewModel.toggleHaptics(it) },
      onDismiss = { showSettingsSheet = false }
    )
  }

  // Stats Dialog
  if (showStatsDialog) {
    StatsDialog(
      stats = uiState.stats,
      onDismiss = { showStatsDialog = false }
    )
  }
}
