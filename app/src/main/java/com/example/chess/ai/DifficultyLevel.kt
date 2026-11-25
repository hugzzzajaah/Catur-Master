package com.example.chess.ai

enum class DifficultyLevel(
  val displayName: String,
  val description: String,
  val estimatedElo: Int,
  val searchDepth: Int,
  val randomnessChance: Float
) {
  ADAPTIF(
    displayName = "Otomatis / Adaptif",
    description = "AI otomatis menyesuaikan kehebatan dengan gaya dan kemampuan bermain Anda",
    estimatedElo = 1200,
    searchDepth = 3,
    randomnessChance = 0.05f
  ),
  PEMULA(
    displayName = "Pemula",
    description = "Cocok untuk yang baru belajar catur. AI sesekali melakukan kesalahan langkah.",
    estimatedElo = 800,
    searchDepth = 1,
    randomnessChance = 0.35f
  ),
  MENENGAH(
    displayName = "Menengah",
    description = "Tingkat seimbang dengan evaluasi posisional yang baik dan taktik standar.",
    estimatedElo = 1300,
    searchDepth = 2,
    randomnessChance = 0.12f
  ),
  MAHIR(
    displayName = "Mahir",
    description = "Taktik tajam, mampu melihat 3-4 langkah ke depan tanpa kesalahan ceroboh.",
    estimatedElo = 1750,
    searchDepth = 3,
    randomnessChance = 0.0f
  ),
  MASTER(
    displayName = "Master",
    description = "Perhitungan mendalam dengan pencarian taktis dan penguasaan posisi penuh.",
    estimatedElo = 2100,
    searchDepth = 4,
    randomnessChance = 0.0f
  )
}
