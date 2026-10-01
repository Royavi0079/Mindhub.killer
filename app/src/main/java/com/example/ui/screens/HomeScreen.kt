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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AgeGroup
import com.example.data.model.PuzzleCategory
import com.example.data.model.UserProgress
import com.example.data.repository.AchievementCatalog
import com.example.ui.components.AchievementMilestoneBanner
import com.example.ui.components.AgeGroupSelector
import com.example.ui.components.AppHeader
import com.example.ui.components.LowFadeGlassSurface
import com.example.ui.components.CategoryCard
import com.example.ui.theme.AiRose
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.AppThemeUtil
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCanvas

@Composable
fun HomeScreen(
    userProgress: UserProgress,
    unclaimedTasksCount: Int = 0,
    isOnline: Boolean = true,
    onRefreshSection: (String) -> Unit = {},
    onRetryConnection: () -> Unit = {},
    onCategoryClick: (PuzzleCategory) -> Unit,
    onAgeSelected: (AgeGroup) -> Unit,
    onOpenTasks: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenCommunity: () -> Unit = {},
    onOpenAiLab: () -> Unit,
    onOpenArrowMaze: () -> Unit = {},
    onOpenSnakeArrow: () -> Unit = {},
    onOpenJigsawStudio: () -> Unit = {},
    onOpenCircuitLink: () -> Unit = {},
    onProfileClick: () -> Unit,
    onOpenAuth: () -> Unit = {},
    onOpenRatingFeedback: () -> Unit = {},
    onDailyStreakClaim: () -> Unit = {},
    onStartDailyChallenge: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isKidsModeActive = userProgress.selectedAgeGroup == AgeGroup.JUNIOR
    val themeColors = remember(userProgress.userProfileBackground) {
        AppThemeUtil.getAppColors(userProgress.userProfileBackground)
    }
    val appBgColor = themeColors.first
    val appSectionColor = themeColors.second

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appBgColor)
    ) {
        AppHeader(
            title = if (isKidsModeActive) "MindMatrix Kids" else "MindMatrix",
            totalXp = userProgress.totalXp,
            starsCount = userProgress.starsEarned,
            coins = userProgress.coins,
            selectedAgeGroup = userProgress.selectedAgeGroup,
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
                if (!isOnline) {
                    // --- OFFLINE-ONLY RESTRICTED VIEW (Few sections with images, ads disabled, next page disabled) ---
                    item {
                        OfflineStatusNoticeCard(onRetry = onRetryConnection)
                    }

                    item {
                        OfflineImageGallerySection(
                            onImageClick = { onCategoryClick(PuzzleCategory.VISUAL) }
                        )
                    }

                    item {
                        OfflineAdsDisabledCard()
                    }

                    item {
                        OfflineCloudSyncCard(onRetry = onRetryConnection)
                    }
                } else {
                    // --- ONLINE-ONLY FULL FEATURE SET WITH PER-SECTION REFRESH & BACKEND SYNC ---
                    item {
                        com.example.ui.components.MatrixMascot(
                            isKidsMode = isKidsModeActive
                        )
                    }

                    // Hero Banner with Low Fade Glass Layout
                    item {
                        LowFadeGlassSurface(
                            shape = RoundedCornerShape(22.dp),
                            elevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = if (isKidsModeActive) R.drawable.img_kids_dino_safari else R.drawable.img_hero_puzzle
                                        ),
                                        contentDescription = "Hero Artwork",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                    )
                                    // Subtle glass white gradient overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, WhiteCanvas.copy(alpha = 0.82f))
                                                )
                                            )
                                    )
                                }

                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isKidsModeActive) "Kids Brain Explorer 🦖" else "MindMatrix 7-Level Arena",
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = if (isKidsModeActive) "Jigsaw & Grid Puzzles for Smart Kids!" else "Progressive STEM & Logic puzzles from Level 1 to 7",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- DAILY CHALLENGE & STREAK REWARD CARD WITH SECTION REFRESH ---
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = BorderStroke(1.5.dp, Color(0xFFFDE68A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🔥", fontSize = 20.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Day ${userProgress.currentStreak} Daily Sprint",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF78350F)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "🔄",
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable { onRefreshSection("Daily Sprint") }
                                                    .padding(2.dp)
                                            )
                                        }
                                        Text(
                                            text = "Solve today's featured puzzle • +${userProgress.currentStreak * 25 + 50} coins",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }

                                Button(
                                    onClick = onStartDailyChallenge,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("btn_daily_challenge")
                                ) {
                                    Text(
                                        text = "Play",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WhiteCanvas
                                    )
                                }
                            }
                        }
                    }



            // --- PLAYER CLOUD AUTHENTICATION & LOGIN BANNER ---
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (userProgress.isLoggedIn) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (userProgress.isLoggedIn) Color(0xFFBBF7D0) else Color(0xFFCBD5E1)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenAuth)
                        .testTag("home_auth_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (userProgress.isLoggedIn) Color(0xFF16A34A) else PrimaryIndigo,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (userProgress.isLoggedIn) userProgress.userAvatarEmoji else "👤",
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (userProgress.isLoggedIn) "Cloud Account Active" else "Player Portal & Sign In",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userProgress.isLoggedIn) Color(0xFF166534) else TextPrimary
                                )
                                Text(
                                    text = if (userProgress.isLoggedIn) "${userProgress.userName} (${userProgress.userEmail})" else "Sign in with Gmail, OTP Reset, or Play as Guest",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (userProgress.isLoggedIn) Color(0xFF15803D) else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAuth,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userProgress.isLoggedIn) Color(0xFF16A34A) else PrimaryIndigo
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (userProgress.isLoggedIn) "Manage" else "Sign In",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }
            }

            // --- TASK & REWARDS BANNER: Check Tasks & Watch 3 Ads for 150 Coins ---
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                    border = BorderStroke(1.5.dp, Color(0xFFFEF08A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenTasks)
                        .testTag("home_tasks_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEAB308),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = "Tasks",
                                        tint = WhiteCanvas,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tasks & Coins Bounty",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF854D0E)
                                    )
                                    if (unclaimedTasksCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEF4444)
                                        ) {
                                            Text(
                                                text = "$unclaimedTasksCount ready",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WhiteCanvas,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Check all tasks • Watch 3 ads for 150 coins 🪙",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA16207)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenTasks,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCA8A04)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Tasks",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }
            }

            // --- COMMUNITY HUB BANNER ---
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.5.dp, Color(0xFFBBF7D0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenCommunity)
                        .testTag("home_community_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌐", fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Community Hub & Co-op",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Text(
                                    text = "Global sprint chest • Solver riddles & polls",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF047857)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenCommunity,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Join",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }
            }

            // --- GLOBAL LEADERBOARD BANNER ---
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    border = BorderStroke(1.5.dp, Color(0xFFC7D2FE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenLeaderboard)
                        .testTag("home_leaderboard_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryIndigo,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Leaderboard",
                                        tint = Color(0xFFFDE68A),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Global Leaderboard 🏆",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF312E81)
                                )
                                Text(
                                    text = "Daily, Weekly & All-Time Rankings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4338CA)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenLeaderboard,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Ranks",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }
            }

            // --- NEXT ACHIEVEMENT MILESTONE BANNER ---
            item {
                val evaluatedBadges = AchievementCatalog.evaluateBadges(userProgress)
                val closestBadge = evaluatedBadges.firstOrNull { !it.isUnlocked } ?: evaluatedBadges.lastOrNull()
                val unlockedCount = evaluatedBadges.count { it.isUnlocked }
                AchievementMilestoneBanner(
                    closestBadge = closestBadge,
                    totalUnlocked = unlockedCount,
                    totalBadges = evaluatedBadges.size,
                    onClick = onProfileClick
                )
            }

            // Age Mode Selector (Junior 5-10, Standard 11-40, Senior/Master 41-70+)
            item {
                AgeGroupSelector(
                    selectedAgeGroup = userProgress.selectedAgeGroup,
                    onAgeSelected = onAgeSelected
                )
            }

            // --- KIDS MODE BRAIN IMPROVEMENT SHOWCASE (If in Junior or highlighted) ---
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.5.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌱", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Kids Mode: Jigsaw & Grid Puzzles",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = "Interactive jigsaw piece trays & 2x2/3x3 grids",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }

                            Button(
                                onClick = { onCategoryClick(PuzzleCategory.VISUAL) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "Play All",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteCanvas
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Specific Puzzle Mechanics Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenJigsawStudio() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🧩", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Jigsaw Puzzle",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onCategoryClick(PuzzleCategory.VISUAL) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📐", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Grid Layout",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3 Kid Photo Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Dino Safari
                            KidPhotoCard(
                                title = "Dino Safari",
                                tag = "🦖 Jigsaw",
                                imageRes = R.drawable.img_kids_dino_safari,
                                modifier = Modifier.weight(1f),
                                onClick = { onCategoryClick(PuzzleCategory.VISUAL) }
                            )
                            // Space Astronaut
                            KidPhotoCard(
                                title = "Space Kid",
                                tag = "🚀 Interlock",
                                imageRes = R.drawable.img_kids_space_explorer,
                                modifier = Modifier.weight(1f),
                                onClick = { onCategoryClick(PuzzleCategory.VISUAL) }
                            )
                            // Ocean Reef
                            KidPhotoCard(
                                title = "Ocean Reef",
                                tag = "🐠 Grid 2x2",
                                imageRes = R.drawable.img_kids_underwater_reef,
                                modifier = Modifier.weight(1f),
                                onClick = { onCategoryClick(PuzzleCategory.VISUAL) }
                            )
                        }
                    }
                }
            }

            // AI Puzzle Lab Highlight Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    border = BorderStroke(1.5.dp, Color(0xFFFECDD3)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenAiLab)
                        .testTag("ai_lab_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AiRose,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Lab",
                                        tint = WhiteCanvas,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Puzzle Generator",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9F1239)
                                )
                                Text(
                                    text = "Generate endless 7-level STEM puzzles",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFBE123C)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenAiLab,
                            colors = ButtonDefaults.buttonColors(containerColor = AiRose),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = "Create",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = WhiteCanvas
                            )
                        }
                    }
                }
            }

            // --- NEW LOGIC & PUZZLE GAMES SECTION ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "🎮 New Logic & Puzzle Games",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Arrow Maze • Jigsaw Studio • Circuit Link",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    // 1. Arrow Escape & Snake Silhouette Puzzle Card (Matching user screenshot)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenSnakeArrow)
                            .testTag("home_arrow_maze_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🍄", fontSize = 22.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Arrow Silhouette Puzzle",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE0F2FE)
                                        ) {
                                            Text(
                                                text = "SILHOUETTES",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF0369A1),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Mushroom Silhouette • Bendy vector lines • Tap to escape unobstructed",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = onOpenSnakeArrow,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Play", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = WhiteCanvas)
                                }
                            }
                        }
                    }

                    // 2. Jigsaw Puzzle Studio Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.5.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenJigsawStudio)
                            .testTag("home_jigsaw_studio_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🧩", fontSize = 22.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Jigsaw Puzzle Studio",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = "JIGSAW",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF15803D),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Piece tray, ghost guides & rich artwork puzzles",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenJigsawStudio,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Play", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = WhiteCanvas)
                            }
                        }
                    }

                    // 3. Circuit Link Energy Flow Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                        border = BorderStroke(1.5.dp, Color(0xFFE9D5FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenCircuitLink)
                            .testTag("home_circuit_link_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF9333EA),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("⚡", fontSize = 22.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Circuit Link Flow",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF3E8FF)
                                        ) {
                                            Text(
                                                text = "NEW GAME",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF7E22CE),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Connect neon color terminals with non-crossing wires",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenCircuitLink,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Play", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = WhiteCanvas)
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Explore 7-Level Categories",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "7 Levels Each • Progressive STEM",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    OutlinedButton(
                        onClick = { onRefreshSection("7-Level Categories") },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("🔄 Refresh", style = MaterialTheme.typography.labelSmall, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // List of Subject Categories (Visual, Math, Physics, Chemistry, Biology, History)
            items(PuzzleCategory.entries.filter { it != PuzzleCategory.AI_LAB }) { category ->
                val solved = userProgress.categorySolvedCounts[category.id] ?: 0
                val total = 7
                CategoryCard(
                    category = category,
                    solvedCount = solved,
                    totalCount = total,
                    onClick = { onCategoryClick(category) }
                )
            }

            // Rating & Player Feedback Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                    border = BorderStroke(1.5.dp, Color(0xFFE9D5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenRatingFeedback)
                        .testTag("home_feedback_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF9333EA),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("⭐", fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Rate Puzzle Set & Feedback",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF581C87)
                                )
                                Text(
                                    text = "Share suggestions • Earn +50 XP & +20 Coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7E22CE)
                                )
                            }
                        }

                        Button(
                            onClick = onOpenRatingFeedback,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Rate",
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

@Composable
private fun KidPhotoCard(
    title: String,
    tag: String,
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
        border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(6.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 11.sp
            )
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF15803D),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun OfflineStatusNoticeCard(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = BorderStroke(1.5.dp, Color(0xFFFECACA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_status_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDC2626),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "⚠️", fontSize = 18.sp)
                    }
                }
                Column {
                    Text(
                        text = "You are currently offline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = "Offline Preview Mode active",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB91C1C)
                    )
                }
            }

            Text(
                text = "When offline, MindMatrix only shows a few preview sections with images. Puzzles, opening next pages, and video ads are disabled. Please turn on Wi-Fi or mobile data to unlock the full online game.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7F1D1D)
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("offline_retry_button")
            ) {
                Text(
                    text = "🔄 Check Connection & Retry",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OfflineImageGallerySection(
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
        border = BorderStroke(1.2.dp, SlateBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "🖼️ Offline Image Gallery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Preview sections (Next page disabled while offline)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            val offlineGalleryItems = listOf(
                Triple("Cosmic Mathematics", "Level 1-7 STEM Preview", R.drawable.img_math_cosmos),
                Triple("Science Laboratory", "Molecular Chemistry", R.drawable.img_science_lab),
                Triple("Deep Ocean Trench", "Marine Life Patterns", R.drawable.img_deep_ocean),
                Triple("Ancient Pyramids", "Historical Enigmas", R.drawable.img_ancient_pyramids),
                Triple("Dino Safari", "Kids Visual Puzzle", R.drawable.img_kids_dino_safari),
                Triple("Quantum Core", "Core Logic Circuits", R.drawable.img_quantum_core)
            )

            offlineGalleryItems.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { (title, subtitle, imgRes) ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateBackground),
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onImageClick)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = imgRes),
                                    contentDescription = title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                )
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFFEE2E2),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "🔒 Offline Preview",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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

@Composable
fun OfflineAdsDisabledCard(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF94A3B8),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🚫", fontSize = 20.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ads Inactive (Offline)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "Rewarded video ads, 2X coin multipliers, and hint skips are unavailable while offline. Connect to internet to earn ad bonuses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun OfflineCloudSyncCard(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF64748B),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "☁️", fontSize = 20.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Backend Server Paused",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Connecting to https://mypuzzlegame.royabhay0079.workers.dev/ requires internet. Leaderboard and quests will sync once reconnected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}
