package com.example.data.model

enum class LeaderboardTimeframe(val title: String, val subtitle: String, val emoji: String) {
    DAILY("Daily", "Resets in 18h • Today's Top Minds", "⚡"),
    WEEKLY("Weekly", "Ends in 3d • Global League Sprint", "🏆"),
    ALL_TIME("All-Time", "Hall of Fame • Global Champions", "👑")
}

enum class LeaderboardMetric(val title: String, val iconEmoji: String) {
    COINS("Coins", "🪙"),
    PUZZLES_SOLVED("Puzzles", "🧩"),
    TOTAL_XP("XP Score", "🔥")
}

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val username: String,
    val countryEmoji: String,
    val countryName: String,
    val avatarColorHex: Long,
    val avatarEmoji: String,
    val solvedPuzzlesCount: Int,
    val coinsEarned: Int,
    val totalXp: Int,
    val titleBadge: String,
    val isCurrentUser: Boolean = false
)

object LeaderboardGenerator {

    private val baseDailyChampions = listOf(
        LeaderboardEntry(1, "p_1", "Elena Rostova", "🇩🇪", "Germany", 0xFF4F46E5, "⚡", 18, 1420, 2900, "Quantum Savant"),
        LeaderboardEntry(2, "p_2", "Kenji Takahashi", "🇯🇵", "Japan", 0xFF0284C7, "🧠", 16, 1280, 2600, "Grandmaster Logic"),
        LeaderboardEntry(3, "p_3", "Aarav Sharma", "🇮🇳", "India", 0xFFD97706, "🚀", 15, 1190, 2450, "STEM Prodigy"),
        LeaderboardEntry(4, "p_4", "Sophia Martinez", "🇲🇽", "Mexico", 0xFF16A34A, "🌸", 14, 1050, 2200, "Matrix Explorer"),
        LeaderboardEntry(5, "p_5", "Lucas Dubois", "🇫🇷", "France", 0xFF9333EA, "🎨", 12, 940, 1950, "Speed Solver"),
        LeaderboardEntry(6, "p_6", "Emma Watson-Lee", "🇬🇧", "UK", 0xFFE11D48, "🔬", 11, 880, 1800, "Lab Researcher"),
        LeaderboardEntry(7, "p_7", "Liam O'Connor", "🇮🇪", "Ireland", 0xFF059669, "🍀", 9, 720, 1500, "Math Whiz"),
        LeaderboardEntry(8, "p_8", "Chen Wei", "🇨🇳", "China", 0xFF2563EB, "🐉", 8, 650, 1350, "Spatial Master"),
        LeaderboardEntry(9, "p_9", "Amara Okafor", "🇳🇬", "Nigeria", 0xFF7C3AED, "⭐", 7, 580, 1200, "Curious Explorer"),
        LeaderboardEntry(10, "p_10", "Mateo Silva", "🇧🇷", "Brazil", 0xFFEA580C, "⚽", 6, 490, 1050, "Brain Booster")
    )

    private val baseWeeklyChampions = listOf(
        LeaderboardEntry(1, "w_1", "Prof. Alexander Vance", "🇨🇦", "Canada", 0xFF4F46E5, "👑", 68, 5400, 11200, "Apex Grandmaster"),
        LeaderboardEntry(2, "w_2", "Dr. Yuna Kim", "🇰🇷", "South Korea", 0xFF9333EA, "💎", 62, 4950, 10400, "Quantum Architect"),
        LeaderboardEntry(3, "w_3", "Julian Thorne", "🇦🇺", "Australia", 0xFF0284C7, "🥇", 55, 4300, 9200, "Hall of Fame Titan"),
        LeaderboardEntry(4, "w_4", "Elena Rostova", "🇩🇪", "Germany", 0xFF4F46E5, "⚡", 51, 4100, 8600, "Quantum Savant"),
        LeaderboardEntry(5, "w_5", "Maya Lin", "🇺🇸", "USA", 0xFF059669, "⚡", 48, 3850, 8100, "STEM Virtuoso"),
        LeaderboardEntry(6, "w_6", "Viktor Novak", "🇨🇿", "Czech Republic", 0xFFD97706, "🧠", 42, 3300, 7100, "Logic Legend"),
        LeaderboardEntry(7, "w_7", "Kenji Takahashi", "🇯🇵", "Japan", 0xFF0284C7, "🧠", 39, 3100, 6600, "Grandmaster Logic"),
        LeaderboardEntry(8, "w_8", "Fatima Al-Mansoor", "🇦🇪", "UAE", 0xFFE11D48, "🌟", 36, 2800, 6000, "Speed Champion"),
        LeaderboardEntry(9, "w_9", "Aarav Sharma", "🇮🇳", "India", 0xFFD97706, "🚀", 34, 2650, 5700, "STEM Prodigy"),
        LeaderboardEntry(10, "w_10", "Dmitri Volkov", "🇵🇱", "Poland", 0xFF2563EB, "🛡️", 31, 2400, 5200, "Puzzle Master")
    )

