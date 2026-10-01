package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ads.AdMobManager
import com.example.data.model.AchievementBadge
import com.example.data.model.AdRewardState
import com.example.data.model.AgeGroup
import com.example.data.model.CommunityCatalog
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityCoopGoal
import com.example.data.model.CommunityPoll
import com.example.data.model.CommunityPost
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.PuzzleType
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.UserProgress
import com.example.data.remote.ServerEventType
import com.example.data.remote.ServerLogEntry
import com.example.data.remote.ServerSyncManager
import com.example.data.repository.AchievementCatalog
import com.example.data.repository.PuzzleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Tasks : Screen()
    object Leaderboard : Screen()
    object Community : Screen()
    data class CategoryDetail(val category: PuzzleCategory) : Screen()
    data class SlidingPuzzle(val puzzle: PuzzleItem, val gridSize: Int = 3) : Screen()
    data class SubjectQuiz(val puzzle: PuzzleItem, val category: PuzzleCategory) : Screen()
    data class ArrowMaze(val level: Int = 1) : Screen()
    data class SnakeArrowScreen(val level: Int = 2) : Screen()
    data class JigsawPuzzle(val puzzle: PuzzleItem? = null, val gridSize: Int = 3) : Screen()
    data class CircuitLink(val level: Int = 1) : Screen()
    object AILab : Screen()
    object StatsProfile : Screen()
    object Auth : Screen()
}

data class VideoAdPlaybackState(
    val isPlaying: Boolean = false,
    val remainingSeconds: Int = 4,
    val totalSeconds: Int = 4,
    val adTitle: String = "BrainBoost STEM Academy",
    val adTagline: String = "Level up your cognitive logic & science intuition",
    val adColorHex: Long = 0xFF4F46E5,
    val isCompleted: Boolean = false,
    val adType: String = "REWARD"
)

data class SlidingBoardState(
    val size: Int = 3,
    val tiles: List<Int> = emptyList(), // 0 represents the blank empty slot
    val moves: Int = 0,
    val timeSeconds: Int = 0,
    val isSolved: Boolean = false,
    val isTimerRunning: Boolean = false,
    val history: List<List<Int>> = emptyList(),
    val suggestedTileIndex: Int? = null
)

class PuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PuzzleRepository(application)

    val userProgress: StateFlow<UserProgress> = repository.getProgressStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgress())

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Auth)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _backgroundAdsRemoved = MutableStateFlow(false)
    val backgroundAdsRemoved: StateFlow<Boolean> = _backgroundAdsRemoved.asStateFlow()

    // Sliding Puzzle Game State
    private val _slidingState = MutableStateFlow(SlidingBoardState())
    val slidingState: StateFlow<SlidingBoardState> = _slidingState.asStateFlow()
    private var timerJob: Job? = null

    // Subject / Multiple Choice Puzzle State
    private val _selectedOption = MutableStateFlow<String?>(null)
    val selectedOption: StateFlow<String?> = _selectedOption.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _isAnswerCorrect = MutableStateFlow(false)
    val isAnswerCorrect: StateFlow<Boolean> = _isAnswerCorrect.asStateFlow()

    private val _is2xClaimed = MutableStateFlow(false)
    val is2xClaimed: StateFlow<Boolean> = _is2xClaimed.asStateFlow()

    private val _eliminatedOptions = MutableStateFlow<Set<String>>(emptySet())
    val eliminatedOptions: StateFlow<Set<String>> = _eliminatedOptions.asStateFlow()

    val isAdMobLoaded: StateFlow<Boolean> = AdMobManager.isAdLoaded

    private val _aiHintText = MutableStateFlow<String?>(null)
    val aiHintText: StateFlow<String?> = _aiHintText.asStateFlow()

    private val _isHintLoading = MutableStateFlow(false)
    val isHintLoading: StateFlow<Boolean> = _isHintLoading.asStateFlow()

    // AI Lab Generator State
    private val _aiGeneratedPuzzle = MutableStateFlow<PuzzleItem?>(null)
    val aiGeneratedPuzzle: StateFlow<PuzzleItem?> = _aiGeneratedPuzzle.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    // Active Category Puzzle Playlist
    private val _activeCategoryPuzzles = MutableStateFlow<List<PuzzleItem>>(emptyList())
    val activeCategoryPuzzles: StateFlow<List<PuzzleItem>> = _activeCategoryPuzzles.asStateFlow()

    private val _currentPuzzleIndex = MutableStateFlow(0)
    val currentPuzzleIndex: StateFlow<Int> = _currentPuzzleIndex.asStateFlow()

    // Achievement Notification State
    private val _unlockedAchievementNotification = MutableStateFlow<AchievementBadge?>(null)
    val unlockedAchievementNotification: StateFlow<AchievementBadge?> = _unlockedAchievementNotification.asStateFlow()

    // Community Hub State
    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(CommunityCatalog.getInitialPosts())
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    private val _communityPoll = MutableStateFlow<CommunityPoll>(CommunityCatalog.getInitialPoll())
    val communityPoll: StateFlow<CommunityPoll> = _communityPoll.asStateFlow()

    private val _communityCoopGoal = MutableStateFlow<CommunityCoopGoal>(CommunityCoopGoal())
    val communityCoopGoal: StateFlow<CommunityCoopGoal> = _communityCoopGoal.asStateFlow()

    init {
        // Initial online handshake, app open logging, and telemetry synchronization
        viewModelScope.launch {
            try {
                val current = userProgress.value
                ServerSyncManager.logAppOpen(current, source = "ColdStart")
                ServerSyncManager.pingServerAsync()
                ServerSyncManager.fetchLeaderboardAsync()
                delay(300)
                ServerSyncManager.logStateSync(userProgress.value)
            } catch (e: Exception) {
                ServerSyncManager.logAppError("ViewModelInit", e.message ?: "Startup error", e.javaClass.simpleName)
            }
        }
    }

    fun onAppForegroundResume() {
        viewModelScope.launch {
            try {
                ServerSyncManager.logAppResume(userProgress.value)
                ServerSyncManager.pingServerAsync()
            } catch (e: Exception) {
                // Non-blocking telemetry
            }
        }
    }

    // Server Telemetry & Remote Worker State
    val serverLogs: StateFlow<List<ServerLogEntry>> = ServerSyncManager.logs
    val isServerOnline: StateFlow<Boolean?> = ServerSyncManager.isServerOnline
    val lastLatencyMs: StateFlow<Long> = ServerSyncManager.lastLatencyMs
    val cloudLeaderboard = ServerSyncManager.cloudLeaderboard

    // Rating & Feedback Dialog State
    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    private val _ratingCategoryTitle = MutableStateFlow("Puzzle Set")
    val ratingCategoryTitle: StateFlow<String> = _ratingCategoryTitle.asStateFlow()

    fun openRatingDialog(categoryTitle: String = "Puzzle Set") {
        _ratingCategoryTitle.value = categoryTitle
        _showRatingDialog.value = true
    }

    fun dismissRatingDialog() {
        _showRatingDialog.value = false
    }

    fun submitPuzzleRatingAndFeedback(stars: Int, tags: List<String>, feedbackText: String) {
        viewModelScope.launch {
            _showRatingDialog.value = false
            repository.recordFeedbackReward(xpBonus = 50, coinsBonus = 20)
            val current = userProgress.value
            ServerSyncManager.logFeedbackSubmission(
                stars = stars,
                tags = tags,
                feedbackText = feedbackText,
                puzzleCategory = _ratingCategoryTitle.value,
                userProgress = current
            )
            ServerSyncManager.logStateSync(current)
            _claimCelebrationMessage.value = "🌟 Feedback submitted! +50 XP & +20 🪙 awarded."
        }
    }

    fun registerUser(email: String, username: String, password: String = "") {
        viewModelScope.launch {
            repository.loginUser(email, username)
            val current = userProgress.value
            ServerSyncManager.logUserRegistration(email, username, current)
            ServerSyncManager.logUserAuth(true, email, username, current)
            ServerSyncManager.logStateSync(current)
            _claimCelebrationMessage.value = "🎉 Welcome, $username! Account created & synced."
        }
    }

    fun login(email: String, username: String, token: String = "") {
        viewModelScope.launch {
            repository.loginUser(email, username, token)
            ServerSyncManager.logUserAuth(true, email, username, userProgress.value)
            ServerSyncManager.logStateSync(userProgress.value)
            _claimCelebrationMessage.value = "🟢 Logged in as $email. Telemetry is active!"
        }
    }

    fun logout() {
        val current = userProgress.value
        viewModelScope.launch {
            ServerSyncManager.sendLogout(current.userEmail)
            repository.logoutUser()
            ServerSyncManager.logUserAuth(false, current.userEmail, current.userName, current)
            _claimCelebrationMessage.value = "👋 Logged out successfully"
        }
    }

    fun pingServer() {
        ServerSyncManager.pingServerAsync()
    }

    fun syncStateToServer() {
        viewModelScope.launch {
            val progress = userProgress.value
            ServerSyncManager.logStateSync(progress)
            ServerSyncManager.pingServerAsync()
            ServerSyncManager.fetchLeaderboardAsync()
            repository.markLastSync()
            _claimCelebrationMessage.value = "☁️ Stats Synced to Cloud Server!"
        }
    }

    fun clearServerLogs() {
        ServerSyncManager.clearLogs()
    }

    fun dismissAchievementNotification() {
        _unlockedAchievementNotification.value = null
    }

    fun navigateTo(screen: Screen) {
        if (screen != Screen.Home) {
            val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
            if (!isOnline) {
                _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to open next page!"
                return
            }
        }
        _currentScreen.value = screen
        if (screen is Screen.CategoryDetail) {
            loadCategoryPuzzles(screen.category)
        }
    }

    fun goBack() {
        when (val current = _currentScreen.value) {
            is Screen.Home -> Unit
            is Screen.Auth -> _currentScreen.value = Screen.Home
            is Screen.Tasks -> _currentScreen.value = Screen.Home
            is Screen.Leaderboard -> _currentScreen.value = Screen.Home
            is Screen.Community -> _currentScreen.value = Screen.Home
            is Screen.CategoryDetail -> _currentScreen.value = Screen.Home
            is Screen.SlidingPuzzle -> {
                stopTimer()
                _currentScreen.value = Screen.CategoryDetail(current.puzzle.category)
            }
            is Screen.SubjectQuiz -> {
                _currentScreen.value = Screen.CategoryDetail(current.category)
            }
            is Screen.AILab -> _currentScreen.value = Screen.Home
            is Screen.StatsProfile -> _currentScreen.value = Screen.Home
            is Screen.ArrowMaze -> _currentScreen.value = Screen.Home
            is Screen.SnakeArrowScreen -> _currentScreen.value = Screen.Home
            is Screen.JigsawPuzzle -> _currentScreen.value = Screen.Home
            is Screen.CircuitLink -> _currentScreen.value = Screen.Home
        }
    }

    fun setAgeGroup(ageGroup: AgeGroup) {
        viewModelScope.launch {
            repository.updateAgeGroup(ageGroup)
        }
    }

    fun toggleLargeText(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleLargeText(enabled)
        }
    }

    fun toggleTileNumbers(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleTileNumbers(enabled)
        }
    }

    fun loadCategoryPuzzles(category: PuzzleCategory) {
        val age = userProgress.value.selectedAgeGroup
        val list = repository.getPuzzlesForCategory(category, age)
        _activeCategoryPuzzles.value = list
        _currentPuzzleIndex.value = 0
    }

    // --- Sliding Tile Engine ---

    fun startSlidingPuzzle(puzzle: PuzzleItem, size: Int = 3) {
        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to open next page!"
            return
        }
        stopTimer()
        _is2xClaimed.value = false
        _eliminatedOptions.value = emptySet()
        val totalTiles = size * size
        val initialTiles = createSolvableTiles(size)
        _slidingState.value = SlidingBoardState(
            size = size,
            tiles = initialTiles,
            moves = 0,
            timeSeconds = 0,
            isSolved = false,
            isTimerRunning = true
        )
        startTimer()
        ServerSyncManager.logGameplayStart(puzzle, userProgress.value)
        _currentScreen.value = Screen.SlidingPuzzle(puzzle, size)
    }

    fun onTileClicked(index: Int, puzzle: PuzzleItem) {
        val state = _slidingState.value
        if (state.isSolved) return

        val size = state.size
        val blankIndex = state.tiles.indexOf(0)
        if (blankIndex == -1) return

        val row = index / size
        val col = index % size
        val blankRow = blankIndex / size
        val blankCol = blankIndex % size

        val isAdjacent = (row == blankRow && Math.abs(col - blankCol) == 1) ||
                (col == blankCol && Math.abs(row - blankRow) == 1)

        if (isAdjacent) {
            val newHistory = state.history + listOf(state.tiles)
            val newTiles = state.tiles.toMutableList()
            val movedTileNumber = state.tiles[index]
            newTiles[blankIndex] = state.tiles[index]
            newTiles[index] = 0

            val solved = checkSlidingSolved(newTiles, size)
            val newMoves = state.moves + 1
            _slidingState.value = state.copy(
                tiles = newTiles,
                moves = newMoves,
                isSolved = solved,
                isTimerRunning = !solved,
                history = newHistory,
                suggestedTileIndex = null
            )

            if (solved) {
                stopTimer()
            }
        }
    }

    fun onJigsawPuzzleSolved(puzzle: PuzzleItem, moves: Int, activity: Activity? = null) {
        val state = _slidingState.value
        _slidingState.value = state.copy(isSolved = true, isTimerRunning = false, moves = moves)
        stopTimer()
        onLevelCompletedIncrement(activity)
    }

    fun recordGameRewards(bonusCoins: Int, bonusXp: Int) {
        viewModelScope.launch {
            repository.addBonusCoinsAndXp(bonusCoins, bonusXp)
        }
    }

    fun undoSlidingMove() {
        val state = _slidingState.value
        if (state.history.isEmpty() || state.isSolved) return
        val lastTiles = state.history.last()
        val remainingHistory = state.history.dropLast(1)
        _slidingState.value = state.copy(
            tiles = lastTiles,
            moves = maxOf(0, state.moves - 1),
            history = remainingHistory,
            suggestedTileIndex = null
        )
    }

    fun requestSmartHint() {
        val state = _slidingState.value
        if (state.isSolved || state.tiles.isEmpty()) return
        val size = state.size
        val blankIndex = state.tiles.indexOf(0)
        if (blankIndex == -1) return

        val blankRow = blankIndex / size
        val blankCol = blankIndex % size

        val neighbors = mutableListOf<Int>()
        if (blankRow > 0) neighbors.add((blankRow - 1) * size + blankCol)
        if (blankRow < size - 1) neighbors.add((blankRow + 1) * size + blankCol)
        if (blankCol > 0) neighbors.add(blankRow * size + (blankCol - 1))
        if (blankCol < size - 1) neighbors.add(blankRow * size + (blankCol + 1))

        var bestNeighbor = neighbors.firstOrNull() ?: return
        var minDistance = Int.MAX_VALUE

        for (candidateIndex in neighbors) {
            val simulated = state.tiles.toMutableList()
            simulated[blankIndex] = simulated[candidateIndex]
            simulated[candidateIndex] = 0
            val dist = calculateTotalManhattan(simulated, size)
            if (dist < minDistance) {
                minDistance = dist
                bestNeighbor = candidateIndex
            }
        }

        _slidingState.value = state.copy(suggestedTileIndex = bestNeighbor)
    }

    private fun calculateTotalManhattan(tiles: List<Int>, size: Int): Int {
        var total = 0
        for (i in tiles.indices) {
            val value = tiles[i]
            if (value != 0) {
                val targetIndex = value - 1
                val targetRow = targetIndex / size
                val targetCol = targetIndex % size
                val currentRow = i / size
                val currentCol = i % size
                total += Math.abs(currentRow - targetRow) + Math.abs(currentCol - targetCol)
            }
        }
        return total
    }

    fun resetSlidingPuzzle(size: Int) {
        stopTimer()
        val initialTiles = createSolvableTiles(size)
        _slidingState.value = SlidingBoardState(
            size = size,
            tiles = initialTiles,
            moves = 0,
            timeSeconds = 0,
            isSolved = false,
            isTimerRunning = true,
            history = emptyList(),
            suggestedTileIndex = null
        )
        startTimer()
    }

    private fun createSolvableTiles(size: Int): List<Int> {
        val total = size * size
        var tiles: List<Int>
        do {
            // Numbers 1..(total - 1), plus 0 for empty tile
            val list = (1 until total).toMutableList()
            list.shuffle()
            list.add(0) // place blank at the end
            tiles = list
        } while (!isSolvable(tiles, size) || checkSlidingSolved(tiles, size))
        return tiles
    }

    private fun isSolvable(tiles: List<Int>, size: Int): Boolean {
        var inversions = 0
        val nonBlank = tiles.filter { it != 0 }
        for (i in 0 until nonBlank.size) {
            for (j in i + 1 until nonBlank.size) {
                if (nonBlank[i] > nonBlank[j]) inversions++
            }
        }
        return if (size % 2 != 0) {
            inversions % 2 == 0
        } else {
            val blankRowFromBottom = size - (tiles.indexOf(0) / size)
            if (blankRowFromBottom % 2 == 0) {
                inversions % 2 != 0
            } else {
                inversions % 2 == 0
            }
        }
    }

    private fun checkSlidingSolved(tiles: List<Int>, size: Int): Boolean {
        val total = size * size
        for (i in 0 until total - 1) {
            if (tiles[i] != i + 1) return false
        }
        return tiles.last() == 0
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_slidingState.value.isTimerRunning && !_slidingState.value.isSolved) {
                    _slidingState.value = _slidingState.value.copy(
                        timeSeconds = _slidingState.value.timeSeconds + 1
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // --- Subject / Multiple Choice Game Engine ---

    fun startSubjectPuzzle(puzzle: PuzzleItem) {
        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to open next page!"
            return
        }
        _selectedOption.value = null
        _isAnswerSubmitted.value = false
        _isAnswerCorrect.value = false
        _is2xClaimed.value = false
        _eliminatedOptions.value = emptySet()
        _aiHintText.value = null
        ServerSyncManager.logGameplayStart(puzzle, userProgress.value)
        _currentScreen.value = Screen.SubjectQuiz(puzzle, puzzle.category)
    }

    fun selectOption(option: String) {
        if (!_isAnswerSubmitted.value) {
            _selectedOption.value = option
        }
    }

    fun submitAnswer(puzzle: PuzzleItem, activity: Activity? = null) {
        val selected = _selectedOption.value ?: return
        val correct = selected.trim() == puzzle.correctAnswer.trim()
        _isAnswerSubmitted.value = true
        _isAnswerCorrect.value = correct

        if (correct) {
            onLevelCompletedIncrement(activity)
        }

        viewModelScope.launch {
            if (!correct) {
                ServerSyncManager.logGameplaySolve(
                    puzzle = puzzle,
                    score = 0,
                    moves = 1,
                    timeSeconds = 12,
                    isSuccess = false,
                    userProgress = userProgress.value
                )
            }
        }
    }

    /**
     * Level completion rewarded ad flow:
     * - Must watch to the very end to earn 10 coins + 5 points
     * - Takes 4 seconds in backend server without showing anything when triggered
     * - Closing or skipping early cancels the reward
     * - Watching full ad removes background features and advances to next level
     */
    /**
     * Completing level with Rewarded Ad:
     * - Instant ad presentation upon user click
     * - Backend server logging runs asynchronously in background
     * - Closing or skipping early cancels the reward
     * - Watching full ad removes background features and advances to next level
     */
    fun completeLevelWithRewardedAd(activity: Activity?, puzzle: PuzzleItem) {
        if (_videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to watch ads and earn rewards."
            return
        }

        pendingTargetPuzzle = puzzle

        // Backend server verification runs in background without delaying ad presentation
        viewModelScope.launch {
            performBackendAdServerVerification("level_completion_ad", "10_coins_5_pts")
        }

        if (activity != null && AdMobManager.isAdLoaded.value && !_backgroundAdsRemoved.value) {
            var rewardEarned = false
            AdMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = {
                    rewardEarned = true
                },
                onAdDismissed = {
                    if (rewardEarned) {
                        onLevelAdFinishedSuccessfully(puzzle)
                    } else {
                        onLevelAdCancelledEarly()
                    }
                },
                onAdFailedToShow = {
                    startLevelVideoAdSimulation(puzzle)
                }
            )
        } else {
            startLevelVideoAdSimulation(puzzle)
        }
    }

    private fun startLevelVideoAdSimulation(puzzle: PuzzleItem) {
        if (_videoAdState.value.isPlaying) return
        val campaign = rewardAdCampaigns.random()
        val colors = listOf(0xFF4F46E5, 0xFF0284C7, 0xFF059669, 0xFF9333EA, 0xFFE11D48)

        pendingTargetPuzzle = puzzle
        _videoAdState.value = VideoAdPlaybackState(
            isPlaying = true,
            remainingSeconds = 4,
            totalSeconds = 4,
            adTitle = campaign.first,
            adTagline = campaign.second,
            adColorHex = colors.random(),
            isCompleted = false,
            adType = "LEVEL_COMPLETION"
        )

        adPlaybackJob?.cancel()
        adPlaybackJob = viewModelScope.launch {
            for (sec in 3 downTo 0) {
                delay(1000)
                _videoAdState.value = _videoAdState.value.copy(
                    remainingSeconds = sec,
                    isCompleted = sec == 0
                )
            }
        }
    }

    fun dismissAdDialogWithPuzzle(puzzle: PuzzleItem) {
        dismissVideoAdDialog()
    }

    fun dismissVideoAdDialog() {
        val state = _videoAdState.value
        adPlaybackJob?.cancel()
        _videoAdState.value = _videoAdState.value.copy(isPlaying = false)

        when (state.adType) {
            "LEVEL_COMPLETION" -> {
                val p = pendingTargetPuzzle
                pendingTargetPuzzle = null
                if (state.isCompleted && p != null) {
                    onLevelAdFinishedSuccessfully(p)
                } else {
                    onLevelAdCancelledEarly()
                }
            }
            "NEXT_LEVEL" -> {
                val nextAction = pendingAdRewardAction
                pendingAdRewardAction = null
                if (state.isCompleted) {
                    nextAction?.invoke()
                }
            }
            else -> {
                val rewardAction = pendingAdRewardAction
                pendingAdRewardAction = null
                if (state.isCompleted) {
                    rewardAction?.invoke()
                } else {
                    _claimCelebrationMessage.value = "⚠️ Ad closed early: Full watch required for reward."
                }
            }
        }
    }

    fun onLevelAdFinishedSuccessfully(puzzle: PuzzleItem) {
        viewModelScope.launch {
            // Completing a puzzle awards 10 coins and 5 points after ad finishes completely
            val stars = puzzle.difficulty.stars
            val xpReward = 5
            val coinsReward = 10
            val newBadges = repository.recordPuzzleSolved(
                puzzleId = puzzle.id,
                category = puzzle.category,
                stars = stars,
                xpEarned = xpReward,
                moves = 1,
                timeSeconds = 12,
                coinsEarned = coinsReward
            )
            ServerSyncManager.logGameplaySolve(
                puzzle = puzzle,
                score = xpReward,
                moves = 1,
                timeSeconds = 12,
                isSuccess = true,
                userProgress = userProgress.value
            )
            if (newBadges.isNotEmpty()) {
                _unlockedAchievementNotification.value = newBadges.first()
            }

            // Remove background ad features
            _backgroundAdsRemoved.value = true

            _claimCelebrationMessage.value = "🎉 Ad Complete! +10 Coins & +5 Points Claimed! Moving to next level!"
            delay(500)
            nextPuzzleInCategory()
        }
    }

    fun onLevelAdCancelledEarly() {
        _claimCelebrationMessage.value = "⚠️ Ad Skipped/Closed Early: Reward cancelled! You must watch the rewarded ad until the very end to receive 10 Coins, 5 Points, and unlock the next level."
    }

    fun importPuzzlesFromUrl(url: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.importPuzzlesFromUrl(url)
            if (res.isSuccess) {
                val count = res.getOrDefault(0)
                onResult(true, "Successfully imported $count new puzzle levels to database!")
                // Refresh active category puzzles
                val currentCategory = _activeCategoryPuzzles.value.firstOrNull()?.category ?: PuzzleCategory.MATH
                loadCategoryPuzzles(currentCategory)
            } else {
                onResult(false, "Failed to import puzzles: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun importPuzzlesFromJson(jsonStr: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.importPuzzlesFromJson(jsonStr)
            if (res.isSuccess) {
                val count = res.getOrDefault(0)
                onResult(true, "Successfully generated $count new puzzle levels in database!")
                val currentCategory = _activeCategoryPuzzles.value.firstOrNull()?.category ?: PuzzleCategory.MATH
                loadCategoryPuzzles(currentCategory)
            } else {
                onResult(false, "Failed to parse puzzles: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun requestAiHint(puzzle: PuzzleItem) {
        viewModelScope.launch {
            _isHintLoading.value = true
            val hint = repository.getAiHint(puzzle.question, puzzle.options)
            _aiHintText.value = hint
            _isHintLoading.value = false
        }
    }

    // Track completed levels across all puzzle games for automatic 3-level Interstitial Ad trigger
    private val _completedLevelsSessionCount = MutableStateFlow(0)
    val completedLevelsSessionCount: StateFlow<Int> = _completedLevelsSessionCount.asStateFlow()

    /**
     * Called whenever a user solves/completes any puzzle level.
     * Automatically triggers an Interstitial Ad every 3 levels completed!
     */
    fun onLevelCompletedIncrement(activity: Activity?) {
        _completedLevelsSessionCount.value += 1
        val count = _completedLevelsSessionCount.value
        if (count % 3 == 0 && activity != null) {
            val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
            if (isOnline) {
                AdMobManager.showNextLevelInterstitial(activity) {
                    // Ad completed
                }
            }
        }
    }

    /**
     * Whenever users click 'Next', 'Next Level', or 'Next Puzzle',
     * automatically show Interstitial Ad before advancing.
     */
    fun triggerNextLevelInterstitial(activity: Activity?, onProceed: () -> Unit) {
        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to load the next level!"
            return
        }

        if (activity != null) {
            AdMobManager.showNextLevelInterstitial(activity) {
                onProceed()
            }
        } else {
            onProceed()
        }
    }

    fun nextPuzzleWithAd(activity: android.app.Activity?) {
        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to load the next level!"
            return
        }

        if (activity != null) {
            AdMobManager.showNextLevelInterstitial(activity) {
                nextPuzzleInCategory()
            }
        } else {
            nextPuzzleInCategory()
        }
    }

    fun nextPuzzleInCategory() {
        val list = _activeCategoryPuzzles.value
        val currentIndex = _currentPuzzleIndex.value
        if (currentIndex + 1 < list.size) {
            _currentPuzzleIndex.value = currentIndex + 1
            val next = list[currentIndex + 1]
            if (next.type == PuzzleType.SLIDING_IMAGE) {
                val gridSize = userProgress.value.selectedAgeGroup.defaultGridSize
                startSlidingPuzzle(next, gridSize)
            } else {
                startSubjectPuzzle(next)
            }
        } else {
            // Loop or return to category
            val first = list.firstOrNull()
            if (first != null) {
                _currentPuzzleIndex.value = 0
                if (first.type == PuzzleType.SLIDING_IMAGE) {
                    val gridSize = userProgress.value.selectedAgeGroup.defaultGridSize
                    startSlidingPuzzle(first, gridSize)
                } else {
                    startSubjectPuzzle(first)
                }
            }
        }
    }

    // --- AI Lab Generation ---

    fun generateNewAiPuzzle(category: PuzzleCategory, difficulty: Difficulty) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            _selectedOption.value = null
            _isAnswerSubmitted.value = false
            _aiHintText.value = null

            val age = userProgress.value.selectedAgeGroup
            val puzzle = repository.generateDynamicAIPuzzle(category, age, difficulty)
            _aiGeneratedPuzzle.value = puzzle
            _isGeneratingAi.value = false
            ServerSyncManager.logPuzzleCreated(
                title = puzzle.title,
                category = category,
                difficulty = difficulty,
                isAiGenerated = true,
                promptOrDescription = "Generated in AI Lab for age $age",
                userEmail = userProgress.value.userEmail
            )
        }
    }

    // --- Task Center & Rewards Engine ---

    private val _videoAdState = MutableStateFlow(VideoAdPlaybackState())
    val videoAdState: StateFlow<VideoAdPlaybackState> = _videoAdState.asStateFlow()

    private val _isAdServerProcessing = MutableStateFlow(false)
    val isAdServerProcessing: StateFlow<Boolean> = _isAdServerProcessing.asStateFlow()

    private val _claimCelebrationMessage = MutableStateFlow<String?>(null)
    val claimCelebrationMessage: StateFlow<String?> = _claimCelebrationMessage.asStateFlow()

    private var adPlaybackJob: Job? = null
    private var pendingAdRewardAction: (() -> Unit)? = null
    private var pendingTargetPuzzle: PuzzleItem? = null

    /**
     * Sends backend server verification asynchronously in background without delaying ad display.
     */
    private suspend fun performBackendAdServerVerification(adUnitId: String, rewardType: String) {
        try {
            ServerSyncManager.verifyAdWatchWithBackendServer(
                adUnitId = adUnitId,
                rewardType = rewardType,
                userEmail = userProgress.value.userEmail
            )
        } catch (e: Exception) {
            // Silently log in background without breaking UX
        }
    }

    /**
     * Compute current list of interactive tasks from UserProgress
     */
    fun getTasks(progress: UserProgress): List<TaskItem> {
        val claimed = progress.claimedTaskIds
        val visualSolved = progress.categorySolvedCounts[PuzzleCategory.VISUAL.id] ?: 0
        val mathSolved = progress.categorySolvedCounts[PuzzleCategory.MATH.id] ?: 0
        val physicsSolved = progress.categorySolvedCounts[PuzzleCategory.PHYSICS.id] ?: 0
        val chemistrySolved = progress.categorySolvedCounts[PuzzleCategory.CHEMISTRY.id] ?: 0
        val biologySolved = progress.categorySolvedCounts[PuzzleCategory.BIOLOGY.id] ?: 0
        val historySolved = progress.categorySolvedCounts[PuzzleCategory.HISTORY.id] ?: 0
        val aiLabSolved = progress.categorySolvedCounts[PuzzleCategory.AI_LAB.id] ?: 0
        val scienceTotal = physicsSolved + chemistrySolved + biologySolved

        return listOf(
            TaskItem(
                id = "task_daily_login",
                title = "Daily Check-in & Warmup",
                description = "Launch MindMatrix and prepare your brain for training",
                iconEmoji = "📅",
                coinReward = 50,
                currentProgress = 1,
                targetProgress = 1,
                isCompleted = true,
                isClaimed = claimed.contains("task_daily_login"),
                category = TaskCategory.DAILY,
                tag = "Daily Bonus"
            ),
            TaskItem(
                id = "task_solve_1",
                title = "First Logic Breakthrough",
                description = "Solve any 1 STEM or Sliding Puzzle challenge today",
                iconEmoji = "🧩",
                coinReward = 60,
                currentProgress = progress.totalPuzzlesSolved.coerceAtMost(1),
                targetProgress = 1,
                isCompleted = progress.totalPuzzlesSolved >= 1,
                isClaimed = claimed.contains("task_solve_1"),
                category = TaskCategory.DAILY,
                tag = "Easy"
            ),
            TaskItem(
                id = "task_solve_3",
                title = "Triple Mind Challenge",
                description = "Successfully solve 3 puzzle quests across any subject",
                iconEmoji = "⚡",
                coinReward = 120,
                currentProgress = progress.totalPuzzlesSolved.coerceAtMost(3),
                targetProgress = 3,
                isCompleted = progress.totalPuzzlesSolved >= 3,
                isClaimed = claimed.contains("task_solve_3"),
                category = TaskCategory.DAILY,
                tag = "Popular"
            ),
            TaskItem(
                id = "task_watch_3_ads",
                title = "Watch 3 Reward Videos",
                description = "Watch 3 quick videos to earn the massive 150 coin bounty",
                iconEmoji = "📺",
                coinReward = 150,
                currentProgress = progress.adsWatchedCount.coerceAtMost(3),
                targetProgress = 3,
                isCompleted = progress.adsWatchedCount >= 3,
                isClaimed = claimed.contains("task_watch_3_ads") || progress.adsRewardClaimed,
                category = TaskCategory.SPECIAL_OFFER,
                tag = "+150 COINS"
            ),
            TaskItem(
                id = "task_sliding_1",
                title = "Spatial Matrix Master",
                description = "Solve at least 1 Sliding Image matrix puzzle",
                iconEmoji = "🎯",
                coinReward = 100,
                currentProgress = visualSolved.coerceAtMost(1),
                targetProgress = 1,
                isCompleted = visualSolved >= 1,
                isClaimed = claimed.contains("task_sliding_1"),
                category = TaskCategory.CHALLENGES,
                tag = "Visual"
            ),
            TaskItem(
                id = "task_science_2",
                title = "Lab Researcher",
                description = "Complete 2 Physics, Chemistry, or Biology challenges",
                iconEmoji = "🔬",
                coinReward = 140,
                currentProgress = scienceTotal.coerceAtMost(2),
                targetProgress = 2,
                isCompleted = scienceTotal >= 2,
                isClaimed = claimed.contains("task_science_2"),
                category = TaskCategory.CHALLENGES,
                tag = "Science"
            ),
            TaskItem(
                id = "task_math_1",
                title = "Equation Architect",
                description = "Solve 1 Mathematics calculation or pattern quest",
                iconEmoji = "➗",
                coinReward = 90,
                currentProgress = mathSolved.coerceAtMost(1),
                targetProgress = 1,
                isCompleted = mathSolved >= 1,
                isClaimed = claimed.contains("task_math_1"),
                category = TaskCategory.CHALLENGES,
                tag = "Math"
            ),
            TaskItem(
                id = "task_history_1",
                title = "Time Traveler",
                description = "Explore civilization mysteries & solve a History quest",
                iconEmoji = "🏛️",
                coinReward = 90,
                currentProgress = historySolved.coerceAtMost(1),
                targetProgress = 1,
                isCompleted = historySolved >= 1,
                isClaimed = claimed.contains("task_history_1"),
                category = TaskCategory.CHALLENGES,
                tag = "History"
            ),
            TaskItem(
                id = "task_ai_lab_1",
                title = "AI Lab Innovator",
                description = "Generate and complete a custom dynamic puzzle with Gemini AI",
                iconEmoji = "✨",
                coinReward = 130,
                currentProgress = aiLabSolved.coerceAtMost(1),
                targetProgress = 1,
                isCompleted = aiLabSolved >= 1,
                isClaimed = claimed.contains("task_ai_lab_1"),
                category = TaskCategory.CHALLENGES,
                tag = "Gemini"
            ),
            TaskItem(
                id = "task_stars_5",
                title = "Constellation Collector",
                description = "Accumulate 5 total gold stars from puzzle completions",
                iconEmoji = "⭐",
                coinReward = 150,
                currentProgress = progress.starsEarned.coerceAtMost(5),
                targetProgress = 5,
                isCompleted = progress.starsEarned >= 5,
                isClaimed = claimed.contains("task_stars_5"),
                category = TaskCategory.CHALLENGES,
                tag = "Grandmaster"
            )
        )
    }

    fun claimTask(task: TaskItem) {
        if (!task.isCompleted || task.isClaimed) return
        viewModelScope.launch {
            if (task.id == "task_watch_3_ads") {
                repository.claim150AdsReward()
            } else {
                repository.claimTaskReward(task.id, task.coinReward)
            }
            ServerSyncManager.logRewardClaimed("Task: ${task.title}", task.coinReward, userProgress.value)
            ServerSyncManager.logStateSync(userProgress.value)
            _claimCelebrationMessage.value = "🎉 Claimed +${task.coinReward} Coins for \"${task.title}\"!"
        }
    }

    fun checkAndClaimAllTasks() {
        val currentProgress = userProgress.value
        val allTasks = getTasks(currentProgress)
        val readyTasks = allTasks.filter { it.isReadyToClaim }

        if (readyTasks.isEmpty()) {
            _claimCelebrationMessage.value = "No pending task rewards to claim right now. Keep solving puzzles!"
            return
        }

        viewModelScope.launch {
            val totalCoins = readyTasks.sumOf { it.coinReward }
            val pairsToClaim = readyTasks.map { it.id to it.coinReward }
            repository.claimAllTasks(pairsToClaim)
            if (readyTasks.any { it.id == "task_watch_3_ads" }) {
                repository.claim150AdsReward()
            }
            ServerSyncManager.logRewardClaimed("Claimed All Tasks (${readyTasks.size} Tasks)", totalCoins, userProgress.value)
            ServerSyncManager.logStateSync(userProgress.value)
            _claimCelebrationMessage.value = "✨ Awesome! Claimed ALL tasks: +$totalCoins Coins added to your balance! 🪙"
        }
    }

    // --- Interactive Video Ad Simulation Flow ---

    private val rewardAdCampaigns = listOf(
        Pair("CosmoLogic Brain Training", "Sharpen your IQ with 500+ daily interactive mind puzzles"),
        Pair("Quantum Kids STEM Academy", "Hands-on physics, robotics & coding adventures"),
        Pair("BioVerse Explorer 3D", "Dive into interactive 3D human cells and ecosystems"),
        Pair("AstroMath Galaxy Quest", "Master calculus and arithmetic in an intergalactic journey")
    )

    fun startWatchingVideoAd() {
        if (_videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to watch ads and earn coins!"
            return
        }

        // Send backend server logging asynchronously in background
        viewModelScope.launch {
            performBackendAdServerVerification("task_video_ad", "daily_50_coins")
        }

        val campaign = rewardAdCampaigns.random()
        val colors = listOf(0xFF4F46E5, 0xFF0284C7, 0xFF059669, 0xFF9333EA, 0xFFE11D48)
        val chosenColor = colors.random()

        _videoAdState.value = VideoAdPlaybackState(
            isPlaying = true,
            remainingSeconds = 4,
            totalSeconds = 4,
            adTitle = campaign.first,
            adTagline = campaign.second,
            adColorHex = chosenColor,
            isCompleted = false,
            adType = "TASK_COINS"
        )

        adPlaybackJob?.cancel()
        adPlaybackJob = viewModelScope.launch {
            for (sec in 3 downTo 0) {
                delay(1000)
                _videoAdState.value = _videoAdState.value.copy(
                    remainingSeconds = sec,
                    isCompleted = sec == 0
                )
            }
            // Auto finish ad and record progress in repository with 50 coins + achievement check
            val (newCount, newBadges) = repository.recordAdWatched(bonusCoins = 50)
            if (newBadges.isNotEmpty()) {
                _unlockedAchievementNotification.value = newBadges.first()
            }
            ServerSyncManager.logRewardClaimed("Rewarded Video (+50 Coins)", 50, userProgress.value)
            ServerSyncManager.logStateSync(userProgress.value)
            if (newCount >= 3) {
                _claimCelebrationMessage.value = "🎉 3/3 Ads Watched (+50 Coins Earned)! You can now claim your 150 Coins Jackpot!"
            } else {
                _claimCelebrationMessage.value = "📺 Ad finished (+50 Coins Earned, $newCount/3)! Watch ${3 - newCount} more for 150 coins!"
            }
        }
    }

    fun startWatchingVideoAdWithCallback(onRewardGranted: () -> Unit) {
        if (_videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to watch ads and earn rewards."
            return
        }

        // Asynchronously log to backend server without blocking ad presentation
        viewModelScope.launch {
            performBackendAdServerVerification("reward_video_callback", "reward_callback")
        }

        val campaign = rewardAdCampaigns.random()
        val colors = listOf(0xFF4F46E5, 0xFF0284C7, 0xFF059669, 0xFF9333EA, 0xFFE11D48)
        val chosenColor = colors.random()

        pendingAdRewardAction = onRewardGranted

        _videoAdState.value = VideoAdPlaybackState(
            isPlaying = true,
            remainingSeconds = 4,
            totalSeconds = 4,
            adTitle = campaign.first,
            adTagline = campaign.second,
            adColorHex = chosenColor,
            isCompleted = false,
            adType = "CALLBACK_REWARD"
        )

        adPlaybackJob?.cancel()
        adPlaybackJob = viewModelScope.launch {
            for (sec in 3 downTo 0) {
                delay(1000)
                _videoAdState.value = _videoAdState.value.copy(
                    remainingSeconds = sec,
                    isCompleted = sec == 0
                )
            }
            // Execute reward callback once full ad completes
            val action = pendingAdRewardAction
            pendingAdRewardAction = null
            action?.invoke()
        }
    }

    // ==========================================
    // GOOGLE ADMOB REWARDED ADS ACTIONS
    // ==========================================

    /**
     * Claim 2X Reward (Double XP and Double Coins) using AdMob Rewarded Ad.
     */
    fun claim2xRewardWithAdMob(activity: Activity, baseRewardXp: Int, baseRewardCoins: Int) {
        if (_is2xClaimed.value || _videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to claim 2X rewards."
            return
        }

        // Backend server logging runs asynchronously without blocking
        viewModelScope.launch {
            performBackendAdServerVerification("2x_reward_admob", "double_reward_${baseRewardCoins}coins")
        }

        if (AdMobManager.isAdLoaded.value) {
            AdMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = {
                    viewModelScope.launch {
                        repository.addBonusCoinsAndXp(bonusCoins = baseRewardCoins, bonusXp = baseRewardXp)
                        _is2xClaimed.value = true
                        ServerSyncManager.logRewardClaimed("AdMob 2X Double Bonus", baseRewardCoins, userProgress.value)
                        ServerSyncManager.logStateSync(userProgress.value)
                        _claimCelebrationMessage.value = "👑 2X DOUBLE REWARDS APPLIED! +$baseRewardXp XP & +$baseRewardCoins Coins added!"
                    }
                },
                onAdDismissed = {
                    // Preloading next ad is handled automatically by AdMobManager
                },
                onAdFailedToShow = {
                    startWatchingVideoAdWithCallback {
                        viewModelScope.launch {
                            repository.addBonusCoinsAndXp(bonusCoins = baseRewardCoins, bonusXp = baseRewardXp)
                            _is2xClaimed.value = true
                            ServerSyncManager.logRewardClaimed("AdMob 2X Double Bonus", baseRewardCoins, userProgress.value)
                            ServerSyncManager.logStateSync(userProgress.value)
                            _claimCelebrationMessage.value = "👑 2X DOUBLE REWARDS APPLIED! +$baseRewardXp XP & +$baseRewardCoins Coins added!"
                        }
                    }
                }
            )
        } else {
            startWatchingVideoAdWithCallback {
                viewModelScope.launch {
                    repository.addBonusCoinsAndXp(bonusCoins = baseRewardCoins, bonusXp = baseRewardXp)
                    _is2xClaimed.value = true
                    ServerSyncManager.logRewardClaimed("2X Double Bonus", baseRewardCoins, userProgress.value)
                    ServerSyncManager.logStateSync(userProgress.value)
                    _claimCelebrationMessage.value = "👑 2X DOUBLE REWARDS APPLIED! +$baseRewardXp XP & +$baseRewardCoins Coins added!"
                }
            }
        }
    }

    /**
     * Watch 1 of 3 Ads for the 150 Coins Jackpot using AdMob Rewarded Ads (+50 coins per ad).
     */
    fun watchAdMobForDailyCoins(activity: Activity) {
        if (_videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to watch ads and earn coins!"
            return
        }

        viewModelScope.launch {
            performBackendAdServerVerification("daily_coins_admob", "daily_50_coins")
        }

        if (AdMobManager.isAdLoaded.value) {
            AdMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = {
                    viewModelScope.launch {
                        val (count, newBadges) = repository.recordAdWatched(bonusCoins = 50)
                        if (newBadges.isNotEmpty()) {
                            _unlockedAchievementNotification.value = newBadges.first()
                        }
                        ServerSyncManager.logRewardClaimed("AdMob Rewarded Video (+50 Coins)", 50, userProgress.value)
                        ServerSyncManager.logStateSync(userProgress.value)
                        if (count >= 3) {
                            _claimCelebrationMessage.value = "🎉 3/3 Ads Watched (+50 Coins)! You can now claim your 150 Coins Jackpot!"
                        } else {
                            _claimCelebrationMessage.value = "📺 Ad finished (+50 Coins, $count/3 watched)! Watch ${3 - count} more for 150 coins!"
                        }
                    }
                },
                onAdDismissed = {},
                onAdFailedToShow = {
                    startWatchingVideoAd()
                }
            )
        } else {
            startWatchingVideoAd()
        }
    }

    /**
     * Second Chance / Revive: Watch Ad to eliminate 2 incorrect answers and get a second try!
     */
    fun watchAdMobForSecondChance(activity: Activity, puzzle: PuzzleItem) {
        if (_videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet for a Second Chance!"
            return
        }

        val wrongOptions = puzzle.options.filter { it.trim() != puzzle.correctAnswer.trim() }
        val toEliminate = wrongOptions.take(2).toSet()

        viewModelScope.launch {
            performBackendAdServerVerification("second_chance_admob", "50_50_elimination")
        }

        if (AdMobManager.isAdLoaded.value) {
            AdMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = {
                    _eliminatedOptions.value = toEliminate
                    _isAnswerSubmitted.value = false
                    _selectedOption.value = null
                    _claimCelebrationMessage.value = "✨ Second Chance Granted! 50/50 incorrect choices eliminated."
                    ServerSyncManager.logRewardClaimed("Second Chance 50/50 Ad Watched", 0, userProgress.value)
                },
                onAdDismissed = {},
                onAdFailedToShow = {
                    startWatchingVideoAdWithCallback {
                        _eliminatedOptions.value = toEliminate
                        _isAnswerSubmitted.value = false
                        _selectedOption.value = null
                        _claimCelebrationMessage.value = "✨ Second Chance Granted! 50/50 incorrect choices eliminated."
                    }
                }
            )
        } else {
            startWatchingVideoAdWithCallback {
                _eliminatedOptions.value = toEliminate
                _isAnswerSubmitted.value = false
                _selectedOption.value = null
                _claimCelebrationMessage.value = "✨ Second Chance Granted! 50/50 incorrect choices eliminated."
            }
        }
    }

    /**
     * Request AI Hint and trigger Interstitial Ad after every 4 hints used to refill +4 more hints.
     */
    fun requestAiHintWithInterstitial(activity: Activity?, puzzle: PuzzleItem) {
        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to ask Gemini AI."
            return
        }

        viewModelScope.launch {
            _isHintLoading.value = true
            val hint = repository.getAiHint(puzzle.question, puzzle.options)
            _aiHintText.value = hint
            _isHintLoading.value = false

            val shouldShowInterstitial = repository.consumeHintAndCheckInterstitial()
            if (shouldShowInterstitial && activity != null) {
                if (AdMobManager.isInterstitialLoaded.value) {
                    AdMobManager.showInterstitialAd(
                        activity = activity,
                        onAdCompleted = {
                            viewModelScope.launch {
                                repository.grantBonusHints(2)
                                _claimCelebrationMessage.value = "✨ Interstitial Ad finished! +2 Free Hints Refilled! 💡"
                            }
                        },
                        onAdFailedToShow = {
                            viewModelScope.launch {
                                repository.grantBonusHints(2)
                                _claimCelebrationMessage.value = "💡 +2 Free Hints Refilled!"
                            }
                        }
                    )
                } else {
                    repository.grantBonusHints(2)
                    _claimCelebrationMessage.value = "💡 +2 Free Hints Refilled!"
                }
            }
        }
    }

    /**
     * Sliding tile smart hint with Interstitial Ad every 4 hints used.
     */
    fun requestSlidingSmartHintWithInterstitial(activity: Activity?) {
        requestSmartHint()
        viewModelScope.launch {
            val shouldShowInterstitial = repository.consumeHintAndCheckInterstitial()
            if (shouldShowInterstitial && activity != null) {
                if (AdMobManager.isInterstitialLoaded.value) {
                    AdMobManager.showInterstitialAd(
                        activity = activity,
                        onAdCompleted = {
                            viewModelScope.launch {
                                repository.grantBonusHints(2)
                                _claimCelebrationMessage.value = "✨ Interstitial Ad finished! +2 Free Hints Refilled! 💡"
                            }
                        },
                        onAdFailedToShow = {
                            viewModelScope.launch {
                                repository.grantBonusHints(2)
                                _claimCelebrationMessage.value = "💡 +2 Free Hints Refilled!"
                            }
                        }
                    )
                } else {
                    repository.grantBonusHints(2)
                    _claimCelebrationMessage.value = "💡 +2 Free Hints Refilled!"
                }
            }
        }
    }

    /**
     * Explicit action: Watch Ad to get +2 More Free Hints directly.
     */
    fun watchInterstitialForBonusHints(activity: Activity) {
        if (_videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to get Free Hints!"
            return
        }

        viewModelScope.launch {
            performBackendAdServerVerification("interstitial_hints_admob", "refill_2_hints")
        }

        if (AdMobManager.isInterstitialLoaded.value) {
            AdMobManager.showInterstitialAd(
                activity = activity,
                onAdCompleted = {
                    viewModelScope.launch {
                        repository.grantBonusHints(2)
                        _claimCelebrationMessage.value = "🎉 Ad watched! +2 Free Hints added! 💡"
                        ServerSyncManager.logRewardClaimed("Bonus 2 Hints Ad Watched", 0, userProgress.value)
                    }
                },
                onAdFailedToShow = {
                    startWatchingVideoAdWithCallback {
                        viewModelScope.launch {
                            repository.grantBonusHints(2)
                            _claimCelebrationMessage.value = "🎉 +2 Free Hints added! 💡"
                        }
                    }
                }
            )
        } else {
            startWatchingVideoAdWithCallback {
                viewModelScope.launch {
                    repository.grantBonusHints(2)
                    _claimCelebrationMessage.value = "🎉 +2 Free Hints added! 💡"
                }
            }
        }
    }

    /**
     * Explicit action: Watch Video for Extra Money / +100 Extra Coins per Level.
     */
    fun watchVideoForLevelBonus(activity: Activity) {
        if (_videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to get Level Bonus!"
            return
        }

        viewModelScope.launch {
            performBackendAdServerVerification("level_bonus_admob", "100_extra_coins")
        }

        if (AdMobManager.isAdLoaded.value) {
            AdMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = {
                    viewModelScope.launch {
                        val newBadges = repository.grantLevelBonusCoins(100)
                        if (newBadges.isNotEmpty()) {
                            _unlockedAchievementNotification.value = newBadges.first()
                        }
                        ServerSyncManager.logRewardClaimed("Level Completion Video Bonus", 100, userProgress.value)
                        ServerSyncManager.logStateSync(userProgress.value)
                        _claimCelebrationMessage.value = "💰 Level Video Bonus Claimed! +100 Extra Coins added! 🪙"
                    }
                },
                onAdDismissed = {},
                onAdFailedToShow = {
                    startWatchingVideoAdWithCallback {
                        viewModelScope.launch {
                            val newBadges = repository.grantLevelBonusCoins(100)
                            if (newBadges.isNotEmpty()) {
                                _unlockedAchievementNotification.value = newBadges.first()
                            }
                            ServerSyncManager.logRewardClaimed("Level Completion Video Bonus", 100, userProgress.value)
                            ServerSyncManager.logStateSync(userProgress.value)
                            _claimCelebrationMessage.value = "💰 Level Video Bonus Claimed! +100 Extra Coins added! 🪙"
                        }
                    }
                }
            )
        } else {
            startWatchingVideoAdWithCallback {
                viewModelScope.launch {
                    val newBadges = repository.grantLevelBonusCoins(100)
                    if (newBadges.isNotEmpty()) {
                        _unlockedAchievementNotification.value = newBadges.first()
                    }
                    ServerSyncManager.logRewardClaimed("Level Completion Video Bonus", 100, userProgress.value)
                    ServerSyncManager.logStateSync(userProgress.value)
                    _claimCelebrationMessage.value = "💰 Level Video Bonus Claimed! +100 Extra Coins added! 🪙"
                }
            }
        }
    }

    /**
     * Watch Ad for Free Smart Hint without spending coins.
     */
    fun watchAdMobForSmartHint(activity: Activity, puzzle: PuzzleItem) {
        if (_videoAdState.value.isPlaying) return

        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to get Free Smart Hint!"
            return
        }

        viewModelScope.launch {
            performBackendAdServerVerification("smart_hint_admob", "free_ai_smart_hint")
        }

        if (AdMobManager.isAdLoaded.value) {
            AdMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = {
                    requestAiHint(puzzle)
                    ServerSyncManager.logRewardClaimed("Free Smart Hint Unlocked via Ad", 0, userProgress.value)
                    _claimCelebrationMessage.value = "💡 Free AI Smart Hint Unlocked!"
                },
                onAdDismissed = {},
                onAdFailedToShow = {
                    requestAiHint(puzzle)
                }
            )
        } else {
            startWatchingVideoAdWithCallback {
                requestAiHint(puzzle)
                ServerSyncManager.logRewardClaimed("Free Smart Hint Unlocked via Ad", 0, userProgress.value)
                _claimCelebrationMessage.value = "💡 Free AI Smart Hint Unlocked!"
            }
        }
    }

    fun dismissAdDialog() {
        dismissVideoAdDialog()
    }

    fun claim150AdsReward() {
        viewModelScope.launch {
            repository.claim150AdsReward()
            ServerSyncManager.logRewardClaimed("3/3 Ads Watched Jackpot Bonus", 150, userProgress.value)
            ServerSyncManager.logStateSync(userProgress.value)
            _claimCelebrationMessage.value = "🎉 Claimed 150 Coins for watching 3 Ads! 🪙"
        }
    }

    fun resetAdsCycle() {
        viewModelScope.launch {
            repository.resetAdsCycle()
            _claimCelebrationMessage.value = "Watch 3 more Ads to unlock another 150 Coins!"
        }
    }

    fun dismissClaimCelebration() {
        _claimCelebrationMessage.value = null
    }

    // ==========================================
    // COMMUNITY HUB ACTIONS
    // ==========================================

    fun toggleLikeCommunityPost(postId: String) {
        _communityPosts.value = _communityPosts.value.map { post ->
            if (post.id == postId) {
                val newLiked = !post.isLikedByMe
                val delta = if (newLiked) 1 else -1
                post.copy(
                    isLikedByMe = newLiked,
                    likesCount = (post.likesCount + delta).coerceAtLeast(0)
                )
            } else post
        }
        ServerSyncManager.logCommunityActivity(
            action = "TOGGLE_LIKE",
            targetId = postId,
            content = "User toggled like on post $postId",
            userEmail = userProgress.value.userEmail
        )
    }

    fun addCommentToCommunityPost(postId: String, commentText: String) {
        if (commentText.isBlank()) return
        val currentProgress = userProgress.value
        val newComment = CommunityComment(
            id = "c_user_${System.currentTimeMillis()}",
            authorName = currentProgress.userName,
            authorAvatarEmoji = currentProgress.userAvatarEmoji,
            authorBadge = currentProgress.userTitle,
            countryEmoji = currentProgress.countryEmoji,
            timestamp = "Just now",
            content = commentText.trim(),
            likesCount = 0,
            isLikedByMe = false
        )
        _communityPosts.value = _communityPosts.value.map { post ->
            if (post.id == postId) {
                post.copy(comments = post.comments + newComment)
            } else post
        }
        ServerSyncManager.logCommunityActivity(
            action = "ADD_COMMENT",
            targetId = postId,
            content = commentText.trim(),
            userEmail = currentProgress.userEmail
        )
        _claimCelebrationMessage.value = "💬 Comment posted to discussion!"
    }

    fun voteCommunityPoll(optionId: String) {
        val currentPoll = _communityPoll.value
        if (currentPoll.userVotedOptionId != null) return
        val updatedOptions = currentPoll.options.map { option ->
            if (option.id == optionId) {
                option.copy(votes = option.votes + 1)
            } else option
        }
        _communityPoll.value = currentPoll.copy(
            totalVotes = currentPoll.totalVotes + 1,
            options = updatedOptions,
            userVotedOptionId = optionId
        )
        ServerSyncManager.logCommunityActivity(
            action = "VOTE_POLL",
            targetId = optionId,
            content = "Poll vote on option $optionId",
            userEmail = userProgress.value.userEmail
        )
        _claimCelebrationMessage.value = "🗳️ Vote recorded! Thanks for contributing to the community poll."
    }

    fun createCommunityPost(
        title: String,
        category: String,
        difficulty: String,
        content: String,
        hint: String?,
        solution: String?
    ) {
        if (title.isBlank() || content.isBlank()) return
        val currentProgress = userProgress.value
        val newPost = CommunityPost(
            id = "post_user_${System.currentTimeMillis()}",
            authorName = currentProgress.userName,
            authorAvatarEmoji = currentProgress.userAvatarEmoji,
            authorBadge = currentProgress.userTitle,
            countryEmoji = currentProgress.countryEmoji,
            timestamp = "Just now",
            category = category,
            difficulty = difficulty,
            title = title.trim(),
            content = content.trim(),
            hint = hint?.ifBlank { null },
            solution = solution?.ifBlank { null },
            likesCount = 1,
            isLikedByMe = true,
            comments = emptyList()
        )
        _communityPosts.value = listOf(newPost) + _communityPosts.value
        ServerSyncManager.logCommunityActivity(
            action = "CREATE_POST",
            targetId = newPost.id,
            content = "Published \"$title\" in $category ($difficulty)",
            userEmail = currentProgress.userEmail
        )
        _claimCelebrationMessage.value = "🎉 Your brain riddle was published to the Community!"
    }

    fun claimCommunityGoalReward() {
        val currentGoal = _communityCoopGoal.value
        val progress = userProgress.value
        if (progress.communityGoalClaimed) {
            _claimCelebrationMessage.value = "You've already claimed this week's Community Chest!"
            return
        }
        viewModelScope.launch {
            repository.claimCommunityGoal(currentGoal.rewardCoins, currentGoal.rewardXp)
            _communityCoopGoal.value = currentGoal.copy(isClaimed = true)
            ServerSyncManager.logRewardClaimed("Weekly Community Chest", currentGoal.rewardCoins, userProgress.value)
            ServerSyncManager.logStateSync(userProgress.value)
            _claimCelebrationMessage.value = "🎁 Community Chest Unlocked: +${currentGoal.rewardCoins} Coins & +${currentGoal.rewardXp} XP! 🏆"
        }
    }

    // ==========================================
    // USER PROFILE & SETTINGS ACTIONS
    // ==========================================

    fun updateUserProfile(
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
        viewModelScope.launch {
            repository.updateUserProfile(
                name = name,
                handle = handle,
                avatarEmoji = avatarEmoji,
                avatarStyle = avatarStyle,
                avatarColorHex = avatarColorHex,
                profileBackground = profileBackground,
                avatarBorder = avatarBorder,
                title = title,
                bio = bio,
                countryEmoji = countryEmoji
            )
            ServerSyncManager.logStateSync(userProgress.value)
            _claimCelebrationMessage.value = "✅ Profile & Theme customized successfully!"
        }
    }

    fun toggleSoundEffects(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleSoundEffects(enabled)
        }
    }

    fun claimDailyStreakReward() {
        viewModelScope.launch {
            val progress = userProgress.value
            val coinsReward = (progress.currentStreak * 25).coerceAtLeast(50)
            repository.claimTaskReward("daily_streak_${System.currentTimeMillis()}", coinsReward)
            ServerSyncManager.logRewardClaimed("Daily Streak Day ${progress.currentStreak} Bonus", coinsReward, userProgress.value)
            ServerSyncManager.logStateSync(userProgress.value)
            _claimCelebrationMessage.value = "🔥 Day ${progress.currentStreak} Streak Bonus Claimed: +$coinsReward Coins! 🪙"
        }
    }

    fun startDailyChallengePuzzle() {
        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to the internet to open next page!"
            return
        }
        val mathPuzzles = repository.getPuzzlesForCategory(PuzzleCategory.MATH, userProgress.value.selectedAgeGroup)
        val dailyPuzzle = mathPuzzles.firstOrNull() ?: repository.getPuzzlesForCategory(PuzzleCategory.VISUAL, userProgress.value.selectedAgeGroup).first()
        if (dailyPuzzle.type == PuzzleType.SLIDING_IMAGE) {
            val gridSize = userProgress.value.selectedAgeGroup.defaultGridSize
            startSlidingPuzzle(dailyPuzzle, gridSize)
        } else {
            startSubjectPuzzle(dailyPuzzle)
        }
    }

    fun refreshSection(sectionName: String) {
        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Cannot refresh $sectionName."
            return
        }

        viewModelScope.launch {
            _claimCelebrationMessage.value = "🔄 Refreshing $sectionName with backend server..."
            val result = ServerSyncManager.performBackendSync(userProgress.value)
            if (result.isSuccess) {
                _claimCelebrationMessage.value = "✅ $sectionName refreshed from backend server!"
            } else {
                _claimCelebrationMessage.value = "✅ $sectionName up to date with server!"
            }
        }
    }

    fun watchInterstitialForSolution(activity: Activity) {
        if (_videoAdState.value.isPlaying) return
        val isOnline = com.example.data.remote.NetworkMonitor.isConnected.value
        if (!isOnline) {
            _claimCelebrationMessage.value = "⚠️ You are currently offline. Connect to internet to unlock solution!"
            return
        }

        viewModelScope.launch {
            performBackendAdServerVerification("interstitial_solution_admob", "unlock_solution")
        }

        if (AdMobManager.isInterstitialLoaded.value) {
            AdMobManager.showInterstitialAd(
                activity = activity,
                onAdCompleted = {
                    _claimCelebrationMessage.value = "🎉 Solution unlocked after watching ad! 💡"
                    ServerSyncManager.logRewardClaimed("Solution Unlocked Ad Watched", 0, userProgress.value)
                },
                onAdFailedToShow = {
                    startWatchingVideoAdWithCallback {
                        _claimCelebrationMessage.value = "🎉 Solution unlocked! 💡"
                    }
                }
            )
        } else {
            startWatchingVideoAdWithCallback {
                _claimCelebrationMessage.value = "🎉 Solution unlocked! 💡"
            }
        }
    }
}

