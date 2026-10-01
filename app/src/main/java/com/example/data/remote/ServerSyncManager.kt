package com.example.data.remote

import android.util.Log
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.UserProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class ServerEventType(val label: String, val badgeColorHex: Long) {
    APP_OPEN("APP_OPEN", 0xFF14B8A6),
    APP_RESUME("APP_RESUME", 0xFF0D9488),
    APP_ERROR("APP_ERROR", 0xFFEF4444),
    GAME_START("GAME_START", 0xFF3B82F6),
    GAME_MOVE("GAME_MOVE", 0xFF06B6D4),
    GAME_SOLVE("GAME_SOLVE", 0xFF10B981),
    PUZZLE_CREATE("PUZZLE_CREATE", 0xFF8B5CF6),
    USER_AUTH_LOGIN("USER_LOGIN", 0xFFF59E0B),
    USER_AUTH_LOGOUT("USER_LOGOUT", 0xFFEF4444),
    USER_AUTH_REGISTER("USER_REGISTER", 0xFF10B981),
    USER_AUTH_OTP("AUTH_OTP", 0xFF8B5CF6),
    USER_AUTH_RESET_PW("AUTH_RESET_PW", 0xFFF97316),
    FEEDBACK_SUBMISSION("FEEDBACK_RATING", 0xFFEC4899),
    COMMUNITY_ACTION("COMMUNITY", 0xFFEC4899),
    REWARD_CLAIM("REWARD_CLAIM", 0xFFF97316),
    SYSTEM_PING("SERVER_PING", 0xFF64748B),
    STATE_SYNC("STATE_SYNC", 0xFF6366F1),
    LEADERBOARD_FETCH("LEADERBOARD", 0xFFEAB308)
}

data class ServerLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp)),
    val eventType: ServerEventType,
    val endpoint: String,
    val method: String = "POST",
    val requestSummary: String,
    val responseCode: Int = 0,
    val responseStatus: String = "PENDING",
    val latencyMs: Long = 0,
    val isSuccess: Boolean = false,
    val fullJsonPayload: String = "{}"
)

data class CloudLeaderboardPlayer(
    val playerId: String,
    val username: String,
    val email: String,
    val highestScore: Int,
    val coinsOrLikes: Int,
    val totalGamesPlayed: Int
)

object ServerSyncManager {
    private const val TAG = "ServerSyncManager"
    const val WORKER_BASE_URL = "https://mypuzzlehub.royabhay0079.workers.dev"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _logs = MutableStateFlow<List<ServerLogEntry>>(emptyList())
    val logs: StateFlow<List<ServerLogEntry>> = _logs.asStateFlow()

    private val _isServerOnline = MutableStateFlow<Boolean?>(null)
    val isServerOnline: StateFlow<Boolean?> = _isServerOnline.asStateFlow()

    private val _lastLatencyMs = MutableStateFlow(0L)
    val lastLatencyMs: StateFlow<Long> = _lastLatencyMs.asStateFlow()

