package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Long,
    val stars: Int,
    val bestMoves: Int,
    val bestTimeSeconds: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
    @PrimaryKey val dateAndTier: String, // e.g. "2026-09-29_1"
    val dateString: String,
    val tier: Int,
    val completed: Boolean,
    val timeSeconds: Int
)

@Entity(tableName = "player_stats")
data class PlayerStatsEntity(
    @PrimaryKey val id: Int = 1,
    val highestUnlockedLevel: Long = 1,
    val totalStars: Int = 0,
    val totalLevelsCleared: Int = 0,
    val blitzHighScore: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val themeIndex: Int = 0 // 0: Neon Night, 1: Minimal Zen, 2: Nordic Frost, 3: Sunset Glow
)

@Dao
interface LevelDao {
    @Query("SELECT * FROM level_progress")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getProgressForLevel(levelNumber: Long): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: LevelProgressEntity)

    @Query("SELECT * FROM daily_records WHERE dateString = :dateString")
    fun getDailyRecords(dateString: String): Flow<List<DailyRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRecord(record: DailyRecordEntity)

    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    fun getPlayerStats(): Flow<PlayerStatsEntity?>

    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    suspend fun getPlayerStatsDirect(): PlayerStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePlayerStats(stats: PlayerStatsEntity)
}

@Database(
    entities = [
        LevelProgressEntity::class,
        DailyRecordEntity::class,
        PlayerStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun levelDao(): LevelDao
}
