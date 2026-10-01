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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CommunityCoopGoal
import com.example.data.model.CommunityPoll
import com.example.data.model.CommunityPost
import com.example.data.model.UserProgress
import com.example.ui.components.AppHeader
import com.example.ui.components.MindMatrixBottomBar
import com.example.ui.components.FiveTabLiquidGlassNavBar
import com.example.ui.components.MainNavTab
import com.example.ui.components.LowFadeGlassSurface
import com.example.ui.theme.AiRose
import com.example.ui.theme.BioLime
import com.example.ui.theme.ChemPurple
import com.example.ui.theme.GoldStar
import com.example.ui.theme.HistoryAmber
import com.example.ui.theme.MathBlue
import com.example.ui.theme.PhysicsGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCanvas

@Composable
fun CommunityScreen(
    userProgress: UserProgress,
    communityPosts: List<CommunityPost>,
    communityPoll: CommunityPoll,
    communityCoopGoal: CommunityCoopGoal,
    onToggleLike: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    onVotePoll: (String) -> Unit,
    onCreatePost: (String, String, String, String, String?, String?) -> Unit,
    onClaimCommunityGoal: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateTasks: () -> Unit,
    onNavigateLeaderboard: () -> Unit,
    onNavigateCommunity: () -> Unit,
    onNavigateProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Physics", "Math", "Chemistry", "Biology", "History", "Riddles")

    val filteredPosts = remember(selectedFilter, communityPosts) {
        if (selectedFilter == "All") communityPosts
        else communityPosts.filter { it.category.equals(selectedFilter, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Community Hub",
                totalXp = userProgress.totalXp,
                starsCount = userProgress.starsEarned,
                coins = userProgress.coins,
                selectedAgeGroup = userProgress.selectedAgeGroup,
                onProfileClick = onNavigateProfile
            )
        },
        bottomBar = {
            FiveTabLiquidGlassNavBar(
                currentTab = MainNavTab.COMMUNITY,
                onTabSelected = { tab ->
                    when (tab) {
                        MainNavTab.HOME -> onNavigateHome()
                        MainNavTab.ARROW_MAZE -> onNavigateHome()
                        MainNavTab.TASKS -> onNavigateTasks()
                        MainNavTab.LEADERBOARD -> onNavigateLeaderboard()
                        MainNavTab.COMMUNITY -> onNavigateCommunity()
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePostDialog = true },
                containerColor = PrimaryIndigo,
                contentColor = WhiteCanvas,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_create_community_post")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Post")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Post Riddle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        containerColor = SlateBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Worldwide Co-op Community Sprint Card
            item {
                CommunityCoopCard(
                    goal = communityCoopGoal,
                    isGoalClaimed = userProgress.communityGoalClaimed,
                    onClaimReward = onClaimCommunityGoal
                )
            }

            // 2. Weekly Scientific Paradox Poll
            item {
                CommunityPollCard(
                    poll = communityPoll,
                    onVote = onVotePoll
                )
            }

            // 3. Category Filter Chips
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Global Brain Teasers & Discussions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${filteredPosts.size} posts",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterOptions) { category ->
                            val isSelected = selectedFilter == category
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryIndigo else WhiteCanvas,
                                border = BorderStroke(1.dp, if (isSelected) PrimaryIndigo else SlateBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedFilter = category }
                                    .testTag("filter_community_$category")
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) WhiteCanvas else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Community Posts Feed
            items(filteredPosts, key = { it.id }) { post ->
                CommunityPostCard(
                    post = post,
                    onToggleLike = { onToggleLike(post.id) },
                    onAddComment = { commentText -> onAddComment(post.id, commentText) }
                )
            }

            // Spacer for FAB clearance
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

    if (showCreatePostDialog) {
        CreateCommunityPostDialog(
            onDismiss = { showCreatePostDialog = false },
            onSubmit = { cat, diff, title, content, hint, sol ->
                onCreatePost(cat, diff, title, content, hint, sol)
                showCreatePostDialog = false
            }
        )
    }
}

/**
 * Global Co-op Progress Goal Card
 */
@Composable
fun CommunityCoopCard(
    goal: CommunityCoopGoal,
    isGoalClaimed: Boolean,
    onClaimReward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (goal.currentCount.toFloat() / goal.targetCount.toFloat()).coerceIn(0f, 1f)
    val isCompleted = goal.currentCount >= goal.targetCount

    LowFadeGlassSurface(
        shape = RoundedCornerShape(20.dp),
        elevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🌍", fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${goal.contributorsCount} Global Solvers Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEEF2FF)
                ) {
                    Text(
                        text = "Week 34",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${goal.currentCount} / ${goal.targetCount} Solved",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryIndigo
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SuccessGreen,
                    trackColor = Color(0xFFF1F5F9)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reward Bounty Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Reward:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF9C3)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = "Coins", tint = GoldStar, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "+${goal.rewardCoins}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF854D0E))
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFEDD5)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Whatshot, contentDescription = "XP", tint = Color(0xFFEA580C), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "+${goal.rewardXp} XP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                        }
                    }
                }

                if (isGoalClaimed) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD1FAE5)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Claimed", tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Chest Claimed", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                } else {
                    Button(
                        onClick = onClaimReward,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isCompleted) SuccessGreen else PrimaryIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("btn_claim_community_goal")
                    ) {
                        Text(
                            text = if (isCompleted) "Claim Chest 🎁" else "Contribute 🚀",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WhiteCanvas
                        )
                    }
                }
            }
        }
    }
}

