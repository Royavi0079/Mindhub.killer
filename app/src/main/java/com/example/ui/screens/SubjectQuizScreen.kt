package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgeGroup
import com.example.data.model.Difficulty
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleItem
import com.example.data.model.UserProgress
import com.example.ui.components.AppHeader
import com.example.ui.components.CelebrationDialog
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
fun SubjectQuizScreen(
    puzzle: PuzzleItem,
    category: PuzzleCategory,
    selectedOption: String?,
    isAnswerSubmitted: Boolean,
    isAnswerCorrect: Boolean,
    aiHintText: String?,
    isHintLoading: Boolean,
    userProgress: UserProgress,
    is2xClaimed: Boolean = false,
    eliminatedOptions: Set<String> = emptySet(),
    onOptionSelected: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onRequestAiHint: () -> Unit,
    onClaim2xReward: (() -> Unit)? = null,
    onWatchLevelBonusVideo: (() -> Unit)? = null,
    onWatchInterstitialForHints: (() -> Unit)? = null,
    onWatchInterstitialForSolution: (() -> Unit)? = null,
    onSecondChanceWatchAd: (() -> Unit)? = null,
    onRateFeedback: (() -> Unit)? = null,
    onCompleteLevelWithRewardedAd: () -> Unit = {},
    onNextPuzzle: () -> Unit,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = Color(category.accentColorHex)
    val lightBg = Color(category.lightBgColorHex)
    val tierColor = Color(puzzle.difficulty.colorHex)
    val heroImage = puzzle.imageRes ?: puzzle.difficulty.tierImageRes
    var showHintSection by remember(puzzle.id) { mutableStateOf(false) }
    var showSolutionSection by remember(puzzle.id) { mutableStateOf(false) }
    var showCelebrationOverlay by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(isAnswerSubmitted, isAnswerCorrect) {
        if (isAnswerSubmitted) {
            if (isAnswerCorrect) {
                HapticManager.performMathSolveSuccess(context)
                showCelebrationOverlay = true
            } else {
                HapticManager.performWrongAnswer(context)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                // Category & Tier Banner Pill Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = lightBg,
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category.iconEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tierColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${puzzle.difficulty.tierCode} • ${puzzle.difficulty.label}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            repeat(puzzle.difficulty.stars) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Tier Star",
                                    tint = GoldStar,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Question Card with AI Artwork Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.5.dp, SlateBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // AI Generated Hero Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Image(
                                painter = painterResource(id = heroImage),
                                contentDescription = puzzle.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0xCC0F172A))
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = puzzle.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteCanvas
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = tierColor
                                ) {
                                    Text(
                                        text = "+${puzzle.difficulty.xpReward} XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = WhiteCanvas,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = puzzle.question,
                                style = if (userProgress.largeTextMode) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                lineHeight = if (userProgress.largeTextMode) 32.sp else 26.sp
                            )
                        }
                    }
                }
            }

            // Interactive Options
            item {
                Text(
                    text = "Select your answer:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(puzzle.options.size) { index ->
                val option = puzzle.options[index]
                val isSelected = selectedOption == option
                val isCorrectAnswer = option.trim() == puzzle.correctAnswer.trim()
                val isEliminated = eliminatedOptions.contains(option)

                val cardContainerColor = when {
                    isEliminated -> Color(0xFFF1F5F9)
                    isAnswerSubmitted && isCorrectAnswer -> Color(0xFFD1FAE5) // Success light green
                    isAnswerSubmitted && isSelected && !isAnswerCorrect -> Color(0xFFFEE2E2) // Error light red
                    isSelected -> accentColor.copy(alpha = 0.08f)
                    else -> WhiteCanvas
                }

                val borderColor = when {
                    isEliminated -> Color(0xFFE2E8F0)
                    isAnswerSubmitted && isCorrectAnswer -> SuccessGreen
                    isAnswerSubmitted && isSelected && !isAnswerCorrect -> ErrorRed
                    isSelected -> accentColor
                    else -> SlateBorder
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                    border = BorderStroke(if (isSelected || isAnswerSubmitted) 1.8.dp else 1.dp, borderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !isAnswerSubmitted && !isEliminated) {
                            HapticManager.performTileSlide(context)
                            onOptionSelected(option)
                        }
                        .testTag("quiz_option_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter / Check Indicator
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isEliminated -> Color(0xFFCBD5E1)
                                isAnswerSubmitted && isCorrectAnswer -> SuccessGreen
                                isAnswerSubmitted && isSelected && !isAnswerCorrect -> ErrorRed
                                isSelected -> accentColor
                                else -> Color(0xFFF1F5F9)
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isEliminated) {
                                    Icon(Icons.Default.Close, contentDescription = "Eliminated", tint = WhiteCanvas, modifier = Modifier.size(18.dp))
                                } else if (isAnswerSubmitted && isCorrectAnswer) {
                                    Icon(Icons.Default.Check, contentDescription = "Correct", tint = WhiteCanvas, modifier = Modifier.size(20.dp))
                                } else if (isAnswerSubmitted && isSelected && !isAnswerCorrect) {
                                    Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = WhiteCanvas, modifier = Modifier.size(20.dp))
                                } else {
                                    val letter = ('A' + index).toString()
                                    Text(
                                        text = letter,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) WhiteCanvas else TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = if (isEliminated) "$option (Eliminated)" else option,
                            style = if (userProgress.largeTextMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isEliminated) Color(0xFF94A3B8) else TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Hint & Solution Drawer Toggle Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!showHintSection) {
                                onWatchInterstitialForHints?.invoke()
                            }
                            showHintSection = !showHintSection
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SlateBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_hint_button")
                    ) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Hint", modifier = Modifier.size(16.dp), tint = GoldStar)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showHintSection) "Hide Hint" else "Hint 📺", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (!showSolutionSection) {
                                onWatchInterstitialForSolution?.invoke() ?: onWatchInterstitialForHints?.invoke()
                            }
                            showSolutionSection = !showSolutionSection
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_solution_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Solution", modifier = Modifier.size(16.dp), tint = Color(0xFF059669))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showSolutionSection) "Hide Sol." else "Solution 📺", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onRequestAiHint,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AiRose.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AiRose),
                        modifier = Modifier.testTag("ai_deep_hint_button")
                    ) {
                        if (isHintLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AiRose, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Hint", modifier = Modifier.size(16.dp), tint = AiRose)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gemini", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Solution Details Box
            if (showSolutionSection) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.2.dp, Color(0xFF86EFAC)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("solution_unlocked_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "✅", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Verified Solution (Ad Unlocked)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Correct Answer: ${puzzle.correctAnswer}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = puzzle.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF14532D)
                            )
                        }
                    }
                }
            }

            // Hint Details Box
            if (showHintSection) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                        border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "💡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Smart Hint",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF854D0E)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = puzzle.hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF713F12)
                            )

                            if (!aiHintText.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = WhiteCanvas,
                                    border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Gemini",
                                                tint = AiRose,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Gemini AI Reasoning",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AiRose
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = aiHintText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            // Hints counter & AdMob Interstitial Refill
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
                                        text = "💡 ${userProgress.freeHintsRemaining} Free Hints Remaining",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                if (onWatchInterstitialForHints != null) {
                                    OutlinedButton(
                                        onClick = onWatchInterstitialForHints,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFCA8A04)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("refill_hints_ad_button")
                                    ) {
                                        Text(
                                            text = "📺 Watch Ad for +4 Hints",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF854D0E)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Explanation & Second Chance Section after submission
            if (isAnswerSubmitted) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAnswerCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                        ),
                        border = BorderStroke(
                            1.2.dp,
                            if (isAnswerCorrect) Color(0xFFA7F3D0) else Color(0xFFFECACA)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAnswerCorrect) "🎉 Correct! +${puzzle.difficulty.xpReward} XP Earned" else "🤔 Not quite!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnswerCorrect) SuccessGreen else ErrorRed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = puzzle.explanation,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )

                            if (puzzle.funFact.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🌟 Fun Fact: ${puzzle.funFact}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            // Second Chance / Chance Over Revive Trigger
                            if (!isAnswerCorrect && onSecondChanceWatchAd != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "⚡", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Need a Second Chance?",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E40AF)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Watch a quick reward video to eliminate 2 incorrect answers and try again!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF1E3A8A)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = onSecondChanceWatchAd,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(42.dp)
                                                .testTag("second_chance_ad_button")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "Second Chance",
                                                    tint = Color(0xFFFEF08A),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Watch Video for 50/50 Chance 📺",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WhiteCanvas
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

            // Action Button (Submit or Next)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                if (!isAnswerSubmitted) {
                    Button(
                        onClick = onSubmitAnswer,
                        enabled = selectedOption != null,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_answer_button")
                    ) {
                        Text(
                            text = "Check Answer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WhiteCanvas
                        )
                    }
                } else if (isAnswerCorrect) {
                    Button(
                        onClick = onCompleteLevelWithRewardedAd,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("claim_ad_reward_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = WhiteCanvas, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Watch Ad: Claim +10 🪙 & +5 Pts & Next Level",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onNextPuzzle,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("next_puzzle_button")
                    ) {
                        Text(
                            text = "Next Challenge",
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

        if (showCelebrationOverlay) {
            CorrectAnswerCelebrationOverlay(
                xpEarned = 5,
                coinsEarned = 10,
                starsCount = puzzle.difficulty.stars,
                titleText = "Correct Answer! 🎉",
                subtitleText = "Watch the full rewarded ad to receive 10 Coins and 5 Points and advance to the next level!",
                is2xClaimed = is2xClaimed,
                onClaim2xReward = onClaim2xReward,
                onWatchLevelBonusVideo = onWatchLevelBonusVideo,
                onRateFeedback = onRateFeedback,
                onDismissOrContinue = {
                    showCelebrationOverlay = false
                    onCompleteLevelWithRewardedAd()
                }
            )
        }
    }
}
