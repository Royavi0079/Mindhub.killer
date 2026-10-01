package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AgeGroup
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.UserProgress
import com.example.ui.components.AppHeader
import com.example.ui.components.CorrectAnswerCelebrationOverlay
import com.example.ui.theme.AiRose
import com.example.ui.theme.ErrorRed
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

@Composable
fun AILabScreen(
    generatedPuzzle: PuzzleItem?,
    isGenerating: Boolean,
    selectedOption: String?,
    isAnswerSubmitted: Boolean,
    isAnswerCorrect: Boolean,
    userProgress: UserProgress,
    is2xClaimed: Boolean = false,
    onClaim2xReward: (() -> Unit)? = null,
    onWatchLevelBonusVideo: (() -> Unit)? = null,
    onGenerate: (PuzzleCategory, Difficulty) -> Unit,
    onOptionSelect: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(PuzzleCategory.MATH) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.NORMAL) }

    androidx.compose.runtime.LaunchedEffect(isAnswerSubmitted, isAnswerCorrect) {
        if (isAnswerSubmitted) {
            if (isAnswerCorrect) {
                HapticManager.performMathSolveSuccess(context)
            } else {
                HapticManager.performWrongAnswer(context)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        AppHeader(
            title = "AI Puzzle Lab",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lab Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.2.dp, Color(0xFFFECDD3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AiRose,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = WhiteCanvas,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gemini AI Puzzle Synthesis",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "High-tier AI problems crafted for ${userProgress.selectedAgeGroup.ageRange}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Step 1: Subject Category Selector Chips
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("1", color = WhiteCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Knowledge Domain",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            listOf(
                                PuzzleCategory.MATH,
                                PuzzleCategory.PHYSICS,
                                PuzzleCategory.CHEMISTRY,
                                PuzzleCategory.BIOLOGY,
                                PuzzleCategory.HISTORY
                            )
                        ) { cat ->
                            val isSelected = selectedCategory == cat
                            val accentColor = Color(cat.accentColorHex)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) accentColor else WhiteCanvas,
                                border = BorderStroke(1.2.dp, if (isSelected) accentColor else SlateBorder),
                                shadowElevation = if (isSelected) 3.dp else 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = cat }
                                    .testTag("ai_category_${cat.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = cat.iconEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) WhiteCanvas else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: 3-Tier AI Difficulty Selection Cards with High-Resolution Artwork
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("2", color = WhiteCanvas, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Puzzle Tier (High & Different)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Difficulty.entries.forEach { diff ->
                            val isSelected = selectedDifficulty == diff
                            val tierColor = Color(diff.colorHex)

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(diff.colorHex).copy(alpha = 0.07f) else WhiteCanvas
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) tierColor else SlateBorder
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedDifficulty = diff }
                                    .testTag("ai_diff_${diff.name.lowercase()}")
                                    .animateContentSize()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Tier AI Generated Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF1F5F9))
                                    ) {
                                        Image(
                                            painter = painterResource(id = diff.tierImageRes),
                                            contentDescription = diff.label,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        // Tier badge overlay
                                        Surface(
                                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                                            color = tierColor,
                                            modifier = Modifier.align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = diff.tierCode,
                                                color = WhiteCanvas,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = diff.label,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) tierColor else TextPrimary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                repeat(diff.stars) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = GoldStar,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = tierColor.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "+${diff.xpReward} XP",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tierColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = diff.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = tierColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 3: Generate Button
            item {
                Button(
                    onClick = { onGenerate(selectedCategory, selectedDifficulty) },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = AiRose),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_ai_puzzle_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = WhiteCanvas,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Synthesizing ${selectedDifficulty.label} AI Puzzle...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WhiteCanvas
                        )
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = WhiteCanvas)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate ${selectedDifficulty.label} Puzzle (${selectedDifficulty.tierCode})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WhiteCanvas
                        )
                    }
                }
            }

            // Generated Puzzle Display Arena
            if (generatedPuzzle != null) {
                item {
                    Text(
                        text = "Active AI Challenge",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                        border = BorderStroke(1.5.dp, Color(generatedPuzzle.difficulty.colorHex).copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // AI Generated Tier Art Hero Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = generatedPuzzle.difficulty.tierImageRes),
                                    contentDescription = generatedPuzzle.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color(0xDD0F172A))
                                            )
                                        )
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomStart)
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(generatedPuzzle.difficulty.colorHex)
                                    ) {
                                        Text(
                                            text = "${generatedPuzzle.difficulty.tierCode} • ${generatedPuzzle.difficulty.label}",
                                            color = WhiteCanvas,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xDD1E293B)
                                    ) {
                                        Text(
                                            text = "+${generatedPuzzle.difficulty.xpReward} XP",
                                            color = GoldStar,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = generatedPuzzle.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(generatedPuzzle.difficulty.colorHex)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = generatedPuzzle.question,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    lineHeight = 26.sp
                                )
                            }
                        }
                    }
                }

                // Options
                items(generatedPuzzle.options.size) { index ->
                    val option = generatedPuzzle.options[index]
                    val isSelected = selectedOption == option
                    val isCorrectAnswer = option.trim() == generatedPuzzle.correctAnswer.trim()

                    val cardBg = when {
                        isAnswerSubmitted && isCorrectAnswer -> Color(0xFFD1FAE5)
                        isAnswerSubmitted && isSelected && !isAnswerCorrect -> Color(0xFFFEE2E2)
                        isSelected -> Color(0xFFFFF1F2)
                        else -> WhiteCanvas
                    }

                    val borderColor = when {
                        isAnswerSubmitted && isCorrectAnswer -> SuccessGreen
                        isAnswerSubmitted && isSelected && !isAnswerCorrect -> ErrorRed
                        isSelected -> AiRose
                        else -> SlateBorder
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.2.dp, borderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !isAnswerSubmitted) {
                                HapticManager.performTileSlide(context)
                                onOptionSelect(option)
                            }
                            .testTag("ai_option_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isAnswerSubmitted && isCorrectAnswer -> SuccessGreen
                                    isAnswerSubmitted && isSelected && !isAnswerCorrect -> ErrorRed
                                    isSelected -> AiRose
                                    else -> Color(0xFFF1F5F9)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isAnswerSubmitted && isCorrectAnswer) {
                                        Icon(Icons.Default.Check, contentDescription = "Correct", tint = WhiteCanvas, modifier = Modifier.size(18.dp))
                                    } else if (isAnswerSubmitted && isSelected && !isAnswerCorrect) {
                                        Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = WhiteCanvas, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            text = ('A' + index).toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) WhiteCanvas else TextPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = option,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Explanation card
                if (isAnswerSubmitted) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAnswerCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                            ),
                            border = BorderStroke(1.dp, if (isAnswerCorrect) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isAnswerCorrect) "🎉 Correct! +${generatedPuzzle.difficulty.xpReward} XP Earned" else "💡 Learning Insight",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnswerCorrect) SuccessGreen else ErrorRed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = generatedPuzzle.explanation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                                if (generatedPuzzle.funFact.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "✨ Trivia: ${generatedPuzzle.funFact}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit button
                if (!isAnswerSubmitted) {
                    item {
                        Button(
                            onClick = onSubmitAnswer,
                            enabled = selectedOption != null,
                            colors = ButtonDefaults.buttonColors(containerColor = AiRose),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_ai_answer_button")
                        ) {
                            Text(
                                text = "Submit AI Challenge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }
            }
        }
    }

        if (isAnswerSubmitted && isAnswerCorrect && generatedPuzzle != null) {
            CorrectAnswerCelebrationOverlay(
                xpEarned = generatedPuzzle.difficulty.xpReward,
                coinsEarned = generatedPuzzle.difficulty.coinsReward,
                starsCount = generatedPuzzle.difficulty.stars,
                titleText = "AI Challenge Mastered! 🧠",
                subtitleText = "Brilliant neural deduction! +${generatedPuzzle.difficulty.xpReward} XP & +${generatedPuzzle.difficulty.coinsReward} Coins awarded.",
                is2xClaimed = is2xClaimed,
                onClaim2xReward = onClaim2xReward,
                onWatchLevelBonusVideo = onWatchLevelBonusVideo,
                onDismissOrContinue = {
                    onGenerate(selectedCategory, selectedDifficulty)
                }
            )
        }
    }
}

