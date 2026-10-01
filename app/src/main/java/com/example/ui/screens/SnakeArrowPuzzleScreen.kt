package com.example.ui.screens

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GridPoint
import com.example.data.model.SnakeArrowLevel
import com.example.data.model.SnakeArrowLevelProvider
import com.example.data.model.SnakeArrowPiece
import com.example.data.model.SnakeSegmentDir
import com.example.data.model.UserProgress
import com.example.ui.components.AppHeader
import com.example.ui.components.LowFadeGlassSurface
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.WhiteCanvas
import com.example.ui.util.HapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * Bendy & Snake-Arrow Labyrinth Puzzle Game Screen
 * Faithfully matches user references:
 * 1. Monochromatic / dark vector maze of bendy lines with arrowheads
 * 2. Pictorial silhouettes (e.g. Mushroom, castle silhouette)
 * 3. Tapping unblocked arrow makes it smoothly slide out of the screen along its trajectory
 * 4. Tapping a blocked arrow triggers tactile bump shake and deducts 1 of 3 hearts
 * 5. Low fade liquid glass cards and layout with smooth animations
 */
@Composable
fun SnakeArrowPuzzleScreen(
    currentLevelNumber: Int = 2, // Defaults to Mushroom level matching user reference!
    userProgress: UserProgress,
    onLevelSolved: (level: Int, moves: Int, stars: Int) -> Unit = { _, _, _ -> },
    onNextLevelClick: (nextLevel: Int) -> Unit = {},
    onWatchAdForHint: (() -> Unit)? = null,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val totalLevels = SnakeArrowLevelProvider.levels.size
    val levelIndex = (currentLevelNumber - 1).coerceIn(0, totalLevels - 1)
    val level = SnakeArrowLevelProvider.levels[levelIndex]

    val themeColors = remember(userProgress.userProfileBackground) {
        com.example.ui.theme.AppThemeUtil.getAppColors(userProgress.userProfileBackground)
    }
    val appBgColor = themeColors.first
    val appSectionColor = themeColors.second

    val activePieces = remember(level.levelNumber) {
        mutableStateListOf<SnakeArrowPiece>().apply { addAll(level.pieces) }
    }
    val flyingPieceOffsets = remember(level.levelNumber) {
        mutableMapOf<Int, Animatable<Float, *>>()
    }
    val collidingPieceShakes = remember(level.levelNumber) {
        mutableMapOf<Int, Animatable<Float, *>>()
    }

    var livesLeft by remember(level.levelNumber) { mutableIntStateOf(level.maxLives) }
    var movesCount by remember(level.levelNumber) { mutableIntStateOf(0) }
    var timeElapsed by remember(level.levelNumber) { mutableIntStateOf(0) }
    var isSolved by remember(level.levelNumber) { mutableStateOf(false) }
    var isGameOver by remember(level.levelNumber) { mutableStateOf(false) }
    var hintedPieceId by remember(level.levelNumber) { mutableStateOf<Int?>(null) }
    var showLevelSelector by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(level.levelNumber, isSolved, isGameOver) {
        while (!isSolved && !isGameOver) {
            delay(1000)
            timeElapsed++
        }
    }

    fun resetGame() {
        activePieces.clear()
        activePieces.addAll(level.pieces)
        livesLeft = level.maxLives
        movesCount = 0
        timeElapsed = 0
        isSolved = false
        isGameOver = false
        hintedPieceId = null
    }

    fun onPieceTapped(piece: SnakeArrowPiece) {
        if (isSolved || isGameOver) return
        movesCount++
        hintedPieceId = null

        val isClear = SnakeArrowLevelProvider.isPieceUnblocked(piece, activePieces)
        if (isClear) {
            // Smooth escape animation
            HapticManager.performTileSlide(context)
            val anim = Animatable(0f)
            flyingPieceOffsets[piece.id] = anim

            coroutineScope.launch {
                anim.animateTo(
                    targetValue = 1800f,
                    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                )
                activePieces.removeAll { it.id == piece.id }
                flyingPieceOffsets.remove(piece.id)

                if (activePieces.isEmpty()) {
                    isSolved = true
                    HapticManager.performLevelComplete(context)
                    val stars = if (livesLeft == level.maxLives) 3 else if (livesLeft >= 2) 2 else 1
                    onLevelSolved(level.levelNumber, movesCount, stars)
                }
            }
        } else {
            // Blocked collision shake
            HapticManager.performWrongAnswer(context)
            livesLeft = (livesLeft - 1).coerceAtLeast(0)
            val shake = Animatable(0f)
            collidingPieceShakes[piece.id] = shake

            coroutineScope.launch {
                shake.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 380
                        -14f at 50
                        14f at 100
                        -10f at 160
                        10f at 220
                        -5f at 280
                        5f at 330
                        0f at 380
                    }
                )
                collidingPieceShakes.remove(piece.id)
                if (livesLeft <= 0) {
                    isGameOver = true
                }
            }
        }
    }

    fun triggerHint() {
        val unblocked = SnakeArrowLevelProvider.findUnblockedPiece(activePieces)
        if (unblocked != null) {
            hintedPieceId = unblocked.id
            HapticManager.performTileSlide(context)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appBgColor)
            .testTag("snake_arrow_puzzle_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
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

            // Top Status Bar: Level Name, Hearts & Lives
            LowFadeGlassSurface(
                elevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Moves & Arrows Left
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🏹 ${activePieces.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // 3 Red Hearts (Matching Screenshot!)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..level.maxLives) {
                            val active = i <= livesLeft
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Life",
                                tint = if (active) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Level Difficulty Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEEF2FF),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE)),
                        modifier = Modifier.clickable { showLevelSelector = true }
                    ) {
                        Text(
                            text = level.difficulty,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // --- THE CLEAN WHITE VECTOR MAZE CANVAS (Mushroom & Pictorial Arrow Silhouette) ---
            LowFadeGlassSurface(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .widthIn(max = 440.dp)
                    .aspectRatio(0.82f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(activePieces.toList()) {
                                detectTapGestures { tapOffset ->
                                    val canvasW = size.width
                                    val canvasH = size.height
                                    val normX = (tapOffset.x / canvasW) * 100f
                                    val normY = (tapOffset.y / canvasH) * 100f

                                    // Find clicked piece nearest to tap
                                    var closestPiece: SnakeArrowPiece? = null
                                    var closestDist = Float.MAX_VALUE

                                    for (piece in activePieces) {
                                        for (i in 0 until piece.points.size - 1) {
                                            val p1 = piece.points[i]
                                            val p2 = piece.points[i + 1]
                                            val d = distanceToSegment(normX, normY, p1.x, p1.y, p2.x, p2.y)
                                            if (d < closestDist && d < 6.5f) {
                                                closestDist = d
                                                closestPiece = piece
                                            }
                                        }
                                    }

                                    closestPiece?.let { onPieceTapped(it) }
                                }
                            }
                    ) {
                        val canvasW = size.width
                        val canvasH = size.height

                        activePieces.forEach { piece ->
                            val flyAnim = flyingPieceOffsets[piece.id]?.value ?: 0f
                            val shakeAnim = collidingPieceShakes[piece.id]?.value ?: 0f
                            val isHinted = hintedPieceId == piece.id
                            val isColliding = collidingPieceShakes.containsKey(piece.id)

                            drawSnakePiece(
                                piece = piece,
                                canvasW = canvasW,
                                canvasH = canvasH,
                                flyOffset = flyAnim,
                                shakeOffset = shakeAnim,
                                isHinted = isHinted,
                                isColliding = isColliding
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Floating Controls (Matching Circular Icons in User Reference screenshot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restart / Grid selector
                Surface(
                    shape = CircleShape,
                    color = WhiteCanvas,
                    shadowElevation = 5.dp,
                    border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { resetGame() }
                        .testTag("btn_snake_restart")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Hint Button with Ad tag (Matching Lightbulb with Blue 'Ad' tag!)
                Box(contentAlignment = Alignment.TopEnd) {
                    Surface(
                        shape = CircleShape,
                        color = WhiteCanvas,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.5.dp, Color(0xFF93C5FD)),
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (onWatchAdForHint != null) {
                                    onWatchAdForHint()
                                    triggerHint()
                                } else {
                                    triggerHint()
                                }
                            }
                            .testTag("btn_snake_hint")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Circular 'Ad' badge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Ad",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }

                // Grid / Levels button (# Icon from screenshot)
                Surface(
                    shape = CircleShape,
                    color = WhiteCanvas,
                    shadowElevation = 5.dp,
                    border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { showLevelSelector = true }
                        .testTag("btn_snake_levels")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Levels",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // Level Won Modal
        if (isSolved) {
            Dialog(onDismissRequest = {}) {
                LowFadeGlassSurface(
                    shape = RoundedCornerShape(26.dp),
                    elevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("🎉 Labyrinth Cleared!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Cleared in $movesCount moves (${timeElapsed}s) with $livesLeft hearts left!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val nextLvl = if (level.levelNumber < totalLevels) level.levelNumber + 1 else 1
                            Button(
                                onClick = { onNextLevelClick(nextLvl) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Next Silhouette Level ➔", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Game Over Modal
        if (isGameOver) {
            Dialog(onDismissRequest = {}) {
                LowFadeGlassSurface(
                    shape = RoundedCornerShape(26.dp),
                    elevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("💥 No Hearts Left!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        Text(
                            text = "Arrows collided with obstacles. Tap retry to attempt the maze again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Try Again", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Level Selector Dialog
        if (showLevelSelector) {
            Dialog(onDismissRequest = { showLevelSelector = false }) {
                LowFadeGlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    elevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Select Arrow Silhouette Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        SnakeArrowLevelProvider.levels.forEach { lvl ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (lvl.levelNumber == level.levelNumber) Color(0xFFEEF2FF) else WhiteCanvas
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (lvl.levelNumber == level.levelNumber) PrimaryIndigo else Color(0xFFE2E8F0)
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
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Level ${lvl.levelNumber}: ${lvl.title}", fontWeight = FontWeight.Bold)
                                    Text("${lvl.pieces.size} arrows • ${lvl.difficulty}", color = Color(0xFF64748B), fontSize = 12.sp)
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
 * Draws an individual bendy snake-arrow path with an arrow tip at its terminus.
 */
private fun DrawScope.drawSnakePiece(
    piece: SnakeArrowPiece,
    canvasW: Float,
    canvasH: Float,
    flyOffset: Float,
    shakeOffset: Float,
    isHinted: Boolean,
    isColliding: Boolean
) {
    val strokeColor = when {
        isColliding -> Color(0xFFEF4444)
        isHinted -> Color(0xFFF59E0B)
        else -> Color(piece.colorHex)
    }

    val strokeWidth = if (isHinted || isColliding) 6.5.dp.toPx() else 4.5.dp.toPx()

    // Calculate translation from flight or collision shake
    var dx = 0f
    var dy = 0f
    when (piece.escapeDirection) {
        SnakeSegmentDir.RIGHT -> {
            dx = flyOffset
            dy = shakeOffset
        }
        SnakeSegmentDir.LEFT -> {
            dx = -flyOffset
            dy = shakeOffset
        }
        SnakeSegmentDir.DOWN -> {
            dy = flyOffset
            dx = shakeOffset
        }
        SnakeSegmentDir.UP -> {
            dy = -flyOffset
            dx = shakeOffset
        }
    }

    val path = Path()
    piece.points.forEachIndexed { index, pt ->
        val px = (pt.x / 100f) * canvasW + dx
        val py = (pt.y / 100f) * canvasH + dy
        if (index == 0) path.moveTo(px, py)
        else path.lineTo(px, py)
    }

    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Draw arrow tip at the terminus (head) pointing along direction
    val head = piece.head
    val hx = (head.x / 100f) * canvasW + dx
    val hy = (head.y / 100f) * canvasH + dy

    val arrowSize = 12.dp.toPx()
    val tipPath = Path()

    when (piece.escapeDirection) {
        SnakeSegmentDir.RIGHT -> {
            tipPath.moveTo(hx + 2f, hy)
            tipPath.lineTo(hx - arrowSize, hy - arrowSize * 0.7f)
            tipPath.lineTo(hx - arrowSize * 0.6f, hy)
            tipPath.lineTo(hx - arrowSize, hy + arrowSize * 0.7f)
            tipPath.close()
        }
        SnakeSegmentDir.LEFT -> {
            tipPath.moveTo(hx - 2f, hy)
            tipPath.lineTo(hx + arrowSize, hy - arrowSize * 0.7f)
            tipPath.lineTo(hx + arrowSize * 0.6f, hy)
            tipPath.lineTo(hx + arrowSize, hy + arrowSize * 0.7f)
            tipPath.close()
        }
        SnakeSegmentDir.DOWN -> {
            tipPath.moveTo(hx, hy + 2f)
            tipPath.lineTo(hx - arrowSize * 0.7f, hy - arrowSize)
            tipPath.lineTo(hx, hy - arrowSize * 0.6f)
            tipPath.lineTo(hx + arrowSize * 0.7f, hy - arrowSize)
            tipPath.close()
        }
        SnakeSegmentDir.UP -> {
            tipPath.moveTo(hx, hy - 2f)
            tipPath.lineTo(hx - arrowSize * 0.7f, hy + arrowSize)
            tipPath.lineTo(hx, hy + arrowSize * 0.6f)
            tipPath.lineTo(hx + arrowSize * 0.7f, hy + arrowSize)
            tipPath.close()
        }
    }

    drawPath(path = tipPath, color = strokeColor)
}

/**
 * Geometric distance from point (px, py) to line segment (x1, y1)-(x2, y2).
 */
private fun distanceToSegment(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
    if (l2 == 0f) return hypot(px - x1, py - y1)

    val t = (((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2).coerceIn(0f, 1f)
    val projX = x1 + t * (x2 - x1)
    val projY = y1 + t * (y2 - y1)
    return hypot(px - projX, py - projY)
}
