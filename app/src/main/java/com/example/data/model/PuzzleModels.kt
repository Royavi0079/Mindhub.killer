package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R

/**
 * Age Group presets for tailoring UI and puzzle complexity (Ages 5 to 70+)
 */
enum class AgeGroup(
    val title: String,
    val ageRange: String,
    val description: String,
    val iconEmoji: String,
    val defaultGridSize: Int = 3
) {
    JUNIOR("Junior Explorer", "Ages 5–10", "Visual puzzles, foundational math, fun nature facts & big buttons", "🌱", 3),
    STANDARD("Casual & Family", "Ages 11–40", "Balanced science challenges, equations & historical quests", "⚡", 3),
    MASTER("Senior & Master", "Ages 41–70+", "Cognitive agility, deep physics, molecular logic & relaxed pace", "🧠", 4)
}

/**
 * Main Subject Categories
 */
enum class PuzzleCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val accentColorHex: Long,
    val lightBgColorHex: Long,
    @DrawableRes val imageRes: Int
) {
    VISUAL("visual", "Visual & Jigsaw", "Sliding tile & spatial memory", "🧩", 0xFF4F46E5, 0xFFEEF2FF, R.drawable.img_hero_puzzle),
    MATH("math", "Mathematics", "Equations, sequences & riddles", "➗", 0xFF0284C7, 0xFFE0F2FE, R.drawable.img_math_cosmos),
    PHYSICS("physics", "Physics", "Mechanics, optics & circuits", "⚡", 0xFF059669, 0xFFECFDF5, R.drawable.img_hero_puzzle),
    CHEMISTRY("chemistry", "Chemistry", "Elements, molecules & reactions", "🧪", 0xFF9333EA, 0xFFFAF5FF, R.drawable.img_science_lab),
    BIOLOGY("biology", "Biology", "Anatomy, DNA, cells & ecology", "🧬", 0xFF16A34A, 0xFFF0FDF4, R.drawable.img_science_lab),
    HISTORY("history", "History", "Timelines, civilizations & wonders", "🏛️", 0xFFD97706, 0xFFFFFBEB, R.drawable.img_ancient_history),
    AI_LAB("ai_lab", "AI Puzzle Lab", "Endless AI-generated challenges", "✨", 0xFFE11D48, 0xFFFFF1F2, R.drawable.img_math_cosmos)
}

/**
 * Puzzle Type Format
 */
enum class PuzzleType {
    SLIDING_IMAGE,      // Interactive sliding tile puzzle
    MULTIPLE_CHOICE,    // Concept or calculation choice
    SEQUENCE_ORDER,     // Arrange items in correct chronological / logical order
    PAIR_MATCHING,      // Match element to symbol, DNA pair, or organ to function
    EQUATION_BUILDER    // Fill in missing numbers or operators
}

/**
 * Three Difficulty Tiers (Normal, Hard, Extreme)
 */
enum class Difficulty(
    val label: String,
    val stars: Int,
    val xpReward: Int,
    val tierCode: String,
    @DrawableRes val tierImageRes: Int,
    val colorHex: Long,
    val description: String,
    val coinsReward: Int
) {
    NORMAL("Normal", 1, 100, "Tier I", R.drawable.img_puzzle_normal, 0xFF0284C7, "Balanced logic & foundational concepts", 10),
    HARD("Hard", 2, 200, "Tier II", R.drawable.img_puzzle_hard, 0xFFD97706, "Complex formulas & multi-step deductions", 30),
    EXTREME("Extreme", 3, 400, "Tier III - Grandmaster", R.drawable.img_puzzle_extreme, 0xFF9333EA, "Quantum physics, advanced synthesis & deep mindbenders", 50);

    val title: String get() = label
    val tierEmoji: String get() = when(this) {
        NORMAL -> "🟢"
        HARD -> "🟡"
        EXTREME -> "🟣"
    }

    companion object {
        val EASY = NORMAL
        val MEDIUM = HARD
    }
}

/**
 * Individual Puzzle Item
 */
