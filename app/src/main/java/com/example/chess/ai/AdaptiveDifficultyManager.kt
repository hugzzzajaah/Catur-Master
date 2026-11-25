package com.example.chess.ai

import android.content.Context
import android.content.SharedPreferences

data class PlayerStats(
  val elo: Int = 1200,
  val winsVsAi: Int = 0,
  val lossesVsAi: Int = 0,
  val drawsVsAi: Int = 0,
  val winsVsHuman: Int = 0,
  val lossesVsHuman: Int = 0,
  val winStreak: Int = 0,
  val bestStreak: Int = 0
) {
  val totalGamesVsAi: Int get() = winsVsAi + lossesVsAi + drawsVsAi
  val winRateVsAi: Float get() = if (totalGamesVsAi > 0) (winsVsAi.toFloat() / totalGamesVsAi) * 100f else 0f
}

data class AdaptiveSettings(
  val effectiveDepth: Int,
  val randomnessChance: Float,
  val dynamicLabel: String,
  val feedbackHint: String
)

class AdaptiveDifficultyManager(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("chess_master_stats", Context.MODE_PRIVATE)

  fun getStats(): PlayerStats {
    return PlayerStats(
      elo = prefs.getInt("player_elo", 1200),
      winsVsAi = prefs.getInt("wins_ai", 0),
      lossesVsAi = prefs.getInt("losses_ai", 0),
      drawsVsAi = prefs.getInt("draws_ai", 0),
      winsVsHuman = prefs.getInt("wins_human", 0),
      lossesVsHuman = prefs.getInt("losses_human", 0),
      winStreak = prefs.getInt("win_streak", 0),
      bestStreak = prefs.getInt("best_streak", 0)
    )
  }

  fun calculateAdaptiveParameters(currentEvalCentipawns: Int): AdaptiveSettings {
    val stats = getStats()
    val elo = stats.elo

    // Base parameters derived from player's current Elo
    val baseDepth: Int
    val baseRandomness: Float
    val label: String

    when {
      elo < 900 -> {
        baseDepth = 1
        baseRandomness = 0.30f
        label = "Adaptif: Santai / Pemula (${elo} ELO)"
      }
      elo < 1250 -> {
        baseDepth = 2
        baseRandomness = 0.15f
        label = "Adaptif: Seimbang / Menengah (${elo} ELO)"
      }
      elo < 1650 -> {
        baseDepth = 3
        baseRandomness = 0.04f
        label = "Adaptif: Kompetitif / Mahir (${elo} ELO)"
      }
      else -> {
        baseDepth = 4
        baseRandomness = 0.0f
        label = "Adaptif: Elite / Master (${elo} ELO)"
      }
    }

    // Dynamic in-game adaptation
    // If player is down by more than 400 centipawns (eval < -400 for player), give slight breathing room
    val adjustedRandomness = if (currentEvalCentipawns < -400) {
      (baseRandomness + 0.15f).coerceAtMost(0.40f)
    } else if (currentEvalCentipawns > 400) {
      // Player is winning strongly; challenge them!
      (baseRandomness * 0.5f).coerceAtLeast(0.0f)
    } else {
      baseRandomness
    }

    val feedback = when {
      currentEvalCentipawns < -400 -> "AI mendeteksi tekanan tinggi; membuka peluang serangan balik untuk Anda."
      currentEvalCentipawns > 400 -> "AI mendeteksi keunggulan Anda; memperkuat pertahanan taktis!"
      else -> "AI menyesuaikan tempo seimbang sesuai rating ${elo} ELO."
    }

    return AdaptiveSettings(
      effectiveDepth = baseDepth,
      randomnessChance = adjustedRandomness,
      dynamicLabel = label,
      feedbackHint = feedback
    )
  }

  fun recordGameResult(isVsAi: Boolean, playerWon: Boolean, isDraw: Boolean, opponentEstimatedElo: Int): Pair<Int, Int> {
    val current = getStats()
    if (!isVsAi) {
      val editor = prefs.edit()
      if (playerWon) {
        editor.putInt("wins_human", current.winsVsHuman + 1)
      } else if (!isDraw) {
        editor.putInt("losses_human", current.lossesVsHuman + 1)
      }
      editor.apply()
      return current.elo to 0
    }

    // Standard Elo Rating formula (K = 32)
    val score = if (isDraw) 0.5 else if (playerWon) 1.0 else 0.0
    val expected = 1.0 / (1.0 + Math.pow(10.0, (opponentEstimatedElo - current.elo).toDouble() / 400.0))
    val kFactor = 32.0
    val deltaElo = Math.round(kFactor * (score - expected)).toInt()
    val newElo = (current.elo + deltaElo).coerceIn(400, 2800)

    val newStreak = if (playerWon) current.winStreak + 1 else if (!isDraw) 0 else current.winStreak
    val newBestStreak = Math.max(current.bestStreak, newStreak)

    val editor = prefs.edit()
    editor.putInt("player_elo", newElo)
    if (playerWon) {
      editor.putInt("wins_ai", current.winsVsAi + 1)
    } else if (isDraw) {
      editor.putInt("draws_ai", current.drawsVsAi + 1)
    } else {
      editor.putInt("losses_ai", current.lossesVsAi + 1)
    }
    editor.putInt("win_streak", newStreak)
    editor.putInt("best_streak", newBestStreak)
    editor.apply()

    return newElo to deltaElo
  }
}
