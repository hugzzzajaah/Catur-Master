package com.example.chess.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class ChessSoundPlayer {
  var isSoundEnabled: Boolean = true

  private val scope = CoroutineScope(Dispatchers.Default)

  fun playMoveSound() {
    if (!isSoundEnabled) return
    scope.launch {
      // Crisp click (800Hz decaying quickly)
      playTone(freq = 650.0, durationMs = 45, decayRate = 12.0)
    }
  }

  fun playCaptureSound() {
    if (!isSoundEnabled) return
    scope.launch {
      // Heavier thud (320Hz with punch)
      playTone(freq = 320.0, durationMs = 80, decayRate = 7.0)
    }
  }

  fun playCheckSound() {
    if (!isSoundEnabled) return
    scope.launch {
      // Two warning blips
      playTone(freq = 880.0, durationMs = 60, decayRate = 5.0)
      kotlinx.coroutines.delay(70)
      playTone(freq = 1174.0, durationMs = 90, decayRate = 4.0)
    }
  }

  fun playVictorySound() {
    if (!isSoundEnabled) return
    scope.launch {
      // C - E - G chord arpeggio
      val notes = listOf(523.25, 659.25, 783.99, 1046.50)
      for (freq in notes) {
        playTone(freq = freq, durationMs = 120, decayRate = 4.0)
        kotlinx.coroutines.delay(90)
      }
    }
  }

  private fun playTone(freq: Double, durationMs: Int, decayRate: Double) {
    try {
      val sampleRate = 22050
      val numSamples = (sampleRate * durationMs) / 1000
      val buffer = ShortArray(numSamples)

      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val progress = i.toDouble() / numSamples
        val envelope = Math.exp(-progress * decayRate)
        val sample = sin(2.0 * Math.PI * freq * t) * envelope
        buffer[i] = (sample * Short.MAX_VALUE * 0.75).toInt().toShort()
      }

      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      audioTrack.write(buffer, 0, buffer.size)
      audioTrack.play()
      // Release after playback finished
      CoroutineScope(Dispatchers.IO).launch {
        kotlinx.coroutines.delay(durationMs.toLong() + 50)
        try {
          audioTrack.stop()
          audioTrack.release()
        } catch (_: Exception) {
        }
      }
    } catch (_: Exception) {
      // Fallback silently if device audio is busy
    }
  }
}
