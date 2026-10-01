package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ads.AdMobManager
import com.example.data.model.AgeGroup
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.PuzzleType
import com.example.data.remote.NetworkMonitor
import com.example.ui.components.AchievementUnlockCelebrationDialog
import com.example.ui.components.CelebrationToast
import com.example.ui.components.FiveTabLiquidGlassNavBar
import com.example.ui.components.MainNavTab
import com.example.ui.components.PuzzleSetRatingDialog
import com.example.ui.components.ServerLogsDialog
import com.example.ui.screens.AILabScreen
import com.example.ui.screens.ArrowEscapeScreen
import com.example.ui.screens.ArrowMazeScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CategoryDetailScreen
import com.example.ui.screens.CircuitLinkScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JigsawPuzzleStudioScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.SlidingPuzzleScreen
import com.example.ui.screens.SnakeArrowPuzzleScreen
import com.example.ui.screens.StatsProfileScreen
import com.example.ui.screens.SubjectQuizScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBackground
import com.example.ui.viewmodel.PuzzleViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: PuzzleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Global uncaught error logger to capture any runtime crash gracefully
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                com.example.data.remote.ServerSyncManager.logAppError(
                    tag = "UncaughtException",
                    message = "${throwable.message}",
                    exceptionName = throwable.javaClass.simpleName
                )
            } catch (e: Exception) {
                // Ignore secondary error
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Fetch and cache FCM token dynamically for user visibility (only if Firebase is configured with a valid API key)
        try {
            val app = com.google.firebase.FirebaseApp.getInstance()
            val apiKey = app.options.apiKey
            if (!apiKey.isNullOrBlank() && apiKey != "AIzaSyDummyKeyPlaceholder" && !apiKey.contains("Placeholder") && apiKey.length > 20) {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            com.example.data.remote.MyFirebaseMessagingService.saveToken(this, token)
                            android.util.Log.d("FCM_Token", "FCM token retrieved: $token")
                        } else {
                            val ex = task.exception
                            if (ex is java.lang.IllegalArgumentException || ex?.message?.contains("API key") == true) {
                                android.util.Log.w("FCM_Token", "FCM token fetch skipped: invalid or placeholder API key")
                            } else {
                                android.util.Log.e("FCM_Token", "FCM token fetch failed", ex)
                            }
                        }
                    }
            } else {
                android.util.Log.w("FCM_Token", "Firebase API key is missing or invalid; skipping FCM token fetch.")
            }
        } catch (e: Exception) {
            android.util.Log.w("FCM_Token", "Failed to retrieve FCM token on startup: ${e.message}")
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SlateBackground)
                        .safeDrawingPadding()
                ) {
                    MindMatrixApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppForegroundResume()
    }
}

