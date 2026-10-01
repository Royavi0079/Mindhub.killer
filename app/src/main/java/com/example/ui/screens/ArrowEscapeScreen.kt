package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ArrowEscapeLevel
import com.example.data.model.ArrowEscapeLevelProvider
import com.example.data.model.EasybrainArrowDir
import com.example.data.model.EasybrainArrowPiece
import com.example.data.model.UserProgress
import com.example.ui.components.AppHeader
import com.example.ui.theme.AiRose
import com.example.ui.theme.GoldStar
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCanvas
import com.example.ui.theme.AppThemeUtil
import com.example.ui.theme.isDark
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.util.HapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Arrow Escape Puzzle Game Screen
 * Faithfully follows the Easybrain 'Arrow Puzzle' gameplay mechanics:
 * 1. Tap an arrow to make it fly along its direction out of the screen.
 * 2. Unobstructed path check: arrow only flies if its forward raycast is clear of all other arrows.
 * 3. Collision & Mistake check: tapping a blocked arrow triggers a bump shake and deducts 1 life.
 * 4. Game Over upon losing all 3 lives, with free retry or ad-refill.
 * 5. Smart Hint highlighting the currently unobstructed arrow.
 * 6. Visual victory celebration with stars, XP, and coin rewards.
 */
@Composable
fun ArrowEscapeScreen(
    currentLevelNumber: Int = 1,
    userProgress: UserProgress,
    onLevelSolved: (level: Int, moves: Int, stars: Int) -> Unit = { _, _, _ -> },
    onNextLevelClick: (nextLevel: Int) -> Unit = {},
    onWatchAdForHint: (() -> Unit)? = null,
    onClaim2xReward: (() -> Unit)? = null,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val themeColors = remember(userProgress.userProfileBackground) {
        AppThemeUtil.getAppColors(userProgress.userProfileBackground)
    }
    val appBgColor = themeColors.first
    val appSectionColor = themeColors.second
    val totalLevels = ArrowEscapeLevelProvider.levels.size
    val levelIndex = (currentLevelNumber - 1).coerceIn(0, totalLevels - 1)
    val level = ArrowEscapeLevelProvider.levels[levelIndex]

    // Active arrows currently remaining on the grid
    val activePieces = remember(level.levelNumber) {
        mutableStateListOf<EasybrainArrowPiece>().apply {
            addAll(level.pieces)
        }
    }

    // Pieces currently in flight (flying off screen)
    val flyingPieceIds = remember(level.levelNumber) { mutableStateListOf<Int>() }

    // Lives left (max 3)
    var livesLeft by remember(level.levelNumber) { mutableIntStateOf(level.maxLives) }
    var movesCount by remember(level.levelNumber) { mutableIntStateOf(0) }
    var timeElapsed by remember(level.levelNumber) { mutableIntStateOf(0) }
    var isSolved by remember(level.levelNumber) { mutableStateOf(false) }
    var isGameOver by remember(level.levelNumber) { mutableStateOf(false) }
    var highlightedHintId by remember(level.levelNumber) { mutableStateOf<Int?>(null) }
    var collidingPieceId by remember(level.levelNumber) { mutableStateOf<Int?>(null) }
    var showLevelSelector by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(level.levelNumber, isSolved, isGameOver) {
        while (!isSolved && !isGameOver) {
            delay(1000)
            timeElapsed++
        }
    }

    // Reset level function
    fun resetLevel() {
        activePieces.clear()
        activePieces.addAll(level.pieces)
        flyingPieceIds.clear()
        livesLeft = level.maxLives
        movesCount = 0
        timeElapsed = 0
        isSolved = false
        isGameOver = false
        highlightedHintId = null
        collidingPieceId = null
    }

    // Handle arrow tap with Easybrain rules
    fun onArrowTapped(piece: EasybrainArrowPiece) {
        if (isSolved || isGameOver || flyingPieceIds.contains(piece.id)) return

        movesCount++
        highlightedHintId = null

        val isClear = ArrowEscapeLevelProvider.isPieceUnblocked(
            piece = piece,
            allActivePieces = activePieces,
            rows = level.rows,
            cols = level.cols
        )

        if (isClear) {
            // Unobstructed: Arrow safely flies away off the board!
            HapticManager.performTileSlide(context)
            flyingPieceIds.add(piece.id)

            coroutineScope.launch {
                // Allow fly animation to finish, then remove piece from state
                delay(380)
                activePieces.removeAll { it.id == piece.id }
                flyingPieceIds.remove(piece.id)

                // Check victory condition
                if (activePieces.isEmpty()) {
                    isSolved = true
                    HapticManager.performLevelComplete(context)
                    val stars = when {
                        livesLeft == level.maxLives -> 3
                        livesLeft >= 2 -> 2
                        else -> 1
                    }
                    onLevelSolved(level.levelNumber, movesCount, stars)
                }
            }
        } else {
            // Collision: Arrow path is blocked!
            HapticManager.performWrongAnswer(context)
            collidingPieceId = piece.id
            livesLeft = (livesLeft - 1).coerceAtLeast(0)

            coroutineScope.launch {
                delay(400)
                collidingPieceId = null
                if (livesLeft <= 0) {
                    isGameOver = true
                }
            }
        }
    }

    // Trigger hint
    fun useHint() {
        val freePiece = ArrowEscapeLevelProvider.findUnblockedPiece(
            allActivePieces = activePieces,
            rows = level.rows,
            cols = level.cols
        )
        if (freePiece != null) {
            highlightedHintId = freePiece.id
            HapticManager.performTileSlide(context)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appBgColor)
            .testTag("arrow_escape_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Header
            AppHeader(
                title = "Arrow Puzzle",
                userProgress = userProgress,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Top Status Bar: Level Name, Lives & Timer
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = appSectionColor),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lvl ${level.levelNumber}: ${level.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimaryIndigo.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = level.difficulty,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Remaining: ${activePieces.size} arrows • ⏱️ ${timeElapsed}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    // Lives Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..level.maxLives) {
                            val hasLife = i <= livesLeft
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Life",
                                tint = if (hasLife) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedButton(
                            onClick = { showLevelSelector = true },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.5f)),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Lvls", style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Gameplay Rule Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap arrows whose path ahead is clear. Blocked taps lose a life!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1E40AF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // --- THE ARROW GRID BOARD ---
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .widthIn(max = 400.dp)
                    .aspectRatio(1f)
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (r in 0 until level.rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (c in 0 until level.cols) {
                                    val piece = activePieces.firstOrNull { it.row == r && it.col == c }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(
                                                if (piece != null) Color.Transparent
                                                else Color(0xFFE2E8F0).copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(14.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (piece != null) {
                                            val blockingDistance = remember(piece, activePieces) {
                                                val blocker = ArrowEscapeLevelProvider.getBlockingPiece(
                                                    piece = piece,
                                                    allActivePieces = activePieces,
                                                    rows = level.rows,
                                                    cols = level.cols
                                                )
                                                if (blocker != null) {
                                                    val distRow = kotlin.math.abs(blocker.row - piece.row)
                                                    val distCol = kotlin.math.abs(blocker.col - piece.col)
                                                    distRow + distCol
                                                } else {
                                                    1
                                                }
                                            }
                                            EasybrainArrowItem(
                                                piece = piece,
                                                isFlying = flyingPieceIds.contains(piece.id),
                                                isColliding = collidingPieceId == piece.id,
                                                blockingDistance = blockingDistance,
                                                isHinted = highlightedHintId == piece.id,
                                                onTap = { onArrowTapped(piece) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Bar: Restart, Hint & Ad-Powered Bonus
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { resetLevel() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF64748B)),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("reset_escape_level_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restart", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { useHint() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
                        .testTag("hint_escape_button")
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = WhiteCanvas, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hint", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = WhiteCanvas)
                }

                if (onWatchAdForHint != null) {
                    OutlinedButton(
                        onClick = { onWatchAdForHint() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo),
                        border = BorderStroke(1.2.dp, PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(46.dp)
                            .testTag("ad_hint_button")
                    ) {
                        Text("📺 +Hints", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- GAME OVER MODAL (Out of Lives) ---
        if (isGameOver) {
            Dialog(
                onDismissRequest = { /* Force explicit decision */ }
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = appSectionColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("game_over_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💔", fontSize = 34.sp)
                            }
                        }

                        Text(
                            text = "Out of Lives!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )

                        Text(
                            text = "An arrow crashed into a blocked path. Plan carefully before tapping!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { resetLevel() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("retry_level_button")
                        ) {
                            Text("Try Again 🔄", fontWeight = FontWeight.Bold, color = WhiteCanvas)
                        }

                        if (onWatchAdForHint != null) {
                            OutlinedButton(
                                onClick = {
                                    livesLeft = level.maxLives
                                    isGameOver = false
                                    onWatchAdForHint()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, Color(0xFF16A34A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text("📺 Watch Ad to Revive (+3 Lives)", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                        }
                    }
                }
            }
        }

        // --- VICTORY CELEBRATION MODAL ---
        if (isSolved) {
            Dialog(
                onDismissRequest = { /* Completed */ }
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = appSectionColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("escape_level_complete_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎉", fontSize = 34.sp)
                            }
                        }

                        Text(
                            text = "Board Cleared!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        // Star ratings
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val stars = when {
                                livesLeft == level.maxLives -> 3
                                livesLeft >= 2 -> 2
                                else -> 1
                            }
                            for (s in 1..3) {
                                Text(
                                    text = if (s <= stars) "⭐" else "☆",
                                    fontSize = 28.sp,
                                    color = if (s <= stars) GoldStar else TextMuted
                                )
                            }
                        }

                        // Stats Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = appBgColor,
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Moves", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("$movesCount", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("${timeElapsed}s", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Lives Left", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("$livesLeft/${level.maxLives}", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }
                        }

                        // 2X Reward claim button (ad-rewarded)
                        OutlinedButton(
                            onClick = { onClaim2xReward?.invoke() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                            border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("claim_2x_escape_reward_button")
                        ) {
                            Text("📺 Claim 2X Coins (+60🪙)", fontWeight = FontWeight.Bold)
                        }

                        // Next Level Button (Triggers Interstitial Ad automatically!)
                        Button(
                            onClick = {
                                val next = if (level.levelNumber < totalLevels) level.levelNumber + 1 else 1
                                onNextLevelClick(next)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("next_escape_level_button")
                        ) {
                            Text(
                                text = if (level.levelNumber < totalLevels) "Next Level ➡️" else "Play Again 🔄",
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }
            }
        }

        // Level Selector Modal
        if (showLevelSelector) {
            Dialog(onDismissRequest = { showLevelSelector = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = appSectionColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select Arrow Puzzle Level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        ArrowEscapeLevelProvider.levels.forEach { lvl ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (lvl.levelNumber == level.levelNumber) PrimaryIndigo.copy(alpha = 0.1f) else appBgColor
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (lvl.levelNumber == level.levelNumber) PrimaryIndigo else SlateBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showLevelSelector = false
                                        onNextLevelClick(lvl.levelNumber)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Level ${lvl.levelNumber}: ${lvl.title}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (lvl.levelNumber == level.levelNumber) PrimaryIndigo else TextPrimary
                                    )
                                    Text(
                                        text = "${lvl.rows}x${lvl.cols} • ${lvl.difficulty}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Arrow Tile on the Grid
 * Supports:
 * - Ultra-smooth Spring fly-away trajectory animation off screen
 * - Tactile slide and bump into the blocker piece, with squish animation
 * - Breathing pulse for hinted tiles
 * - Rounded modern arrow styling with directional arrow head & trail
 */
@Composable
fun EasybrainArrowItem(
    piece: EasybrainArrowPiece,
    isFlying: Boolean,
    isColliding: Boolean,
    blockingDistance: Int = 1,
    isHinted: Boolean,
    onTap: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    val shakeOffset = remember { Animatable(0f) }

    var cellWidth by remember { androidx.compose.runtime.mutableStateOf(0) }
    var cellHeight by remember { androidx.compose.runtime.mutableStateOf(0) }

    // Smooth Fly away animation when unblocked
    LaunchedEffect(isFlying) {
        if (isFlying) {
            val travelDist = 1800f
            // Slightly compress before launching for tactile spring pop
            scaleX.animateTo(1.15f, tween(80, easing = FastOutSlowInEasing))
            scaleY.animateTo(1.15f, tween(80, easing = FastOutSlowInEasing))
            when (piece.direction) {
                EasybrainArrowDir.UP -> {
                    offsetY.animateTo(-travelDist, tween(360, easing = FastOutSlowInEasing))
                }
                EasybrainArrowDir.DOWN -> {
                    offsetY.animateTo(travelDist, tween(360, easing = FastOutSlowInEasing))
                }
                EasybrainArrowDir.LEFT -> {
                    offsetX.animateTo(-travelDist, tween(360, easing = FastOutSlowInEasing))
                }
                EasybrainArrowDir.RIGHT -> {
                    offsetX.animateTo(travelDist, tween(360, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    // Slide, Bump & Squish animation when blocked
    LaunchedEffect(isColliding) {
        if (isColliding) {
            val cellDim = if (piece.direction == EasybrainArrowDir.LEFT || piece.direction == EasybrainArrowDir.RIGHT) {
                if (cellWidth > 0) cellWidth.toFloat() else 100f
            } else {
                if (cellHeight > 0) cellHeight.toFloat() else 100f
            }
            
            // Slide forward until it hits the blocker (distance in cells)
            // Slide distance: (blockingDistance - 1) full cells + 0.2f cell bump overlap
            val maxSlide = cellDim * (blockingDistance - 1 + 0.22f)
            val targetOffset = when (piece.direction) {
                EasybrainArrowDir.LEFT, EasybrainArrowDir.UP -> -maxSlide
                EasybrainArrowDir.RIGHT, EasybrainArrowDir.DOWN -> maxSlide
            }

            launch {
                if (piece.direction == EasybrainArrowDir.LEFT || piece.direction == EasybrainArrowDir.RIGHT) {
                    // Slide forward
                    offsetX.animateTo(targetOffset, tween(120, easing = FastOutSlowInEasing))
                    
                    // Squish deformation on impact!
                    launch {
                        scaleX.animateTo(0.72f, tween(60))
                        scaleX.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    launch {
                        scaleY.animateTo(1.18f, tween(60))
                        scaleY.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    
                    // Bounce back home
                    offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                } else {
                    // Slide forward
                    offsetY.animateTo(targetOffset, tween(120, easing = FastOutSlowInEasing))
                    
                    // Squish deformation on impact!
                    launch {
                        scaleY.animateTo(0.72f, tween(60))
                        scaleY.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    launch {
                        scaleX.animateTo(1.18f, tween(60))
                        scaleX.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    
                    // Bounce back home
                    offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                }
            }

            // Also trigger a minor secondary vibrational shake perpendicular to movement
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    -10f at 50
                    10f at 100
                    -6f at 170
                    6f at 240
                    0f at 350
                }
            )
        }
    }

    // Hint gentle pulse
    val hintBorderAlpha = remember { Animatable(1f) }
    LaunchedEffect(isHinted) {
        if (isHinted) {
            while (true) {
                hintBorderAlpha.animateTo(0.3f, tween(400))
                hintBorderAlpha.animateTo(1f, tween(400))
            }
        } else {
            hintBorderAlpha.snapTo(1f)
        }
    }

    val tileColor = when {
        isColliding -> Color(0xFFEF4444)
        isHinted -> Color(0xFFF59E0B)
        else -> Color(piece.colorHex)
    }

    val currentShakeX = if (piece.direction == EasybrainArrowDir.UP || piece.direction == EasybrainArrowDir.DOWN) shakeOffset.value else 0f
    val currentShakeY = if (piece.direction == EasybrainArrowDir.LEFT || piece.direction == EasybrainArrowDir.RIGHT) shakeOffset.value else 0f

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tileColor,
        border = BorderStroke(
            width = if (isHinted) 3.dp else if (isColliding) 2.5.dp else 1.5.dp,
            color = if (isColliding) Color(0xFFB91C1C)
            else if (isHinted) Color(0xFFFDE047).copy(alpha = hintBorderAlpha.value)
            else WhiteCanvas.copy(alpha = 0.45f)
        ),
        shadowElevation = if (isFlying) 0.dp else 6.dp,
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                cellWidth = size.width
                cellHeight = size.height
            }
            .offset {
                IntOffset(
                    (offsetX.value + currentShakeX).roundToInt(),
                    (offsetY.value + currentShakeY).roundToInt()
                )
            }
            .graphicsLayer {
                this.scaleX = scaleX.value
                this.scaleY = scaleY.value
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            )
            .testTag("arrow_piece_${piece.id}")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // High-visibility directional arrow glyph with smooth rotation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.rotate(piece.direction.rotationDegrees - 90f)
            ) {
                Text(
                    text = "➔",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WhiteCanvas
                )
            }
        }
    }
}
