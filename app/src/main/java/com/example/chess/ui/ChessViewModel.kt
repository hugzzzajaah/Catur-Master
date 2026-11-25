package com.example.chess.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.ai.AdaptiveDifficultyManager
import com.example.chess.ai.AdaptiveSettings
import com.example.chess.ai.ChessAI
import com.example.chess.ai.DifficultyLevel
import com.example.chess.ai.PlayerStats
import com.example.chess.audio.ChessSoundPlayer
import com.example.chess.model.ChessEngine
import com.example.chess.model.GameState
import com.example.chess.model.Move
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Square
import com.example.chess.ui.theme.BoardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChessUiState(
  val gameState: GameState = GameState(board = com.example.chess.model.Board.initialBoard()),
  val selectedSquare: Square? = null,
  val legalMovesForSelected: List<Move> = emptyList(),
  val lastMove: Move? = null,
  val hintMove: Move? = null,
  val isBoardFlipped: Boolean = false,
  val isVsAi: Boolean = true,
  val playerColor: PieceColor = PieceColor.WHITE,
  val difficultyLevel: DifficultyLevel = DifficultyLevel.ADAPTIF,
  val adaptiveSettings: AdaptiveSettings = AdaptiveSettings(
    effectiveDepth = 2,
    randomnessChance = 0.1f,
    dynamicLabel = "Adaptif: Memulai...",
    feedbackHint = "Menyesuaikan otomatis dengan gaya bermain Anda"
  ),
  val isAiThinking: Boolean = false,
  val pendingPromotionMove: Move? = null,
  val showGameOverDialog: Boolean = false,
  val gameOverTitle: String = "",
  val gameOverSubtitle: String = "",
  val gameOverEloChange: Pair<Int, Int>? = null,
  val boardTheme: BoardTheme = BoardTheme.KAYU_KLASIK,
  val isSoundEnabled: Boolean = true,
  val isHapticsEnabled: Boolean = true,
  val timerOptionSeconds: Long? = 600L, // 10 minutes default
  val whiteTimeRemaining: Long? = 600L,
  val blackTimeRemaining: Long? = 600L,
  val currentEvalCentipawns: Int = 0,
  val stats: PlayerStats = PlayerStats(),
  val whiteMaterialAdvantage: Int = 0,
  val blackMaterialAdvantage: Int = 0
)

class ChessViewModel(application: Application) : AndroidViewModel(application) {
  private val engine = ChessEngine()
  private val soundPlayer = ChessSoundPlayer()
  private val adaptiveManager = AdaptiveDifficultyManager(application)

  private val _uiState = MutableStateFlow(ChessUiState())
  val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

  private var timerJob: Job? = null

  init {
    loadStats()
    refreshAdaptiveParams()
    startTimer()
  }

  fun loadStats() {
    val stats = adaptiveManager.getStats()
    _uiState.update { it.copy(stats = stats) }
  }

  private fun refreshAdaptiveParams() {
    val params = adaptiveManager.calculateAdaptiveParameters(_uiState.value.currentEvalCentipawns)
    _uiState.update { it.copy(adaptiveSettings = params) }
  }

  fun setBoardTheme(theme: BoardTheme) {
    _uiState.update { it.copy(boardTheme = theme) }
  }

  fun toggleSound(enabled: Boolean) {
    soundPlayer.isSoundEnabled = enabled
    _uiState.update { it.copy(isSoundEnabled = enabled) }
  }

  fun toggleHaptics(enabled: Boolean) {
    _uiState.update { it.copy(isHapticsEnabled = enabled) }
  }

  fun toggleBoardFlip() {
    _uiState.update { it.copy(isBoardFlipped = !it.isBoardFlipped) }
  }

  fun startNewGame(
    isVsAi: Boolean,
    playerColor: PieceColor = PieceColor.WHITE,
    difficulty: DifficultyLevel = DifficultyLevel.ADAPTIF,
    timerSeconds: Long? = 600L
  ) {
    timerJob?.cancel()
    engine.reset()
    val state = engine.currentState
    val isFlipped = (playerColor == PieceColor.BLACK && isVsAi)

    _uiState.update {
      it.copy(
        gameState = state,
        selectedSquare = null,
        legalMovesForSelected = emptyList(),
        lastMove = null,
        hintMove = null,
        isBoardFlipped = isFlipped,
        isVsAi = isVsAi,
        playerColor = playerColor,
        difficultyLevel = difficulty,
        isAiThinking = false,
        pendingPromotionMove = null,
        showGameOverDialog = false,
        timerOptionSeconds = timerSeconds,
        whiteTimeRemaining = timerSeconds,
        blackTimeRemaining = timerSeconds,
        currentEvalCentipawns = 0,
        whiteMaterialAdvantage = 0,
        blackMaterialAdvantage = 0
      )
    }

    refreshAdaptiveParams()
    startTimer()

    // If playing as Black against AI, AI makes the first move as White
    if (isVsAi && playerColor == PieceColor.BLACK) {
      triggerAiMove()
    }
  }

