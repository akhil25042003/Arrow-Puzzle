package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class GameRepository(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "arrow_puzzle.db"
    ).build()

    private val dao = db.levelDao()

    val allProgress: Flow<List<LevelProgressEntity>> = dao.getAllProgress()
    val playerStats: Flow<PlayerStatsEntity?> = dao.getPlayerStats()

    fun getDailyRecords(dateString: String): Flow<List<DailyRecordEntity>> =
        dao.getDailyRecords(dateString)

    suspend fun recordLevelCompleted(
        levelNumber: Long,
        stars: Int,
        moves: Int,
        timeSeconds: Int
    ) {
        val existing = dao.getProgressForLevel(levelNumber)
        val bestStars = maxOf(existing?.stars ?: 0, stars)
        val bestMoves = if (existing != null) minOf(existing.bestMoves, moves) else moves
        val bestTime = if (existing != null) minOf(existing.bestTimeSeconds, timeSeconds) else timeSeconds

        dao.insertOrUpdateProgress(
            LevelProgressEntity(
                levelNumber = levelNumber,
                stars = bestStars,
                bestMoves = bestMoves,
                bestTimeSeconds = bestTime,
                completedAt = System.currentTimeMillis()
            )
        )

        val currentStats = dao.getPlayerStatsDirect() ?: PlayerStatsEntity()
        val nextLevel = maxOf(currentStats.highestUnlockedLevel, levelNumber + 1)
        val addedLevel = if (existing == null) 1 else 0

        dao.updatePlayerStats(
            currentStats.copy(
                highestUnlockedLevel = nextLevel,
                totalLevelsCleared = currentStats.totalLevelsCleared + addedLevel
            )
        )
    }

    suspend fun recordDailyCompleted(dateString: String, tier: Int, timeSeconds: Int) {
        dao.insertDailyRecord(
            DailyRecordEntity(
                dateAndTier = "${dateString}_$tier",
                dateString = dateString,
                tier = tier,
                completed = true,
                timeSeconds = timeSeconds
            )
        )
    }

    suspend fun updateBlitzScore(score: Int) {
        val currentStats = dao.getPlayerStatsDirect() ?: PlayerStatsEntity()
        if (score > currentStats.blitzHighScore) {
            dao.updatePlayerStats(currentStats.copy(blitzHighScore = score))
        }
    }

    suspend fun updateSettings(sound: Boolean, haptic: Boolean, theme: Int) {
        val currentStats = dao.getPlayerStatsDirect() ?: PlayerStatsEntity()
        dao.updatePlayerStats(
            currentStats.copy(
                soundEnabled = sound,
                hapticEnabled = haptic,
                themeIndex = theme
            )
        )
    }
}
