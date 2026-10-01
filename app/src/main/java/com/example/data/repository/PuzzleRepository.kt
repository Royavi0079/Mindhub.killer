package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ProgressEntity
import com.example.data.local.SolvedPuzzleEntity
import com.example.data.model.AgeGroup
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.PuzzleType
import com.example.data.model.UserProgress
import com.example.data.remote.GeminiPuzzleService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PuzzleRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "mindmatrix_db"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.puzzleDao()
    private val dbManager = PuzzleDatabaseManager(context)

    fun getProgressStream(): Flow<UserProgress> {
        return dao.getProgressFlow().map { entity ->
            if (entity == null) {
                UserProgress()
            } else {
                val age = try { AgeGroup.valueOf(entity.ageGroup) } catch (e: Exception) { AgeGroup.STANDARD }
                val claimedSet = if (entity.claimedTaskIds.isBlank()) {
                    emptySet()
                } else {
                    entity.claimedTaskIds.split(",").filter { it.isNotBlank() }.toSet()
                }
                val badgeSet = if (entity.unlockedBadgeIds.isBlank()) {
                    setOf("badge_first_puzzle")
                } else {
                    entity.unlockedBadgeIds.split(",").filter { it.isNotBlank() }.toSet()
                }
                UserProgress(
                    userName = entity.userName,
                    userHandle = entity.userHandle,
                    userAvatarEmoji = entity.userAvatarEmoji,
                    userAvatarStyle = entity.userAvatarStyle,
                    userAvatarColorHex = entity.userAvatarColorHex,
                    userProfileBackground = entity.userProfileBackground,
                    userAvatarBorder = entity.userAvatarBorder,
                    userTitle = entity.userTitle,
                    userBio = entity.userBio,
                    countryEmoji = entity.countryEmoji,
                    selectedAgeGroup = age,
                    totalXp = entity.totalXp,
                    coins = entity.coins,
                    currentStreak = entity.currentStreak,
                    longestStreak = entity.longestStreak,
                    totalPuzzlesSolved = entity.totalSolved,
                    starsEarned = entity.starsEarned,
                    averageSolveTimeSeconds = entity.averageSolveTimeSeconds,
                    categorySolvedCounts = mapOf(
                        PuzzleCategory.VISUAL.id to entity.visualSolved,
                        PuzzleCategory.MATH.id to entity.mathSolved,
                        PuzzleCategory.PHYSICS.id to entity.physicsSolved,
                        PuzzleCategory.CHEMISTRY.id to entity.chemistrySolved,
                        PuzzleCategory.BIOLOGY.id to entity.biologySolved,
                        PuzzleCategory.HISTORY.id to entity.historySolved,
                        PuzzleCategory.AI_LAB.id to entity.aiLabSolved
                    ),
                    unlockedBadgeIds = badgeSet,
                    claimedTaskIds = claimedSet,
                    adsWatchedCount = entity.adsWatched,
                    adsRewardClaimed = entity.adsRewardClaimed,
                    largeTextMode = entity.largeTextMode,
                    showTileNumbers = entity.showTileNumbers,
                    soundEffectsEnabled = entity.soundEffectsEnabled,
                    communityGoalClaimed = entity.communityGoalClaimed,
                    freeHintsRemaining = entity.freeHintsRemaining,
                    hintsUsedCount = entity.hintsUsedCount,
                    isLoggedIn = entity.isLoggedIn,
                    userEmail = entity.userEmail,
                    sessionToken = entity.sessionToken,
                    lastSyncTimestamp = entity.lastSyncTimestamp,
                    serverUrl = entity.serverUrl
                )
            }
        }
    }

    suspend fun recordPuzzleSolved(
        puzzleId: String,
        category: PuzzleCategory,
        stars: Int,
        xpEarned: Int,
        moves: Int = 0,
        timeSeconds: Int = 0,
        coinsEarned: Int = 0
    ): List<com.example.data.model.AchievementBadge> {
        val existing = dao.getProgress() ?: ProgressEntity()
        val updatedVisual = existing.visualSolved + if (category == PuzzleCategory.VISUAL) 1 else 0
        val updatedMath = existing.mathSolved + if (category == PuzzleCategory.MATH) 1 else 0
        val updatedPhysics = existing.physicsSolved + if (category == PuzzleCategory.PHYSICS) 1 else 0
        val updatedChemistry = existing.chemistrySolved + if (category == PuzzleCategory.CHEMISTRY) 1 else 0
        val updatedBiology = existing.biologySolved + if (category == PuzzleCategory.BIOLOGY) 1 else 0
        val updatedHistory = existing.historySolved + if (category == PuzzleCategory.HISTORY) 1 else 0
        val updatedAi = existing.aiLabSolved + if (category == PuzzleCategory.AI_LAB) 1 else 0

        // Per level complete coin is 10, hard 30, and extreme 50 coins in all puzzles
        val actualCoinsEarned = if (coinsEarned > 0) coinsEarned else when (stars) {
            3 -> 50
            2 -> 30
            else -> 10
        }

        val currentBadgeSet = if (existing.unlockedBadgeIds.isBlank()) {
            mutableSetOf("badge_first_puzzle")
        } else {
            existing.unlockedBadgeIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        }

        // Temporary progress for achievement evaluation
        val tempProgress = UserProgress(
            totalXp = existing.totalXp + xpEarned,
            coins = existing.coins + actualCoinsEarned,
            totalPuzzlesSolved = existing.totalSolved + 1,
            starsEarned = existing.starsEarned + stars,
            categorySolvedCounts = mapOf(
                PuzzleCategory.VISUAL.id to updatedVisual,
                PuzzleCategory.MATH.id to updatedMath,
                PuzzleCategory.PHYSICS.id to updatedPhysics,
                PuzzleCategory.CHEMISTRY.id to updatedChemistry,
                PuzzleCategory.BIOLOGY.id to updatedBiology,
                PuzzleCategory.HISTORY.id to updatedHistory,
                PuzzleCategory.AI_LAB.id to updatedAi
            ),
            unlockedBadgeIds = currentBadgeSet
        )

        val evaluatedBadges = AchievementCatalog.evaluateBadges(tempProgress)
        val newlyUnlockedBadges = mutableListOf<com.example.data.model.AchievementBadge>()

        var achievementBonusXp = 0
        var achievementBonusCoins = 0

        for (badge in evaluatedBadges) {
            if (badge.isUnlocked && !currentBadgeSet.contains(badge.id)) {
                currentBadgeSet.add(badge.id)
                newlyUnlockedBadges.add(badge)
                achievementBonusXp += badge.rewardXp
                achievementBonusCoins += badge.rewardCoins
            }
        }

        val updatedProgress = existing.copy(
            totalXp = existing.totalXp + xpEarned + achievementBonusXp,
            coins = existing.coins + actualCoinsEarned + achievementBonusCoins,
            totalSolved = existing.totalSolved + 1,
            starsEarned = existing.starsEarned + stars,
            visualSolved = updatedVisual,
            mathSolved = updatedMath,
            physicsSolved = updatedPhysics,
            chemistrySolved = updatedChemistry,
            biologySolved = updatedBiology,
            historySolved = updatedHistory,
            aiLabSolved = updatedAi,
            unlockedBadgeIds = currentBadgeSet.joinToString(",")
        )
        dao.saveProgress(updatedProgress)
        dao.insertSolvedPuzzle(
            SolvedPuzzleEntity(
                puzzleId = puzzleId,
                categoryId = category.id,
                starsEarned = stars,
                bestMoves = moves,
                bestTimeSeconds = timeSeconds
            )
        )

        return newlyUnlockedBadges
    }

    suspend fun claimTaskReward(taskId: String, coinReward: Int) {
        val current = dao.getProgress() ?: ProgressEntity()
        val existingClaimed = if (current.claimedTaskIds.isBlank()) {
            mutableSetOf()
        } else {
            current.claimedTaskIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        }
        if (existingClaimed.add(taskId)) {
            val updated = current.copy(
                coins = current.coins + coinReward,
                claimedTaskIds = existingClaimed.joinToString(",")
            )
            dao.saveProgress(updated)
        }
    }

    suspend fun claimAllTasks(tasksToClaim: List<Pair<String, Int>>) {
        if (tasksToClaim.isEmpty()) return
        val current = dao.getProgress() ?: ProgressEntity()
        val existingClaimed = if (current.claimedTaskIds.isBlank()) {
            mutableSetOf()
        } else {
            current.claimedTaskIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        }
        var totalCoinsToAdd = 0
        for ((taskId, reward) in tasksToClaim) {
            if (existingClaimed.add(taskId)) {
                totalCoinsToAdd += reward
            }
        }
        val updated = current.copy(
            coins = current.coins + totalCoinsToAdd,
            claimedTaskIds = existingClaimed.joinToString(",")
        )
        dao.saveProgress(updated)
    }

    suspend fun addBonusCoinsAndXp(bonusCoins: Int, bonusXp: Int) {
        val current = dao.getProgress() ?: ProgressEntity()
        val updated = current.copy(
            coins = current.coins + bonusCoins,
            totalXp = current.totalXp + bonusXp
        )
        dao.saveProgress(updated)
    }

    suspend fun consumeHintAndCheckInterstitial(): Boolean {
        val current = dao.getProgress() ?: ProgressEntity()
        val newHintsRemaining = (current.freeHintsRemaining - 1).coerceAtLeast(0)
        val newHintsUsed = current.hintsUsedCount + 1
        // Interstitial should trigger every 4 hints used or when hints run out
        val shouldShowInterstitial = (newHintsUsed % 4 == 0) || (newHintsRemaining == 0)
        val updated = current.copy(
            freeHintsRemaining = newHintsRemaining,
            hintsUsedCount = newHintsUsed
        )
        dao.saveProgress(updated)
        return shouldShowInterstitial
    }

    suspend fun grantBonusHints(count: Int = 4) {
        val current = dao.getProgress() ?: ProgressEntity()
        val updated = current.copy(
            freeHintsRemaining = current.freeHintsRemaining + count
        )
        dao.saveProgress(updated)
    }

    suspend fun grantLevelBonusCoins(bonusCoins: Int = 100): List<com.example.data.model.AchievementBadge> {
        val current = dao.getProgress() ?: ProgressEntity()
        val currentBadgeSet = if (current.unlockedBadgeIds.isBlank()) {
            mutableSetOf()
        } else {
            current.unlockedBadgeIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        }

        val newAdCount = (current.adsWatched + 1).coerceAtLeast(1)
        val tempProgress = UserProgress(
            totalXp = current.totalXp,
            coins = current.coins + bonusCoins,
            starsEarned = current.starsEarned,
            totalPuzzlesSolved = current.totalSolved,
            currentStreak = current.currentStreak,
            adsWatchedCount = newAdCount,
            hintsUsedCount = current.hintsUsedCount,
            claimedTaskIds = if (current.claimedTaskIds.isBlank()) emptySet() else current.claimedTaskIds.split(",").toSet(),
            categorySolvedCounts = mapOf(
                PuzzleCategory.VISUAL.id to current.visualSolved,
                PuzzleCategory.MATH.id to current.mathSolved,
                PuzzleCategory.PHYSICS.id to current.physicsSolved,
                PuzzleCategory.CHEMISTRY.id to current.chemistrySolved,
                PuzzleCategory.BIOLOGY.id to current.biologySolved,
                PuzzleCategory.HISTORY.id to current.historySolved,
                PuzzleCategory.AI_LAB.id to current.aiLabSolved
            ),
            unlockedBadgeIds = currentBadgeSet
        )

        val evaluatedBadges = AchievementCatalog.evaluateBadges(tempProgress)
        val newlyUnlockedBadges = mutableListOf<com.example.data.model.AchievementBadge>()
        var achievementBonusXp = 0
        var achievementBonusCoins = 0

        for (badge in evaluatedBadges) {
            if (badge.isUnlocked && !currentBadgeSet.contains(badge.id)) {
                currentBadgeSet.add(badge.id)
                newlyUnlockedBadges.add(badge)
                achievementBonusXp += badge.rewardXp
                achievementBonusCoins += badge.rewardCoins
            }
        }

        val updated = current.copy(
            coins = current.coins + bonusCoins + achievementBonusCoins,
            totalXp = current.totalXp + achievementBonusXp,
            adsWatched = (current.adsWatched + 1).coerceAtMost(3),
            unlockedBadgeIds = currentBadgeSet.joinToString(",")
        )
        dao.saveProgress(updated)
        return newlyUnlockedBadges
    }

    suspend fun recordAdWatched(bonusCoins: Int = 50): Pair<Int, List<com.example.data.model.AchievementBadge>> {
        val current = dao.getProgress() ?: ProgressEntity()
        val newAdCount = current.adsWatched + 1
        val currentBadgeSet = if (current.unlockedBadgeIds.isBlank()) {
            mutableSetOf()
        } else {
            current.unlockedBadgeIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        }

        val tempProgress = UserProgress(
            totalXp = current.totalXp,
            coins = current.coins + bonusCoins,
            starsEarned = current.starsEarned,
            totalPuzzlesSolved = current.totalSolved,
            currentStreak = current.currentStreak,
            adsWatchedCount = newAdCount,
            hintsUsedCount = current.hintsUsedCount,
            claimedTaskIds = if (current.claimedTaskIds.isBlank()) emptySet() else current.claimedTaskIds.split(",").toSet(),
            categorySolvedCounts = mapOf(
                PuzzleCategory.VISUAL.id to current.visualSolved,
                PuzzleCategory.MATH.id to current.mathSolved,
                PuzzleCategory.PHYSICS.id to current.physicsSolved,
                PuzzleCategory.CHEMISTRY.id to current.chemistrySolved,
                PuzzleCategory.BIOLOGY.id to current.biologySolved,
                PuzzleCategory.HISTORY.id to current.historySolved,
                PuzzleCategory.AI_LAB.id to current.aiLabSolved
            ),
            unlockedBadgeIds = currentBadgeSet
        )

        val evaluatedBadges = AchievementCatalog.evaluateBadges(tempProgress)
        val newlyUnlockedBadges = mutableListOf<com.example.data.model.AchievementBadge>()
        var achievementBonusXp = 0
        var achievementBonusCoins = 0

        for (badge in evaluatedBadges) {
            if (badge.isUnlocked && !currentBadgeSet.contains(badge.id)) {
                currentBadgeSet.add(badge.id)
                newlyUnlockedBadges.add(badge)
                achievementBonusXp += badge.rewardXp
                achievementBonusCoins += badge.rewardCoins
            }
        }

        val updated = current.copy(
            adsWatched = newAdCount.coerceAtMost(3),
            coins = current.coins + bonusCoins + achievementBonusCoins,
            totalXp = current.totalXp + achievementBonusXp,
            unlockedBadgeIds = currentBadgeSet.joinToString(",")
        )
        dao.saveProgress(updated)
        return Pair(updated.adsWatched, newlyUnlockedBadges)
    }

    suspend fun claim150AdsReward() {
        val current = dao.getProgress() ?: ProgressEntity()
        if (current.adsWatched >= 3 && !current.adsRewardClaimed) {
            val existingClaimed = if (current.claimedTaskIds.isBlank()) {
                mutableSetOf()
            } else {
                current.claimedTaskIds.split(",").filter { it.isNotBlank() }.toMutableSet()
            }
            existingClaimed.add("task_watch_3_ads")
            val updated = current.copy(
                coins = current.coins + 150,
                adsRewardClaimed = true,
                claimedTaskIds = existingClaimed.joinToString(",")
            )
            dao.saveProgress(updated)
        }
    }

    suspend fun resetAdsCycle() {
        val current = dao.getProgress() ?: ProgressEntity()
        val existingClaimed = if (current.claimedTaskIds.isBlank()) {
            mutableSetOf()
        } else {
            current.claimedTaskIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        }
        existingClaimed.remove("task_watch_3_ads")
        val updated = current.copy(
            adsWatched = 0,
            adsRewardClaimed = false,
            claimedTaskIds = existingClaimed.joinToString(",")
        )
        dao.saveProgress(updated)
    }

    suspend fun updateAgeGroup(ageGroup: AgeGroup) {
        val current = dao.getProgress() ?: ProgressEntity()
        dao.saveProgress(current.copy(ageGroup = ageGroup.name))
    }

    suspend fun toggleLargeText(enabled: Boolean) {
        val current = dao.getProgress() ?: ProgressEntity()
        dao.saveProgress(current.copy(largeTextMode = enabled))
    }

    suspend fun toggleTileNumbers(enabled: Boolean) {
        val current = dao.getProgress() ?: ProgressEntity()
        dao.saveProgress(current.copy(showTileNumbers = enabled))
    }

    suspend fun toggleSoundEffects(enabled: Boolean) {
        val current = dao.getProgress() ?: ProgressEntity()
        dao.saveProgress(current.copy(soundEffectsEnabled = enabled))
    }

    suspend fun updateUserProfile(
        name: String,
        handle: String = "alex_thinker",
        avatarEmoji: String = "🧠",
        avatarStyle: String = "EMOJI",
        avatarColorHex: Long = 0xFF4F46E5,
        profileBackground: String = "COSMIC_NEBULA",
        avatarBorder: String = "NEON_CYAN",
        title: String = "Quantum Logician",
        bio: String = "",
        countryEmoji: String = "🌍"
    ) {
        val current = dao.getProgress() ?: ProgressEntity()
        dao.saveProgress(
            current.copy(
                userName = name.ifBlank { "Alex Thinker" },
                userHandle = handle.ifBlank { "alex_thinker" }.removePrefix("@"),
                userAvatarEmoji = avatarEmoji,
                userAvatarStyle = avatarStyle,
                userAvatarColorHex = avatarColorHex,
                userProfileBackground = profileBackground,
                userAvatarBorder = avatarBorder,
                userTitle = title.ifBlank { "Quantum Logician" },
                userBio = bio,
                countryEmoji = countryEmoji
            )
        )
    }

    suspend fun loginUser(email: String, username: String, token: String = "") {
        val current = dao.getProgress() ?: ProgressEntity()
        val formattedHandle = email.substringBefore("@").replace(".", "_").lowercase()
        val finalToken = token.ifEmpty { "token_${System.currentTimeMillis()}_${(1000..9999).random()}" }
        dao.saveProgress(
            current.copy(
                isLoggedIn = true,
                userEmail = email,
                userName = username.ifBlank { current.userName },
                userHandle = formattedHandle.ifBlank { current.userHandle },
                sessionToken = finalToken,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun logoutUser() {
        val current = dao.getProgress() ?: ProgressEntity()
        dao.saveProgress(
            current.copy(
                isLoggedIn = false,
                sessionToken = "",
                lastSyncTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun markLastSync() {
        val current = dao.getProgress() ?: ProgressEntity()
        dao.saveProgress(
            current.copy(lastSyncTimestamp = System.currentTimeMillis())
        )
    }

    suspend fun recordFeedbackReward(xpBonus: Int = 50, coinsBonus: Int = 20) {
        val current = dao.getProgress() ?: ProgressEntity()
        dao.saveProgress(
            current.copy(
                coins = current.coins + coinsBonus,
                totalXp = current.totalXp + xpBonus
            )
        )
    }

    suspend fun claimCommunityGoal(rewardCoins: Int, rewardXp: Int) {
        val current = dao.getProgress() ?: ProgressEntity()
        if (!current.communityGoalClaimed) {
            dao.saveProgress(
                current.copy(
                    coins = current.coins + rewardCoins,
                    totalXp = current.totalXp + rewardXp,
                    communityGoalClaimed = true
                )
            )
        }
    }

    suspend fun generateDynamicAIPuzzle(
        category: PuzzleCategory,
        ageGroup: AgeGroup,
        difficulty: Difficulty
    ): PuzzleItem {
        val aiGenerated = GeminiPuzzleService.generateAIPuzzle(category, ageGroup, difficulty)
        if (aiGenerated != null) return aiGenerated

        val fallbackList = getFallbackPuzzles(category, ageGroup).filter { it.difficulty == difficulty }
        val chosen = fallbackList.randomOrNull() ?: getFallbackPuzzles(category, ageGroup).random()
        return chosen.copy(
            id = "offline_ai_${System.currentTimeMillis()}",
            difficulty = difficulty,
            imageRes = difficulty.tierImageRes
        )
    }

    suspend fun getAiHint(question: String, options: List<String>): String {
        return GeminiPuzzleService.askGeminiForDeepHint(question, options)
    }

    /**
     * Get puzzles filtered by category and age bracket.
     * Enforces exactly 15 base puzzles per section.
     * When puzzles are uploaded to the Room database, new levels (16, 17, etc.) are dynamically loaded.
     */
    fun getPuzzlesForCategory(category: PuzzleCategory, ageGroup: AgeGroup): List<PuzzleItem> {
        val curated = getFallbackPuzzles(category, ageGroup).filter { it.category == category }
        val curatedMaxLevel = curated.maxOfOrNull { it.level } ?: 7
        val needed = (15 - curatedMaxLevel).coerceAtLeast(0)
        val procedural = if (needed > 0) {
            ProceduralPuzzleEngine.getPuzzlesForRange(
                category = category,
                startLevel = curatedMaxLevel + 1,
                count = needed,
                ageGroup = ageGroup
            )
        } else emptyList()
        val base15 = (curated + procedural).take(15).sortedBy { it.level }

        // Fetch any dynamic puzzle levels uploaded to Room database
        val uploadedPuzzles = try {
            kotlinx.coroutines.runBlocking {
                dbManager.getUploadedPuzzleItems(category)
            }
        } catch (e: Exception) {
            emptyList()
        }

        return (base15 + uploadedPuzzles).sortedBy { it.level }
    }

    suspend fun importPuzzlesFromUrl(url: String): Result<Int> {
        return dbManager.importPuzzlesFromUrl(url)
    }

    suspend fun importPuzzlesFromJson(jsonStr: String): Result<Int> {
        return dbManager.importPuzzlesFromJson(jsonStr)
    }

    suspend fun getUploadedPuzzles(category: PuzzleCategory): List<PuzzleItem> {
        return dbManager.getUploadedPuzzleItems(category)
    }

    /**
     * Paged range query for high-performance virtualized 1000+ level lists
     */
    fun getPuzzlesForCategoryRange(
        category: PuzzleCategory,
        startLevel: Int,
        count: Int,
        ageGroup: AgeGroup
    ): List<PuzzleItem> {
        val curated = getFallbackPuzzles(category, ageGroup).filter { it.category == category }
        val curatedMap = curated.associateBy { it.level }
        val result = mutableListOf<PuzzleItem>()
        for (lvl in startLevel until (startLevel + count)) {
            val item = curatedMap[lvl] ?: ProceduralPuzzleEngine.getPuzzleForLevel(category, lvl, ageGroup)
            result.add(item)
        }
        return result
    }

    /**
     * Built-in rich library of 7 progressive levels per category
     */
    private fun getFallbackPuzzles(category: PuzzleCategory, ageGroup: AgeGroup): List<PuzzleItem> {
        return when (category) {
            PuzzleCategory.VISUAL -> getVisualPuzzles()
            PuzzleCategory.MATH -> getMathPuzzles(ageGroup)
            PuzzleCategory.PHYSICS -> getPhysicsPuzzles(ageGroup)
            PuzzleCategory.CHEMISTRY -> getChemistryPuzzles(ageGroup)
            PuzzleCategory.BIOLOGY -> getBiologyPuzzles(ageGroup)
            PuzzleCategory.HISTORY -> getHistoryPuzzles(ageGroup)
            PuzzleCategory.AI_LAB -> getVisualPuzzles() + getMathPuzzles(ageGroup)
        }
    }

    // ==========================================
    // 7 PROGRESSIVE VISUAL PUZZLES (AI PHOTOS)
    // ==========================================
    private fun getVisualPuzzles(): List<PuzzleItem> = listOf(
        // Level 1: Starter (Dino Safari Adventure)
        PuzzleItem(
            id = "vis_lvl1_dino",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 1: Dino Jungle Safari",
            question = "Slide the vibrant photo tiles to assemble the friendly baby dinosaur exploring colorful prehistoric flowers!",
            hint = "Start by locating the bright smiling green dinosaur face in the center, then complete the top corners!",
            explanation = "Spatial puzzle solving with bright visual landmarks activates pattern recognition and spatial orientation in children and adults alike.",
            funFact = "Fossil records show that dinosaurs roamed the Earth for over 165 million years!",
            difficulty = Difficulty.NORMAL,
            level = 1,
            targetAge = AgeGroup.JUNIOR,
            imageRes = R.drawable.img_kids_dino_safari
        ),
        // Level 2: Explorer (Space Child Astronaut)
        PuzzleItem(
            id = "vis_lvl2_space",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 2: Cosmic Space Explorer",
            question = "Realign the cheerful kid astronaut, smiling rocket, and shiny cosmic stars!",
            hint = "Align the silver helmet and orange rocket engine first to establish your top horizontal row.",
            explanation = "Assembling contrasting celestial shapes exercises visual working memory and fine motor hand-eye coordination.",
            funFact = "There are more stars in the observable universe than grains of sand on all the beaches on Earth!",
            difficulty = Difficulty.NORMAL,
            level = 2,
            targetAge = AgeGroup.JUNIOR,
            imageRes = R.drawable.img_kids_space_explorer
        ),
        // Level 3: Apprentice (Underwater Coral Reef)
        PuzzleItem(
            id = "vis_lvl3_reef",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 3: Enchanted Coral Reef",
            question = "Assemble the magical underwater ocean scene featuring sea turtles, clownfish, and sparkling ocean treasures!",
            hint = "Find the golden treasure chest in the bottom corner and the bright orange clownfish in the center.",
            explanation = "Multi-color marine puzzles enhance visual discrimination and mental rotation agility.",
            funFact = "Coral reefs support more than 25% of all known marine life, despite covering less than 1% of the ocean floor!",
            difficulty = Difficulty.HARD,
            level = 3,
            targetAge = AgeGroup.STANDARD,
            imageRes = R.drawable.img_kids_underwater_reef
        ),
        // Level 4: Adept (Isometric Geometric Harmony)
        PuzzleItem(
            id = "vis_lvl4_geo",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 4: Geometric Harmony",
            question = "Slide the minimalist isometric blocks to assemble the pristine geometric crystalline cube!",
            hint = "Focus on the white and cyan edges of the cube first to establish the isometric horizon line.",
            explanation = "Spatial isometric puzzle solving builds visual cortex orientation and mental rotation agility.",
            funFact = "Isometric projection allows 3D objects to be represented in two dimensions without perspective distortion!",
            difficulty = Difficulty.HARD,
            level = 4,
            targetAge = AgeGroup.STANDARD,
            imageRes = R.drawable.img_puzzle_normal
        ),
        // Level 5: Expert (Ancient History Wonder)
        PuzzleItem(
            id = "vis_lvl5_ancient",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 5: Ancient History Wonder",
            question = "Reconstruct the majestic Great Pyramids and ancient architectural wonders of world history!",
            hint = "Look for the warm golden stone textures and sky horizon boundary to align the top rows.",
            explanation = "Complex visual landmark positioning strengthens the brain's hippocampus and parietal cortex.",
            funFact = "The Great Pyramid of Giza was the tallest man-made structure in the world for over 3,800 years!",
            difficulty = Difficulty.HARD,
            level = 5,
            targetAge = AgeGroup.MASTER,
            imageRes = R.drawable.img_ancient_history
        ),
        // Level 6: Master (Quantum Science Lab)
        PuzzleItem(
            id = "vis_lvl6_science",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 6: Quantum Alchemy Lab",
            question = "Restore the glowing atomic chemistry lab puzzle to pristine scientific matrix resolution!",
            hint = "Identify the illuminated glass beakers and fluorescent flasks to anchor your top coordinates.",
            explanation = "Visual reconstruction exercises activate pattern recognition circuits in the visual cortex.",
            funFact = "Solving spatial puzzles for 10 minutes a day has been shown to boost working memory retention.",
            difficulty = Difficulty.EXTREME,
            level = 6,
            targetAge = AgeGroup.MASTER,
            imageRes = R.drawable.img_science_lab
        ),
        // Level 7: Grandmaster (Mind Nexus Banner)
        PuzzleItem(
            id = "vis_lvl7_nexus",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 7: Grandmaster Cyber Nexus",
            question = "Synthesize the multi-dimensional MindMatrix hero nexus in grandmaster resolution!",
            hint = "Observe the floating mathematical symbols and DNA helix radiating symmetrically from the quantum core.",
            explanation = "Hyperdimensional visualizations stretch spatial cognition beyond standard 3-axis planes.",
            funFact = "Leonardo da Vinci solved spatial geometric riddles every morning to warm up his creative mind.",
            difficulty = Difficulty.EXTREME,
            level = 7,
            targetAge = AgeGroup.MASTER,
            imageRes = R.drawable.img_hero_puzzle
        ),
        // Level 8: Quantum Core Singularity (AI Art)
        PuzzleItem(
            id = "vis_lvl8_quantum",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 8: Quantum Fusion Core",
            question = "Assemble the glowing crystalline quantum reactor surrounded by concentric harmonic rings!",
            hint = "Center the cyan glowing crystal core first, then arrange the outer purple energy rings.",
            explanation = "Rotational symmetry and high-contrast color gradients stimulate bilateral hemisphere coordination.",
            funFact = "Quantum fusion reactors could one day generate clean energy by replicating the power of the Sun!",
            difficulty = Difficulty.EXTREME,
            level = 8,
            targetAge = AgeGroup.MASTER,
            imageRes = R.drawable.img_quantum_core
        ),
        // Level 9: Solarpunk Botanical Metropolis (AI Art)
        PuzzleItem(
            id = "vis_lvl9_cityscape",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 9: Solarpunk Sky Metropolis",
            question = "Rebuild the futuristic floating sky city with lush hanging botanical gardens and solar glass spires!",
            hint = "Align the blue sky and floating cloud lines at the top before locking in the bottom botanical towers.",
            explanation = "Complex architectural perspectives develop depth perception and fine visual discrimination.",
            funFact = "Solarpunk architecture envisions cities powered entirely by clean renewable energy and vertical forests!",
            difficulty = Difficulty.EXTREME,
            level = 9,
            targetAge = AgeGroup.MASTER,
            imageRes = R.drawable.img_cyber_cityscape
        ),
        // Level 10: Bioluminescent Abyssal Reef (AI Art)
        PuzzleItem(
            id = "vis_lvl10_deepsea",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 10: Bioluminescent Abyss",
            question = "Slide the luminous ocean tiles to construct the glowing neon jellyfish swimming through deep sea coral!",
            hint = "Look for the intense azure jellyfish bell and glowing tentacles to anchor the center grid.",
            explanation = "Dark-mode visual puzzles with concentrated luminescence enhance visual contrast sensitivity.",
            funFact = "Over 75% of deep-sea creatures produce their own light through luciferin chemical reactions!",
            difficulty = Difficulty.EXTREME,
            level = 10,
            targetAge = AgeGroup.MASTER,
            imageRes = R.drawable.img_deep_ocean
        ),
        // Level 11: Celestial Golden Pyramids (AI Art)
        PuzzleItem(
            id = "vis_lvl11_pyramids",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 11: Celestial Golden Pyramids",
            question = "Realign the monumental golden Egyptian pyramids under a sparkling twilight starry cosmos!",
            hint = "Align the triangular golden apexes of the pyramids to match the horizon boundary.",
            explanation = "Geometric pyramid alignment trains the parietal lobe for angular symmetry estimation.",
            funFact = "The pyramids of Giza are aligned with Orion's Belt and true north to within 1/15th of a degree!",
            difficulty = Difficulty.EXTREME,
            level = 11,
            targetAge = AgeGroup.MASTER,
            imageRes = R.drawable.img_ancient_pyramids
        ),
        // Level 12: Microscopic Cellular Cosmos (AI Art)
        PuzzleItem(
            id = "vis_lvl12_cell",
            category = PuzzleCategory.VISUAL,
            type = PuzzleType.SLIDING_IMAGE,
            title = "Level 12: Microscopic Cellular Cosmos",
            question = "Reconstruct the glowing 3D eukaryotic cell with mitochondria, nucleus, and illuminated DNA strands!",
            hint = "Start with the large central spherical nucleus, then connect the surrounding orange mitochondria.",
            explanation = "Organic biological shape assembly promotes topological and fluid shape recognition.",
            funFact = "The human body contains an estimated 37.2 trillion living cells working together continuously!",
            difficulty = Difficulty.EXTREME,
            level = 12,
            targetAge = AgeGroup.MASTER,
            imageRes = R.drawable.img_microscopic_cell
        )
    )

    // ==========================================
    // 7 PROGRESSIVE MATHEMATICS PUZZLES
    // ==========================================
    private fun getMathPuzzles(age: AgeGroup): List<PuzzleItem> = listOf(
        // Level 1: Starter (Kids Brain & Foundational)
        PuzzleItem(
            id = "math_lvl1",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 1: Fruit Counting Fun",
            question = "A fruit basket has 4 red apples 🍎, 3 sweet bananas 🍌, and 5 juicy oranges 🍊. If you eat 2 bananas, how many fruits are left in total?",
            options = listOf("10 fruits", "9 fruits", "11 fruits", "8 fruits"),
            correctAnswer = "10 fruits",
            hint = "Add all fruits first (4 + 3 + 5 = 12), then subtract the 2 eaten bananas!",
            explanation = "4 + 3 + 5 = 12 total fruits. After eating 2 bananas: 12 - 2 = 10 fruits remain.",
            funFact = "Bananas are botanically berries, but strawberries are not!",
            difficulty = Difficulty.NORMAL,
            level = 1,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_normal
        ),
        // Level 2: Explorer (Fractions)
        PuzzleItem(
            id = "math_lvl2",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 2: Pizza Fraction Quest",
            question = "A round pizza 🍕 is sliced into 8 equal slices. Leo eats 2 slices and Maya eats 2 slices. What fraction of the pizza is remaining?",
            options = listOf("Half (1/2)", "One fourth (1/4)", "Three fourths (3/4)", "One eighth (1/8)"),
            correctAnswer = "Half (1/2)",
            hint = "They ate 2 + 2 = 4 slices out of 8 total slices.",
            explanation = "8 - 4 = 4 slices left. 4 out of 8 is 4/8, which simplifies to 1/2 (half).",
            funFact = "Ancient Egyptian merchants used unit fractions with 1 on top for daily grain trade!",
            difficulty = Difficulty.NORMAL,
            level = 2,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_normal
        ),
        // Level 3: Apprentice (Pattern Hunter)
        PuzzleItem(
            id = "math_lvl3",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 3: Mystery Balance Scales",
            question = "If 2 Bunnies 🐰 + 1 Carrot 🥕 = 15, and 1 Carrot 🥕 = 3, what is the value of 1 Bunny?",
            options = listOf("6", "5", "7", "4"),
            correctAnswer = "6",
            hint = "Subtract 3 from 15 to get 12, then divide 12 equally between the 2 bunnies!",
            explanation = "2 Bunnies + 3 = 15 => 2 Bunnies = 12 => 1 Bunny = 6.",
            funFact = "Algebra balances both sides like a playground seesaw!",
            difficulty = Difficulty.HARD,
            level = 3,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_hard
        ),
        // Level 4: Adept (Fibonacci Spiral)
        PuzzleItem(
            id = "math_lvl4",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 4: The Golden Sequence",
            question = "What is the missing number in the Fibonacci sequence: 1, 1, 2, 3, 5, 8, 13, __, 34?",
            options = listOf("21", "20", "19", "22"),
            correctAnswer = "21",
            hint = "Each term is the sum of the two preceding numbers (8 + 13)!",
            explanation = "8 + 13 = 21. Next term 13 + 21 = 34.",
            funFact = "The Fibonacci sequence appears naturally in sunflower seed spirals, pinecones, and nautilus shells!",
            difficulty = Difficulty.HARD,
            level = 4,
            targetAge = age,
            imageRes = R.drawable.img_math_cosmos
        ),
        // Level 5: Expert (Two-Variable Equations)
        PuzzleItem(
            id = "math_lvl5",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 5: Scale Balance Equation",
            question = "If 3 Circles + 1 Square = 22, and 1 Square = 2 Circles - 3, what is the exact value of 1 Circle?",
            options = listOf("5", "4", "6", "7"),
            correctAnswer = "5",
            hint = "Substitute (2C - 3) in place of the Square in the first equation!",
            explanation = "3C + (2C - 3) = 22 => 5C - 3 = 22 => 5C = 25 => Circle = 5.",
            funFact = "Algebra comes from the Arabic word 'al-jabr', meaning 'reunion of broken parts'.",
            difficulty = Difficulty.HARD,
            level = 5,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_hard
        ),
        // Level 6: Master (Modular Arithmetic)
        PuzzleItem(
            id = "math_lvl6",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 6: Modular Cryptography",
            question = "What is the remainder when 3²⁰²⁴ is divided by 10 (the last unit digit of 3²⁰²⁴)?",
            options = listOf("1", "3", "7", "9"),
            correctAnswer = "1",
            hint = "Observe unit digits pattern for powers of 3: 3¹=3, 3²=9, 3³=27(7), 3⁴=81(1). The cycle repeats every 4 powers!",
            explanation = "Since 2024 is divisible by 4 (2024 mod 4 = 0), the units digit is the 4th in the cycle: 1.",
            funFact = "RSA public key encryption that secures world banking is built on modular exponentiation!",
            difficulty = Difficulty.EXTREME,
            level = 6,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 7: Grandmaster (Euler Polyhedra Topology)
        PuzzleItem(
            id = "math_lvl7",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 7: Euler's Polyhedral Invariant",
            question = "For any convex 3D polyhedron with 12 vertices (V) and 30 edges (E), how many faces (F) must it have according to Euler's formula V - E + F = 2?",
            options = listOf("20 faces", "18 faces", "22 faces", "24 faces"),
            correctAnswer = "20 faces",
            hint = "Substitute into Euler's formula: 12 - 30 + F = 2 => -18 + F = 2 => F = 20!",
            explanation = "Euler's Polyhedral Formula V - E + F = 2 is a foundational invariant in algebraic topology and graph theory.",
            funFact = "Leonhard Euler wrote more pages of mathematics than anyone in history, publishing even after losing his sight!",
            difficulty = Difficulty.EXTREME,
            level = 7,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 8: Combinatorial Handshake Theorem
        PuzzleItem(
            id = "math_lvl8",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 8: Combinatorial Handshake Theorem",
            question = "In a tournament of 10 chess grandmasters, each grandmaster plays exactly one match against every other grandmaster. How many total matches are played?",
            options = listOf("45 matches", "90 matches", "50 matches", "100 matches"),
            correctAnswer = "45 matches",
            hint = "Use combination formula n*(n-1)/2: (10 * 9) / 2!",
            explanation = "C(10, 2) = (10 × 9) / 2 = 45 unique pairings.",
            funFact = "Combinatorics is fundamental to designing error-correcting codes and fiber-optic data routing!",
            difficulty = Difficulty.EXTREME,
            level = 8,
            targetAge = age,
            imageRes = R.drawable.img_math_cosmos
        ),
        // Level 9: Cryptographic Prime Sieve
        PuzzleItem(
            id = "math_lvl9",
            category = PuzzleCategory.MATH,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 9: Cryptographic Prime Sieve",
            question = "Which of the following numbers is a pure PRIME number (divisible only by 1 and itself)?",
            options = listOf("97", "91", "87", "93"),
            correctAnswer = "97",
            hint = "Test divisibility: 91 = 7 × 13, 87 = 3 × 29, 93 = 3 × 31!",
            explanation = "97 cannot be divided by any prime below √97 (~9.8: 2, 3, 5, 7), so 97 is prime.",
            funFact = "Prime numbers are the 'atoms' of number theory and form the backbone of modern cybersecurity!",
            difficulty = Difficulty.EXTREME,
            level = 9,
            targetAge = age,
            imageRes = R.drawable.img_quantum_core
        )
    )

    // ==========================================
    // 7 PROGRESSIVE PHYSICS PUZZLES
    // ==========================================
    private fun getPhysicsPuzzles(age: AgeGroup): List<PuzzleItem> = listOf(
        // Level 1: Starter (Kids Physics & Magnetism)
        PuzzleItem(
            id = "phy_lvl1",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 1: Magnet Magic & Attraction",
            question = "When you bring the North pole (N) of a bar magnet close to the North pole (N) of another magnet, what happens?",
            options = listOf("They push away (Repel)", "They stick together (Attract)", "They spin in circles", "They lose gravity"),
            correctAnswer = "They push away (Repel)",
            hint = "Remember the fundamental magnetic rule: Like poles repel, Opposite poles attract!",
            explanation = "Magnetic field lines push apart when two identical magnetic poles face each other.",
            funFact = "Earth itself is a giant magnet with its own protective magnetic field shielding us from solar winds!",
            difficulty = Difficulty.NORMAL,
            level = 1,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_normal
        ),
        // Level 2: Explorer (Prism & Light)
        PuzzleItem(
            id = "phy_lvl2",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 2: Prism Light Refraction",
            question = "When white sunlight passes through a triangular glass prism, which visible color bends (refracts) the MOST?",
            options = listOf("Violet", "Red", "Green", "Yellow"),
            correctAnswer = "Violet",
            hint = "Shorter wavelengths slow down more inside dense glass, resulting in greater deflection!",
            explanation = "Violet light has the shortest visible wavelength (~400 nm) and highest refractive index, refracting most.",
            funFact = "Sir Isaac Newton proved in 1666 that prisms do not color the light, but separate existing wavelengths!",
            difficulty = Difficulty.NORMAL,
            level = 2,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_normal
        ),
        // Level 3: Apprentice (Simple Machines)
        PuzzleItem(
            id = "phy_lvl3",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 3: Pulley & Mechanical Advantage",
            question = "If you use a system with 2 supporting ropes in a pulley system to lift a 100 kg weight, how much pulling force is required?",
            options = listOf("50 kg force", "100 kg force", "25 kg force", "200 kg force"),
            correctAnswer = "50 kg force",
            hint = "Mechanical advantage of a 2-rope pulley divides the required effort force by 2!",
            explanation = "A 2-pulley system distributes the load equally across both supporting strands of rope, cutting the effort in half.",
            funFact = "Archimedes once used a complex pulley system to pull an entire fully-loaded warship onto land by himself!",
            difficulty = Difficulty.HARD,
            level = 3,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_hard
        ),
        // Level 4: Adept (Torque Equilibrium)
        PuzzleItem(
            id = "phy_lvl4",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 4: See-Saw Torque Equilibrium",
            question = "A 40 kg child sits 3 meters from the center of a see-saw. How far from the center must a 60 kg adult sit to achieve perfect rotational balance?",
            options = listOf("2.0 meters", "1.5 meters", "2.5 meters", "3.0 meters"),
            correctAnswer = "2.0 meters",
            hint = "Torque = Force x Distance. So (40 kg * 3 m) must equal (60 kg * X m)!",
            explanation = "Torque balance: 40 kg * 3 m = 120 kg·m. 120 / 60 kg = 2.0 meters.",
            funFact = "Archimedes famously stated: 'Give me a lever long enough and a fulcrum on which to place it, and I shall move the world.'",
            difficulty = Difficulty.HARD,
            level = 4,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_hard
        ),
        // Level 5: Expert (Electromagnetic Induction)
        PuzzleItem(
            id = "phy_lvl5",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 5: Faraday's Law & Induction",
            question = "What happens inside a coil of copper wire when you rapidly move a strong magnet through its center?",
            options = listOf(
                "An electric current is induced and flows through the wire",
                "The copper turns into iron",
                "The wire loses all electrical resistance",
                "The magnet loses its magnetic field permanently"
            ),
            correctAnswer = "An electric current is induced and flows through the wire",
            hint = "Changing magnetic flux induces an electromotive force (EMF) according to Faraday's Law!",
            explanation = "Faraday's Law of Electromagnetic Induction is the working principle behind modern wind turbines and hydroelectric power generators.",
            funFact = "Michael Faraday had almost no formal mathematical training, yet is considered one of history's greatest experimental physicists!",
            difficulty = Difficulty.HARD,
            level = 5,
            targetAge = age,
            imageRes = R.drawable.img_science_lab
        ),
        // Level 6: Master (Relativity & Gravitational Waves)
        PuzzleItem(
            id = "phy_lvl6",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 6: Special Relativity & Time Dilation",
            question = "According to Einstein's Special Relativity, what happens to time on a spacecraft moving near the speed of light compared to an observer on Earth?",
            options = listOf(
                "Time passes significantly slower for the moving spacecraft",
                "Time passes significantly faster for the moving spacecraft",
                "Time runs backward instantaneously",
                "Time stops completely for all universe observers"
            ),
            correctAnswer = "Time passes significantly slower for the moving spacecraft",
            hint = "Lorentz factor γ = 1 / √(1 - v²/c²) causes moving clocks to tick slower!",
            explanation = "Time dilation is a verified physical phenomenon; GPS satellites must correct for relativistic clock drift every single day.",
            funFact = "Astronauts aboard the International Space Station age about 0.005 seconds less than people on Earth every 6 months!",
            difficulty = Difficulty.EXTREME,
            level = 6,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 7: Grandmaster (Quantum Superposition)
        PuzzleItem(
            id = "phy_lvl7",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 7: Quantum Superposition & Double Slit",
            question = "In the Double-Slit experiment with single electrons fired one at a time, what creates the interference pattern on the detector screen?",
            options = listOf(
                "Each electron's wave function interferes with itself through both slits simultaneously",
                "Electrons collide with residual air particles",
                "Magnetic attraction between the slit walls",
                "Gravitational lensing by the slit barrier"
            ),
            correctAnswer = "Each electron's wave function interferes with itself through both slits simultaneously",
            hint = "Even when fired one particle at a time, quantum probability amplitudes pass through all possible paths!",
            explanation = "The electron behaves as a wave function Ψ until measured, creating self-interference.",
            funFact = "Quantum computers harness superposition and entanglement to solve certain optimization problems exponentially faster!",
            difficulty = Difficulty.EXTREME,
            level = 7,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 8: Nuclear Fusion & Stellar Nucleosynthesis
        PuzzleItem(
            id = "phy_lvl8",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 8: Solar Core Nuclear Fusion",
            question = "In the core of our Sun, what primary nuclear fusion reaction converts 600 million tons of Hydrogen per second into radiant energy?",
            options = listOf(
                "Proton-Proton chain fusing Hydrogen into Helium",
                "Uranium-235 nuclear fission",
                "Chemical coal combustion",
                "Gravitational friction of space dust"
            ),
            correctAnswer = "Proton-Proton chain fusing Hydrogen into Helium",
            hint = "Four Hydrogen nuclei (protons) fuse through intermediate deuterium steps to create one Helium nucleus + energy (E=mc²)!",
            explanation = "The p-p chain converts mass defect directly into high-energy gamma photon radiation.",
            funFact = "It takes a photon about 100,000 years to travel from the center of the Sun to its surface!",
            difficulty = Difficulty.EXTREME,
            level = 8,
            targetAge = age,
            imageRes = R.drawable.img_quantum_core
        ),
        // Level 9: Black Hole Event Horizon & Hawking Radiation
        PuzzleItem(
            id = "phy_lvl9",
            category = PuzzleCategory.PHYSICS,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 9: Black Hole Schwarzschild Radius",
            question = "What is the boundary around a black hole beyond which nothing—not even light—can escape called?",
            options = listOf("Event Horizon", "Accretion Disk", "Photon Sphere", "Ergosphere"),
            correctAnswer = "Event Horizon",
            hint = "The escape velocity at this radius equals the universal speed of light c!",
            explanation = "At the event horizon, spacetime curvature becomes so intense that all future light cones point inward toward the singularity.",
            funFact = "Stephen Hawking discovered in 1974 that quantum virtual particle pair separation causes black holes to slowly evaporate!",
            difficulty = Difficulty.EXTREME,
            level = 9,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        )
    )
    // ==========================================
    private fun getChemistryPuzzles(age: AgeGroup): List<PuzzleItem> = listOf(
        // Level 1: Starter (States of Matter)
        PuzzleItem(
            id = "chem_lvl1",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 1: Kitchen States of Matter",
            question = "When liquid water in a tea kettle boils and turns into invisible steam, what state of matter did it change into?",
            options = listOf("Gas (Vapor)", "Solid (Ice)", "Plasma", "Liquid"),
            correctAnswer = "Gas (Vapor)",
            hint = "Water molecules heat up, gain energy, and float freely into the air as a gas!",
            explanation = "Thermal energy increases kinetic movement of molecules, causing a phase change from liquid to gas.",
            funFact = "Water is the only natural substance on Earth that exists abundantly in three physical states: solid, liquid, and gas!",
            difficulty = Difficulty.NORMAL,
            level = 1,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_normal
        ),
        // Level 2: Explorer (Water Molecule Architect)
        PuzzleItem(
            id = "chem_lvl2",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 2: Molecule Architect - Water",
            question = "To assemble one molecule of pure water (H₂O), which atoms must chemically bond together?",
            options = listOf(
                "2 Hydrogen atoms + 1 Oxygen atom",
                "1 Hydrogen atom + 2 Oxygen atoms",
                "2 Helium atoms + 1 Oxide",
                "1 Carbon atom + 2 Hydrogen atoms"
            ),
            correctAnswer = "2 Hydrogen atoms + 1 Oxygen atom",
            hint = "Look at the chemical formula H₂O: 'H₂' means two Hydrogens!",
            explanation = "Two Hydrogen atoms share electrons via covalent bonds with one central Oxygen atom in a bent 104.5° angle.",
            funFact = "Water expands when it freezes, which is why ice cubes float in your drink!",
            difficulty = Difficulty.NORMAL,
            level = 2,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_normal
        ),
        // Level 3: Apprentice (pH Scale & Acids)
        PuzzleItem(
            id = "chem_lvl3",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 3: The pH Scale & Acidity",
            question = "On the standard pH scale from 0 to 14, what is the pH value of pure neutral distilled water?",
            options = listOf("pH 7 (Neutral)", "pH 0 (Strong Acid)", "pH 14 (Strong Base)", "pH 1"),
            correctAnswer = "pH 7 (Neutral)",
            hint = "Values below 7 are acidic (like lemon juice), above 7 are basic (like soap), and right in the middle is neutral!",
            explanation = "A pH of 7 represents an equal balance of hydrogen ions (H+) and hydroxide ions (OH-).",
            funFact = "Lemon juice has a pH around 2, making it over 100,000 times more acidic than pure neutral water!",
            difficulty = Difficulty.HARD,
            level = 3,
            targetAge = age,
            imageRes = R.drawable.img_science_lab
        ),
        // Level 4: Adept (Noble Gas Octet)
        PuzzleItem(
            id = "chem_lvl4",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 4: Periodic Noble Octet",
            question = "Why do Noble Gases (Helium, Neon, Argon, Krypton) almost never react with other elements in nature?",
            options = listOf(
                "Their valence electron shells are completely filled (stable octet/duet)",
                "They have too many neutrons in their core",
                "They lack protons in their atomic nucleus",
                "They freeze at room temperature"
            ),
            correctAnswer = "Their valence electron shells are completely filled (stable octet/duet)",
            hint = "Atoms bond to achieve full outer electron shells; noble gases already possess full shells!",
            explanation = "Group 18 elements have maximum valence electron stability (8 electrons, or 2 for He), giving minimal chemical reactivity.",
            funFact = "Helium was first discovered in the Sun's spectral lines before being isolated on Earth!",
            difficulty = Difficulty.HARD,
            level = 4,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_hard
        ),
        // Level 5: Expert (Conservation of Mass)
        PuzzleItem(
            id = "chem_lvl5",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 5: Conservation of Mass",
            question = "If you burn 12 grams of solid Carbon in the presence of 32 grams of Oxygen gas in a closed container, what is the exact mass of Carbon Dioxide (CO₂) produced?",
            options = listOf("44 grams", "20 grams", "24 grams", "32 grams"),
            correctAnswer = "44 grams",
            hint = "According to Lavoisier's Law of Conservation of Mass, Mass of Reactants = Mass of Products (12 + 32)!",
            explanation = "12g C + 32g O₂ = 44g CO₂. Matter is neither created nor destroyed in a chemical reaction.",
            funFact = "Antoine Lavoisier, known as the father of modern chemistry, discovered the role of oxygen in combustion in 1778!",
            difficulty = Difficulty.HARD,
            level = 5,
            targetAge = age,
            imageRes = R.drawable.img_science_lab
        ),
        // Level 6: Master (Catalysts & Activation Energy)
        PuzzleItem(
            id = "chem_lvl6",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 6: Catalysts & Activation Energy",
            question = "How does a chemical catalyst (or biological enzyme) speed up a reaction by orders of magnitude?",
            options = listOf(
                "It lowers the activation energy (Ea) required for the transition state",
                "It increases the boiling point of the solvent",
                "It gets consumed to create more fuel",
                "It cools the reaction to absolute zero"
            ),
            correctAnswer = "It lowers the activation energy (Ea) required for the transition state",
            hint = "Catalysts provide an alternative reaction pathway with lower energetic barriers without being consumed!",
            explanation = "Enzymes in our body accelerate metabolic biochemical reactions by up to 10 million times by lowering activation energy.",
            funFact = "A single molecule of the enzyme catalase in your liver can break down millions of toxic hydrogen peroxide molecules per second!",
            difficulty = Difficulty.EXTREME,
            level = 6,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 7: Grandmaster (Gibbs Free Energy)
        PuzzleItem(
            id = "chem_lvl7",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 7: Gibbs Free Energy Spontaneity",
            question = "According to thermodynamic law ΔG = ΔH - TΔS, what condition guarantees a chemical reaction is SPONTANEOUS at ALL temperatures?",
            options = listOf(
                "Exothermic (ΔH < 0) and increasing entropy (ΔS > 0)",
                "Endothermic (ΔH > 0) and decreasing entropy (ΔS < 0)",
                "Zero enthalpy (ΔH = 0) and zero entropy (ΔS = 0)",
                "High pressure with negative absolute temperature"
            ),
            correctAnswer = "Exothermic (ΔH < 0) and increasing entropy (ΔS > 0)",
            hint = "A negative ΔG means spontaneous. When ΔH is negative and -TΔS is negative, ΔG is strictly negative!",
            explanation = "When heat is released (ΔH < 0) and disorder increases (ΔS > 0), ΔG remains negative across all temperatures Kelvin.",
            funFact = "Josiah Willard Gibbs was awarded America's first PhD in engineering in 1863!",
            difficulty = Difficulty.EXTREME,
            level = 7,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 8: Electrochemistry & Nernst Equation
        PuzzleItem(
            id = "chem_lvl8",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 8: Galvanic Battery Cell & Redox",
            question = "In a standard Lithium-ion or Zinc-Copper galvanic battery, at which electrode does OXIDATION (loss of electrons) always take place?",
            options = listOf("Anode", "Cathode", "Dielectric separator", "Electrolyte salt"),
            correctAnswer = "Anode",
            hint = "Remember the mnemonic: An Ox (Anode = Oxidation) and Red Cat (Reduction = Cathode)!",
            explanation = "Electrons flow spontaneously from the anode (oxidation site) through the external circuit to the cathode.",
            funFact = "Alessandro Volta invented the first electric battery (Voltaic Pile) in 1800 using alternating zinc and copper disks!",
            difficulty = Difficulty.EXTREME,
            level = 8,
            targetAge = age,
            imageRes = R.drawable.img_science_lab
        ),
        // Level 9: Organic Polymerization & Graphene
        PuzzleItem(
            id = "chem_lvl9",
            category = PuzzleCategory.CHEMISTRY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 9: 2D Carbon Allotropes - Graphene",
            question = "What is the single-atom-thick 2D honeycomb hexagonal lattice of pure carbon atoms known for extraordinary electrical conductivity and strength?",
            options = listOf("Graphene", "Diamond", "Fullerene Buckyball", "Amorphous Carbon"),
            correctAnswer = "Graphene",
            hint = "It can be exfoliated from graphite using simple adhesive Scotch tape!",
            explanation = "Graphene's sp² hybridized carbon bonds make it 200 times stronger than steel by weight and an exceptional ballistic thermal conductor.",
            funFact = "Andre Geim and Konstantin Novoselov won the 2010 Nobel Prize in Physics for isolating graphene using adhesive tape!",
            difficulty = Difficulty.EXTREME,
            level = 9,
            targetAge = age,
            imageRes = R.drawable.img_quantum_core
        )
    )
    // ==========================================
    private fun getBiologyPuzzles(age: AgeGroup): List<PuzzleItem> = listOf(
        // Level 1: Starter (Animal Safari & Habitats)
        PuzzleItem(
            id = "bio_lvl1",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 1: Animal Habitat Safari",
            question = "Which amazing animal breathes underwater through gills, has waterproof scales, and lives in coral reefs?",
            options = listOf("Clownfish 🐠", "Cheetah 🐆", "Eagle 🦅", "Koala 🐨"),
            correctAnswer = "Clownfish 🐠",
            hint = "Think of Nemo swimming through colorful anemones in the ocean!",
            explanation = "Fish use gills to extract dissolved oxygen directly from water.",
            funFact = "Clownfish have a special mucus layer on their skin that protects them from the stinging tentacles of sea anemones!",
            difficulty = Difficulty.NORMAL,
            level = 1,
            targetAge = age,
            imageRes = R.drawable.img_kids_underwater_reef
        ),
        // Level 2: Explorer (Photosynthesis)
        PuzzleItem(
            id = "bio_lvl2",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 2: Plant Sunlight Food Cycle",
            question = "What vital gas do green plants absorb from the air during photosynthesis to produce glucose sugar and release fresh oxygen?",
            options = listOf("Carbon Dioxide (CO₂)", "Helium (He)", "Nitrogen Gas (N₂)", "Argon (Ar)"),
            correctAnswer = "Carbon Dioxide (CO₂)",
            hint = "Plants inhale CO₂ that animals exhale, and exhale the fresh oxygen that animals breathe!",
            explanation = "Chloroplasts use sunlight energy to combine CO₂ and H₂O into glucose sugar and oxygen gas.",
            funFact = "Over 50% of the world's oxygen is produced by microscopic phytoplankton floating in the oceans!",
            difficulty = Difficulty.NORMAL,
            level = 2,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_normal
        ),
        // Level 3: Apprentice (DNA Base Pairing)
        PuzzleItem(
            id = "bio_lvl3",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 3: DNA Double Helix Code",
            question = "In the double helix structure of DNA, which nitrogenous base always pairs with Adenine (A)?",
            options = listOf("Thymine (T)", "Cytosine (C)", "Guanine (G)", "Uracil (U)"),
            correctAnswer = "Thymine (T)",
            hint = "Remember the base pairing rule: A pairs with T, and C pairs with G!",
            explanation = "Adenine forms two hydrogen bonds exclusively with Thymine (A-T), while Guanine forms three with Cytosine (G-C).",
            funFact = "If you uncoiled all the DNA in your body's cells, it would stretch from Earth to Pluto and back!",
            difficulty = Difficulty.HARD,
            level = 3,
            targetAge = age,
            imageRes = R.drawable.img_science_lab
        ),
        // Level 4: Adept (Brain Neurons & Synapses)
        PuzzleItem(
            id = "bio_lvl4",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 4: Neuroplasticity & Synapses",
            question = "What are the specialized cells in your brain that transmit electrical signals and form new learning connections?",
            options = listOf("Neurons", "Red Blood Cells", "Osteocytes", "Epithelial cells"),
            correctAnswer = "Neurons",
            hint = "Your brain contains roughly 86 billion of these interconnected thinking cells!",
            explanation = "Neurons communicate via chemical neurotransmitters across synaptic junctions, forming memory pathways.",
            funFact = "Solving challenging puzzles stimulates neurogenesis and strengthens synaptic plasticity!",
            difficulty = Difficulty.HARD,
            level = 4,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_hard
        ),
        // Level 5: Expert (Cellular Mitochondria)
        PuzzleItem(
            id = "bio_lvl5",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 5: Cellular Powerhouse & ATP",
            question = "Which cell organelle generates over 90% of the chemical energy currency (ATP) needed for cellular life?",
            options = listOf("Mitochondria", "Ribosome", "Golgi Apparatus", "Lysosome"),
            correctAnswer = "Mitochondria",
            hint = "It carries out the citric acid cycle and oxidative phosphorylation!",
            explanation = "Mitochondria convert nutrients and oxygen into ATP, maintaining cellular vitality.",
            funFact = "Mitochondria have their own distinct circular DNA, inherited almost entirely from maternal lineage!",
            difficulty = Difficulty.HARD,
            level = 5,
            targetAge = age,
            imageRes = R.drawable.img_science_lab
        ),
        // Level 6: Master (Trophic Energy Pyramids)
        PuzzleItem(
            id = "bio_lvl6",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 6: Trophic Energy Efficiency",
            question = "According to Lindeman's 10% ecological energy rule, roughly what percentage of energy is transferred from one trophic level to the next (e.g. from plants to herbivores)?",
            options = listOf("10%", "50%", "90%", "100%"),
            correctAnswer = "10%",
            hint = "About 90% of energy is lost as metabolic heat and respiration at each step in the food chain!",
            explanation = "Only ~10% of biomass energy is incorporated into the next consumer level, which is why top apex predators are rare.",
            funFact = "Because of the 10% rule, food chains rarely exceed 4 or 5 trophic levels in nature!",
            difficulty = Difficulty.EXTREME,
            level = 6,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 7: Grandmaster (CRISPR-Cas9 Editing)
        PuzzleItem(
            id = "bio_lvl7",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 7: CRISPR-Cas9 Gene Editing",
            question = "In the CRISPR-Cas9 molecular gene editing system, what component guides the Cas9 endonuclease enzyme to the exact target genomic sequence?",
            options = listOf(
                "Single Guide RNA (sgRNA)",
                "Ribosomal 28S sub-unit",
                "Lipid bilayer membrane",
                "DNA Polymerase III"
            ),
            correctAnswer = "Single Guide RNA (sgRNA)",
            hint = "The guide RNA matches complementary base pairs on the target genome, directing the molecular scissors!",
            explanation = "The sgRNA contains a 20-nucleotide sequence complementary to the target gene adjacent to a PAM site, directing Cas9 to cut.",
            funFact = "Jennifer Doudna and Emmanuelle Charpentier won the 2020 Nobel Prize in Chemistry for discovering CRISPR-Cas9!",
            difficulty = Difficulty.EXTREME,
            level = 7,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 8: Epigenetics & Histone Methylation
        PuzzleItem(
            id = "bio_lvl8",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 8: Epigenetic Gene Expression",
            question = "How does DNA methylation regulate gene expression without altering the underlying genetic sequence?",
            options = listOf(
                "Methyl groups attach to cytosines, turning gene transcription on or off",
                "It splits chromosomes in half",
                "It changes Adenine into Guanine",
                "It destroys the nuclear envelope"
            ),
            correctAnswer = "Methyl groups attach to cytosines, turning gene transcription on or off",
            hint = "Epigenetics acts like molecular bookmark switches that control which genes are read by RNA polymerase!",
            explanation = "Methylation of CpG islands typically recruits histone deacetylases that condense chromatin, silencing gene expression.",
            funFact = "Identical twins share 100% of their DNA sequence, yet their epigenetic patterns diverge as they live in different environments!",
            difficulty = Difficulty.EXTREME,
            level = 8,
            targetAge = age,
            imageRes = R.drawable.img_microscopic_cell
        ),
        // Level 9: Symbiogenesis & Eukaryotic Origins
        PuzzleItem(
            id = "bio_lvl9",
            category = PuzzleCategory.BIOLOGY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 9: Endosymbiotic Theory of Organelles",
            question = "According to Lynn Margulis's Endosymbiotic Theory, what were mitochondria and chloroplasts originally before evolving into eukaryotic organelles?",
            options = listOf(
                "Free-living prokaryotic bacteria engulfed by an ancestral host cell",
                "Viral protein crystals",
                "Pieces of dissolved cell wall",
                "Spontaneous crystallization of amino acids"
            ),
            correctAnswer = "Free-living prokaryotic bacteria engulfed by an ancestral host cell",
            hint = "They possess their own circular double-stranded bacterial-like DNA and 70S ribosomes!",
            explanation = "Aerobic proteobacteria entered an archaeal host roughly 1.5 billion years ago, creating the eukaryotic lineage.",
            funFact = "Endosymbiosis is considered one of the greatest evolutionary leaps in the history of life on Earth!",
            difficulty = Difficulty.EXTREME,
            level = 9,
            targetAge = age,
            imageRes = R.drawable.img_microscopic_cell
        )
    )
    // ==========================================
    private fun getHistoryPuzzles(age: AgeGroup): List<PuzzleItem> = listOf(
        // Level 1: Starter (Prehistoric Dinosaurs & Cave Art)
        PuzzleItem(
            id = "hist_lvl1",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 1: Prehistoric Wonders",
            question = "Which famous ancient creatures ruled the prehistoric Earth millions of years before humans invented writing and tools?",
            options = listOf("Dinosaurs 🦖", "Robots 🤖", "Submarines 🚢", "Airplanes ✈️"),
            correctAnswer = "Dinosaurs 🦖",
            hint = "Look for the giant Tyrannosaurus Rex and Triceratops fossils in natural history museums!",
            explanation = "Dinosaurs lived during the Mesozoic Era long before the dawn of human civilization.",
            funFact = "Paleontologists discover about 50 new dinosaur species every single year!",
            difficulty = Difficulty.NORMAL,
            level = 1,
            targetAge = age,
            imageRes = R.drawable.img_kids_dino_safari
        ),
        // Level 2: Explorer (Ancient Egyptian Pyramids)
        PuzzleItem(
            id = "hist_lvl2",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 2: Ancient Wonders Timeline",
            question = "Which of these historic human landmarks was built FIRST in recorded world history?",
            options = listOf(
                "Construction of the Great Pyramid of Giza (~2560 BCE)",
                "Founding of the Roman Colosseum (~70 CE)",
                "Invention of Gutenberg's Printing Press (1440 CE)",
                "Apollo 11 Moon Landing (1969 CE)"
            ),
            correctAnswer = "Construction of the Great Pyramid of Giza (~2560 BCE)",
            hint = "Look for the ancient Egyptian wonder built over 4,500 years ago during Egypt's Old Kingdom!",
            explanation = "The Great Pyramid dates back to circa 2560 BCE, making it millennia older than Rome and the printing press.",
            funFact = "Cleopatra lived closer in time to the Moon landing than to the construction of the Great Pyramid!",
            difficulty = Difficulty.NORMAL,
            level = 2,
            targetAge = age,
            imageRes = R.drawable.img_ancient_history
        ),
        // Level 3: Apprentice (Silk Road & Compass)
        PuzzleItem(
            id = "hist_lvl3",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 3: The Silk Road & Navigation",
            question = "Which navigational invention, created in ancient China using magnetic lodestone, revolutionized global sea exploration?",
            options = listOf("Magnetic Compass", "Telescope", "Steam Engine", "Barometer"),
            correctAnswer = "Magnetic Compass",
            hint = "It points toward the Earth's magnetic North pole and guided navigators across uncharted oceans!",
            explanation = "The magnetic compass was invented in Han Dynasty China around 200 BCE and enabled the Age of Global Discovery.",
            funFact = "The Silk Road stretched over 4,000 miles, connecting Chang'an (Xi'an) with Rome and Venice!",
            difficulty = Difficulty.HARD,
            level = 3,
            targetAge = age,
            imageRes = R.drawable.img_ancient_history
        ),
        // Level 4: Adept (Gutenberg Printing Press)
        PuzzleItem(
            id = "hist_lvl4",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 4: The Printing Revolution",
            question = "In the 1440s in Mainz, Germany, who invented the movable metal type printing press that democratized books and science?",
            options = listOf("Johannes Gutenberg", "Galileo Galilei", "Leonardo da Vinci", "Isaac Newton"),
            correctAnswer = "Johannes Gutenberg",
            hint = "The famous 42-line Bible printed in 1455 carries his name!",
            explanation = "Gutenberg's invention made books affordable and accelerated the Renaissance, Scientific Revolution, and modern literacy.",
            funFact = "Before the printing press, scribes took up to a full year to hand-copy a single manuscript!",
            difficulty = Difficulty.HARD,
            level = 4,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_hard
        ),
        // Level 5: Expert (Heliocentric Astronomy)
        PuzzleItem(
            id = "hist_lvl5",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 5: Scientific Renaissance",
            question = "Which Polish astronomer formulated the revolutionary heliocentric model, proving that planets orbit the Sun?",
            options = listOf("Nicolaus Copernicus", "Galileo Galilei", "Johannes Kepler", "Isaac Newton"),
            correctAnswer = "Nicolaus Copernicus",
            hint = "He published 'De revolutionibus orbium coelestium' in 1543!",
            explanation = "Copernicus's heliocentric model fundamentally transformed modern physics and sparked the Scientific Revolution.",
            funFact = "Chemical element 112 (Copernicium) was named in honor of Nicolaus Copernicus!",
            difficulty = Difficulty.HARD,
            level = 5,
            targetAge = age,
            imageRes = R.drawable.img_math_cosmos
        ),
        // Level 6: Master (Industrial Steam Power)
        PuzzleItem(
            id = "hist_lvl6",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 6: Industrial Steam Revolution",
            question = "Which Scottish inventor significantly improved the steam engine with a separate condenser in 1769, launching the Industrial Revolution?",
            options = listOf("James Watt", "Thomas Edison", "Nikola Tesla", "Alexander Graham Bell"),
            correctAnswer = "James Watt",
            hint = "The standard unit of electrical and mechanical power (W) is named in his honor!",
            explanation = "Watt's condenser increased steam engine efficiency by over 75%, powering factories, locomotives, and modern mechanization.",
            funFact = "The unit of electrical power, the 'Watt', was officially coined at the Second Congress of the British Association in 1889!",
            difficulty = Difficulty.EXTREME,
            level = 6,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 7: Grandmaster (Turing Bombe & Cryptanalysis)
        PuzzleItem(
            id = "hist_lvl7",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 7: Enigma & The Turing Machine",
            question = "During World War II at Bletchley Park, Alan Turing designed the electromechanical 'Bombe' machine to decipher encrypted military ciphers from which machine?",
            options = listOf("Enigma Machine", "Lorenz Cipher", "Caesar Cipher", "Scytale Cylinder"),
            correctAnswer = "Enigma Machine",
            hint = "The rotor machine had millions of billions of combinations changed daily by military operators!",
            explanation = "Turing's Bombe rapidly searched rotor permutations to break Enigma, shortening WWII and laying the foundation for computer science.",
            funFact = "The Turing Test was proposed in 1950 in a paper titled 'Computing Machinery and Intelligence'!",
            difficulty = Difficulty.EXTREME,
            level = 7,
            targetAge = age,
            imageRes = R.drawable.img_puzzle_extreme
        ),
        // Level 8: Space Age & Apollo Navigation Computer
        PuzzleItem(
            id = "hist_lvl8",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 8: Apollo 11 Lunar Guidance Computer",
            question = "In 1969, Margaret Hamilton led the MIT team that developed the pioneering asynchronous software for which historic spacecraft computer that landed humans on the Moon?",
            options = listOf(
                "Apollo Guidance Computer (AGC)",
                "ENIAC",
                "IBM 7090",
                "Commodore 64"
            ),
            correctAnswer = "Apollo Guidance Computer (AGC)",
            hint = "The computer used revolutionary rope core memory and real-time task prioritization to prevent landing aborts!",
            explanation = "Hamilton's robust priority scheduling allowed the AGC to ignore radar overload alarms and safely guide Apollo 11 to the lunar surface.",
            funFact = "Margaret Hamilton popularized the very term 'software engineering' during her work on the Apollo project!",
            difficulty = Difficulty.EXTREME,
            level = 8,
            targetAge = age,
            imageRes = R.drawable.img_kids_space_explorer
        ),
        // Level 9: The World Wide Web & Global Connectivity
        PuzzleItem(
            id = "hist_lvl9",
            category = PuzzleCategory.HISTORY,
            type = PuzzleType.MULTIPLE_CHOICE,
            title = "Level 9: Invention of the World Wide Web",
            question = "In 1989 at CERN, which British computer scientist created HTTP, HTML, and the World Wide Web to share scientific data globally?",
            options = listOf("Tim Berners-Lee", "Vint Cerf", "Linus Torvalds", "Ada Lovelace"),
            correctAnswer = "Tim Berners-Lee",
            hint = "He made the World Wide Web royalty-free and open to the entire world without patents in 1993!",
            explanation = "Tim Berners-Lee developed the three fundamental web technologies (URI, HTTP, HTML) that transformed global human civilization.",
            funFact = "The very first website in human history was hosted on a NeXT computer at CERN and is still online today!",
            difficulty = Difficulty.EXTREME,
            level = 9,
            targetAge = age,
            imageRes = R.drawable.img_cyber_cityscape
        )
    )
}
