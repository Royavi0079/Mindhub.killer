package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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

data class JigsawTheme(
    val id: String,
    val title: String,
    val drawableRes: Int
)

@Composable
fun JigsawPuzzleStudioScreen(
    userProgress: UserProgress,
    onPuzzleSolved: (moves: Int, timeSeconds: Int) -> Unit = { _, _ -> },
    onNextPuzzleClick: () -> Unit = {},
    onWatchAdForHint: (() -> Unit)? = null,
    onClaim2xReward: (() -> Unit)? = null,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val themes = remember {
        listOf(
            JigsawTheme("cosmos", "Cosmic Cosmos", R.drawable.img_math_cosmos),
            JigsawTheme("ocean", "Deep Ocean Abyss", R.drawable.img_deep_ocean),
            JigsawTheme("pyramids", "Ancient Pyramids", R.drawable.img_ancient_pyramids),
            JigsawTheme("cyber", "Cyber Cityscape", R.drawable.img_cyber_cityscape),
            JigsawTheme("dino", "Dino Safari Jungle", R.drawable.img_kids_dino_safari),
            JigsawTheme("science", "Quantum Science Lab", R.drawable.img_science_lab)
        )
    }

    var selectedThemeIndex by remember { mutableIntStateOf(0) }
    var gridSize by remember { mutableIntStateOf(3) } // 2, 3, or 4
    val totalPieces = gridSize * gridSize
    val activeTheme = themes[selectedThemeIndex]

    // Board state: Slot index -> Piece ID (0 until totalPieces)
    var boardSlots by remember(selectedThemeIndex, gridSize) {
        mutableStateOf(List<Int?>(totalPieces) { null })
    }
    var unplacedPieces by remember(selectedThemeIndex, gridSize) {
        mutableStateOf((0 until totalPieces).shuffled())
    }
    var selectedPieceId by remember { mutableStateOf<Int?>(null) }
    var showGhostGuide by remember { mutableStateOf(false) }
    var isSolved by remember { mutableStateOf(false) }
    var movesCount by remember(selectedThemeIndex, gridSize) { mutableIntStateOf(0) }
    var timeElapsed by remember(selectedThemeIndex, gridSize) { mutableIntStateOf(0) }
    var showThemePicker by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(selectedThemeIndex, gridSize, isSolved) {
        while (!isSolved) {
            delay(1000)
            timeElapsed++
        }
    }

    // Check completion condition
    fun checkCompletion(currentSlots: List<Int?>) {
        val completed = currentSlots.indices.all { i -> currentSlots[i] == i }
        if (completed && currentSlots.all { it != null }) {
            isSolved = true
            HapticManager.performLevelComplete(context)
            onPuzzleSolved(movesCount, timeElapsed)
        }
    }

    fun placePieceInSlot(slotIndex: Int) {
        val pieceToPlace = selectedPieceId ?: return
        HapticManager.performTileSlide(context)
        movesCount++

        val newSlots = boardSlots.toMutableList()
        val oldPieceInSlot = newSlots[slotIndex]

        newSlots[slotIndex] = pieceToPlace
        val newUnplaced = unplacedPieces.toMutableList()
        newUnplaced.remove(pieceToPlace)
        if (oldPieceInSlot != null) {
            newUnplaced.add(oldPieceInSlot)
        }

        boardSlots = newSlots
        unplacedPieces = newUnplaced
        selectedPieceId = null

        checkCompletion(newSlots)
    }

    fun autoSnapHint() {
        // Find first misplaced or unplaced piece
        val correctSlot = (0 until totalPieces).firstOrNull { slot -> boardSlots[slot] != slot } ?: return
        val targetPiece = correctSlot

        HapticManager.performTileSlide(context)
        val newSlots = boardSlots.toMutableList()

        // If piece was placed somewhere else on board, vacate that slot
        val currentSlotOfTarget = newSlots.indexOf(targetPiece)
        if (currentSlotOfTarget != -1) {
            newSlots[currentSlotOfTarget] = null
        }

        // If something was in target slot, move it back to unplaced
        val displaced = newSlots[correctSlot]
        newSlots[correctSlot] = targetPiece

        val newUnplaced = unplacedPieces.toMutableList()
        newUnplaced.remove(targetPiece)
        if (displaced != null && displaced != targetPiece) {
            newUnplaced.add(displaced)
        }

        boardSlots = newSlots
        unplacedPieces = newUnplaced
        selectedPieceId = null
        checkCompletion(newSlots)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("jigsaw_puzzle_studio_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            AppHeader(
                title = "Jigsaw Studio",
                userProgress = userProgress,
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Info Bar with Artwork & Size Selector
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
                    Column(modifier = Modifier.clickable { showThemePicker = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeTheme.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🎨", fontSize = 14.sp)
                        }
                        Text(
                            text = "${gridSize}x${gridSize} • ${totalPieces} Pieces • Moves: $movesCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    // Grid size switcher chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(2, 3, 4).forEach { size ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (gridSize == size) PrimaryIndigo else SlateBackground,
                                border = BorderStroke(1.dp, if (gridSize == size) PrimaryIndigo else SlateBorder),
                                modifier = Modifier.clickable {
                                    if (gridSize != size) {
                                        gridSize = size
                                        isSolved = false
                                        selectedPieceId = null
                                    }
                                }
                            ) {
                                Text(
                                    text = "${size}x${size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (gridSize == size) WhiteCanvas else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Controls row (Ghost overlay guide, Reset, Hint)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showGhostGuide = !showGhostGuide },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (showGhostGuide) PrimaryIndigo else Color(0xFF94A3B8)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = if (showGhostGuide) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Ghost Guide",
                        modifier = Modifier.size(16.dp),
                        tint = if (showGhostGuide) PrimaryIndigo else TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showGhostGuide) "Guide On" else "Ghost Guide",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (showGhostGuide) PrimaryIndigo else TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            boardSlots = List(totalPieces) { null }
                            unplacedPieces = (0 until totalPieces).shuffled()
                            selectedPieceId = null
                            isSolved = false
                            movesCount = 0
                            timeElapsed = 0
                            HapticManager.performTileSlide(context)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onWatchAdForHint?.invoke()
                            autoSnapHint()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AiRose),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Snap Hint", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto Snap 📺", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = WhiteCanvas)
                    }
                }
            }

            // --- THE JIGSAW PUZZLE BOARD ---
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(2.dp, SlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .widthIn(max = 380.dp)
                    .aspectRatio(1f)
                    .padding(vertical = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Full Ghost Background image guide if enabled
                    if (showGhostGuide) {
                        Image(
                            painter = painterResource(id = activeTheme.drawableRes),
                            contentDescription = "Ghost Guide Image",
                            contentScale = ContentScale.Crop,
                            alpha = 0.28f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Grid Cells
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (r in 0 until gridSize) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (c in 0 until gridSize) {
                                    val slotIndex = r * gridSize + c
                                    val pieceInSlot = boardSlots[slotIndex]
                                    val isCorrect = pieceInSlot == slotIndex

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (pieceInSlot != null) Color(0xFF334155) else Color(0x33FFFFFF),
                                        border = BorderStroke(
                                            width = if (isCorrect) 1.5.dp else 1.dp,
                                            color = if (isCorrect) SuccessGreen else Color(0x55FFFFFF)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (selectedPieceId != null) {
                                                    placePieceInSlot(slotIndex)
                                                } else if (pieceInSlot != null) {
                                                    // Return piece to tray
                                                    val newSlots = boardSlots.toMutableList()
                                                    newSlots[slotIndex] = null
                                                    boardSlots = newSlots
                                                    unplacedPieces = unplacedPieces + pieceInSlot
                                                    HapticManager.performTileSlide(context)
                                                }
                                            }
                                            .testTag("board_slot_$slotIndex")
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (pieceInSlot != null) {
                                                // Piece is placed: Show cropped piece slice representation
                                                Image(
                                                    painter = painterResource(id = activeTheme.drawableRes),
                                                    contentDescription = "Placed piece $pieceInSlot",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )

                                                // Small badge with piece and match indicator
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isCorrect) SuccessGreen else Color(0xCC000000),
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .align(Alignment.TopEnd)
                                                        .padding(2.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        if (isCorrect) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Snapped",
                                                                tint = WhiteCanvas,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        } else {
                                                            Text(
                                                                text = "${pieceInSlot + 1}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontSize = 9.sp,
                                                                color = WhiteCanvas,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                // Empty slot placeholder
                                                Text(
                                                    text = "${slotIndex + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.4f),
                                                    fontWeight = FontWeight.Bold
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

            Spacer(modifier = Modifier.height(10.dp))

            // --- TRAY OF UNPLACED PIECES ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
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
                            text = if (selectedPieceId != null) "Tap slot to place Piece #${selectedPieceId!! + 1}" else "Tap piece to pick up",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedPieceId != null) PrimaryIndigo else TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (unplacedPieces.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨ All pieces placed on board!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(unplacedPieces) { pieceId ->
                                val isSelected = pieceId == selectedPieceId
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PrimaryIndigo.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryIndigo else SlateBorder
                                    ),
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedPieceId = if (isSelected) null else pieceId
                                            HapticManager.performTileSlide(context)
                                        }
                                        .testTag("piece_tray_$pieceId")
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = activeTheme.drawableRes),
                                            contentDescription = "Piece $pieceId",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xAA000000),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.Center)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${pieceId + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = WhiteCanvas,
                                                    fontWeight = FontWeight.Bold,
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
            }
        }

        // Solved Modal
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
                        .testTag("jigsaw_solved_dialog")
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
                                Text("🧩", fontSize = 34.sp)
                            }
                        }

                        Text(
                            text = "Jigsaw Assembled!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "${activeTheme.title} (${gridSize}x$gridSize)",
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
                                    Text("Moves", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("$movesCount", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("${timeElapsed}s", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Bounty", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("+80 XP • +40🪙", fontWeight = FontWeight.Bold, color = PrimaryIndigo)
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
                                .testTag("claim_2x_jigsaw_reward_button")
                        ) {
                            Text("📺 Claim 2X Reward (+80 Coins)", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isSolved = false
                                selectedThemeIndex = (selectedThemeIndex + 1) % themes.size
                                onNextPuzzleClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("next_jigsaw_puzzle_button")
                        ) {
                            Text(
                                text = "Next Jigsaw Puzzle ➡️",
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }
            }
        }

        // Theme picker dialog
        if (showThemePicker) {
            Dialog(onDismissRequest = { showThemePicker = false }) {
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
                            text = "Select Jigsaw Artwork",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        themes.forEachIndexed { index, item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (index == selectedThemeIndex) PrimaryIndigo.copy(alpha = 0.1f) else SlateBackground
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (index == selectedThemeIndex) PrimaryIndigo else SlateBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedThemeIndex = index
                                        showThemePicker = false
                                        isSolved = false
                                        selectedPieceId = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = item.drawableRes),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == selectedThemeIndex) PrimaryIndigo else TextPrimary
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