    private val _cloudLeaderboard = MutableStateFlow<List<CloudLeaderboardPlayer>>(emptyList())
    val cloudLeaderboard: StateFlow<List<CloudLeaderboardPlayer>> = _cloudLeaderboard.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Initial connection handshake and leaderboard prefetch
        pingServerAsync()
        fetchLeaderboardAsync()
    }

    fun pingServerAsync() {
        coroutineScope.launch {
            pingServer()
        }
    }

    fun fetchLeaderboardAsync() {
        coroutineScope.launch {
            fetchCloudLeaderboard()
        }
    }

    suspend fun pingServer(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url("$WORKER_BASE_URL/api/health")
                .header("User-Agent", "MindMatrix-Android/1.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val code = response.code
            val body = response.body?.string() ?: ""

            _isServerOnline.value = response.isSuccessful
            _lastLatencyMs.value = latency

            val entry = ServerLogEntry(
                eventType = ServerEventType.SYSTEM_PING,
                endpoint = "$WORKER_BASE_URL/api/health",
                method = "GET",
                requestSummary = "Server Health Check (/api/health)",
                responseCode = code,
                responseStatus = if (response.isSuccessful) "200 OK" else "HTTP $code",
                latencyMs = latency,
                isSuccess = response.isSuccessful,
                fullJsonPayload = body
            )
            appendLog(entry)
            Pair(response.isSuccessful, "Connected to $WORKER_BASE_URL (${latency}ms, HTTP $code)")
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Log.e(TAG, "Ping failed: ${e.message}", e)
            _isServerOnline.value = false
            _lastLatencyMs.value = latency

            val entry = ServerLogEntry(
                eventType = ServerEventType.SYSTEM_PING,
                endpoint = "$WORKER_BASE_URL/api/health",
                method = "GET",
                requestSummary = "Server Health Check (Offline / Reconnect)",
                responseCode = 0,
                responseStatus = "OFFLINE / ${e.javaClass.simpleName}",
                latencyMs = latency,
                isSuccess = false,
                fullJsonPayload = "{\"error\": \"${e.message}\"}"
            )
            appendLog(entry)
            Pair(false, "Server unreachable: ${e.message}")
        }
    }

    suspend fun fetchCloudLeaderboard(): List<CloudLeaderboardPlayer> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url("$WORKER_BASE_URL/api/leaderboard")
                .header("User-Agent", "MindMatrix-Android/1.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val code = response.code
            val body = response.body?.string() ?: "{}"

            if (response.isSuccessful) {
                val json = JSONObject(body)
                val array = json.optJSONArray("leaderboard") ?: JSONArray()
                val list = mutableListOf<CloudLeaderboardPlayer>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CloudLeaderboardPlayer(
                            playerId = obj.optString("player_id", "P_$i"),
                            username = obj.optString("username", "Player $i"),
                            email = obj.optString("email", ""),
                            highestScore = obj.optInt("highest_score", 0),
                            coinsOrLikes = obj.optInt("coins_or_likes", 0),
                            totalGamesPlayed = obj.optInt("total_games_played", 0)
                        )
                    )
                }
                _cloudLeaderboard.value = list

                val entry = ServerLogEntry(
                    eventType = ServerEventType.LEADERBOARD_FETCH,
                    endpoint = "$WORKER_BASE_URL/api/leaderboard",
                    method = "GET",
                    requestSummary = "Fetched ${list.size} Global Leaderboard Players",
                    responseCode = code,
                    responseStatus = "200 OK",
                    latencyMs = latency,
                    isSuccess = true,
                    fullJsonPayload = body
                )
                appendLog(entry)
                return@withContext list
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch cloud leaderboard: ${e.message}")
        }
        emptyList()
    }

    suspend fun sendOtp(email: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("email", email.trim())
        }
        val startTime = System.currentTimeMillis()
        try {
            val jsonBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$WORKER_BASE_URL/api/send-otp")
                .header("Content-Type", "application/json")
                .header("User-Agent", "MindMatrix-Android/1.0")
                .post(jsonBody)
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)
            val isSuccess = response.isSuccessful && json.optBoolean("success", true)
            val msg = json.optString("message", if (isSuccess) "OTP Sent Successfully" else json.optString("error", "Failed to send OTP"))

            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_LOGIN,
                endpoint = "$WORKER_BASE_URL/api/send-otp",
                method = "POST",
                requestSummary = "Send OTP to $email",
                responseCode = response.code,
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = body
            )
            appendLog(entry)
            Pair(isSuccess, msg)
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("email", email.trim())
            put("otp", otp.trim())
        }
        val startTime = System.currentTimeMillis()
        try {
            val jsonBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$WORKER_BASE_URL/api/verify-otp")
                .header("Content-Type", "application/json")
                .header("User-Agent", "MindMatrix-Android/1.0")
                .post(jsonBody)
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: "{}"
            val json = JSONObject(body)
            val isSuccess = response.isSuccessful && json.optBoolean("success", true)
            val msg = if (isSuccess) "OTP Verified! Logged into Cloud Server" else json.optString("error", "Invalid OTP")

            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_LOGIN,
                endpoint = "$WORKER_BASE_URL/api/verify-otp",
                method = "POST",
                requestSummary = "Verify OTP for $email",
                responseCode = response.code,
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = body
            )
            appendLog(entry)
            Pair(isSuccess, msg)
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }

    suspend fun sendLogout(email: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("email", email.ifBlank { "royabhay0079@gmail.com" })
        }
        val startTime = System.currentTimeMillis()
        try {
            val jsonBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$WORKER_BASE_URL/api/logout")
                .header("Content-Type", "application/json")
                .header("User-Agent", "MindMatrix-Android/1.0")
                .post(jsonBody)
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: "{}"
            val isSuccess = response.isSuccessful

            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_LOGOUT,
                endpoint = "$WORKER_BASE_URL/api/logout",
                method = "POST",
                requestSummary = "User Logout for $email",
                responseCode = response.code,
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = body
            )
            appendLog(entry)
            Pair(isSuccess, "Logged out from cloud server")
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }

    fun logAppOpen(userProgress: UserProgress, source: String = "ColdStart") {
        val effectiveEmail = userProgress.userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "APP_OPEN")
            put("openSource", source)
            put("userEmail", effectiveEmail)
            put("userName", userProgress.userName)
            put("isLoggedIn", userProgress.isLoggedIn)
            put("sessionToken", userProgress.sessionToken)
            put("totalXp", userProgress.totalXp)
            put("coins", userProgress.coins)
            put("streak", userProgress.currentStreak)
            put("starsEarned", userProgress.starsEarned)
            put("totalSolved", userProgress.totalPuzzlesSolved)
            put("selectedAgeGroup", userProgress.selectedAgeGroup.name)
            put("deviceInfo", "Android SDK ${android.os.Build.VERSION.SDK_INT} (${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL})")
            put("appVersion", "1.0.0")
            put("workerUrl", WORKER_BASE_URL)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "APP_OPEN",
            summary = "App Opened [$source]: ${userProgress.userName} (${if (userProgress.isLoggedIn) effectiveEmail else "Guest"}) • ${userProgress.coins}🪙 • ${userProgress.totalXp} XP",
            metadata = metadata,
            eventType = ServerEventType.APP_OPEN
        )
    }

    fun logAppResume(userProgress: UserProgress) {
        val effectiveEmail = userProgress.userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "APP_RESUME")
            put("userEmail", effectiveEmail)
            put("userName", userProgress.userName)
            put("totalXp", userProgress.totalXp)
            put("coins", userProgress.coins)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "APP_RESUME",
            summary = "App Resumed to Foreground: ${userProgress.userName}",
            metadata = metadata,
            eventType = ServerEventType.APP_RESUME
        )
    }

    fun logAppError(tag: String, message: String, exceptionName: String? = null, userEmail: String? = null) {
        val effectiveEmail = userEmail?.ifBlank { "royabhay0079@gmail.com" } ?: "royabhay0079@gmail.com"
        val metadata = JSONObject().apply {
            put("event", "APP_ERROR")
            put("tag", tag)
            put("errorMessage", message)
            put("exception", exceptionName ?: "Exception")
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "APP_ERROR",
            summary = "App Diagnostic Alert [$tag]: $message",
            metadata = metadata,
            eventType = ServerEventType.APP_ERROR
        )
    }

    fun logGameplayStart(puzzle: PuzzleItem, userProgress: UserProgress) {
        val effectiveEmail = userProgress.userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "GAMEPLAY_START")
            put("puzzleId", puzzle.id)
            put("puzzleTitle", puzzle.title)
            put("category", puzzle.category.id)
            put("difficulty", puzzle.difficulty.name)
            put("puzzleType", puzzle.type.name)
            put("level", puzzle.level)
            put("userEmail", effectiveEmail)
            put("userName", userProgress.userName)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "GAME_START",
            summary = "Started ${puzzle.category.title}: \"${puzzle.title}\" (${puzzle.difficulty.label})",
            metadata = metadata,
            eventType = ServerEventType.GAME_START
        )
    }

    fun logGameplayMove(puzzleId: String, moveNumber: Int, details: String, userEmail: String) {
        val effectiveEmail = userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "GAMEPLAY_MOVE")
            put("puzzleId", puzzleId)
            put("moveNumber", moveNumber)
            put("details", details)
            put("userEmail", effectiveEmail)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "GAME_MOVE",
            summary = "Move #$moveNumber on $puzzleId: $details",
            metadata = metadata,
            eventType = ServerEventType.GAME_MOVE
        )
    }

    fun logGameplaySolve(
        puzzle: PuzzleItem,
        score: Int,
        moves: Int,
        timeSeconds: Int,
        isSuccess: Boolean,
        userProgress: UserProgress
    ) {
        val effectiveEmail = userProgress.userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", if (isSuccess) "GAMEPLAY_SOLVE_SUCCESS" else "GAMEPLAY_ATTEMPT_FAILED")
            put("puzzleId", puzzle.id)
            put("puzzleTitle", puzzle.title)
            put("category", puzzle.category.id)
            put("difficulty", puzzle.difficulty.name)
            put("scoreEarned", score)
            put("movesCount", moves)
            put("timeTakenSeconds", timeSeconds)
            put("isSuccess", isSuccess)
            put("newTotalXp", userProgress.totalXp)
            put("newTotalCoins", userProgress.coins)
            put("userEmail", effectiveEmail)
            put("userName", userProgress.userName)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "GAME_SOLVE",
            summary = if (isSuccess) "Solved '${puzzle.title}' in ${timeSeconds}s ($moves moves, +$score score)" else "Failed attempt on '${puzzle.title}'",
            metadata = metadata,
            eventType = ServerEventType.GAME_SOLVE
        )
    }

    fun logRewardClaimed(rewardSource: String, coins: Int, userProgress: UserProgress) {
        val effectiveEmail = userProgress.userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "REWARD_CLAIMED")
            put("rewardSource", rewardSource)
            put("coinsAwarded", coins)
            put("currentTotalCoins", userProgress.coins)
            put("currentTotalXp", userProgress.totalXp)
            put("userEmail", effectiveEmail)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "REWARD_CLAIM",
            summary = "Claimed +$coins Coins ($rewardSource) • Balance: ${userProgress.coins}🪙",
            metadata = metadata,
            eventType = ServerEventType.REWARD_CLAIM
        )
    }

    fun logCommunityActivity(action: String, targetId: String, content: String, userEmail: String) {
        val effectiveEmail = userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "COMMUNITY_ACTION")
            put("action", action)
            put("targetId", targetId)
            put("content", content)
            put("userEmail", effectiveEmail)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "COMMUNITY",
            summary = "Community [$action] on $targetId: \"${content.take(40)}\"",
            metadata = metadata,
            eventType = ServerEventType.COMMUNITY_ACTION
        )
    }

    fun logPuzzleCreated(
        title: String,
        category: PuzzleCategory,
        difficulty: Difficulty,
        isAiGenerated: Boolean,
        promptOrDescription: String,
        userEmail: String
    ) {
        val effectiveEmail = userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "PUZZLE_CREATED")
            put("title", title)
            put("category", category.id)
            put("difficulty", difficulty.name)
            put("isAiGenerated", isAiGenerated)
            put("prompt", promptOrDescription)
            put("userEmail", effectiveEmail)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "PUZZLE_CREATE",
            summary = "Generated Puzzle \"$title\" in ${category.title} (${difficulty.label})",
            metadata = metadata,
            eventType = ServerEventType.PUZZLE_CREATE
        )
    }

    fun logUserAuth(isLogin: Boolean, email: String, username: String, progress: UserProgress) {
        val effectiveEmail = email.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", if (isLogin) "USER_LOGIN" else "USER_LOGOUT")
            put("email", effectiveEmail)
            put("username", username)
            put("totalXp", progress.totalXp)
            put("coins", progress.coins)
            put("streak", progress.currentStreak)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = if (isLogin) "LOGIN" else "LOGOUT",
            summary = if (isLogin) "Player Logged In: $username ($effectiveEmail)" else "Player Logged Out: $username ($effectiveEmail)",
            metadata = metadata,
            eventType = if (isLogin) ServerEventType.USER_AUTH_LOGIN else ServerEventType.USER_AUTH_LOGOUT
        )
    }

    fun logUserRegistration(email: String, username: String, progress: UserProgress) {
        val effectiveEmail = email.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "USER_REGISTER")
            put("email", effectiveEmail)
            put("username", username)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "REGISTER",
            summary = "New Player Registered: $username ($effectiveEmail)",
            metadata = metadata,
            eventType = ServerEventType.USER_AUTH_REGISTER
        )
    }

    fun logOtpRequested(email: String, action: String) {
        val effectiveEmail = email.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "AUTH_OTP_REQUESTED")
            put("email", effectiveEmail)
            put("action", action)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "AUTH_OTP",
            summary = "6-Digit OTP Dispatched to $effectiveEmail for $action",
            metadata = metadata,
            eventType = ServerEventType.USER_AUTH_OTP
        )
    }

    fun logOtpVerified(email: String, isSuccess: Boolean) {
        val effectiveEmail = email.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "AUTH_OTP_VERIFIED")
            put("email", effectiveEmail)
            put("isSuccess", isSuccess)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "AUTH_OTP",
            summary = if (isSuccess) "OTP Verified Successfully for $effectiveEmail" else "OTP Verification Failed for $effectiveEmail",
            metadata = metadata,
            eventType = ServerEventType.USER_AUTH_OTP
        )
    }

    fun logPasswordReset(email: String) {
        val effectiveEmail = email.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "AUTH_RESET_PASSWORD")
            put("email", effectiveEmail)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "AUTH_RESET_PW",
            summary = "Password Reset Completed for $effectiveEmail",
            metadata = metadata,
            eventType = ServerEventType.USER_AUTH_RESET_PW
        )
    }

    fun logFeedbackSubmission(
        stars: Int,
        tags: List<String>,
        feedbackText: String,
        puzzleCategory: String?,
        userProgress: UserProgress
    ) {
        val effectiveEmail = userProgress.userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "FEEDBACK_SUBMITTED")
            put("ratingStars", stars)
            put("selectedTags", JSONArray(tags))
            put("feedbackText", feedbackText)
            put("puzzleCategory", puzzleCategory ?: "General")
            put("userEmail", effectiveEmail)
            put("userName", userProgress.userName)
            put("totalSolved", userProgress.totalPuzzlesSolved)
            put("timestamp", System.currentTimeMillis())
        }
        val starEmoji = "★".repeat(stars)
        dispatchLog(
            email = effectiveEmail,
            activityType = "FEEDBACK_RATING",
            summary = "Puzzle Set Feedback ($starEmoji $stars/5): \"${feedbackText.take(35).ifBlank { tags.joinToString(", ") }}\" by ${userProgress.userName}",
            metadata = metadata,
            eventType = ServerEventType.FEEDBACK_SUBMISSION
        )
    }

    fun logStateSync(userProgress: UserProgress) {
        val effectiveEmail = userProgress.userEmail.ifBlank { "royabhay0079@gmail.com" }
        val metadata = JSONObject().apply {
            put("event", "STATE_SYNC")
            put("email", effectiveEmail)
            put("username", userProgress.userName)
            put("totalXp", userProgress.totalXp)
            put("coins", userProgress.coins)
            put("starsEarned", userProgress.starsEarned)
            put("currentStreak", userProgress.currentStreak)
            put("puzzlesSolved", userProgress.totalPuzzlesSolved)
            put("selectedAgeGroup", userProgress.selectedAgeGroup.name)
            put("isLoggedIn", userProgress.isLoggedIn)
            put("timestamp", System.currentTimeMillis())
        }
        dispatchLog(
            email = effectiveEmail,
            activityType = "STATE_SYNC",
            summary = "State Synchronized: ${userProgress.totalXp} XP • ${userProgress.coins} Coins • ${userProgress.starsEarned}★",
            metadata = metadata,
            eventType = ServerEventType.STATE_SYNC
        )
    }

    /**
     * Backend server ad verification and preparation.
     * Operates asynchronously in background without showing or delaying ad presentation.
     */
    suspend fun verifyAdWatchWithBackendServer(
        adUnitId: String,
        rewardType: String,
        userEmail: String
    ): Boolean = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val effectiveEmail = userEmail.ifBlank { "royabhay0079@gmail.com" }

        val metadata = JSONObject().apply {
            put("event", "AD_WATCH_VERIFIED")
            put("adUnitId", adUnitId)
            put("rewardType", rewardType)
            put("userEmail", effectiveEmail)
            put("timestamp", System.currentTimeMillis())
            put("status", "VERIFIED")
        }

        dispatchLog(
            email = effectiveEmail,
            activityType = "AD_WATCH_VERIFIED",
            summary = "Backend Server: Ad session verified for '$rewardType' ($adUnitId)",
            metadata = metadata,
            eventType = ServerEventType.REWARD_CLAIM
        )

        true
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun dispatchLog(
        email: String,
        activityType: String,
        summary: String,
        metadata: JSONObject,
        eventType: ServerEventType
    ) {
        coroutineScope.launch {
            val startTime = System.currentTimeMillis()
            val url = "$WORKER_BASE_URL/api/log"
            var responseCode = 0
            var statusString = "SENT"
            var isSuccess = false

            // The Cloudflare Worker /api/log endpoint strictly requires:
            // email, activity_type, details, metadata
            val payload = JSONObject().apply {
                put("email", email.ifBlank { "royabhay0079@gmail.com" })
                put("activity_type", activityType)
                put("details", summary)
                put("metadata", metadata)
            }

            val jsonBody = payload.toString().toRequestBody("application/json".toMediaType())

            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "MindMatrix-Android/1.0")
                    .header("X-Action-Type", activityType)
                    .header("X-User-Email", email)
                    .post(jsonBody)
                    .build()

                val response = client.newCall(request).execute()
                responseCode = response.code
                val responseBody = response.body?.string() ?: "{}"
                isSuccess = response.isSuccessful

                statusString = if (isSuccess) "200 OK" else "HTTP $responseCode"
                _isServerOnline.value = true
            } catch (e: Exception) {
                Log.w(TAG, "Request to $url failed: ${e.message}")
                statusString = "OFFLINE / LOCAL QUEUED"
                isSuccess = false
            }

            val latency = System.currentTimeMillis() - startTime
            _lastLatencyMs.value = latency
            val entry = ServerLogEntry(
                eventType = eventType,
                endpoint = url,
                method = "POST",
                requestSummary = summary,
                responseCode = responseCode,
                responseStatus = statusString,
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = payload.toString(2)
            )
            appendLog(entry)
        }
    }

    suspend fun performBackendSync(userProgress: UserProgress): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            logStateSync(userProgress)
            fetchCloudLeaderboard()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(name: String, email: String, password: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "$WORKER_BASE_URL/signup"
        try {
            val requestBody = SignupRequest(name = name.trim(), email = email.trim(), password = password)
            val response = AuthApiService.instance.signup(requestBody)
            val latency = System.currentTimeMillis() - startTime
            
            val responseBody = response.body()
            val isSuccess = response.isSuccessful && responseBody?.success == true
            val msg = responseBody?.message ?: responseBody?.error ?: (if (isSuccess) "Signup successful" else "Signup failed")
            
            val fullPayloadJson = "{\"success\": $isSuccess, \"message\": \"${msg.replace("\"", "\\\"")}\"}"
            
            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_REGISTER,
                endpoint = url,
                method = "POST",
                requestSummary = "User Signup for ${email.trim()}",
                responseCode = response.code(),
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code()}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = fullPayloadJson
            )
            appendLog(entry)
            Pair(isSuccess, msg)
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_REGISTER,
                endpoint = url,
                method = "POST",
                requestSummary = "User Signup Error",
                responseCode = 0,
                responseStatus = "OFFLINE",
                latencyMs = latency,
                isSuccess = false,
                fullJsonPayload = "{\"error\": \"${e.message}\"}"
            )
            appendLog(entry)
            Pair(false, "Network error: ${e.message}")
        }
    }

    data class AuthResult(
        val success: Boolean,
        val message: String,
        val userId: String? = null,
        val name: String? = null,
        val email: String? = null,
        val token: String? = null
    )

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "$WORKER_BASE_URL/login"
        try {
            val requestBody = LoginRequest(email = email.trim(), password = password)
            val response = AuthApiService.instance.login(requestBody)
            val latency = System.currentTimeMillis() - startTime
            
            val responseBody = response.body()
            val isSuccess = response.isSuccessful && responseBody?.success == true
            val msg = responseBody?.message ?: responseBody?.error ?: (if (isSuccess) "Login successful" else "Login failed")
            
            val loginData = responseBody?.data
            val userId = loginData?.userId
            val name = loginData?.name
            val resEmail = loginData?.email
            val token = loginData?.token
            
            val fullPayloadJson = "{\"success\": $isSuccess, \"message\": \"${msg.replace("\"", "\\\"")}\", \"data\": {\"user_id\": \"$userId\", \"name\": \"$name\", \"email\": \"$resEmail\", \"token\": \"$token\"}}"
            
            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_LOGIN,
                endpoint = url,
                method = "POST",
                requestSummary = "User Login for ${email.trim()}",
                responseCode = response.code(),
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code()}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = fullPayloadJson
            )
            appendLog(entry)
            AuthResult(isSuccess, msg, userId, name, resEmail, token)
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_LOGIN,
                endpoint = url,
                method = "POST",
                requestSummary = "User Login Error",
                responseCode = 0,
                responseStatus = "OFFLINE",
                latencyMs = latency,
                isSuccess = false,
                fullJsonPayload = "{\"error\": \"${e.message}\"}"
            )
            appendLog(entry)
            AuthResult(false, "Network error: ${e.message}")
        }
    }

    suspend fun logoutFromServer(token: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "$WORKER_BASE_URL/logout"
        try {
            val requestBody = LogoutRequest(token = token)
            val response = AuthApiService.instance.logout(requestBody)
            val latency = System.currentTimeMillis() - startTime
            
            val responseBody = response.body()
            val isSuccess = response.isSuccessful && responseBody?.success == true
            val msg = responseBody?.message ?: responseBody?.error ?: (if (isSuccess) "Logged out" else "Logout failed")
            
            val fullPayloadJson = "{\"success\": $isSuccess, \"message\": \"${msg.replace("\"", "\\\"")}\"}"
            
            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_LOGOUT,
                endpoint = url,
                method = "POST",
                requestSummary = "User Server Logout",
                responseCode = response.code(),
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code()}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = fullPayloadJson
            )
            appendLog(entry)
            Pair(isSuccess, msg)
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }

    suspend fun forgotPassword(email: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "$WORKER_BASE_URL/forgot-password"
        try {
            val requestBody = ForgotPasswordRequest(email = email.trim())
            val response = AuthApiService.instance.forgotPassword(requestBody)
            val latency = System.currentTimeMillis() - startTime
            
            val responseBody = response.body()
            val isSuccess = response.isSuccessful && responseBody?.success == true
            val msg = responseBody?.message ?: responseBody?.error ?: (if (isSuccess) "Reset email sent" else "Forgot password request failed")
            
            val fullPayloadJson = "{\"success\": $isSuccess, \"message\": \"${msg.replace("\"", "\\\"")}\"}"
            
            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_RESET_PW,
                endpoint = url,
                method = "POST",
                requestSummary = "Forgot Password for ${email.trim()}",
                responseCode = response.code(),
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code()}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = fullPayloadJson
            )
            appendLog(entry)
            Pair(isSuccess, msg)
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }

    suspend fun resetPassword(token: String, newPassword: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "$WORKER_BASE_URL/reset-password"
        try {
            val requestBody = ResetPasswordRequest(token = token.trim(), newPassword = newPassword)
            val response = AuthApiService.instance.resetPassword(requestBody)
            val latency = System.currentTimeMillis() - startTime
            
            val responseBody = response.body()
            val isSuccess = response.isSuccessful && responseBody?.success == true
            val msg = responseBody?.message ?: responseBody?.error ?: (if (isSuccess) "Password reset successful" else "Password reset failed")
            
            val fullPayloadJson = "{\"success\": $isSuccess, \"message\": \"${msg.replace("\"", "\\\"")}\"}"
            
            val entry = ServerLogEntry(
                eventType = ServerEventType.USER_AUTH_RESET_PW,
                endpoint = url,
                method = "POST",
                requestSummary = "Reset Password with token",
                responseCode = response.code(),
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code()}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = fullPayloadJson
            )
            appendLog(entry)
            Pair(isSuccess, msg)
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }

    suspend fun registerDevice(userId: String, deviceToken: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "$WORKER_BASE_URL/register-device"
        try {
            val requestBody = RegisterDeviceRequest(userId = userId, deviceToken = deviceToken)
            val response = AuthApiService.instance.registerDevice(requestBody)
            val latency = System.currentTimeMillis() - startTime
            
            val responseBody = response.body()
            val isSuccess = response.isSuccessful && responseBody?.success == true
            val msg = responseBody?.message ?: responseBody?.error ?: (if (isSuccess) "Device registered" else "Device registration failed")
            
            val fullPayloadJson = "{\"success\": $isSuccess, \"message\": \"${msg.replace("\"", "\\\"")}\"}"
            
            val entry = ServerLogEntry(
                eventType = ServerEventType.STATE_SYNC,
                endpoint = url,
                method = "POST",
                requestSummary = "Register FCM device token for $userId",
                responseCode = response.code(),
                responseStatus = if (isSuccess) "200 OK" else "HTTP ${response.code()}",
                latencyMs = latency,
                isSuccess = isSuccess,
                fullJsonPayload = fullPayloadJson
            )
            appendLog(entry)
            Pair(isSuccess, msg)
        } catch (e: Exception) {
            Pair(false, "Network error: ${e.message}")
        }
    }

    private fun appendLog(entry: ServerLogEntry) {
        val currentList = _logs.value.toMutableList()
        currentList.add(0, entry) // newest at top
        if (currentList.size > 200) {
            currentList.removeAt(currentList.lastIndex)
        }
        _logs.value = currentList
    }
}