@Composable
fun MindMatrixApp(viewModel: PuzzleViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val currentScreen by viewModel.currentScreen.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val slidingBoardState by viewModel.slidingState.collectAsState()
    val activePuzzles by viewModel.activeCategoryPuzzles.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()
    val isAnswerSubmitted by viewModel.isAnswerSubmitted.collectAsState()
    val isAnswerCorrect by viewModel.isAnswerCorrect.collectAsState()
    val is2xClaimed by viewModel.is2xClaimed.collectAsState()
    val eliminatedOptions by viewModel.eliminatedOptions.collectAsState()
    val aiHintText by viewModel.aiHintText.collectAsState()
    val isHintLoading by viewModel.isHintLoading.collectAsState()
    val aiGeneratedPuzzle by viewModel.aiGeneratedPuzzle.collectAsState()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsState()

    val communityPosts by viewModel.communityPosts.collectAsState()
    val communityPoll by viewModel.communityPoll.collectAsState()
    val communityCoopGoal by viewModel.communityCoopGoal.collectAsState()

    val videoAdState by viewModel.videoAdState.collectAsState()
    val isAdServerProcessing by viewModel.isAdServerProcessing.collectAsState()
    val claimCelebrationMessage by viewModel.claimCelebrationMessage.collectAsState()
    val unlockedAchievementNotification by viewModel.unlockedAchievementNotification.collectAsState()

    val serverLogs by viewModel.serverLogs.collectAsState()
    val isServerOnline by viewModel.isServerOnline.collectAsState()
    val lastLatencyMs by viewModel.lastLatencyMs.collectAsState()

    val showRatingDialog by viewModel.showRatingDialog.collectAsState()
    val ratingCategoryTitle by viewModel.ratingCategoryTitle.collectAsState()

    var showServerLogsDialog by remember { mutableStateOf(false) }

    val allTasks = remember(userProgress) { viewModel.getTasks(userProgress) }
    val unclaimedTasksCount = remember(allTasks) { allTasks.count { it.isReadyToClaim } }

    // If users watch the ads or server verification is in progress, back navigation is disabled
    BackHandler(enabled = videoAdState.isPlaying || isAdServerProcessing) {
        // Ignored while watching ads or during backend server verification
    }

    // When not on the Home screen and not watching ads, route hardware/gesture back to goBack()
    BackHandler(enabled = !videoAdState.isPlaying && !isAdServerProcessing && currentScreen != Screen.Home) {
        viewModel.goBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = currentScreen) {
            is Screen.Auth -> {
                AuthScreen(
                    userProgress = userProgress,
                    onLogin = { email, username, token ->
                        viewModel.login(email, username, token)
                        viewModel.navigateTo(Screen.Home)
                    },
                    onRegister = { email, username, password ->
                        viewModel.registerUser(email, username, password)
                        viewModel.navigateTo(Screen.Home)
                    },
                    onLogout = {
                        viewModel.logout()
                    },
                    onContinueAsGuest = {
                        viewModel.navigateTo(Screen.Home)
                    },
                    onBackClick = {
                        viewModel.navigateTo(Screen.Home)
                    }
                )
            }

            is Screen.Home -> {
                val isOnline by NetworkMonitor.isConnected.collectAsState()
                HomeScreen(
                    userProgress = userProgress,
                    unclaimedTasksCount = unclaimedTasksCount,
                    isOnline = isOnline,
                    onRefreshSection = { section -> viewModel.refreshSection(section) },
                    onRetryConnection = { NetworkMonitor.checkNow() },
                    onCategoryClick = { category ->
                        viewModel.navigateTo(Screen.CategoryDetail(category))
                    },
                    onAgeSelected = { age ->
                        viewModel.setAgeGroup(age)
                    },
                    onOpenTasks = {
                        viewModel.navigateTo(Screen.Tasks)
                    },
                    onOpenLeaderboard = {
                        viewModel.navigateTo(Screen.Leaderboard)
                    },
                    onOpenCommunity = {
                        viewModel.navigateTo(Screen.Community)
                    },
                    onOpenAiLab = {
                        viewModel.navigateTo(Screen.AILab)
                    },
                    onOpenArrowMaze = {
                        viewModel.navigateTo(Screen.ArrowMaze(1))
                    },
                    onOpenSnakeArrow = {
                        viewModel.navigateTo(Screen.SnakeArrowScreen(2))
                    },
                    onOpenJigsawStudio = {
                        viewModel.navigateTo(Screen.JigsawPuzzle())
                    },
                    onOpenCircuitLink = {
                        viewModel.navigateTo(Screen.CircuitLink(1))
                    },
                    onProfileClick = {
                        viewModel.navigateTo(Screen.StatsProfile)
                    },
                    onOpenAuth = {
                        viewModel.navigateTo(Screen.Auth)
                    },
                    onOpenRatingFeedback = {
                        viewModel.openRatingDialog("MindMatrix Puzzle Arena")
                    },
                    onDailyStreakClaim = {
                        viewModel.claimDailyStreakReward()
                    },
                    onStartDailyChallenge = {
                        viewModel.startDailyChallengePuzzle()
                    }
                )
            }

            is Screen.Tasks -> {
                TasksScreen(
                    userProgress = userProgress,
                    tasks = allTasks,
                    onClaimTask = { task ->
                        viewModel.claimTask(task)
                    },
                    onClaimAllTasks = {
                        viewModel.checkAndClaimAllTasks()
                    },
                    onWatchAd = {
                        if (activity != null) {
                            viewModel.watchAdMobForDailyCoins(activity)
                        } else {
                            viewModel.startWatchingVideoAd()
                        }
                    },
                    onClaim150AdsReward = {
                        viewModel.claim150AdsReward()
                    },
                    onResetAdsCycle = {
                        viewModel.resetAdsCycle()
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) },
                    onNavigateHome = { viewModel.navigateTo(Screen.Home) },
                    onNavigateTasks = { /* Already here */ },
                    onNavigateLeaderboard = { viewModel.navigateTo(Screen.Leaderboard) },
                    onNavigateCommunity = { viewModel.navigateTo(Screen.Community) },
                    onNavigateProfile = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.Leaderboard -> {
                LeaderboardScreen(
                    userProgress = userProgress,
                    unclaimedTasksCount = unclaimedTasksCount,
                    onBackClick = { viewModel.goBack() },
                    onNavigateHome = { viewModel.navigateTo(Screen.Home) },
                    onNavigateTasks = { viewModel.navigateTo(Screen.Tasks) },
                    onNavigateLeaderboard = { /* Already here */ },
                    onNavigateCommunity = { viewModel.navigateTo(Screen.Community) },
                    onNavigateAiLab = { viewModel.navigateTo(Screen.AILab) },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.Community -> {
                CommunityScreen(
                    userProgress = userProgress,
                    communityPosts = communityPosts,
                    communityPoll = communityPoll,
                    communityCoopGoal = communityCoopGoal,
                    onToggleLike = { postId ->
                        viewModel.toggleLikeCommunityPost(postId)
                    },
                    onAddComment = { postId, commentText ->
                        viewModel.addCommentToCommunityPost(postId, commentText)
                    },
                    onVotePoll = { optionId ->
                        viewModel.voteCommunityPoll(optionId)
                    },
                    onCreatePost = { category, difficulty, title, content, hint, solution ->
                        viewModel.createCommunityPost(title, category, difficulty, content, hint, solution)
                    },
                    onClaimCommunityGoal = {
                        viewModel.claimCommunityGoalReward()
                    },
                    onNavigateHome = { viewModel.navigateTo(Screen.Home) },
                    onNavigateTasks = { viewModel.navigateTo(Screen.Tasks) },
                    onNavigateLeaderboard = { viewModel.navigateTo(Screen.Leaderboard) },
                    onNavigateCommunity = { /* Already here */ },
                    onNavigateProfile = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.CategoryDetail -> {
                CategoryDetailScreen(
                    category = screen.category,
                    puzzles = activePuzzles,
                    userProgress = userProgress,
                    onPuzzleSelect = { puzzle ->
                        if (puzzle.type == PuzzleType.SLIDING_IMAGE) {
                            val gridSize = userProgress.selectedAgeGroup.defaultGridSize
                            viewModel.startSlidingPuzzle(puzzle, gridSize)
                        } else {
                            viewModel.startSubjectPuzzle(puzzle)
                        }
                    },
                    onImportFromUrl = { url, callback ->
                        viewModel.importPuzzlesFromUrl(url, callback)
                    },
                    onImportFromJson = { json, callback ->
                        viewModel.importPuzzlesFromJson(json, callback)
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.SlidingPuzzle -> {
                SlidingPuzzleScreen(
                    puzzle = screen.puzzle,
                    boardState = slidingBoardState,
                    userProgress = userProgress,
                    is2xClaimed = is2xClaimed,
                    onTileClick = { index ->
                        viewModel.onTileClicked(index, screen.puzzle)
                    },
                    onResetClick = { size ->
                        viewModel.resetSlidingPuzzle(size)
                    },
                    onUndoClick = {
                        viewModel.undoSlidingMove()
                    },
                    onSmartHintClick = {
                        viewModel.requestSlidingSmartHintWithInterstitial(activity)
                    },
                    onJigsawSolved = { puzzle, moves ->
                        viewModel.onJigsawPuzzleSolved(puzzle, moves, activity)
                    },
                    onClaim2xReward = {
                        if (activity != null) {
                            viewModel.claim2xRewardWithAdMob(
                                activity = activity,
                                baseRewardXp = screen.puzzle.difficulty.xpReward,
                                baseRewardCoins = screen.puzzle.difficulty.coinsReward
                            )
                        }
                    },
                    onWatchLevelBonusVideo = {
                        if (activity != null) {
                            viewModel.watchVideoForLevelBonus(activity)
                        }
                    },
                    onWatchInterstitialForHints = {
                        if (activity != null) {
                            viewModel.watchInterstitialForBonusHints(activity)
                        }
                    },
                    onRateFeedback = {
                        viewModel.openRatingDialog("Sliding & Jigsaw Puzzles")
                    },
                    onCompleteLevelWithRewardedAd = {
                        viewModel.completeLevelWithRewardedAd(activity, screen.puzzle)
                    },
                    onNextPuzzle = {
                        viewModel.nextPuzzleWithAd(activity)
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.SubjectQuiz -> {
                SubjectQuizScreen(
                    puzzle = screen.puzzle,
                    category = screen.category,
                    selectedOption = selectedOption,
                    isAnswerSubmitted = isAnswerSubmitted,
                    isAnswerCorrect = isAnswerCorrect,
                    aiHintText = aiHintText,
                    isHintLoading = isHintLoading,
                    userProgress = userProgress,
                    is2xClaimed = is2xClaimed,
                    eliminatedOptions = eliminatedOptions,
                    onOptionSelected = { option ->
                        viewModel.selectOption(option)
                    },
                    onSubmitAnswer = {
                        viewModel.submitAnswer(screen.puzzle, activity)
                    },
                    onRequestAiHint = {
                        viewModel.requestAiHintWithInterstitial(activity, screen.puzzle)
                    },
                    onClaim2xReward = {
                        if (activity != null) {
                            viewModel.claim2xRewardWithAdMob(
                                activity = activity,
                                baseRewardXp = screen.puzzle.difficulty.xpReward,
                                baseRewardCoins = screen.puzzle.difficulty.coinsReward
                            )
                        }
                    },
                    onWatchLevelBonusVideo = {
                        if (activity != null) {
                            viewModel.watchVideoForLevelBonus(activity)
                        }
                    },
                    onWatchInterstitialForHints = {
                        if (activity != null) {
                            viewModel.watchInterstitialForBonusHints(activity)
                        }
                    },
                    onWatchInterstitialForSolution = {
                        if (activity != null) {
                            viewModel.watchInterstitialForSolution(activity)
                        }
                    },
                    onSecondChanceWatchAd = {
                        if (activity != null) {
                            viewModel.watchAdMobForSecondChance(activity, screen.puzzle)
                        }
                    },
                    onRateFeedback = {
                        viewModel.openRatingDialog(screen.category.title)
                    },
                    onCompleteLevelWithRewardedAd = {
                        viewModel.completeLevelWithRewardedAd(activity, screen.puzzle)
                    },
                    onNextPuzzle = {
                        viewModel.nextPuzzleWithAd(activity)
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.AILab -> {
                AILabScreen(
                    generatedPuzzle = aiGeneratedPuzzle,
                    isGenerating = isGeneratingAi,
                    selectedOption = selectedOption,
                    isAnswerSubmitted = isAnswerSubmitted,
                    isAnswerCorrect = isAnswerCorrect,
                    userProgress = userProgress,
                    is2xClaimed = is2xClaimed,
                    onClaim2xReward = {
                        if (activity != null && aiGeneratedPuzzle != null) {
                            viewModel.claim2xRewardWithAdMob(
                                activity = activity,
                                baseRewardXp = aiGeneratedPuzzle!!.difficulty.xpReward,
                                baseRewardCoins = aiGeneratedPuzzle!!.difficulty.coinsReward
                            )
                        }
                    },
                    onWatchLevelBonusVideo = {
                        if (activity != null) {
                            viewModel.watchVideoForLevelBonus(activity)
                        }
                    },
                    onGenerate = { cat, diff ->
                        viewModel.generateNewAiPuzzle(cat, diff)
                    },
                    onOptionSelect = { opt ->
                        viewModel.selectOption(opt)
                    },
                    onSubmitAnswer = {
                        aiGeneratedPuzzle?.let { viewModel.submitAnswer(it) }
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.StatsProfile -> {
                StatsProfileScreen(
                    userProgress = userProgress,
                    onAgeSelected = { age ->
                        viewModel.setAgeGroup(age)
                    },
                    onToggleLargeText = { enabled ->
                        viewModel.toggleLargeText(enabled)
                    },
                    onToggleTileNumbers = { enabled ->
                        viewModel.toggleTileNumbers(enabled)
                    },
                    onToggleSoundEffects = { enabled ->
                        viewModel.toggleSoundEffects(enabled)
                    },
                    onLogin = { email, username ->
                        viewModel.login(email, username)
                    },
                    onLogout = {
                        viewModel.logout()
                    },
                    onPingServer = {
                        viewModel.pingServer()
                    },
                    onSyncState = {
                        viewModel.syncStateToServer()
                    },
                    onClearLogs = {
                        viewModel.clearServerLogs()
                    },
                    serverLogs = serverLogs,
                    isServerOnline = isServerOnline,
                    lastLatencyMs = lastLatencyMs,
                    onUpdateProfile = { name, handle, avatarEmoji, avatarStyle, avatarColorHex, profileBackground, avatarBorder, title, bio, countryEmoji ->
                        viewModel.updateUserProfile(
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
                    },
                    onOpenAuth = { viewModel.navigateTo(Screen.Auth) },
                    onOpenRatingFeedback = { viewModel.openRatingDialog("MindMatrix Puzzle Arena") },
                    onNavigateHome = { viewModel.navigateTo(Screen.Home) },
                    onNavigateTasks = { viewModel.navigateTo(Screen.Tasks) },
                    onNavigateLeaderboard = { viewModel.navigateTo(Screen.Leaderboard) },
                    onNavigateCommunity = { viewModel.navigateTo(Screen.Community) },
                    onNavigateProfile = { /* Already here */ },
                    onBackClick = { viewModel.goBack() }
                )
            }

            is Screen.ArrowMaze -> {
                ArrowEscapeScreen(
                    currentLevelNumber = screen.level,
                    userProgress = userProgress,
                    onLevelSolved = { level, moves, stars ->
                        viewModel.onLevelCompletedIncrement(activity)
                        viewModel.recordGameRewards(bonusCoins = 30, bonusXp = 50)
                    },
                    onNextLevelClick = { nextLvl ->
                        viewModel.triggerNextLevelInterstitial(activity) {
                            viewModel.navigateTo(Screen.ArrowMaze(nextLvl))
                        }
                    },
                    onWatchAdForHint = {
                        if (activity != null) {
                            viewModel.watchInterstitialForBonusHints(activity)
                        }
                    },
                    onClaim2xReward = {
                        if (activity != null) {
                            viewModel.claim2xRewardWithAdMob(activity, 60, 30)
                        }
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.SnakeArrowScreen -> {
                SnakeArrowPuzzleScreen(
                    currentLevelNumber = screen.level,
                    userProgress = userProgress,
                    onLevelSolved = { level, moves, stars ->
                        viewModel.onLevelCompletedIncrement(activity)
                        viewModel.recordGameRewards(bonusCoins = 45, bonusXp = 75)
                    },
                    onNextLevelClick = { nextLvl ->
                        viewModel.triggerNextLevelInterstitial(activity) {
                            viewModel.navigateTo(Screen.SnakeArrowScreen(nextLvl))
                        }
                    },
                    onWatchAdForHint = {
                        if (activity != null) {
                            viewModel.watchInterstitialForBonusHints(activity)
                        }
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.JigsawPuzzle -> {
                JigsawPuzzleStudioScreen(
                    userProgress = userProgress,
                    onPuzzleSolved = { moves, timeSeconds ->
                        viewModel.onLevelCompletedIncrement(activity)
                        viewModel.recordGameRewards(bonusCoins = 40, bonusXp = 80)
                    },
                    onNextPuzzleClick = {
                        viewModel.triggerNextLevelInterstitial(activity) {
                            viewModel.navigateTo(Screen.JigsawPuzzle())
                        }
                    },
                    onWatchAdForHint = {
                        if (activity != null) {
                            viewModel.watchInterstitialForBonusHints(activity)
                        }
                    },
                    onClaim2xReward = {
                        if (activity != null) {
                            viewModel.claim2xRewardWithAdMob(activity, 80, 40)
                        }
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }

            is Screen.CircuitLink -> {
                CircuitLinkScreen(
                    currentLevelNumber = screen.level,
                    userProgress = userProgress,
                    onLevelSolved = { level, moves ->
                        viewModel.onLevelCompletedIncrement(activity)
                        viewModel.recordGameRewards(bonusCoins = 35, bonusXp = 60)
                    },
                    onNextLevelClick = { nextLvl ->
                        viewModel.triggerNextLevelInterstitial(activity) {
                            viewModel.navigateTo(Screen.CircuitLink(nextLvl))
                        }
                    },
                    onWatchAdForHint = {
                        if (activity != null) {
                            viewModel.watchInterstitialForBonusHints(activity)
                        }
                    },
                    onClaim2xReward = {
                        if (activity != null) {
                            viewModel.claim2xRewardWithAdMob(activity, 70, 35)
                        }
                    },
                    onBackClick = { viewModel.goBack() },
                    onProfileClick = { viewModel.navigateTo(Screen.StatsProfile) }
                )
            }
        }

        // Achievement Unlock Celebration Modal
        unlockedAchievementNotification?.let { badge ->
            AchievementUnlockCelebrationDialog(
                badge = badge,
                onDismiss = { viewModel.dismissAchievementNotification() }
            )
        }

        // Custom Rating & Feedback Dialog
        if (showRatingDialog) {
            PuzzleSetRatingDialog(
                categoryTitle = ratingCategoryTitle,
                onDismiss = { viewModel.dismissRatingDialog() },
                onSubmitRating = { stars, tags, feedback ->
                    viewModel.submitPuzzleRatingAndFeedback(stars, tags, feedback)
                }
            )
        }

        // Server Activity & Live Telemetry Logs Dialog
        if (showServerLogsDialog) {
            ServerLogsDialog(
                logs = serverLogs,
                isServerOnline = isServerOnline,
                lastLatencyMs = lastLatencyMs,
                onPingServer = { viewModel.pingServer() },
                onSyncState = { viewModel.syncStateToServer() },
                onClearLogs = { viewModel.clearServerLogs() },
                onDismiss = { showServerLogsDialog = false }
            )
        }

        // Floating Celebration / Reward Notification Toast
        AnimatedVisibility(
            visible = claimCelebrationMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        ) {
            claimCelebrationMessage?.let { msg ->
                CelebrationToast(
                    message = msg,
                    onDismiss = { viewModel.dismissClaimCelebration() }
                )
            }
        }

        // Online-Only Enforcement: When offline on non-Home screens, show modal with option to view offline preview
        val isOnline by NetworkMonitor.isConnected.collectAsState()
        if (!isOnline && currentScreen !is Screen.Home) {
            OnlineOnlyEnforcementOverlay(
                onRetry = {
                    NetworkMonitor.checkNow()
                },
                onGoBackHome = {
                    viewModel.navigateTo(Screen.Home)
                }
            )
        }

        // --- FIVE-TAB BOTTOM NAVIGATION BAR WITH LOW LIQUID GLASS ANIMATION ---
        val showBottomNav = currentScreen is Screen.Home ||
                currentScreen is Screen.ArrowMaze ||
                currentScreen is Screen.SnakeArrowScreen ||
                currentScreen is Screen.Tasks ||
                currentScreen is Screen.Leaderboard ||
                currentScreen is Screen.Community

        val currentNavTab = when (currentScreen) {
            is Screen.Home -> MainNavTab.HOME
            is Screen.ArrowMaze, is Screen.SnakeArrowScreen -> MainNavTab.ARROW_MAZE
            is Screen.Tasks -> MainNavTab.TASKS
            is Screen.Leaderboard -> MainNavTab.LEADERBOARD
            is Screen.Community -> MainNavTab.COMMUNITY
            else -> MainNavTab.HOME
        }

        if (showBottomNav && !videoAdState.isPlaying && !isAdServerProcessing) {
            FiveTabLiquidGlassNavBar(
                currentTab = currentNavTab,
                taskBadgeCount = unclaimedTasksCount,
                onTabSelected = { tab ->
                    when (tab) {
                        MainNavTab.HOME -> viewModel.navigateTo(Screen.Home)
                        MainNavTab.ARROW_MAZE -> viewModel.navigateTo(Screen.SnakeArrowScreen(2))
                        MainNavTab.TASKS -> viewModel.navigateTo(Screen.Tasks)
                        MainNavTab.LEADERBOARD -> viewModel.navigateTo(Screen.Leaderboard)
                        MainNavTab.COMMUNITY -> viewModel.navigateTo(Screen.Community)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun OnlineOnlyEnforcementOverlay(
    onRetry: () -> Unit,
    onGoBackHome: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("offline_enforcement_screen"),
        color = Color(0xFF0F172A).copy(alpha = 0.96f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.5.dp, Color(0xFF334155)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDC2626).copy(alpha = 0.15f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🌐", fontSize = 32.sp)
                        }
                    }

                    Text(
                        text = "Network Connection Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Text(
                        text = "MindMatrix operates online-only to sync cloud quests, verify database puzzles, and deliver secure level progression. Please connect to Wi-Fi or mobile data to play.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("retry_connection_button")
                    ) {
                        Text(
                            text = "Check Connection & Retry",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onGoBackHome,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("offline_view_home_button")
                    ) {
                        Text(
                            text = "Back to Offline Preview",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }
    }
}
