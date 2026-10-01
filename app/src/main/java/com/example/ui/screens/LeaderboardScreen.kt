package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.data.model.LeaderboardGenerator
import com.example.data.model.LeaderboardMetric
import com.example.data.model.LeaderboardTimeframe
import com.example.data.model.UserProgress
import com.example.ui.components.AppHeader
import com.example.ui.components.LowFadeGlassSurface
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCanvas

@Composable
fun LeaderboardScreen(
    userProgress: UserProgress,
    unclaimedTasksCount: Int = 0,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateTasks: () -> Unit,
    onNavigateLeaderboard: () -> Unit = {},
    onNavigateCommunity: () -> Unit = {},
    onNavigateProfile: () -> Unit = onProfileClick,
    onNavigateAiLab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf(LeaderboardTimeframe.DAILY) }
    var selectedMetric by remember { mutableStateOf(LeaderboardMetric.COINS) }

    val (rankedList, currentUserRanked) = remember(selectedTimeframe, selectedMetric, userProgress) {
        LeaderboardGenerator.getLeaderboard(selectedTimeframe, selectedMetric, userProgress)
    }

    val top1 = rankedList.getOrNull(0)
    val top2 = rankedList.getOrNull(1)
    val top3 = rankedList.getOrNull(2)
    val restOfRankings = if (rankedList.size > 3) rankedList.subList(3, rankedList.size) else emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        AppHeader(
            title = "Global Leaderboard",
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Timeframe Segmented Switcher (Daily vs All-Time)
            item {
                LowFadeGlassSurface(
                    shape = RoundedCornerShape(18.dp),
                    elevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeaderboardTimeframe.entries.forEach { timeframe ->
                            val isSelected = selectedTimeframe == timeframe
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryIndigo else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTimeframe = timeframe }
                                    .testTag("timeframe_${timeframe.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = timeframe.emoji,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = timeframe.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) WhiteCanvas else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Metric Sort Filter Chips (Coins vs Puzzles Solved)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeaderboardMetric.entries.forEach { metric ->
                        val isSelected = selectedMetric == metric
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMetric = metric },
                            label = {
                                Text(
                                    text = "${metric.iconEmoji} ${metric.title}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFFB45309),
                                containerColor = WhiteCanvas,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFF59E0B) else SlateBorder
                            ),
                            modifier = Modifier.testTag("metric_${metric.name.lowercase()}")
                        )
                    }
                }
            }

            // --- TOP 3 PODIUM ---
            item {
                PodiumSection(
                    top1 = top1,
                    top2 = top2,
                    top3 = top3,
                    selectedMetric = selectedMetric
                )
            }

            // Section Label
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Global Challenger Rankings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${rankedList.size} Minds Ranked",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Remaining Leaderboard List (Rank 4 to N)
            items(restOfRankings) { entry ->
                LeaderboardRowCard(
                    entry = entry,
                    selectedMetric = selectedMetric
                )
            }
        }
    }

        // Sticky Bottom User Standing Bar
        StickyUserStandingCard(
            currentUserEntry = currentUserRanked,
            selectedTimeframe = selectedTimeframe,
            selectedMetric = selectedMetric
        )

    }
}

