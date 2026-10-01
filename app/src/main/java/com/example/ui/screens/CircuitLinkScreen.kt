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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CircuitColor
import com.example.data.model.CircuitLinkLevel
import com.example.data.model.CircuitLinkLevelProvider
import com.example.data.model.CircuitTerminal
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
import kotlin.math.abs

@Composable
fun CircuitLinkScreen(
    currentLevelNumber: Int = 1,
    userProgress: UserProgress,
    onLevelSolved: (level: Int, moves: Int) -> Unit = { _, _ -> },
    onNextLevelClick: (nextLevel: Int) -> Unit = {},
    onWatchAdForHint: (() -> Unit)? = null,
    onClaim2xReward: (() -> Unit)? = null,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalLevels = CircuitLinkLevelProvider.levels.size
    val levelIndex = (currentLevelNumber - 1).coerceIn(0, totalLevels - 1)
    val level = CircuitLinkLevelProvider.levels[levelIndex]

    // State: paths for each color
    var paths by remember(level.levelNumber) {
        mutableStateOf<Map<CircuitColor, List<Pair<Int, Int>>>>(emptyMap())
    }
    var activeColor by remember(level.levelNumber) { mutableStateOf<CircuitColor?>(null) }
    var isSolved by remember(level.levelNumber) { mutableStateOf(false) }
    var timeElapsed by remember(level.levelNumber) { mutableIntStateOf(0) }
    var movesCount by remember(level.levelNumber) { mutableIntStateOf(0) }
    var showLevelSelector by remember { mutableStateOf(false) }

    // Map of cell to terminal if any
    val terminalMap = remember(level.levelNumber) {
        val map = mutableMapOf<Pair<Int, Int>, CircuitTerminal>()
        level.terminals.forEach { term ->
            map[term.pos1] = term
            map[term.pos2] = term
        }
        map
    }

    // Timer
    LaunchedEffect(level.levelNumber, isSolved) {
        while (!isSolved) {
            delay(1000)
            timeElapsed++
        }
    }

    // Check if color is fully connected between pos1 and pos2
    fun isColorConnected(c: CircuitColor): Boolean {
        val p = paths[c] ?: return false
        val term = level.terminals.firstOrNull { it.color == c } ?: return false
        if (p.size < 2) return false
        return (p.first() == term.pos1 && p.last() == term.pos2) ||
                (p.first() == term.pos2 && p.last() == term.pos1)
    }

    val completedCount = level.terminals.count { isColorConnected(it.color) }
    val totalCoveredCells = paths.values.flatten().toSet().size
    val totalCells = level.size * level.size
    val coveragePercent = ((totalCoveredCells.toFloat() / totalCells.toFloat()) * 100).toInt()

    fun checkVictory() {
        val allConnected = level.terminals.all { isColorConnected(it.color) }
        if (allConnected && !isSolved) {
            isSolved = true
            HapticManager.performLevelComplete(context)
            onLevelSolved(level.levelNumber, movesCount)
        }
    }

    fun handleCellTap(row: Int, col: Int) {
        if (isSolved) return
        val pos = Pair(row, col)
        val term = terminalMap[pos]

        // 1. If tapping a terminal
        if (term != null) {
            HapticManager.performTileSlide(context)
            activeColor = term.color
            val existing = paths[term.color]
            if (existing == null || existing.isEmpty()) {
                paths = paths + (term.color to listOf(pos))
            } else if (!isColorConnected(term.color)) {
                // Restart path from this terminal
                paths = paths + (term.color to listOf(pos))
            }
            return
        }

        // 2. If no active color is selected, check if user tapped an existing wire to select it
        val currentActive = activeColor
        if (currentActive == null) {
            val colorAtCell = paths.entries.firstOrNull { it.value.contains(pos) }?.key
            if (colorAtCell != null) {
                activeColor = colorAtCell
                HapticManager.performTileSlide(context)
            }
            return
        }

        // 3. Extending or modifying active wire
        val curPath = paths[currentActive] ?: emptyList()
        if (curPath.isEmpty()) {
            val termForColor = level.terminals.firstOrNull { it.color == currentActive } ?: return
            paths = paths + (currentActive to listOf(termForColor.pos1))
            return
        }

        val lastPos = curPath.last()
        val isAdjacent = (abs(lastPos.first - row) + abs(lastPos.second - col)) == 1

        if (isAdjacent) {
            // Check if cell has another color's path; clear that cell from other path
            val otherColor = paths.entries.firstOrNull { it.key != currentActive && it.value.contains(pos) }?.key
            val newPaths = paths.toMutableMap()
            if (otherColor != null) {
                val otherPath = newPaths[otherColor]?.toMutableList() ?: mutableListOf()
                otherPath.remove(pos)
                newPaths[otherColor] = otherPath
            }

            // Check if pos already in current path -> truncate to pos
            val existingIdx = curPath.indexOf(pos)
            if (existingIdx != -1) {
                newPaths[currentActive] = curPath.subList(0, existingIdx + 1)
            } else {
                newPaths[currentActive] = curPath + pos
            }

            movesCount++
            paths = newPaths
            HapticManager.performTileSlide(context)
            checkVictory()
        }
    }

    fun applyHint() {
        if (level.sampleSolution.isNotEmpty()) {
            // Pick first unconnected color
            val unconnected = level.terminals.firstOrNull { !isColorConnected(it.color) } ?: return
            val solvedWire = level.sampleSolution[unconnected.color]
            if (solvedWire != null) {
                HapticManager.performTileSlide(context)
                val newPaths = paths.toMutableMap()
                // clear collisions
                newPaths.forEach { (c, p) ->
                    if (c != unconnected.color) {
                        newPaths[c] = p.filterNot { solvedWire.contains(it) }
                    }
                }
                newPaths[unconnected.color] = solvedWire
                paths = newPaths
                checkVictory()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("circuit_link_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Header
            AppHeader(
                title = "Circuit Link",
                userProgress = userProgress,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                color = PrimaryIndigo.copy(alpha = 0.1f)
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
                            text = "Grid ${level.size}x${level.size} • Pipes: $completedCount/${level.terminals.size} • Filled: $coveragePercent%",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
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

            // Energy Terminal Color Palette Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                level.terminals.forEach { term ->
                    val isConnected = isColorConnected(term.color)
                    val isCurrent = activeColor == term.color
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCurrent) term.color.color.copy(alpha = 0.2f) else SlateBackground,
                        border = BorderStroke(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) term.color.color else if (isConnected) SuccessGreen else SlateBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clickable {
                                activeColor = term.color
                                HapticManager.performTileSlide(context)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = term.color.color,
                                    modifier = Modifier.size(10.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(4.dp))
                                if (isConnected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = term.color.name.take(3),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- THE CIRCUIT LINK BOARD ---
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(2.dp, SlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .widthIn(max = 380.dp)
                    .aspectRatio(1f)
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (r in 0 until level.size) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until level.size) {
                                val pos = Pair(r, c)
                                val terminal = terminalMap[pos]
                                val wireColor = paths.entries.firstOrNull { it.value.contains(pos) }?.key

                                val cellBgColor = when {
                                    terminal != null -> terminal.color.color.copy(alpha = 0.25f)
                                    wireColor != null -> wireColor.color.copy(alpha = 0.35f)
                                    else -> Color(0xFF1E293B)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = cellBgColor,
                                    border = BorderStroke(
                                        width = if (terminal != null || wireColor != null) 1.5.dp else 0.5.dp,
                                        color = terminal?.color?.color ?: wireColor?.color ?: Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { handleCellTap(r, c) }
                                        .testTag("circuit_cell_${r}_${c}")
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (terminal != null) {
                                            // Glowing Terminal Node
                                            Surface(
                                                shape = CircleShape,
                                                color = terminal.color.color,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (isColorConnected(terminal.color)) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = "Connected",
                                                            tint = WhiteCanvas,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        } else if (wireColor != null) {
                                            // Wire conduit dot
                                            Surface(
                                                shape = CircleShape,
                                                color = wireColor.color,
                                                modifier = Modifier.size(12.dp)
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

            // Action Buttons (Clear Active, Reset, Hint)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        activeColor?.let { c ->
                            val newPaths = paths.toMutableMap()
                            newPaths.remove(c)
                            paths = newPaths
                            HapticManager.performTileSlide(context)
                        }
                    },
                    enabled = activeColor != null && paths.containsKey(activeColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF64748B)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Wire", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Wire", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        paths = emptyMap()
                        activeColor = null
                        isSolved = false
                        movesCount = 0
                        timeElapsed = 0
                        HapticManager.performTileSlide(context)
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryIndigo),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp), tint = PrimaryIndigo)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                }

                Button(
                    onClick = {
                        onWatchAdForHint?.invoke()
                        applyHint()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AiRose),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Hint", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hint 📺", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = WhiteCanvas)
                }
            }
        }

        // Victory Dialog
        AnimatedVisibility(
            visible = isSolved,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(2.dp, GoldStar),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("circuit_solved_dialog")
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
                                Text("⚡", fontSize = 34.sp)
                            }
                        }

                        Text(
                            text = "Circuits Energized!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "Level ${level.levelNumber}: ${level.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

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
                                    Text("Pipes", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("${level.terminals.size}/${level.terminals.size}", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("${timeElapsed}s", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Bounty", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("+60 XP • +35🪙", fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { onClaim2xReward?.invoke() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                            border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("claim_2x_circuit_reward_button")
                        ) {
                            Text("📺 Claim 2X Reward (+70 Coins)", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isSolved = false
                                val next = if (level.levelNumber < totalLevels) level.levelNumber + 1 else 1
                                onNextLevelClick(next)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("next_circuit_level_button")
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Select Circuit Link Level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        CircuitLinkLevelProvider.levels.forEach { lvl ->
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
                                        text = "${lvl.size}x${lvl.size} • ${lvl.difficulty}",
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
