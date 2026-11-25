package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chess.ui.ChessViewModel
import com.example.chess.ui.components.SettingsSheet
import com.example.chess.ui.components.StatsDialog
import com.example.chess.ui.screens.ChessGameScreen
import com.example.chess.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme

enum class Screen {
  HOME,
  GAME
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          ChessAppRoot()
        }
      }
    }
  }
}

@Composable
fun ChessAppRoot(viewModel: ChessViewModel = viewModel()) {
  var currentScreen by remember { mutableStateOf(Screen.HOME) }
  var showHomeSettings by remember { mutableStateOf(false) }
  var showHomeStats by remember { mutableStateOf(false) }

  val uiState by viewModel.uiState.collectAsState()

  Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
    when (screen) {
      Screen.HOME -> {
        HomeScreen(
          stats = uiState.stats,
          onStartVsAi = { color, difficulty, timer ->
            viewModel.startNewGame(
              isVsAi = true,
              playerColor = color,
              difficulty = difficulty,
              timerSeconds = timer
            )
            currentScreen = Screen.GAME
          },
          onStartVsHuman = { timer ->
            viewModel.startNewGame(
              isVsAi = false,
              playerColor = com.example.chess.model.PieceColor.WHITE,
              timerSeconds = timer
            )
            currentScreen = Screen.GAME
          },
          onOpenSettings = { showHomeSettings = true },
          onOpenStats = {
            viewModel.loadStats()
            showHomeStats = true
          }
        )
      }
      Screen.GAME -> {
        ChessGameScreen(
          viewModel = viewModel,
          onNavigateHome = { currentScreen = Screen.HOME }
        )
      }
    }
  }

  if (showHomeSettings) {
    SettingsSheet(
      currentTheme = uiState.boardTheme,
      onThemeSelected = { viewModel.setBoardTheme(it) },
      isSoundEnabled = uiState.isSoundEnabled,
      onToggleSound = { viewModel.toggleSound(it) },
      isHapticsEnabled = uiState.isHapticsEnabled,
      onToggleHaptics = { viewModel.toggleHaptics(it) },
      onDismiss = { showHomeSettings = false }
    )
  }

  if (showHomeStats) {
    StatsDialog(
      stats = uiState.stats,
      onDismiss = { showHomeStats = false }
    )
  }
}
