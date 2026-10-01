package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgeGroup
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.PuzzleType
import com.example.data.model.UserProgress
import com.example.ui.components.AppHeader
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
import kotlinx.coroutines.launch

data class LevelRangeOption(val label: String, val start: Int, val end: Int)

@Composable
fun CategoryDetailScreen(
    category: PuzzleCategory,
    puzzles: List<PuzzleItem>,
    userProgress: UserProgress,
    onPuzzleSelect: (PuzzleItem) -> Unit,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onImportFromUrl: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onImportFromJson: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = Color(category.accentColorHex)
    val focusManager = LocalFocusManager.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val solvedInThisCategory = userProgress.categorySolvedCounts[category.id] ?: 0
    val achievementCount = userProgress.unlockedBadgeIds.size
    // Unlock formula: Complete previous level OR unlock every 5-10 achievements
    val maxUnlockedLevel = (solvedInThisCategory + 1 + (achievementCount / 5)).coerceAtLeast(1)

    var lockedNoticeMessage by remember { mutableStateOf<String?>(null) }
    var selectedRange by remember { mutableStateOf(LevelRangeOption("All Levels (${puzzles.size})", 1, 9999)) }
    var selectedDifficultyFilter by remember { mutableStateOf<Difficulty?>(null) }
    var jumpLevelText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importUrlInput by remember { mutableStateOf("") }
    var importJsonInput by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    val levelRanges = remember(puzzles.size) {
        val list = mutableListOf(
            LevelRangeOption("All Levels (${puzzles.size})", 1, 9999),
            LevelRangeOption("Levels 1 - 15", 1, 15)
        )
        if (puzzles.size > 15) {
            list.add(LevelRangeOption("Uploaded Levels (16+)", 16, 9999))
        }
        list
    }

    val filteredPuzzles = remember(puzzles, selectedRange, selectedDifficultyFilter, searchQuery) {
        puzzles.filter { puzzle ->
            val matchesRange = puzzle.level in selectedRange.start..selectedRange.end
            val matchesDifficulty = selectedDifficultyFilter == null || puzzle.difficulty == selectedDifficultyFilter
            val matchesSearch = searchQuery.isBlank() ||
                    puzzle.title.contains(searchQuery, ignoreCase = true) ||
                    puzzle.question.contains(searchQuery, ignoreCase = true) ||
                    "Level ${puzzle.level}".contains(searchQuery, ignoreCase = true)
            matchesRange && matchesDifficulty && matchesSearch
        }.sortedBy { it.level }
    }

    var puzzleLimit by remember(selectedRange, selectedDifficultyFilter, searchQuery) { mutableStateOf(5) }
    var isGeneratingMorePuzzles by remember { mutableStateOf(false) }

    val puzzlesToShow = remember(filteredPuzzles, puzzleLimit) {
        filteredPuzzles.take(puzzleLimit)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        AppHeader(
            title = category.title,
            totalXp = userProgress.totalXp,
            starsCount = userProgress.starsEarned,
            coins = userProgress.coins,
            selectedAgeGroup = userProgress.selectedAgeGroup,
            onBackClick = onBackClick,
            onProfileClick = onProfileClick
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Category Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.2.dp, SlateBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Image(
                                painter = painterResource(id = category.imageRes),
                                contentDescription = category.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            )
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = category.iconEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${category.title} Quest Catalog",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "15 Core Levels • Unlimited Database Uploads (${puzzles.size} Available)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showImportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("import_puzzles_button")
                            ) {
                                Text("📥 Upload / Import Puzzles via Database URL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Quick Level Jump & Search Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Search / Keyword filter
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search 1000+ quests...", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    unfocusedBorderColor = SlateBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            // Quick Jump Level input
                            OutlinedTextField(
                                value = jumpLevelText,
                                onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) jumpLevelText = it },
                                placeholder = { Text("Lvl #", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = {
                                    val targetLvl = jumpLevelText.toIntOrNull()
                                    if (targetLvl != null && targetLvl in 1..1000) {
                                        selectedRange = LevelRangeOption("Level $targetLvl", targetLvl, targetLvl)
                                        focusManager.clearFocus()
                                    }
                                }),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val targetLvl = jumpLevelText.toIntOrNull()
                                        if (targetLvl != null && targetLvl in 1..1000) {
                                            selectedRange = LevelRangeOption("Level $targetLvl", targetLvl, targetLvl)
                                            focusManager.clearFocus()
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Jump", tint = PrimaryIndigo, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    unfocusedBorderColor = SlateBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(105.dp)
                            )
                        }

                        // Level Range Pills
                        Text(
                            text = "Level Ranges:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(levelRanges) { range ->
                                val isSelected = selectedRange == range
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) PrimaryIndigo else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) PrimaryIndigo else SlateBorder),
                                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedRange = range }
                                ) {
                                    Text(
                                        text = range.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) WhiteCanvas else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Difficulty filter chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                val isAll = selectedDifficultyFilter == null
                                FilterChip(
                                    selected = isAll,
                                    onClick = { selectedDifficultyFilter = null },
                                    label = { Text("All Tiers") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0F172A),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(Difficulty.entries) { diff ->
                                val isSelected = selectedDifficultyFilter == diff
                                val diffColor = Color(diff.colorHex)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDifficultyFilter = if (isSelected) null else diff },
                                    label = { Text("${diff.tierEmoji} ${diff.title}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = diffColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Matching Quests (${filteredPuzzles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Rewards: +XP & 🪙 Coins",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Empty state
            if (filteredPuzzles.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                        border = BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No puzzles found in range",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search query or selecting a different level range.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Virtualized 1000+ Puzzle List Items
            items(puzzlesToShow, key = { it.id }) { puzzle ->
                val imageToDisplay = puzzle.imageRes ?: puzzle.difficulty.tierImageRes
                val tierColor = Color(puzzle.difficulty.colorHex)
                val isUnlocked = puzzle.level == 1 || puzzle.level <= maxUnlockedLevel
                val isCompleted = puzzle.level <= solvedInThisCategory
                val isCurrent = puzzle.level == (solvedInThisCategory + 1)

                val cardContainerColor = when {
                    !isUnlocked -> Color(0xFFF1F5F9)
                    isCurrent -> accentColor.copy(alpha = 0.06f)
                    isCompleted -> Color(0xFFF8FAFC)
                    else -> WhiteCanvas
                }

                val cardBorderColor = when {
                    !isUnlocked -> Color(0xFFE2E8F0)
                    isCurrent -> accentColor
                    isCompleted -> SuccessGreen.copy(alpha = 0.5f)
                    else -> SlateBorder
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                    border = BorderStroke(if (isCurrent) 2.dp else 1.2.dp, cardBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (isUnlocked) {
                                HapticManager.performTileSlide(context)
                                onPuzzleSelect(puzzle)
                            } else {
                                HapticManager.performWrongAnswer(context)
                                lockedNoticeMessage = "🔒 Level ${puzzle.level} is locked! Complete Level ${puzzle.level - 1} to unlock this challenge."
                            }
                        }
                        .testTag("puzzle_item_${puzzle.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Puzzle AI Photo Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Image(
                                    painter = painterResource(id = imageToDisplay),
                                    contentDescription = puzzle.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .then(if (!isUnlocked) Modifier.background(Color(0x77000000)) else Modifier)
                                )
                                // Level tag or Lock icon
                                Surface(
                                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                                    color = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF64748B),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = if (isUnlocked) "LVL ${puzzle.level}" else "🔒 LVL ${puzzle.level}",
                                        color = if (isUnlocked) Color(0xFFFDE68A) else WhiteCanvas,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = puzzle.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) TextPrimary else TextMuted,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (isCompleted) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (isUnlocked) puzzle.question else "🔒 Complete Level ${puzzle.level - 1} to unlock",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isUnlocked) TextSecondary else TextMuted,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isUnlocked) tierColor.copy(alpha = 0.12f) else Color(0xFFE2E8F0),
                                        border = BorderStroke(0.6.dp, if (isUnlocked) tierColor.copy(alpha = 0.4f) else Color(0xFFCBD5E1))
                                    ) {
                                        Text(
                                            text = "${puzzle.difficulty.tierEmoji} ${puzzle.difficulty.title}",
                                            color = if (isUnlocked) tierColor else TextMuted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${puzzle.difficulty.xpReward} XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) PrimaryIndigo else TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "• +${puzzle.difficulty.coinsReward} 🪙",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) Color(0xFFD97706) else TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Play Button or Lock Badge
                        Surface(
                            shape = CircleShape,
                            color = when {
                                !isUnlocked -> Color(0xFFE2E8F0)
                                isCurrent -> accentColor
                                isCompleted -> SuccessGreen.copy(alpha = 0.15f)
                                else -> PrimaryIndigo.copy(alpha = 0.12f)
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Current",
                                        tint = WhiteCanvas,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredPuzzles.size > puzzleLimit) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                        border = BorderStroke(1.2.dp, SlateBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isGeneratingMorePuzzles) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = accentColor,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Generating next 10 puzzles...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Text(
                                    text = "Showing ${puzzlesToShow.size} of ${filteredPuzzles.size} matching quests",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                                Button(
                                    onClick = {
                                        isGeneratingMorePuzzles = true
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            puzzleLimit += 10
                                            isGeneratingMorePuzzles = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Generate Next 10 Puzzles ⚡",
                                        fontWeight = FontWeight.Bold,
                                        color = WhiteCanvas,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                importStatusMessage = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importUrlInput.isNotBlank()) {
                            isImporting = true
                            importStatusMessage = "Fetching puzzles from database URL..."
                            onImportFromUrl?.invoke(importUrlInput.trim()) { success, msg ->
                                isImporting = false
                                importStatusMessage = msg
                            }
                        } else if (importJsonInput.isNotBlank()) {
                            isImporting = true
                            importStatusMessage = "Parsing and storing puzzles in database..."
                            onImportFromJson?.invoke(importJsonInput.trim()) { success, msg ->
                                isImporting = false
                                importStatusMessage = msg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    enabled = !isImporting && (importUrlInput.isNotBlank() || importJsonInput.isNotBlank())
                ) {
                    Text(if (isImporting) "Importing..." else "Upload to Database", color = WhiteCanvas, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showImportDialog = false
                        importStatusMessage = null
                    }
                ) {
                    Text("Close")
                }
            },
            title = {
                Text(
                    text = "📥 Database Puzzle Importer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Add puzzles directly into the database through your custom database URL or JSON array. New levels will automatically be generated in the category!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = importUrlInput,
                        onValueChange = { importUrlInput = it },
                        placeholder = { Text("https://my-puzzle-4e5f3.firebaseio.com/puzzles.json", fontSize = 12.sp) },
                        label = { Text("Database URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "— OR PASTE PUZZLE JSON —",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("[{\"title\": \"Quest\", \"question\": \"...\", ...}]", fontSize = 11.sp) },
                        label = { Text("Puzzle JSON") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (importStatusMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = importStatusMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        )
    }

    if (lockedNoticeMessage != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { lockedNoticeMessage = null },
            confirmButton = {
                Button(
                    onClick = { lockedNoticeMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Understood", color = WhiteCanvas, fontWeight = FontWeight.Bold)
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Level",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Level Progression Locked 🔒",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = lockedNoticeMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        )
    }
}
}