data class PuzzleItem(
    val id: String,
    val category: PuzzleCategory,
    val type: PuzzleType,
    val title: String,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val sequenceItems: List<String> = emptyList(), // For timeline/order puzzles
    val pairItems: List<Pair<String, String>> = emptyList(), // For matching pairs
    val hint: String,
    val explanation: String,
    val funFact: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val level: Int = 1, // Progressive Level 1 to 7
    val targetAge: AgeGroup = AgeGroup.STANDARD,
    @DrawableRes val imageRes: Int? = null
)

/**
 * Profile Background Theme Presets
 */
enum class ProfileBackgroundTheme(
    val id: String,
    val title: String,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val emoji: String
) {
    COSMIC_NEBULA("COSMIC_NEBULA", "Cosmic Nebula", "Deep space galaxy with violet star dust", 0xFF1E1B4B, 0xFF312E81, 0xFF818CF8, "🌌"),
    CYBERPUNK("CYBERPUNK", "Cyberpunk Grid", "Neon cyan and electric magenta grid", 0xFF0F172A, 0xFF1E293B, 0xFF06B6D4, "⚡"),
    GOLDEN_AURORA("GOLDEN_AURORA", "Golden Aurora", "Radiant amber & regal gold luxury glow", 0xFF451A03, 0xFF78350F, 0xFFF59E0B, "👑"),
    EMERALD_MATRIX("EMERALD_MATRIX", "Emerald Matrix", "Quantum green neural node circuit", 0xFF064E3B, 0xFF065F46, 0xFF10B981, "💎"),
    OCEANIC_ABYSS("OCEANIC_ABYSS", "Oceanic Abyss", "Bioluminescent deep ocean sapphire", 0xFF082F49, 0xFF0C4A6E, 0xFF38BDF8, "🌊"),
    SOLAR_FLARE("SOLAR_FLARE", "Solar Flare", "Energetic crimson & molten plasma", 0xFF4C0519, 0xFF881337, 0xFFF43F5E, "🔥"),
    MINIMAL_SLATE("MINIMAL_SLATE", "Minimal Slate", "Clean executive titanium dark obsidian", 0xFF1E293B, 0xFF334155, 0xFF94A3B8, "🛡️")
}

/**
 * Avatar Glow Border Presets
 */
enum class AvatarBorderTier(
    val id: String,
    val title: String,
    val colorHex: Long,
    val glowColorHex: Long
) {
    NEON_CYAN("NEON_CYAN", "Neon Azure", 0xFF06B6D4, 0xFF67E8F9),
    GOLD_ROYAL("GOLD_ROYAL", "Celestial Gold", 0xFFF59E0B, 0xFFFDE68A),
    CYBER_VIOLET("CYBER_VIOLET", "Cyber Violet", 0xFF8B5CF6, 0xFFDDD6FE),
    EMERALD_SPARK("EMERALD_SPARK", "Emerald Spark", 0xFF10B981, 0xFFA7F3D0),
    CRIMSON_FLAME("CRIMSON_FLAME", "Crimson Flame", 0xFFF43F5E, 0xFFFECDD3),
    TITANIUM("TITANIUM", "Titanium Sleek", 0xFF64748B, 0xFFCBD5E1)
}

/**
 * User Progression & Category Statistics
 */
data class UserProgress(
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
    val selectedAgeGroup: AgeGroup = AgeGroup.STANDARD,
    val totalXp: Int = 0,
    val coins: Int = 100,
    val currentStreak: Int = 1,
    val longestStreak: Int = 5,
    val totalPuzzlesSolved: Int = 0,
    val starsEarned: Int = 0,
    val averageSolveTimeSeconds: Int = 42,
    val weeklyActivitySolved: List<Int> = listOf(3, 5, 8, 4, 10, 6, 7),
    val communityContributedCount: Int = 3,
    val categorySolvedCounts: Map<String, Int> = emptyMap(),
    val unlockedBadges: List<String> = listOf("Curious Starter"),
    val unlockedBadgeIds: Set<String> = setOf("badge_first_step"),
    val claimedTaskIds: Set<String> = emptySet(),
    val adsWatchedCount: Int = 0,
    val adsRewardClaimed: Boolean = false,
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

/**
 * Game session result
 */
data class GameResult(
    val isSuccess: Boolean,
    val scoreEarned: Int,
    val timeTakenSeconds: Int,
    val movesCount: Int = 0,
    val explanation: String = "",
    val funFact: String = ""
)
