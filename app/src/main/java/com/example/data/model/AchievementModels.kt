package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Achievement Badge Tiers with distinct colors and styling
 */
enum class BadgeTier(
    val label: String,
    val colorHex: Long,
    val bgHex: Long,
    val starRating: Int,
    val iconEmoji: String
) {
    BRONZE("Bronze Tier", 0xFFB45309, 0xFFFEF3C7, 1, "🥉"),
    SILVER("Silver Tier", 0xFF64748B, 0xFFF1F5F9, 2, "🥈"),
    GOLD("Gold Tier", 0xFFD97706, 0xFFFEFCE8, 3, "🥇"),
    PLATINUM("Platinum Tier", 0xFF0284C7, 0xFFE0F2FE, 4, "💠"),
    DIAMOND("Diamond Tier", 0xFF7C3AED, 0xFFF5F3FF, 5, "💎"),
    MYTHIC("Mythic Grandmaster", 0xFFDB2777, 0xFFFDF2F8, 6, "👑")
}

/**
 * Filter Categories for Achievements
 */
enum class AchievementCategory(val label: String, val iconEmoji: String) {
    ALL("All Badges", "🏆"),
    MILESTONES("Milestones", "🎯"),
    CATEGORY_MASTERY("Category Mastery", "👑"),
    SKILL_SPEED("Skill & Speed", "⚡"),
    SPECIAL("Special Feats", "✨")
}

/**
 * Individual Achievement Badge Item
 */
data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val tier: BadgeTier,
    val category: AchievementCategory,
    val currentProgress: Int,
    val targetProgress: Int,
    val isUnlocked: Boolean = currentProgress >= targetProgress,
    val rewardXp: Int,
    val rewardCoins: Int,
    val targetCategory: PuzzleCategory? = null,
    val loreDescription: String = ""
) {
    val progressFraction: Float
        get() = if (targetProgress > 0) (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}