    private val baseAllTimeLegends = listOf(
        LeaderboardEntry(1, "all_1", "Prof. Alexander Vance", "🇨🇦", "Canada", 0xFF4F46E5, "👑", 240, 18500, 39400, "Apex Grandmaster"),
        LeaderboardEntry(2, "all_2", "Dr. Yuna Kim", "🇰🇷", "South Korea", 0xFF9333EA, "💎", 215, 16800, 35200, "Quantum Architect"),
        LeaderboardEntry(3, "all_3", "Julian Thorne", "🇦🇺", "Australia", 0xFF0284C7, "🥇", 198, 15400, 32100, "Hall of Fame Titan"),
        LeaderboardEntry(4, "all_4", "Maya Lin", "🇺🇸", "USA", 0xFF059669, "⚡", 182, 14100, 29800, "STEM Virtuoso"),
        LeaderboardEntry(5, "all_5", "Viktor Novak", "🇨🇿", "Czech Republic", 0xFFD97706, "🧠", 165, 12900, 27500, "Logic Legend"),
        LeaderboardEntry(6, "all_6", "Fatima Al-Mansoor", "🇦🇪", "UAE", 0xFFE11D48, "🌟", 150, 11800, 24900, "Speed Champion"),
        LeaderboardEntry(7, "all_7", "Dmitri Volkov", "🇵🇱", "Poland", 0xFF2563EB, "🛡️", 138, 10700, 22600, "Puzzle Master"),
        LeaderboardEntry(8, "all_8", "Isabella Rossi", "🇮🇹", "Italy", 0xFF16A34A, "🎭", 125, 9600, 20400, "Matrix Pioneer"),
        LeaderboardEntry(9, "all_9", "Oliver Hansen", "🇩🇰", "Denmark", 0xFF7C3AED, "⚓", 112, 8900, 18700, "Agile Mind"),
        LeaderboardEntry(10, "all_10", "Zainab Khan", "🇵🇰", "Pakistan", 0xFFEA580C, "🎯", 98, 7800, 16500, "Rising Star")
    )

    fun getLeaderboard(
        timeframe: LeaderboardTimeframe,
        metric: LeaderboardMetric,
        userProgress: UserProgress
    ): Pair<List<LeaderboardEntry>, LeaderboardEntry> {
        val baseList = when (timeframe) {
            LeaderboardTimeframe.DAILY -> baseDailyChampions
            LeaderboardTimeframe.WEEKLY -> baseWeeklyChampions
            LeaderboardTimeframe.ALL_TIME -> baseAllTimeLegends
        }

        val userSolved = userProgress.totalPuzzlesSolved
        val userCoins = userProgress.coins
        val userXp = userProgress.totalXp

        val currentUserEntry = LeaderboardEntry(
            rank = 1,
            userId = "current_user",
            username = "${userProgress.userName} (You)",
            countryEmoji = userProgress.countryEmoji,
            countryName = "Global",
            avatarColorHex = 0xFF4F46E5,
            avatarEmoji = userProgress.userAvatarEmoji,
            solvedPuzzlesCount = userSolved,
            coinsEarned = userCoins,
            totalXp = userXp,
            titleBadge = userProgress.userTitle,
            isCurrentUser = true
        )

        val combined = (baseList + currentUserEntry).toMutableList()

        when (metric) {
            LeaderboardMetric.COINS -> combined.sortByDescending { it.coinsEarned }
            LeaderboardMetric.PUZZLES_SOLVED -> combined.sortByDescending { it.solvedPuzzlesCount }
            LeaderboardMetric.TOTAL_XP -> combined.sortByDescending { it.totalXp }
        }

        val rankedList = combined.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }

        val userRanked = rankedList.firstOrNull { it.isCurrentUser } ?: currentUserEntry
        return Pair(rankedList, userRanked)
    }
}