  fun onSquareClicked(square: Square) {
    val state = _uiState.value
    if (state.isAiThinking || state.gameState.isGameOver || state.pendingPromotionMove != null) return

    val currentSelected = state.selectedSquare
    val board = state.gameState.board
    val clickedPiece = board[square]

    // If playing vs AI, player can only initiate moves for their assigned color
    if (state.isVsAi && state.gameState.activeColor != state.playerColor) {
      return
    }

    if (currentSelected == null) {
      // First selection: Must be a piece of the active color
      if (clickedPiece != null && clickedPiece.color == state.gameState.activeColor) {
        val legalMoves = engine.getLegalMovesForSquare(square)
        _uiState.update {
          it.copy(
            selectedSquare = square,
            legalMovesForSelected = legalMoves,
            hintMove = null
          )
        }
      }
    } else {
      // A square is already selected
      if (currentSelected == square) {
        // Deselect
        _uiState.update {
          it.copy(selectedSquare = null, legalMovesForSelected = emptyList())
        }
      } else if (clickedPiece != null && clickedPiece.color == state.gameState.activeColor) {
        // Switch selection to another own piece
        val legalMoves = engine.getLegalMovesForSquare(square)
        _uiState.update {
          it.copy(selectedSquare = square, legalMovesForSelected = legalMoves)
        }
      } else {
        // Attempting to move to target square
        val targetMove = state.legalMovesForSelected.find { it.to == square }
        if (targetMove != null) {
          // Check if promotion is needed
          val piece = board[currentSelected]
          val isPromotion = piece?.type == PieceType.PAWN &&
            ((piece.isWhite && square.rank == 7) || (piece.isBlack && square.rank == 0))

          if (isPromotion) {
            _uiState.update { it.copy(pendingPromotionMove = targetMove) }
          } else {
            executeMove(targetMove)
          }
        } else {
          // Deselect if clicked invalid square
          _uiState.update {
            it.copy(selectedSquare = null, legalMovesForSelected = emptyList())
          }
        }
      }
    }
  }

  fun completePromotion(promoType: PieceType) {
    val pending = _uiState.value.pendingPromotionMove ?: return
    val promoMove = pending.copy(promotion = promoType)
    _uiState.update { it.copy(pendingPromotionMove = null) }
    executeMove(promoMove)
  }

  fun cancelPromotion() {
    _uiState.update {
      it.copy(pendingPromotionMove = null, selectedSquare = null, legalMovesForSelected = emptyList())
    }
  }

  private fun executeMove(move: Move) {
    val success = engine.makeMove(move)
    if (!success) return

    val newState = engine.currentState
    val isCapture = move.isCapture

    // Sound effect
    if (newState.isCheck) {
      soundPlayer.playCheckSound()
    } else if (isCapture) {
      soundPlayer.playCaptureSound()
    } else {
      soundPlayer.playMoveSound()
    }

    // Material difference calculation
    val whiteMat = newState.capturedByWhite.sumOf { it.type.baseValue }
    val blackMat = newState.capturedByBlack.sumOf { it.type.baseValue }
    val whiteAdv = ((whiteMat - blackMat) / 100).coerceAtLeast(0)
    val blackAdv = ((blackMat - whiteMat) / 100).coerceAtLeast(0)

    val eval = ChessAI.evaluate(newState)

    _uiState.update {
      it.copy(
        gameState = newState,
        selectedSquare = null,
        legalMovesForSelected = emptyList(),
        lastMove = move,
        hintMove = null,
        currentEvalCentipawns = eval,
        whiteMaterialAdvantage = whiteAdv,
        blackMaterialAdvantage = blackAdv
      )
    }

    refreshAdaptiveParams()

    // Check game over
    if (newState.isGameOver) {
      handleGameOver(newState)
      return
    }

    // Trigger AI response if playing vs AI and it's AI's turn
    val current = _uiState.value
    if (current.isVsAi && newState.activeColor != current.playerColor) {
      triggerAiMove()
    }
  }

  private fun triggerAiMove() {
    _uiState.update { it.copy(isAiThinking = true) }

    viewModelScope.launch {
      // Natural thinking delay (300ms to 600ms)
      delay(450)

      val stateSnapshot = _uiState.value.gameState
      val difficulty = _uiState.value.difficultyLevel

      val (depth, randomness) = if (difficulty == DifficultyLevel.ADAPTIF) {
        val adaptive = adaptiveManager.calculateAdaptiveParameters(_uiState.value.currentEvalCentipawns)
        adaptive.effectiveDepth to adaptive.randomnessChance
      } else {
        difficulty.searchDepth to difficulty.randomnessChance
      }

      val aiMove = withContext(Dispatchers.Default) {
        ChessAI.findBestMove(stateSnapshot, depth, randomness)
      }

      _uiState.update { it.copy(isAiThinking = false) }

      if (aiMove != null) {
        executeMove(aiMove)
      }
    }
  }

