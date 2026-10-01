package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "puzzle_progress")
data class ProgressEntity(
    @PrimaryKey val id: String = "user_stats",
    val userName: String = "Alex Thinker",
    val userHandle: String = "alex_thinker",
    val userAvatarEmoji: String = "🧠",
    val userAvatarStyle: String = "EMOJI",
    val userAvatarColorHex: Long = 0xFF4F46E5,
    val userProfileBackground: String = "COSMIC_NEBULA",
    val userAvatarBorder: String = "NEON_CYAN",
    val userTitle: String = "Quantum Logician",
    val userBio: String = "Passionate STEM learner & speed solver. Targeting top 1% global rank!",
    val countryEmoji: String = "🌍",
    val ageGroup: String = "STANDARD",
    val totalXp: Int = 0,
    val coins: Int = 100,
    val currentStreak: Int = 1,
    val longestStreak: Int = 5,
    val totalSolved: Int = 0,
    val starsEarned: Int = 0,
    val averageSolveTimeSeconds: Int = 42,
    val visualSolved: Int = 0,
    val mathSolved: Int = 0,
    val physicsSolved: Int = 0,
    val chemistrySolved: Int = 0,
    val biologySolved: Int = 0,
    val historySolved: Int = 0,
    val aiLabSolved: Int = 0,
    val claimedTaskIds: String = "",
    val adsWatched: Int = 0,
    val adsRewardClaimed: Boolean = false,
    val unlockedBadgeIds: String = "badge_first_step",
    val largeTextMode: Boolean = false,
    val showTileNumbers: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val communityGoalClaimed: Boolean = false,
    val freeHintsRemaining: Int = 4,
    val hintsUsedCount: Int = 0,
    val isLoggedIn: Boolean = true,
    val userEmail: String = "",
    val sessionToken: String = "token_usr_0079",
    val lastSyncTimestamp: Long = 0L,
    val serverUrl: String = "https://mypuzzlegame.royabhay0079.workers.dev/"
)

@Entity(tableName = "solved_puzzles")
data class SolvedPuzzleEntity(
    @PrimaryKey val puzzleId: String,
    val categoryId: String,
    val solvedTimestamp: Long = System.currentTimeMillis(),
    val starsEarned: Int = 1,
    val bestMoves: Int = 0,
    val bestTimeSeconds: Int = 0
)

@Entity(tableName = "uploaded_puzzles")
data class UploadedPuzzleEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val level: Int,
    val title: String,
    val question: String,
    val optionsJson: String,
    val correctAnswer: String,
    val explanation: String,
    val hint: String,
    val type: String = "MULTIPLE_CHOICE",
    val difficulty: String = "NORMAL",
    val imageUrl: String? = null
)

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzle_progress WHERE id = 'user_stats'")
    fun getProgressFlow(): Flow<ProgressEntity?>

    @Query("SELECT * FROM puzzle_progress WHERE id = 'user_stats'")
    suspend fun getProgress(): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ProgressEntity)

    @Query("SELECT * FROM solved_puzzles")
    fun getAllSolvedPuzzlesFlow(): Flow<List<SolvedPuzzleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolvedPuzzle(solved: SolvedPuzzleEntity)

    @Query("SELECT COUNT(*) FROM solved_puzzles WHERE categoryId = :categoryId")
    suspend fun getSolvedCountForCategory(categoryId: String): Int

    @Query("SELECT * FROM uploaded_puzzles WHERE categoryId = :categoryId ORDER BY level ASC")
    fun getUploadedPuzzlesFlow(categoryId: String): Flow<List<UploadedPuzzleEntity>>

    @Query("SELECT * FROM uploaded_puzzles WHERE categoryId = :categoryId ORDER BY level ASC")
    suspend fun getUploadedPuzzles(categoryId: String): List<UploadedPuzzleEntity>

    @Query("SELECT * FROM uploaded_puzzles ORDER BY level ASC")
    suspend fun getAllUploadedPuzzles(): List<UploadedPuzzleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadedPuzzles(puzzles: List<UploadedPuzzleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadedPuzzle(puzzle: UploadedPuzzleEntity)

    @Query("SELECT MAX(level) FROM uploaded_puzzles WHERE categoryId = :categoryId")
    suspend fun getMaxUploadedLevel(categoryId: String): Int?

    @Query("DELETE FROM uploaded_puzzles")
    suspend fun clearUploadedPuzzles()
}

@Database(
    entities = [ProgressEntity::class, SolvedPuzzleEntity::class, UploadedPuzzleEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
}
