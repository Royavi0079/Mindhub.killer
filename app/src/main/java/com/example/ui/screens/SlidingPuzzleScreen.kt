package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.AgeGroup
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.UserProgress
import com.example.ui.components.AppHeader
import com.example.ui.components.CorrectAnswerCelebrationOverlay
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
import com.example.ui.viewmodel.SlidingBoardState
import kotlin.math.sin

/**
 * 2 Interactive Visual Puzzle Modes
 */
enum class PuzzlePlayStyle(val title: String, val emoji: String, val subtitle: String) {
    JIGSAW("Jigsaw Puzzle", "🧩", "Piece tray with snap-to-board layout"),
    GRID_SLIDE("Grid Layout", "📐", "Classic sliding numbered tiles")
}

@Composable
fun SlidingPuzzleScreen(
    puzzle: PuzzleItem,
    boardState: SlidingBoardState,
    userProgress: UserProgress,
    is2xClaimed: Boolean = false,
    onTileClick: (Int) -> Unit,
    onResetClick: (Int) -> Unit,
    onUndoClick: () -> Unit = {},
    onSmartHintClick: () -> Unit = {},
    onJigsawSolved: (PuzzleItem, Int) -> Unit = { _, _ -> },
    onClaim2xReward: (() -> Unit)? = null,
    onWatchLevelBonusVideo: (() -> Unit)? = null,
    onWatchInterstitialForHints: (() -> Unit)? = null,
    onRateFeedback: (() -> Unit)? = null,
    onCompleteLevelWithRewardedAd: () -> Unit = {},
    onNextPuzzle: () -> Unit,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isKidsMode = userProgress.selectedAgeGroup == AgeGroup.JUNIOR

    // Play style selection (defaults to Jigsaw for Junior/Kids, or Grid for Standard)
    var currentPlayStyle by remember {
        mutableStateOf(if (isKidsMode) PuzzlePlayStyle.JIGSAW else PuzzlePlayStyle.GRID_SLIDE)
    }

    var showPreviewDialog by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var showNumbers by remember { mutableStateOf(userProgress.showTileNumbers) }
    var showGhostOutline by remember { mutableStateOf(true) }

    val imageRes = puzzle.imageRes ?: puzzle.difficulty.tierImageRes
    val size = boardState.size

    // --- Jigsaw State ---
    // Placed slots map: slotIndex (0 until size*size) -> pieceIndex (0 until size*size)
    val placedSlots = remember(puzzle.id, size, currentPlayStyle) {
        mutableStateMapOf<Int, Int>()
    }
    var selectedTrayPiece by remember(puzzle.id, size, currentPlayStyle) {
        mutableStateOf<Int?>(null)
    }
    var jigsawMoves by remember(puzzle.id, size, currentPlayStyle) {
        mutableIntStateOf(0)
    }
    var isJigsawSolved by remember(puzzle.id, size, currentPlayStyle) {
        mutableStateOf(false)
    }

    val totalPieces = size * size

    // Clear and reset jigsaw state on puzzle change
    LaunchedEffect(puzzle.id) {
        placedSlots.clear()
        selectedTrayPiece = null
        jigsawMoves = 0
        isJigsawSolved = false
    }

    // List of unplaced pieces for the tray
    val unplacedPieces by remember(totalPieces, puzzle.id) {
        derivedStateOf {
            val placedSet = placedSlots.values.toSet()
            (0 until totalPieces).filter { it !in placedSet }
        }
    }

    // Check completion for Jigsaw mode
    LaunchedEffect(placedSlots.size, totalPieces, puzzle.id) {
        if (placedSlots.size == totalPieces && !isJigsawSolved) {
            val allCorrect = (0 until totalPieces).all { slot -> placedSlots[slot] == slot }
            if (allCorrect) {
                isJigsawSolved = true
                onJigsawSolved(puzzle, jigsawMoves)
            }
        }
    }

    // Correctly placed count for progress
    val correctCount by remember(currentPlayStyle, boardState.tiles, placedSlots.size) {
        derivedStateOf {
            if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) {
                var count = 0
                val total = size * size
                for (i in 0 until (total - 1)) {
                    if (i < boardState.tiles.size && boardState.tiles[i] == i + 1) {
                        count++
                    }
                }
                count
            } else {
                placedSlots.count { (slot, piece) -> slot == piece }
            }
        }
    }

    val totalRequired = if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) (size * size - 1).coerceAtLeast(1) else (size * size)
    val progressFraction by remember(correctCount, totalRequired) {
        derivedStateOf { (correctCount.toFloat() / totalRequired).coerceIn(0f, 1f) }
    }

    val isCurrentGameSolved = if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) boardState.isSolved else isJigsawSolved

    LaunchedEffect(isCurrentGameSolved) {
        if (isCurrentGameSolved) {
            HapticManager.performMathSolveSuccess(context)
        }
    }

    // Pre-sliced & pre-converted ImageBitmaps cached cleanly with remember to eliminate lag
    val tileImageBitmaps = remember(imageRes, size) {
        val original = BitmapFactory.decodeResource(context.resources, imageRes)
        val map = mutableMapOf<Int, ImageBitmap>()
        if (original != null) {
            val minDim = Math.min(original.width, original.height)
            val startX = (original.width - minDim) / 2
            val startY = (original.height - minDim) / 2
            val squareBitmap = Bitmap.createBitmap(original, startX, startY, minDim, minDim)
            val tileWidth = minDim / size
            val tileHeight = minDim / size

            for (row in 0 until size) {
                for (col in 0 until size) {
                    val index = row * size + col
                    val piece = Bitmap.createBitmap(
                        squareBitmap,
                        col * tileWidth,
                        row * tileHeight,
                        tileWidth,
                        tileHeight
                    )
                    map[index] = piece.asImageBitmap()
                }
            }
        }
        map
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        AppHeader(
            title = if (isKidsMode) "Kids ${currentPlayStyle.title}" else puzzle.title,
            totalXp = userProgress.totalXp,
            starsCount = userProgress.starsEarned,
            coins = userProgress.coins,
            selectedAgeGroup = userProgress.selectedAgeGroup,
            onBackClick = onBackClick,
            onProfileClick = onProfileClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // --- 2-MODE PLAY STYLE SWITCHER (Grid Layout, Jigsaw Puzzle) ---
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                        border = BorderStroke(1.2.dp, SlateBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isKidsMode) "Kids Mode Style 🌟" else "Puzzle Game Style",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = currentPlayStyle.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PuzzlePlayStyle.entries.forEach { style ->
                                    val isSelected = currentPlayStyle == style
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) PrimaryIndigo else Color(0xFFF1F5F9),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) PrimaryIndigo else Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { currentPlayStyle = style }
                                            .testTag("style_${style.name.lowercase()}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(style.emoji, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = style.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) WhiteCanvas else TextPrimary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- STATS BAR: Moves, Time, and Grid Size Selector ---
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                        border = BorderStroke(1.dp, SlateBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Moves
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "MOVES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) "${boardState.moves}" else "$jigsawMoves",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            // Timer
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TIMER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                val mins = boardState.timeSeconds / 60
                                val secs = boardState.timeSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                            }

                            // Grid Size Buttons (2x2 for Kids, 3x3, 4x4)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val gridOptions = if (isKidsMode) {
                                    listOf(
                                        Triple(2, "2x2", "Easy"),
                                        Triple(3, "3x3", "Normal"),
                                        Triple(4, "4x4", "Hard")
                                    )
                                } else {
                                    listOf(
                                        Triple(3, "3x3", "Normal"),
                                        Triple(4, "4x4", "Hard"),
                                        Triple(5, "5x5", "Extreme")
                                    )
                                }

                                gridOptions.forEach { (gridSize, label, _) ->
                                    val isSelected = size == gridSize
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) PrimaryIndigo else Color(0xFFF1F5F9),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                placedSlots.clear()
                                                selectedTrayPiece = null
                                                jigsawMoves = 0
                                                isJigsawSolved = false
                                                onResetClick(gridSize)
                                            }
                                            .testTag("grid_size_$gridSize")
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) WhiteCanvas else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- PROGRESS BAR ---
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                        border = BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Pieces in place: $correctCount / $totalRequired",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (progressFraction == 1f) SuccessGreen else PrimaryIndigo
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = if (progressFraction == 1f) SuccessGreen else PrimaryIndigo,
                            trackColor = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                    }

                    // --- ACTION TOOLBAR (Peek, 123 Numbers, Hint, Undo/Auto-place, Reset) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showPreviewDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SlateBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("peek_button")
                        ) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = "Peek", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Peek", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }

                        if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) {
                            OutlinedButton(
                                onClick = { showNumbers = !showNumbers },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (showNumbers) PrimaryIndigo else SlateBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (showNumbers) PrimaryIndigo else TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("toggle_numbers_button")
                            ) {
                                Text("123", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showGhostOutline = !showGhostOutline },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (showGhostOutline) PrimaryIndigo else SlateBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (showGhostOutline) PrimaryIndigo else TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("toggle_ghost_button")
                            ) {
                                Icon(imageVector = Icons.Default.GridOn, contentDescription = "Ghost", modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Ghost", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onWatchInterstitialForHints?.invoke()
                                if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) {
                                    onSmartHintClick()
                                    showHintDialog = true
                                } else {
                                    // Auto-place one unplaced or misplaced piece into its correct slot
                                    val firstWrongSlot = (0 until totalPieces).firstOrNull { slot -> placedSlots[slot] != slot }
                                    if (firstWrongSlot != null) {
                                        val existingSlotForPiece = placedSlots.entries.firstOrNull { it.value == firstWrongSlot }?.key
                                        if (existingSlotForPiece != null) {
                                            placedSlots.remove(existingSlotForPiece)
                                        }
                                        placedSlots[firstWrongSlot] = firstWrongSlot
                                        if (selectedTrayPiece == firstWrongSlot) {
                                            selectedTrayPiece = null
                                        }
                                        jigsawMoves++
                                        HapticManager.performPieceSnap(context)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AiRose),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AiRose),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hint_button")
                        ) {
                            Icon(
                                imageVector = if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) Icons.Default.Lightbulb else Icons.Default.AutoAwesome,
                                contentDescription = "Hint",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) "Hint" else "Snap",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                placedSlots.clear()
                                selectedTrayPiece = null
                                jigsawMoves = 0
                                isJigsawSolved = false
                                onResetClick(size)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart",
                                tint = TextPrimary
                            )
                        }
                    }

                    // --- PUZZLE BOARD ARENA ---
                    if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) {
                        // Classic Sliding Tile Grid
                        SlidingGridBoard(
                            size = size,
                            boardState = boardState,
                            tileImageBitmaps = tileImageBitmaps,
                            showNumbers = showNumbers,
                            onTileClick = { index ->
                                HapticManager.performTileSlide(context)
                                onTileClick(index)
                            }
                        )
                    } else {
                        // Jigsaw Board Arena
                        JigsawBoard(
                            size = size,
                            imageRes = imageRes,
                            tileImageBitmaps = tileImageBitmaps,
                            placedSlots = placedSlots,
                            selectedTrayPiece = selectedTrayPiece,
                            showGhost = showGhostOutline,
                            onSlotClick = { slotIndex ->
                                if (isCurrentGameSolved) return@JigsawBoard
                                val selected = selectedTrayPiece
                                if (selected != null) {
                                    val previousInSlot = placedSlots[slotIndex]
                                    placedSlots[slotIndex] = selected
                                    jigsawMoves++
                                    if (selected == slotIndex) {
                                        // Tactile snap when connecting into correct slot
                                        HapticManager.performPieceSnap(context)
                                    } else {
                                        HapticManager.performTileSlide(context)
                                    }
                                    selectedTrayPiece = previousInSlot
                                } else if (placedSlots.containsKey(slotIndex)) {
                                    // Pick piece back up
                                    HapticManager.performTileSlide(context)
                                    val removed = placedSlots.remove(slotIndex)
                                    selectedTrayPiece = removed
                                }
                            }
                        )

                        // --- PIECE TRAY CAROUSEL (For Jigsaw) ---
                        if (unplacedPieces.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Piece Tray (${unplacedPieces.size} remaining)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Tap piece then tap board slot",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(unplacedPieces, key = { it }) { pieceIndex ->
                                            val isSelected = selectedTrayPiece == pieceIndex
                                            val imageBitmap = tileImageBitmaps[pieceIndex]

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = WhiteCanvas,
                                                border = BorderStroke(
                                                    if (isSelected) 2.5.dp else 1.dp,
                                                    if (isSelected) PrimaryIndigo else SlateBorder
                                                ),
                                                shadowElevation = if (isSelected) 6.dp else 2.dp,
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        selectedTrayPiece = if (selectedTrayPiece == pieceIndex) null else pieceIndex
                                                    }
                                                    .testTag("tray_piece_$pieceIndex")
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (imageBitmap != null) {
                                                        Image(
                                                            bitmap = imageBitmap,
                                                            contentDescription = "Piece $pieceIndex",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    if (isSelected) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(PrimaryIndigo.copy(alpha = 0.25f))
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) {
                            "Tap any adjacent tile to slide into the slot. Align all pieces in order!"
                        } else {
                            "Select a piece from the tray and place it into its matching position on the board!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // --- PREVIEW ORIGINAL ARTWORK DIALOG ---
    if (showPreviewDialog) {
        Dialog(onDismissRequest = { showPreviewDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Original Artwork Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Original artwork preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showPreviewDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Resume Puzzle", color = WhiteCanvas, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- HINT DIALOG ---
    if (showHintDialog) {
        Dialog(onDismissRequest = { showHintDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart Strategy Insight",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = puzzle.hint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = puzzle.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    // Hints counter and ad refill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = "💡 ${userProgress.freeHintsRemaining} Hints Left",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (onWatchInterstitialForHints != null) {
                            OutlinedButton(
                                onClick = {
                                    showHintDialog = false
                                    onWatchInterstitialForHints()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCA8A04)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "📺 Watch Ad (+4 Hints)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF854D0E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showHintDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got it!", color = WhiteCanvas, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- VICTORY CELEBRATION OVERLAY ---
    if (isCurrentGameSolved) {
        val movesCount = if (currentPlayStyle == PuzzlePlayStyle.GRID_SLIDE) boardState.moves else jigsawMoves
        val stars = puzzle.difficulty.stars
        val earnedXp = 5
        val earnedCoins = 10
        CorrectAnswerCelebrationOverlay(
            xpEarned = earnedXp,
            coinsEarned = earnedCoins,
            starsCount = stars,
            titleText = if (isKidsMode) "Kids Explorer Champion! 🦖🌟" else "Puzzle Mastered! 🏆",
            subtitleText = "Solved in $movesCount moves! Watch the rewarded ad until the end to claim 10 Coins & 5 Points and advance to the next level!",
            is2xClaimed = is2xClaimed,
            onClaim2xReward = onClaim2xReward,
            onWatchLevelBonusVideo = onWatchLevelBonusVideo,
            onRateFeedback = onRateFeedback,
            onDismissOrContinue = {
                placedSlots.clear()
                selectedTrayPiece = null
                jigsawMoves = 0
                isJigsawSolved = false
                onCompleteLevelWithRewardedAd()
            }
        )
    }
}

/**
 * Classic Sliding Tile Board
 */
@Composable
private fun SlidingGridBoard(
    size: Int,
    boardState: SlidingBoardState,
    tileImageBitmaps: Map<Int, ImageBitmap>,
    showNumbers: Boolean,
    onTileClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
        border = BorderStroke(2.dp, SlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("sliding_puzzle_grid")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(size),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                itemsIndexed(boardState.tiles) { index, tileValue ->
                    val isCorrectSpot = tileValue != 0 && tileValue == index + 1
                    val isSuggested = boardState.suggestedTileIndex == index

                    val tileBorderColor = when {
                        isSuggested -> AiRose
                        isCorrectSpot -> SuccessGreen
                        tileValue == 0 -> Color(0xFFE2E8F0)
                        else -> SlateBorder
                    }

                    val tileBorderWidth = when {
                        isSuggested -> 2.5.dp
                        isCorrectSpot -> 1.8.dp
                        else -> 1.dp
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (tileValue == 0) Color(0xFFF1F5F9) else WhiteCanvas)
                            .border(
                                width = tileBorderWidth,
                                color = tileBorderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = tileValue != 0 && !boardState.isSolved) {
                                onTileClick(index)
                            }
                            .testTag("tile_${tileValue}_at_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (tileValue != 0) {
                            val imageBitmap = tileImageBitmaps[tileValue - 1]
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = "Tile $tileValue",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (isCorrectSpot) {
                                Surface(
                                    shape = CircleShape,
                                    color = SuccessGreen,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(3.dp)
                                        .size(10.dp)
                                ) {}
                            }

                            if (showNumbers) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(3.dp)
                                ) {
                                    Text(
                                        text = "$tileValue",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WhiteCanvas,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (isSuggested) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = AiRose,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(3.dp)
                                ) {
                                    Text(
                                        text = "TAP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = WhiteCanvas,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "SLOT",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Jigsaw Board Arena
 */
@Composable
private fun JigsawBoard(
    size: Int,
    imageRes: Int,
    tileImageBitmaps: Map<Int, ImageBitmap>,
    placedSlots: Map<Int, Int>,
    selectedTrayPiece: Int?,
    showGhost: Boolean,
    onSlotClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
        border = BorderStroke(2.dp, SlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("jigsaw_board")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            // Ghost reference image
            if (showGhost) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Ghost reference",
                    contentScale = ContentScale.Crop,
                    alpha = 0.18f,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Grid Slots
            LazyVerticalGrid(
                columns = GridCells.Fixed(size),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(size * size) { slotIndex ->
                    val placedPieceIndex = placedSlots[slotIndex]
                    val isCorrect = placedPieceIndex == slotIndex
                    val isTargetSlot = selectedTrayPiece != null && placedPieceIndex == null

                    val slotBorderColor = when {
                        isCorrect -> SuccessGreen
                        placedPieceIndex != null -> Color(0xFFF59E0B)
                        isTargetSlot -> PrimaryIndigo
                        else -> Color(0xFFCBD5E1)
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (placedPieceIndex == null) Color.White.copy(alpha = 0.6f) else WhiteCanvas
                            )
                            .border(
                                width = if (isTargetSlot || isCorrect) 2.dp else 1.dp,
                                color = slotBorderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSlotClick(slotIndex) }
                            .testTag("slot_$slotIndex"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (placedPieceIndex != null) {
                            val imageBitmap = tileImageBitmaps[placedPieceIndex]
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = "Placed piece $placedPieceIndex",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (isCorrect) {
                                Surface(
                                    shape = CircleShape,
                                    color = SuccessGreen,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(3.dp)
                                        .size(10.dp)
                                ) {}
                            }
                        } else {
                            if (isTargetSlot) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryIndigo.copy(alpha = 0.15f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "+",
                                            color = PrimaryIndigo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "${slotIndex + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