  fun requestHint() {
    val state = _uiState.value
    if (state.gameState.isGameOver || state.isAiThinking) return

    viewModelScope.launch {
      val bestMove = withContext(Dispatchers.Default) {
        ChessAI.findBestMove(state.gameState, depth = 3, randomnessChance = 0f)
      }
      _uiState.update { it.copy(hintMove = bestMove) }
    }
  }

  fun undoMove() {
    val state = _uiState.value
    if (state.isAiThinking) return

    if (state.isVsAi) {
      // Undo both AI move and Player move
      engine.undo()
      engine.undo()
    } else {
      engine.undo()
    }

    val restoredState = engine.currentState
    val lastMove = restoredState.moveHistory.lastOrNull()
    val eval = ChessAI.evaluate(restoredState)

    _uiState.update {
      it.copy(
        gameState = restoredState,
        selectedSquare = null,
        legalMovesForSelected = emptyList(),
        lastMove = lastMove,
        hintMove = null,
        currentEvalCentipawns = eval,
        showGameOverDialog = false
      )
    }
    refreshAdaptiveParams()
  }

  private fun handleGameOver(state: GameState) {
    timerJob?.cancel()
    val ui = _uiState.value
    val isVsAi = ui.isVsAi

    val title: String
    val subtitle: String
    var eloChange: Pair<Int, Int>? = null
    var playerWon = false
    val isDraw = state.isDraw

    if (state.isCheckmate) {
      val winnerName = if (state.winner == PieceColor.WHITE) "Putih" else "Hitam"
      title = "Skakmat! $winnerName Menang!"
      playerWon = if (isVsAi) (state.winner == ui.playerColor) else true
      subtitle = if (isVsAi) {
        if (playerWon) "Selamat! Anda mengalahkan komputer!" else "Komputer memenangkan pertandingan."
      } else {
        "Kemenangan telak untuk $winnerName."
      }
      if (playerWon) soundPlayer.playVictorySound()
    } else if (state.isStalemate) {
      title = "Remis (Stalemate)!"
      subtitle = "Tidak ada langkah legal yang tersisa dan Raja tidak dalam posisi skak."
    } else {
      title = "Permainan Berakhir Seri!"
      subtitle = "Aturan 50 langkah atau materi bidak tidak mencukupi."
    }

    if (isVsAi) {
      val opponentElo = if (ui.difficultyLevel == DifficultyLevel.ADAPTIF) {
        ui.stats.elo
      } else {
        ui.difficultyLevel.estimatedElo
      }
      eloChange = adaptiveManager.recordGameResult(
        isVsAi = true,
        playerWon = playerWon,
        isDraw = isDraw,
        opponentEstimatedElo = opponentElo
      )
      loadStats()
    }

    _uiState.update {
      it.copy(
        showGameOverDialog = true,
        gameOverTitle = title,
        gameOverSubtitle = subtitle,
        gameOverEloChange = eloChange
      )
    }
  }

  fun dismissGameOverDialog() {
    _uiState.update { it.copy(showGameOverDialog = false) }
  }

  private fun startTimer() {
    timerJob?.cancel()
    if (_uiState.value.timerOptionSeconds == null) return

    timerJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        val state = _uiState.value
        if (state.gameState.isGameOver || state.timerOptionSeconds == null) break

        val isWhiteTurn = state.gameState.activeColor == PieceColor.WHITE
        if (isWhiteTurn) {
          val current = state.whiteTimeRemaining ?: continue
          val next = current - 1
          if (next <= 0) {
            handleTimeOut(PieceColor.WHITE)
            break
          } else {
            _uiState.update { it.copy(whiteTimeRemaining = next) }
          }
        } else {
          val current = state.blackTimeRemaining ?: continue
          val next = current - 1
          if (next <= 0) {
            handleTimeOut(PieceColor.BLACK)
            break
          } else {
            _uiState.update { it.copy(blackTimeRemaining = next) }
          }
        }
      }
    }
  }

  private fun handleTimeOut(color: PieceColor) {
    timerJob?.cancel()
    val loserName = if (color == PieceColor.WHITE) "Putih" else "Hitam"
    val winnerColor = color.opposite()
    val winnerName = if (winnerColor == PieceColor.WHITE) "Putih" else "Hitam"

    val title = "Waktu Habis!"
    val subtitle = "Waktu $loserName telah habis. $winnerName menang!"
    val isVsAi = _uiState.value.isVsAi
    val playerWon = if (isVsAi) (_uiState.value.playerColor == winnerColor) else false

    var eloChange: Pair<Int, Int>? = null
    if (isVsAi) {
      eloChange = adaptiveManager.recordGameResult(
        isVsAi = true,
        playerWon = playerWon,
        isDraw = false,
        opponentEstimatedElo = _uiState.value.difficultyLevel.estimatedElo
      )
      loadStats()
    }

    _uiState.update {
      it.copy(
        showGameOverDialog = true,
        gameOverTitle = title,
        gameOverSubtitle = subtitle,
        gameOverEloChange = eloChange
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
