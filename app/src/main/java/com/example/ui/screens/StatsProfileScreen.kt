package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementBadge
import com.example.data.model.AchievementCategory
import com.example.data.model.AgeGroup
import com.example.data.model.AvatarBorderTier
import com.example.data.model.BadgeTier
import com.example.data.model.ProfileBackgroundTheme
import com.example.data.model.PuzzleCategory
import com.example.data.model.UserProgress
import com.example.data.remote.ServerLogEntry
import com.example.data.remote.ServerSyncManager
import kotlinx.coroutines.launch
import com.example.data.repository.AchievementCatalog
import com.example.ui.components.AchievementBadgeCard
import com.example.ui.components.AchievementDetailDialog
import com.example.ui.components.AgeGroupSelector
import com.example.ui.components.AppHeader
import com.example.ui.components.LoginDialog
import com.example.ui.components.LogoutConfirmDialog
import com.example.ui.components.LowFadeGlassSurface
import com.example.ui.components.ServerLogsDialog
import com.example.ui.theme.GoldStar
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCanvas
import com.example.ui.theme.AppThemeUtil
import com.example.ui.theme.isDark


@Composable
fun StatsProfileScreen(
    userProgress: UserProgress,
    onAgeSelected: (AgeGroup) -> Unit,
    onToggleLargeText: (Boolean) -> Unit,
    onToggleTileNumbers: (Boolean) -> Unit,
    onToggleSoundEffects: (Boolean) -> Unit = {},
    onLogin: (email: String, username: String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {},
    onPingServer: () -> Unit = {},
    onSyncState: () -> Unit = {},
    onClearLogs: () -> Unit = {},
    serverLogs: List<ServerLogEntry> = emptyList(),
    isServerOnline: Boolean? = true,
    lastLatencyMs: Long = 0L,
    onUpdateProfile: (
        name: String,
        handle: String,
        avatarEmoji: String,
        avatarStyle: String,
        avatarColorHex: Long,
        profileBackground: String,
        avatarBorder: String,
        title: String,
        bio: String,
        countryEmoji: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onOpenAuth: () -> Unit = {},
    onOpenRatingFeedback: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateTasks: () -> Unit = {},
    onNavigateLeaderboard: () -> Unit = {},
    onNavigateCommunity: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val evaluatedBadges = remember(userProgress) {
        AchievementCatalog.evaluateBadges(userProgress)
    }

    var selectedCategoryFilter by remember { mutableStateOf(AchievementCategory.ALL) }
    var visibleLimit by remember(selectedCategoryFilter) { mutableStateOf(5) }
    var isGeneratingMoreBadges by remember { mutableStateOf(false) }
    var inspectingBadge by remember { mutableStateOf<AchievementBadge?>(null) }
    var isEditingProfile by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    var fcmToken by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf(userProgress.userEmail) }
    var isSendingEmail by remember { mutableStateOf(false) }
    var emailSendResult by remember { mutableStateOf<String?>(null) }
    var showSendEmailStatusDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        fcmToken = com.example.data.remote.MyFirebaseMessagingService.getSavedToken(context)
        if (fcmToken.isEmpty()) {
            kotlinx.coroutines.delay(1000)
            fcmToken = com.example.data.remote.MyFirebaseMessagingService.getSavedToken(context)
        }
    }

    val themeColors = remember(userProgress.userProfileBackground) {
        AppThemeUtil.getAppColors(userProgress.userProfileBackground)
    }
    val appBgColor = themeColors.first
    val appSectionColor = themeColors.second

    val activeBgTheme = remember(userProgress.userProfileBackground) {
        try {
            ProfileBackgroundTheme.valueOf(userProgress.userProfileBackground)
        } catch (e: Exception) {
            ProfileBackgroundTheme.COSMIC_NEBULA
        }
    }

    val activeBorderTier = remember(userProgress.userAvatarBorder) {
        try {
            AvatarBorderTier.valueOf(userProgress.userAvatarBorder)
        } catch (e: Exception) {
            AvatarBorderTier.NEON_CYAN
        }
    }

    val filteredBadges = remember(evaluatedBadges, selectedCategoryFilter) {
        if (selectedCategoryFilter == AchievementCategory.ALL) {
            evaluatedBadges
        } else {
            evaluatedBadges.filter { it.category == selectedCategoryFilter }
        }
    }

    val badgesToShow = remember(filteredBadges, visibleLimit) {
        filteredBadges.take(visibleLimit)
    }

    val unlockedCount = evaluatedBadges.count { it.isUnlocked }
    val totalBadgesCount = evaluatedBadges.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(appBgColor)
    ) {
        AppHeader(
            title = "Profile & Trophies",
            totalXp = userProgress.totalXp,
            starsCount = userProgress.starsEarned,
            coins = userProgress.coins,
            selectedAgeGroup = userProgress.selectedAgeGroup,
            onBackClick = onBackClick,
            onProfileClick = {}
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ==========================================
                // 1. DYNAMIC PROFILE CARD & CUSTOM THEME BANNER
                // ==========================================
            item {
                LowFadeGlassSurface(
                    shape = RoundedCornerShape(22.dp),
                    elevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Custom Theme Background Banner Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(activeBgTheme.primaryColorHex),
                                            Color(activeBgTheme.secondaryColorHex),
                                            Color(activeBgTheme.accentColorHex).copy(alpha = 0.85f)
                                        )
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            // Theme Pill Top Left
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.35f),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = activeBgTheme.emoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = activeBgTheme.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Edit Profile Button Top Right
                            Button(
                                onClick = { isEditingProfile = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.92f),
                                    contentColor = Color(0xFF0F172A)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .testTag("btn_edit_profile")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Customize",
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Customize",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Avatar & Profile Details Overlay
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Customized Avatar Photo with Glowing Border Tier
                                Surface(
                                    shape = CircleShape,
                                    color = Color(userProgress.userAvatarColorHex),
                                    border = BorderStroke(3.5.dp, Color(activeBorderTier.colorHex)),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clickable { isEditingProfile = true }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = userProgress.userAvatarEmoji,
                                            fontSize = 32.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = userProgress.userName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = userProgress.countryEmoji, fontSize = 16.sp)
                                    }
                                    Text(
                                        text = "@${userProgress.userHandle}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryIndigo
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = BorderStroke(0.6.dp, Color(0xFFFCD34D))
                                        ) {
                                            Text(
                                                text = userProgress.userTitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309),
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${userProgress.selectedAgeGroup.title}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            if (userProgress.userBio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "\"${userProgress.userBio}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats Grid (Total XP, Coins, Solved, Stars)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatBox(
                                    label = "Total XP",
                                    value = "${userProgress.totalXp}",
                                    icon = Icons.Default.Whatshot,
                                    iconColor = Color(0xFFEA580C),
                                    modifier = Modifier.weight(1f)
                                )
                                StatBox(
                                    label = "Coins",
                                    value = "${userProgress.coins}",
                                    icon = Icons.Default.MonetizationOn,
                                    iconColor = Color(0xFFD97706),
                                    modifier = Modifier.weight(1f)
                                )
                                StatBox(
                                    label = "Solved",
                                    value = "${userProgress.totalPuzzlesSolved}",
                                    icon = Icons.Default.EmojiEvents,
                                    iconColor = PrimaryIndigo,
                                    modifier = Modifier.weight(1f)
                                )
                                StatBox(
                                    label = "Stars",
                                    value = "${userProgress.starsEarned}",
                                    icon = Icons.Default.Star,
                                    iconColor = GoldStar,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. ACHIEVEMENT TROPHY HALL
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.2.dp, PrimaryIndigo.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🏆", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Achievement Trophy Hall",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Milestones & Category Badges",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Unlocked Count Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFCD34D))
                            ) {
                                Text(
                                    text = "$unlockedCount / $totalBadgesCount",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tier Summary Badges Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(BadgeTier.entries) { tier ->
                                val tierCount = evaluatedBadges.count { it.tier == tier && it.isUnlocked }
                                val totalInTier = evaluatedBadges.count { it.tier == tier }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(tier.bgHex),
                                    border = BorderStroke(0.8.dp, Color(tier.colorHex).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = tier.iconEmoji, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$tierCount/$totalInTier",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(tier.colorHex),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AchievementCategory.entries) { cat ->
                                val isSelected = selectedCategoryFilter == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = {
                                        Text(
                                            text = "${cat.iconEmoji} ${cat.label}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryIndigo,
                                        selectedLabelColor = WhiteCanvas,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = TextSecondary
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryIndigo else SlateBorder
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Badges List
            items(badgesToShow, key = { it.id }) { badge ->
                AchievementBadgeCard(
                    badge = badge,
                    onClick = { inspectingBadge = badge }
                )
            }

            if (filteredBadges.size > visibleLimit) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                        border = BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isGeneratingMoreBadges) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = PrimaryIndigo,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Revealing accomplishments...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Text(
                                    text = "Showing ${badgesToShow.size} of ${filteredBadges.size} badges",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Button(
                                    onClick = {
                                        isGeneratingMoreBadges = true
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(800)
                                            visibleLimit += 10
                                            isGeneratingMoreBadges = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Show More Achievements (+10)",
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

            // Age Bracket Switcher (5 to 70 years)
            item {
                AgeGroupSelector(
                    selectedAgeGroup = userProgress.selectedAgeGroup,
                    onAgeSelected = onAgeSelected
                )
            }

            // ==========================================
            // 3. SUBJECT MASTERY & 1000+ LEVEL PROGRESS
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Subject Mastery (1000+ Levels)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEEF2FF),
                                border = BorderStroke(0.8.dp, PrimaryIndigo.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "1000+ Per Category",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        PuzzleCategory.entries.filter { it != PuzzleCategory.AI_LAB }.forEach { cat ->
                            val solved = userProgress.categorySolvedCounts[cat.id] ?: 0
                            val accentColor = Color(cat.accentColorHex)
                            val progressFloat = (solved.toFloat() / 50f).coerceIn(0f, 1f)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = cat.iconEmoji, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = cat.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "$solved Solved",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progressFloat },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = accentColor,
                                    trackColor = Color(0xFFF1F5F9)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. ACCESSIBILITY & AUDIO SETTINGS
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Game & Accessibility Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Text Mode Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = "Large Text",
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "High Contrast & Large Text",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Increases font sizes across questions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = userProgress.largeTextMode,
                                onCheckedChange = onToggleLargeText,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = WhiteCanvas,
                                    checkedTrackColor = PrimaryIndigo
                                )
                            )
                        }

                        // Tile Numbers Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Numbers,
                                    contentDescription = "Tile Numbers",
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Show Sliding Tile Numbers",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Displays numbers on image pieces",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = userProgress.showTileNumbers,
                                onCheckedChange = onToggleTileNumbers,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = WhiteCanvas,
                                    checkedTrackColor = PrimaryIndigo
                                )
                            )
                        }

                        // Sound Effects Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Sound Effects",
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Haptic & Audio Chimes",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Sound feedback on puzzle moves",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Switch(
                                checked = userProgress.soundEffectsEnabled,
                                onCheckedChange = onToggleSoundEffects,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = WhiteCanvas,
                                    checkedTrackColor = PrimaryIndigo
                                )
                            )
                        }
                    }
                }
            }

            item {
                val backgroundPalette = listOf(
                    Pair("Light Gray", 0xFFF8FAFC),
                    Pair("Soft White", 0xFFFFFFFF),
                    Pair("Sky Blue", 0xFFE0F2FE),
                    Pair("Sage Green", 0xFFDCFCE7),
                    Pair("Lavender", 0xFFF3E8FF),
                    Pair("Warm Amber", 0xFFFEF3C7),
                    Pair("Soft Rose", 0xFFFFE4E6),
                    Pair("Midnight", 0xFF0F172A),
                    Pair("Deep Indigo", 0xFF1E1B4B),
                    Pair("Charcoal", 0xFF121212)
                )

                val sectionPalette = listOf(
                    Pair("Pure White", 0xFFFFFFFF),
                    Pair("Cool Gray", 0xFFF1F5F9),
                    Pair("Sky Card", 0xFFF0F9FF),
                    Pair("Mint Card", 0xFFF0FDF4),
                    Pair("Lila Card", 0xFFFAF5FF),
                    Pair("Gold Card", 0xFFFFFBEB),
                    Pair("Dark Slate", 0xFF1E293B),
                    Pair("Indigo Card", 0xFF312E81),
                    Pair("Deep Plum", 0xFF4A044E),
                    Pair("Charcoal Panel", 0xFF1E1E1E)
                )

                val currentThemeString = userProgress.userProfileBackground
                val currentBgHex = if (currentThemeString.startsWith("CUSTOM_THEME:")) {
                    try { currentThemeString.split(":")[1].toLong() } catch(e: Exception) { 0xFFF8FAFC }
                } else {
                    when (currentThemeString) {
                        "COSMIC_NEBULA" -> 0xFF1E1B4B
                        "CYBERPUNK" -> 0xFF0F172A
                        "GOLDEN_AURORA" -> 0xFF451A03
                        "EMERALD_MATRIX" -> 0xFF064E3B
                        "OCEANIC_ABYSS" -> 0xFF082F49
                        "SOLAR_FLARE" -> 0xFF4C0519
                        "MINIMAL_SLATE" -> 0xFF1E293B
                        else -> 0xFFF8FAFC
                    }
                }
                val currentSecHex = if (currentThemeString.startsWith("CUSTOM_THEME:")) {
                    try { currentThemeString.split(":")[2].toLong() } catch(e: Exception) { 0xFFFFFFFF }
                } else {
                    when (currentThemeString) {
                        "COSMIC_NEBULA" -> 0xFF312E81
                        "CYBERPUNK" -> 0xFF1E293B
                        "GOLDEN_AURORA" -> 0xFF78350F
                        "EMERALD_MATRIX" -> 0xFF065F46
                        "OCEANIC_ABYSS" -> 0xFF0C4A6E
                        "SOLAR_FLARE" -> 0xFF881337
                        "MINIMAL_SLATE" -> 0xFF334155
                        else -> 0xFFFFFFFF
                    }
                }

                val titleTextColor = if (Color(currentBgHex).isDark()) Color.White else Color(0xFF0F172A)
                val bodyTextColor = if (Color(currentBgHex).isDark()) Color.White.copy(alpha = 0.7f) else Color(0xFF475569)

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(currentSecHex)),
                    border = BorderStroke(1.5.dp, Color(currentBgHex).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎨 Custom Screen Background Color",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleTextColor
                        )
                        Text(
                            text = "Select a custom background color to apply to all app pages",
                            style = MaterialTheme.typography.bodySmall,
                            color = bodyTextColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(backgroundPalette) { (_, bgHex) ->
                                val isSelected = currentBgHex == bgHex
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(bgHex))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF4F46E5) else Color.Gray.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            val newThemeString = "CUSTOM_THEME:${bgHex}:${currentSecHex}"
                                            onUpdateProfile(
                                                userProgress.userName,
                                                userProgress.userHandle,
                                                userProgress.userAvatarEmoji,
                                                userProgress.userAvatarStyle,
                                                userProgress.userAvatarColorHex,
                                                newThemeString,
                                                userProgress.userAvatarBorder,
                                                userProgress.userTitle,
                                                userProgress.userBio,
                                                userProgress.countryEmoji
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Text(
                                            text = "✓",
                                            color = if (Color(bgHex).isDark()) Color.White else Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "🧱 Custom Section & Card Color",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleTextColor
                        )
                        Text(
                            text = "Select a custom card panel color to apply to all cards & lists",
                            style = MaterialTheme.typography.bodySmall,
                            color = bodyTextColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sectionPalette) { (_, secHex) ->
                                val isSelected = currentSecHex == secHex
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(secHex))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF4F46E5) else Color.Gray.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            val newThemeString = "CUSTOM_THEME:${currentBgHex}:${secHex}"
                                            onUpdateProfile(
                                                userProgress.userName,
                                                userProgress.userHandle,
                                                userProgress.userAvatarEmoji,
                                                userProgress.userAvatarStyle,
                                                userProgress.userAvatarColorHex,
                                                newThemeString,
                                                userProgress.userAvatarBorder,
                                                userProgress.userTitle,
                                                userProgress.userBio,
                                                userProgress.countryEmoji
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Text(
                                            text = "✓",
                                            color = if (Color(secHex).isDark()) Color.White else Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }


            // ==========================================
            // 5. CLOUD SERVER TELEMETRY & ACCOUNT CARD
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.2.dp, PrimaryIndigo.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("server_cloud_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryIndigo.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Cloud Server",
                                        tint = PrimaryIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cloud Account & Sync",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Cloud Backup & Cross-Device Progress",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Connection status pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (userProgress.isLoggedIn) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    0.8.dp,
                                    if (userProgress.isLoggedIn) SuccessGreen.copy(alpha = 0.5f) else SlateBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (userProgress.isLoggedIn) SuccessGreen else Color(0xFF94A3B8))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (userProgress.isLoggedIn) "CONNECTED" else "GUEST",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (userProgress.isLoggedIn) SuccessGreen else Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Account & Auth Information Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Active Account",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Text(
                                    text = if (userProgress.isLoggedIn) userProgress.userEmail else "Guest Player (Offline)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            if (lastLatencyMs > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF0FDF4)
                                ) {
                                    Text(
                                        text = "Ping: ${lastLatencyMs}ms",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Actions Row: View Logs, Ping/Sync, Login/Logout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showLogsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_server_logs_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Logs",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Server Logs (${serverLogs.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (userProgress.isLoggedIn) {
                                OutlinedButton(
                                    onClick = { showLogoutDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                    modifier = Modifier.testTag("logout_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Log Out",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Log Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onOpenAuth,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo),
                                    border = BorderStroke(1.dp, PrimaryIndigo),
                                    modifier = Modifier.testTag("login_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = "Log In",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rating & Feedback Row
                        OutlinedButton(
                            onClick = onOpenRatingFeedback,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, Color(0xFF9333EA)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("profile_rating_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Rate Puzzles & Submit Feedback (+50 XP)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9333EA)
                                )
                            }
                        }
                    }
                }
            }

            // 7. Dedicated Account & Session Management Section (Logout)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (userProgress.isLoggedIn) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(
                        1.2.dp,
                        if (userProgress.isLoggedIn) Color(0xFFFCA5A5) else Color(0xFFCBD5E1)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_logout_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (userProgress.isLoggedIn) Color(0xFFFEE2E2) else Color(0xFFE2E8F0)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Account Status",
                                    tint = if (userProgress.isLoggedIn) Color(0xFFDC2626) else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Account & Logout",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (userProgress.isLoggedIn) "Logged in as ${userProgress.userEmail}" else "Current: Guest Session",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (userProgress.isLoggedIn) Color(0xFFB91C1C) else TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (userProgress.isLoggedIn) {
                            Button(
                                onClick = { showLogoutDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("profile_section_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Log Out",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Log Out from Profile",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onOpenAuth,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("profile_section_login_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = "Sign In",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign In", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showLogoutDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("profile_section_reset_session_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Log Out",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Log Out", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 8. Notification & Email Sync Hub
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.2.dp, SlateBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notification_sync_hub_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Sync Hub",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Notification & Email Sync Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Connect this device with your Firebase Messaging and Resend email accounts to push real-time alerts & progress reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // A. FCM PUSH NOTIFICATION SECTION
                        Text(
                            text = "📱 Firebase Cloud Messaging (FCM)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This device automatically registers with Google servers to receive push alerts. Share your FCM Token to target notifications to this phone.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // FCM Token Display Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Your FCM Device Token:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fcmToken.ifEmpty { "Generating secure FCM token on device..." },
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (fcmToken.isEmpty()) TextMuted else Color(0xFF0F172A),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (fcmToken.isNotEmpty()) {
                                        clipboardManager.setText(
                                            androidx.compose.ui.text.AnnotatedString(fcmToken)
                                        )
                                        ServerSyncManager.logAppError("FCMTokenCopy", "Token copied by user", "Info")
                                    }
                                },
                                enabled = fcmToken.isNotEmpty(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Copy Token", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            val app = com.google.firebase.FirebaseApp.getInstance()
                                            val apiKey = app.options.apiKey
                                            if (!apiKey.isNullOrBlank() && apiKey != "AIzaSyDummyKeyPlaceholder") {
                                                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            fcmToken = task.result
                                                            com.example.data.remote.MyFirebaseMessagingService.saveToken(context, fcmToken)
                                                        }
                                                    }
                                            }
                                        } catch (e: Exception) {
                                            // Safe fallback when Firebase is not configured or terms are declined
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Refresh", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        androidx.compose.material3.HorizontalDivider(color = SlateBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // B. RESEND EMAIL PROGRESS REPORT SECTION
                        Text(
                            text = "✉️ Resend Email Progress Report",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter your email address to receive a beautifully structured HTML progress report directly in your inbox.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Recipient Email", fontSize = 11.sp) },
                            singleLine = true,
                            placeholder = { Text("your@email.com") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF059669),
                                focusedLabelColor = Color(0xFF059669),
                                cursorColor = Color(0xFF059669)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (emailInput.isBlank()) return@Button
                                isSendingEmail = true
                                coroutineScope.launch {
                                val result = com.example.data.remote.ResendEmailService.sendProgressReport(
                                        toEmail = emailInput,
                                        username = userProgress.userName.ifEmpty { "MindMatrix Explorer" },
                                        coins = userProgress.coins,
                                        completedPuzzles = userProgress.totalPuzzlesSolved,
                                        achievementsCount = unlockedCount,
                                        highestScore = userProgress.totalXp
                                    )
                                    isSendingEmail = false
                                    if (result.isSuccess) {
                                        emailSendResult = "Success! Your MindMatrix progress report has been successfully dispatched via Resend API to: $emailInput."
                                        ServerSyncManager.logAppError("ResendEmail", "Report sent to $emailInput", "Info")
                                    } else {
                                        emailSendResult = "Failed to send: ${result.exceptionOrNull()?.message}"
                                        ServerSyncManager.logAppError("ResendEmail", "Failed to send email: ${result.exceptionOrNull()?.message}", "Error")
                                    }
                                    showSendEmailStatusDialog = true
                                }
                            },
                            enabled = !isSendingEmail && emailInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSendingEmail) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sending Report...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Send My Progress Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ Note: Resend Sandbox API Keys only allow sending emails to the owner's verified address.",
                            fontSize = 10.sp,
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

    // Email Send Status Dialog
    if (showSendEmailStatusDialog) {
        AlertDialog(
            onDismissRequest = { showSendEmailStatusDialog = false },
            title = {
                Text(
                    text = "Email Dispatch Status",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = emailSendResult ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showSendEmailStatusDialog = false }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Server Logs Telemetry Dialog
    if (showLogsDialog) {
        ServerLogsDialog(
            logs = serverLogs,
            isServerOnline = isServerOnline,
            lastLatencyMs = lastLatencyMs,
            onPingServer = onPingServer,
            onSyncState = onSyncState,
            onClearLogs = onClearLogs,
            onDismiss = { showLogsDialog = false }
        )
    }

    // Login Dialog
    if (showLoginDialog) {
        LoginDialog(
            currentEmail = userProgress.userEmail,
            currentUsername = userProgress.userName,
            onLoginSubmit = { email, username ->
                onLogin(email, username)
            },
            onDismiss = { showLoginDialog = false }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            userEmail = userProgress.userEmail,
            userName = userProgress.userName,
            onConfirmLogout = onLogout,
            onDismiss = { showLogoutDialog = false }
        )
    }

    // Badge Inspector Dialog
    inspectingBadge?.let { badge ->
        AchievementDetailDialog(
            badge = badge,
            onDismiss = { inspectingBadge = null }
        )
    }

    // Interactive Custom Profile Studio Dialog
    if (isEditingProfile) {
        EnhancedProfileCustomizerDialog(
            userProgress = userProgress,
            onDismiss = { isEditingProfile = false },
            onSave = { name, handle, avatar, style, colorHex, bgTheme, borderTier, title, bio, country ->
                onUpdateProfile(name, handle, avatar, style, colorHex, bgTheme, borderTier, title, bio, country)
                isEditingProfile = false
            }
        )
    }
}

/**
 * Full-featured Profile Customization Dialog with real-time live preview
 */
@Composable
private fun EnhancedProfileCustomizerDialog(
    userProgress: UserProgress,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        handle: String,
        avatarEmoji: String,
        avatarStyle: String,
        avatarColorHex: Long,
        profileBackground: String,
        avatarBorder: String,
        title: String,
        bio: String,
        countryEmoji: String
    ) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    var name by remember { mutableStateOf(userProgress.userName) }
    var handle by remember { mutableStateOf(userProgress.userHandle) }
    var selectedAvatar by remember { mutableStateOf(userProgress.userAvatarEmoji) }
    var selectedAvatarColorHex by remember { mutableStateOf(userProgress.userAvatarColorHex) }
    var selectedBgTheme by remember { mutableStateOf(userProgress.userProfileBackground) }
    var selectedBorderTier by remember { mutableStateOf(userProgress.userAvatarBorder) }
    var selectedTitle by remember { mutableStateOf(userProgress.userTitle) }
    var bio by remember { mutableStateOf(userProgress.userBio) }
    var countryEmoji by remember { mutableStateOf(userProgress.countryEmoji) }

    val currentThemeObj = remember(selectedBgTheme) {
        try { ProfileBackgroundTheme.valueOf(selectedBgTheme) } catch (e: Exception) { ProfileBackgroundTheme.COSMIC_NEBULA }
    }
    val currentBorderObj = remember(selectedBorderTier) {
        try { AvatarBorderTier.valueOf(selectedBorderTier) } catch (e: Exception) { AvatarBorderTier.NEON_CYAN }
    }

    val avatarPresets = listOf("🧠", "🦉", "🚀", "⚡", "💎", "👑", "🎯", "🌟", "🔬", "🤖", "🔥", "🦄", "🦊", "🦖", "🧙‍♂️", "🪐", "🏆", "🧬")
    val avatarBgColors = listOf(0xFF4F46E5, 0xFF0284C7, 0xFF059669, 0xFFD97706, 0xFFDC2626, 0xFF7C3AED, 0xFF0D9488, 0xFF334155)
    val titleOptions = listOf(
        "Quantum Logician",
        "Master Solver",
        "STEM Prodigy",
        "Jigsaw Architect",
        "Science Pioneer",
        "Logic Grandmaster",
        "Speed Thinker",
        "Cosmic Scholar",
        "AI Virtuoso"
    )
    val countryFlags = listOf("🌍", "🇺🇸", "🇬🇧", "🇨🇦", "🇩🇪", "🇫🇷", "🇯🇵", "🇰🇷", "🇮🇳", "🇧🇷", "🇦🇺", "🇮🇹", "🇪🇸", "🇨🇭")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎨", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Customize Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // ==========================================
                // REAL-TIME LIVE PREVIEW CARD
                // ==========================================
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                    border = BorderStroke(1.dp, SlateBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column {
                        // Banner preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(currentThemeObj.primaryColorHex),
                                            Color(currentThemeObj.secondaryColorHex),
                                            Color(currentThemeObj.accentColorHex)
                                        )
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${currentThemeObj.emoji} ${currentThemeObj.title}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }

                        // Avatar & Identity preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(selectedAvatarColorHex),
                                border = BorderStroke(2.5.dp, Color(currentBorderObj.colorHex)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = selectedAvatar, fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = name.ifBlank { "Alex Thinker" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = countryEmoji, fontSize = 14.sp)
                                }
                                Text(
                                    text = "@${handle.ifBlank { "alex_thinker" }} • $selectedTitle",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Sub-tabs for customization: 0: Backgrounds, 1: Avatar & Glow, 2: Identity
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF1F5F9),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryIndigo,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🖼️ Theme", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("✨ Avatar", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("✍️ Identity", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> { // THEME & BACKGROUNDS
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select Profile Banner Theme:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            LazyColumn(
                                modifier = Modifier.height(200.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(ProfileBackgroundTheme.entries) { theme ->
                                    val isSelected = selectedBgTheme == theme.id
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) PrimaryIndigo.copy(alpha = 0.1f) else Color(0xFFF8FAFC),
                                        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, if (isSelected) PrimaryIndigo else SlateBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedBgTheme = theme.id }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(
                                                                    Color(theme.primaryColorHex),
                                                                    Color(theme.secondaryColorHex),
                                                                    Color(theme.accentColorHex)
                                                                )
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = theme.emoji, fontSize = 16.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = theme.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        text = theme.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Text(text = "✓", color = PrimaryIndigo, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // AVATAR & GLOW RING
                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "Choose Avatar Character:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(avatarPresets) { emoji ->
                                        val isSelected = selectedAvatar == emoji
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) PrimaryIndigo.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.5.dp, if (isSelected) PrimaryIndigo else SlateBorder),
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .clickable { selectedAvatar = emoji }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = emoji, fontSize = 20.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "Avatar Background Color:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(avatarBgColors) { colorHex ->
                                        val isSelected = selectedAvatarColorHex == colorHex
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(colorHex),
                                            border = BorderStroke(2.dp, if (isSelected) Color.White else Color.Transparent),
                                            shadowElevation = if (isSelected) 3.dp else 0.dp,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .clickable { selectedAvatarColorHex = colorHex }
                                        ) {
                                            if (isSelected) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "Avatar Glow Border Tier:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(AvatarBorderTier.entries) { tier ->
                                        val isSelected = selectedBorderTier == tier.id
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color(tier.colorHex).copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.5.dp, Color(tier.colorHex)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedBorderTier = tier.id }
                                        ) {
                                            Text(
                                                text = tier.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(tier.colorHex),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // IDENTITY & DETAILS
                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Display Name") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryIndigo,
                                        focusedLabelColor = PrimaryIndigo
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_profile_name")
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = handle,
                                    onValueChange = { handle = it.replace(" ", "_").lowercase() },
                                    label = { Text("Unique Handle (@username)") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryIndigo,
                                        focusedLabelColor = PrimaryIndigo
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_profile_handle")
                                )
                            }

                            item {
                                Text(
                                    text = "Honorary Title:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(titleOptions) { title ->
                                        val isSelected = selectedTitle == title
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) PrimaryIndigo else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, if (isSelected) PrimaryIndigo else SlateBorder),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedTitle = title }
                                        ) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) WhiteCanvas else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "Country Flag:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(countryFlags) { flag ->
                                        val isSelected = countryEmoji == flag
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) PrimaryIndigo.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
                                            border = BorderStroke(1.2.dp, if (isSelected) PrimaryIndigo else SlateBorder),
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .clickable { countryEmoji = flag }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = flag, fontSize = 18.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = bio,
                                    onValueChange = { bio = it },
                                    label = { Text("Bio / Scholar Quote") },
                                    maxLines = 2,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryIndigo,
                                        focusedLabelColor = PrimaryIndigo
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_profile_bio")
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name.trim().ifBlank { "Alex Thinker" },
                        handle.trim().ifBlank { "alex_thinker" },
                        selectedAvatar,
                        "EMOJI",
                        selectedAvatarColorHex,
                        selectedBgTheme,
                        selectedBorderTier,
                        selectedTitle,
                        bio.trim(),
                        countryEmoji
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_profile")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, SlateBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
