package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ArrowDirection
import com.example.data.model.ArrowMazeLevel
import com.example.data.model.ArrowMazeLevelProvider
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
import com.example.ui.util.HapticManager
import kotlinx.coroutines.delay

@Composable
fun ArrowMazeScreen(
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
    val totalLevels = ArrowMazeLevelProvider.levels.size
    val levelIndex = (currentLevelNumber - 1).coerceIn(0, totalLevels - 1)
    val level = ArrowMazeLevelProvider.levels[levelIndex]

    // Game state
    var currentPos by remember(level.levelNumber) { mutableStateOf(level.startPos) }
    var moveHistory by remember(level.levelNumber) { mutableStateOf(listOf(level.startPos)) }
    var isSolved by remember(level.levelNumber) { mutableStateOf(false) }
    var timeElapsed by remember(level.levelNumber) { mutableIntStateOf(0) }
    var showHint by remember(level.levelNumber) { mutableStateOf(false) }
    var showLevelSelector by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(level.levelNumber, isSolved) {
        while (!isSolved) {
            delay(1000)
            timeElapsed++
        }
    }

    // Direction at current cell
    val currentDirection = level.grid[currentPos.first][currentPos.second]
    val legalMoves = remember(currentPos, level.levelNumber) {
        ArrowMazeLevelProvider.getLegalDestinations(
            currentPos = currentPos,
            rows = level.rows,
            cols = level.cols,
            direction = currentDirection
        )
    }

    // Hint target from precomputed solution path
    val nextHintCell: Pair<Int, Int>? = remember(currentPos, showHint) {
        if (!showHint) null
        else {
            val idx = level.solutionPath.indexOf(currentPos)
            if (idx != -1 && idx + 1 < level.solutionPath.size) {
                level.solutionPath[idx + 1]
            } else null
        }
    }

    // Check solve condition
    fun handleCellClick(target: Pair<Int, Int>) {
        if (isSolved) return
        if (target in legalMoves) {
            HapticManager.performTileSlide(context)
            currentPos = target
            moveHistory = moveHistory + target
            if (target == level.goalPos) {
                isSolved = true
                HapticManager.performLevelComplete(context)
                val stars = when {
                    moveHistory.size - 1 <= level.parMoves -> 3
                    moveHistory.size - 1 <= level.parMoves + 2 -> 2
                    else -> 1
                }
                onLevelSolved(level.levelNumber, moveHistory.size - 1, stars)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("arrow_maze_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Header
            AppHeader(
                title = "Arrow Maze",
                userProgress = userProgress,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Level & Stats Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
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
                            text = "Grid: ${level.rows}x${level.cols} • Par: ${level.parMoves} moves",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SlateBackground,
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Moves: ${moveHistory.size - 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = { showLevelSelector = true },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.5f)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Lvls", style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Direction Compass Guide
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧭", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentDirection == ArrowDirection.GOAL) "🎯 Reached Goal Target!" else "Follow arrow ${currentDirection.symbol} (${currentDirection.label}) along its ray",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF166534)
                        )
                    }
                    Text(
                        text = "⏱️ ${timeElapsed}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- THE ARROW MAZE GRID ARENA ---
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                border = BorderStroke(1.5.dp, SlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .widthIn(max = 420.dp)
                    .aspectRatio(1f)
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (r in 0 until level.rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (c in 0 until level.cols) {
                                val cellPos = Pair(r, c)
                                val direction = level.grid[r][c]
                                val isCurrent = cellPos == currentPos
                                val isGoal = cellPos == level.goalPos
                                val isStart = cellPos == level.startPos
                                val isLegal = cellPos in legalMoves
                                val isHint = cellPos == nextHintCell
                                val visitedIndex = moveHistory.indexOf(cellPos)

                                val bgColor = when {
                                    isCurrent -> PrimaryIndigo
                                    isHint -> Color(0xFFFDE047)
                                    isLegal -> Color(0xFFEEF2FF)
                                    isGoal -> Color(0xFFFFF1F2)
                                    isStart -> Color(0xFFF0FDF4)
                                    visitedIndex != -1 -> Color(0xFFF8FAFC)
                                    else -> Color(0xFFFAFAFA)
                                }

                                val borderColor = when {
                                    isCurrent -> Color(0xFF4338CA)
                                    isHint -> Color(0xFFEAB308)
                                    isLegal -> PrimaryIndigo.copy(alpha = 0.8f)
                                    isGoal -> AiRose
                                    isStart -> SuccessGreen
                                    else -> Color(0xFFE2E8F0)
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = bgColor),
                                    border = BorderStroke(
                                        width = if (isCurrent || isHint || isLegal) 2.dp else 1.dp,
                                        color = borderColor
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = isLegal) {
                                            handleCellClick(cellPos)
                                        }
                                        .testTag("cell_${r}_${c}")
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Step badge if visited
                                        if (visitedIndex != -1 && !isCurrent) {
                                            Text(
                                                text = "$visitedIndex",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = Color(0xFF94A3B8),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(3.dp)
                                            )
                                        }

                                        // Goal or Direction arrow
                                        if (isGoal) {
                                            Text(
                                                text = if (isCurrent) "🏆" else "🎯",
                                                fontSize = if (level.rows <= 4) 28.sp else 22.sp
                                            )
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = direction.symbol,
                                                    fontSize = if (level.rows <= 4) 26.sp else 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isCurrent) WhiteCanvas else if (isLegal) PrimaryIndigo else TextPrimary
                                                )
                                                if (isStart && visitedIndex <= 0) {
                                                    Text(
                                                        text = "START",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isCurrent) WhiteCanvas.copy(alpha = 0.8f) else Color(0xFF16A34A)
                                                    )
                                                }
                                            }
                                        }

                                        // Indicator if current position
                                        if (isCurrent && !isGoal) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFCD34D),
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 2.dp)
                                            ) {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Control Buttons: Undo, Reset, Hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (moveHistory.size > 1 && !isSolved) {
                            moveHistory = moveHistory.dropLast(1)
                            currentPos = moveHistory.last()
                            HapticManager.performTileSlide(context)
                        }
                    },
                    enabled = moveHistory.size > 1 && !isSolved,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF64748B)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("undo_move_button")
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        currentPos = level.startPos
                        moveHistory = listOf(level.startPos)
                        isSolved = false
                        showHint = false
                        timeElapsed = 0
                        HapticManager.performTileSlide(context)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryIndigo),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("reset_maze_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp), tint = PrimaryIndigo)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                }

                Button(
                    onClick = {
                        showHint = true
                        onWatchAdForHint?.invoke()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AiRose),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("hint_maze_button")
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Hint", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hint 📺", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = WhiteCanvas)
                }
            }
        }

        // Level Win Celebration Modal
        AnimatedVisibility(
            visible = isSolved,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Dialog(onDismissRequest = { /* Require user to click next */ }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(2.dp, GoldStar),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("maze_solved_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF08A),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🎯", fontSize = 34.sp)
                            }
                        }

                        Text(
                            text = "Maze Conquered!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "Level ${level.levelNumber}: ${level.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        // Stars row
                        val stars = when {
                            moveHistory.size - 1 <= level.parMoves -> 3
                            moveHistory.size - 1 <= level.parMoves + 2 -> 2
                            else -> 1
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(3) { i ->
                                Text(
                                    text = if (i < stars) "⭐" else "☆",
                                    fontSize = 24.sp,
                                    color = GoldStar
                                )
                            }
                        }

                        // Stats summary
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SlateBackground,
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
                                    Text("${moveHistory.size - 1} (Par ${level.parMoves})", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("${timeElapsed}s", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Reward", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("+50 XP • +30🪙", fontWeight = FontWeight.Bold, color = PrimaryIndigo)
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
                                .testTag("claim_2x_maze_reward_button")
                        ) {
                            Text("📺 Claim 2X Reward (+60 Coins)", fontWeight = FontWeight.Bold)
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
                                .testTag("next_maze_level_button")
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
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
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
                            text = "Select Arrow Maze Level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        ArrowMazeLevelProvider.levels.forEach { lvl ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (lvl.levelNumber == level.levelNumber) PrimaryIndigo.copy(alpha = 0.1f) else SlateBackground
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