/**
 * Community Scientific Poll Card
 */
@Composable
fun CommunityPollCard(
    poll: CommunityPoll,
    onVote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasVoted = poll.userVotedOptionId != null

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
        border = BorderStroke(1.2.dp, SlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Poll, contentDescription = "Poll", tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weekly Paradox Poll",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "${poll.totalVotes} Votes",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = poll.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                poll.options.forEach { option ->
                    val isSelectedByMe = poll.userVotedOptionId == option.id
                    val percent = if (poll.totalVotes > 0) (option.votes.toFloat() / poll.totalVotes.toFloat()) else 0f

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelectedByMe) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (isSelectedByMe) PrimaryIndigo else SlateBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !hasVoted) { onVote(option.id) }
                            .testTag("poll_option_${option.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = option.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelectedByMe) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelectedByMe) PrimaryIndigo else TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (hasVoted) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${(percent * 100).toInt()}% (${option.votes})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedByMe) PrimaryIndigo else TextSecondary
                                    )
                                }
                            }

                            if (hasVoted) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { percent },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (isSelectedByMe) PrimaryIndigo else Color(0xFF94A3B8),
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Community Riddle & Discussion Post Card
 */
@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onToggleLike: () -> Unit,
    onAddComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showHint by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }

    val categoryColor = when (post.category.lowercase()) {
        "physics" -> PhysicsGreen
        "math" -> MathBlue
        "chemistry" -> ChemPurple
        "biology" -> BioLime
        "history" -> HistoryAmber
        else -> PrimaryIndigo
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
        border = BorderStroke(1.2.dp, SlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("community_post_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(post.authorAvatarEmoji, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = post.countryEmoji, fontSize = 14.sp)
                        }
                        Text(
                            text = "${post.authorBadge} • ${post.timestamp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                // Category & Difficulty Pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = post.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFEF2F2)
                    ) {
                        Text(
                            text = post.difficulty,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AiRose,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            // Hint Drawer
            if (post.hint != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEFCE8),
                    border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showHint = !showHint }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color(0xFFCA8A04), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showHint) "Hint Revealed" else "Tap for Hint 💡",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF854D0E)
                                )
                            }
                            Text(
                                text = if (showHint) "Hide" else "Show",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA16207)
                            )
                        }
                        if (showHint) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = post.hint,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF713F12)
                            )
                        }
                    }
                }
            }

            // Solution Drawer
            if (post.solution != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSolution = !showSolution }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Solution", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showSolution) "Scientific Explanation" else "Reveal Answer & Explanation 🔓",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                            }
                            Text(
                                text = if (showSolution) "Hide" else "Show",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF047857)
                            )
                        }
                        if (showSolution) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = post.solution,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF064E3B),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SlateBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Likes & Comments
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleLike)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) PrimaryIndigo else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount} Likes",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (post.isLikedByMe) FontWeight.Bold else FontWeight.Medium,
                        color = if (post.isLikedByMe) PrimaryIndigo else TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showComments = !showComments }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.comments.size} Comments",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Expanded Comments Section
            if (showComments) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Discussion & Reasoning (${post.comments.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    post.comments.forEach { comment ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = WhiteCanvas,
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(text = comment.authorAvatarEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = comment.authorName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = comment.countryEmoji, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "• ${comment.timestamp}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = comment.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Comment Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            placeholder = { Text("Share your solution or thought...", style = MaterialTheme.typography.bodySmall) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = WhiteCanvas,
                                unfocusedContainerColor = WhiteCanvas,
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = SlateBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentInput.isNotBlank()) {
                                    onAddComment(commentInput.trim())
                                    commentInput = ""
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryIndigo,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = WhiteCanvas, modifier = Modifier.size(16.dp))
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
 * Create Post / Share Riddle Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCommunityPostDialog(
    onDismiss: () -> Unit,
    onSubmit: (category: String, difficulty: String, title: String, content: String, hint: String?, solution: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Physics") }
    var selectedDifficulty by remember { mutableStateOf("Normal") }

    val categories = listOf("Physics", "Math", "Chemistry", "Biology", "History", "Riddles")
    val difficulties = listOf("Normal", "Hard", "Grandmaster")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Post a Riddle / Problem",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Subject", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(categories) { cat ->
                                val isSel = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) PrimaryIndigo else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSel) WhiteCanvas else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Problem Title") },
                    placeholder = { Text("e.g. The Quantum Mirror Paradox") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Riddle / Problem Description
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Riddle / Paradox Content") },
                    placeholder = { Text("Describe the puzzle or challenge...") },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional Hint
                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Optional Hint") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional Solution
                OutlinedTextField(
                    value = solution,
                    onValueChange = { solution = it },
                    label = { Text("Optional Solution & Scientific Fact") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSubmit(
                            selectedCategory,
                            selectedDifficulty,
                            title.trim(),
                            content.trim(),
                            hint.trim().ifBlank { null },
                            solution.trim().ifBlank { null }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Publish Riddle 🚀", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