@Composable
private fun PodiumSection(
    top1: LeaderboardEntry?,
    top2: LeaderboardEntry?,
    top3: LeaderboardEntry?,
    selectedMetric: LeaderboardMetric
) {
    LowFadeGlassSurface(
        shape = RoundedCornerShape(22.dp),
        elevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_podium")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✨ Top Podium Champions ✨",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryIndigo
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (top2 != null) {
                    PodiumColumn(
                        entry = top2,
                        rankNumber = 2,
                        medalEmoji = "🥈",
                        ringColor = Color(0xFF94A3B8),
                        pillarHeight = 90.dp,
                        pillarColor = Color(0xFFF1F5F9),
                        selectedMetric = selectedMetric,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 1st Place (Gold)
                if (top1 != null) {
                    PodiumColumn(
                        entry = top1,
                        rankNumber = 1,
                        medalEmoji = "🥇",
                        ringColor = Color(0xFFF59E0B),
                        pillarHeight = 120.dp,
                        pillarColor = Color(0xFFFEF3C7),
                        isFirstPlace = true,
                        selectedMetric = selectedMetric,
                        modifier = Modifier.weight(1.1f)
                    )
                }

                // 3rd Place (Bronze)
                if (top3 != null) {
                    PodiumColumn(
                        entry = top3,
                        rankNumber = 3,
                        medalEmoji = "🥉",
                        ringColor = Color(0xFFD97706),
                        pillarHeight = 70.dp,
                        pillarColor = Color(0xFFFFFBEB),
                        selectedMetric = selectedMetric,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardEntry,
    rankNumber: Int,
    medalEmoji: String,
    ringColor: Color,
    pillarHeight: androidx.compose.ui.unit.Dp,
    pillarColor: Color,
    isFirstPlace: Boolean = false,
    selectedMetric: LeaderboardMetric,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with medal crown
        Box(contentAlignment = Alignment.TopCenter) {
            Surface(
                shape = CircleShape,
                color = Color(entry.avatarColorHex),
                border = BorderStroke(if (isFirstPlace) 3.dp else 2.dp, ringColor),
                modifier = Modifier
                    .size(if (isFirstPlace) 54.dp else 44.dp)
                    .padding(top = if (isFirstPlace) 4.dp else 0.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = entry.avatarEmoji,
                        fontSize = if (isFirstPlace) 22.sp else 18.sp
                    )
                }
            }

            // Medal indicator
            Surface(
                shape = CircleShape,
                color = WhiteCanvas,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = medalEmoji, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Username & Country Flag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = entry.countryEmoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (entry.isCurrentUser) "You" else entry.username.split(" ").firstOrNull() ?: entry.username,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (entry.isCurrentUser) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (entry.isCurrentUser) PrimaryIndigo else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Score display
        Text(
            text = if (selectedMetric == LeaderboardMetric.COINS) "🪙 ${entry.coinsEarned}" else "🧩 ${entry.solvedPuzzlesCount}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (selectedMetric == LeaderboardMetric.COINS) Color(0xFFB45309) else PrimaryIndigo,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Pedestal Block
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = pillarColor,
            border = BorderStroke(1.dp, ringColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(pillarHeight)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "#$rankNumber",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ringColor
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRowCard(
    entry: LeaderboardEntry,
    selectedMetric: LeaderboardMetric
) {
    val isUser = entry.isCurrentUser
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) Color(0xFFEEF2FF) else WhiteCanvas
        ),
        border = BorderStroke(
            1.2.dp,
            if (isUser) PrimaryIndigo else SlateBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 2.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (isUser) "leaderboard_user_row" else "leaderboard_row_${entry.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank Number Pill
            Surface(
                shape = CircleShape,
                color = if (isUser) PrimaryIndigo else Color(0xFFF1F5F9),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${entry.rank}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) WhiteCanvas else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Avatar & Country
            Box {
                Surface(
                    shape = CircleShape,
                    color = Color(entry.avatarColorHex),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = entry.avatarEmoji, fontSize = 16.sp)
                    }
                }
                Text(
                    text = entry.countryEmoji,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Info (Name & Title Badge)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.username,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isUser) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isUser) PrimaryIndigo else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PrimaryIndigo
                        ) {
                            Text(
                                text = "YOU",
                                color = WhiteCanvas,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = entry.titleBadge,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Metric Value Pill
            Column(horizontalAlignment = Alignment.End) {
                if (selectedMetric == LeaderboardMetric.COINS) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${entry.coinsEarned}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                    Text(
                        text = "${entry.solvedPuzzlesCount} solved",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEF2FF),
                        border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🧩", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${entry.solvedPuzzlesCount}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryIndigo
                            )
                        }
                    }
                    Text(
                        text = "${entry.coinsEarned} coins",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StickyUserStandingCard(
    currentUserEntry: LeaderboardEntry,
    selectedTimeframe: LeaderboardTimeframe,
    selectedMetric: LeaderboardMetric
) {
    Surface(
        color = Color(0xFF1E293B),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 76.dp)
            .testTag("sticky_user_standing")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${currentUserEntry.rank}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = WhiteCanvas
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Your Standing (${selectedTimeframe.title})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WhiteCanvas
                    )
                    Text(
                        text = if (currentUserEntry.rank <= 3) "🔥 You are on the World Podium!" else "Solve more puzzles to climb to #1!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF334155)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedMetric == LeaderboardMetric.COINS) "🪙 ${currentUserEntry.coinsEarned}" else "🧩 ${currentUserEntry.solvedPuzzlesCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE68A)
                        )
                    }
                }
            }
        }
    }
}
