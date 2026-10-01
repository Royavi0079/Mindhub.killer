package com.example.data.model

/**
 * Task Categories in the Task & Reward Center
 */
enum class TaskCategory(val label: String, val iconEmoji: String) {
    DAILY("Daily Tasks", "📅"),
    CHALLENGES("Puzzle Quests", "🎯"),
    SPECIAL_OFFER("Special Offers", "🎁")
}

/**
 * Task Item representation
 */
data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val coinReward: Int,
    val currentProgress: Int,
    val targetProgress: Int,
    val isCompleted: Boolean = currentProgress >= targetProgress,
    val isClaimed: Boolean = false,
    val category: TaskCategory = TaskCategory.DAILY,
    val tag: String = ""
) {
    val progressFraction: Float
        get() = if (targetProgress > 0) (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f) else 0f

    val isReadyToClaim: Boolean
        get() = isCompleted && !isClaimed
}

/**
 * Watch 3 Ads for 150 Coins State
 */
data class AdRewardState(
    val adsWatched: Int = 0,
    val targetAds: Int = 3,
    val coinReward: Int = 150,
    val isClaimed: Boolean = false
) {
    val isReadyToClaim: Boolean
        get() = adsWatched >= targetAds && !isClaimed

    val progressFraction: Float
        get() = (adsWatched.toFloat() / targetAds.toFloat()).coerceIn(0f, 1f)
}
