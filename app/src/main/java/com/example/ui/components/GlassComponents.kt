package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.WhiteCanvas

/**
 * Premium Low-Fade Liquid Glass Container / Card
 * Renders an ultra-smooth translucent frosted glass aesthetic with:
 * - Ambient gradient background with soft white specular highlight
 * - Multi-stop glass edge border
 * - Gentle ambient shadow elevation
 */
@Composable
fun LowFadeGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = Color.Transparent,
        shadowElevation = elevation,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WhiteCanvas.copy(alpha = 0.92f),
                            Color(0xFFF8FAFC).copy(alpha = 0.85f),
                            WhiteCanvas.copy(alpha = 0.78f)
                        )
                    ),
                    shape = shape
                )
                .border(
                    BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WhiteCanvas.copy(alpha = 0.95f),
                                Color(0xFFE2E8F0).copy(alpha = 0.65f),
                                Color(0xFFCBD5E1).copy(alpha = 0.40f)
                            )
                        )
                    ),
                    shape = shape
                )
        ) {
            content()
        }
    }
}

/**
 * Five-Tab Low Liquid Glass Bottom Navigation Bar
 * Tabs:
 * 1. Home (STEM Arena)
 * 2. Maze (Arrow Escape Labyrinth)
 * 3. Quests (Tasks & Daily Challenges)
 * 4. Ranks (Global Leaderboard)
 * 5. Squad (Community & Co-op)
 */
enum class MainNavTab(val title: String, val icon: ImageVector, val badge: String? = null) {
    HOME("Home", Icons.Default.Home),
    ARROW_MAZE("Arrow", Icons.Default.Navigation),
    TASKS("Quests", Icons.Default.TaskAlt),
    LEADERBOARD("Ranks", Icons.Default.EmojiEvents),
    COMMUNITY("Squad", Icons.Default.Groups)
}

@Composable
fun FiveTabLiquidGlassNavBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    taskBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating liquid glass capsule
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent,
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WhiteCanvas.copy(alpha = 0.94f),
                                Color(0xFFF1F5F9).copy(alpha = 0.90f),
                                WhiteCanvas.copy(alpha = 0.88f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        BorderStroke(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    WhiteCanvas,
                                    Color(0xFFE2E8F0).copy(alpha = 0.8f),
                                    PrimaryIndigo.copy(alpha = 0.25f)
                                )
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainNavTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        LiquidGlassNavTabItem(
                            tab = tab,
                            isSelected = isSelected,
                            badgeCount = if (tab == MainNavTab.TASKS) taskBadgeCount else 0,
                            onClick = { onTabSelected(tab) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassNavTabItem(
    tab: MainNavTab,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tab_scale"
    )

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "tab_bg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) PrimaryIndigo else Color(0xFF64748B),
        animationSpec = tween(durationMillis = 200),
        label = "tab_tint"
    )

    Box(
        modifier = Modifier
            .scale(scaleAnim)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("nav_tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        // Liquid glass pill indicator when selected
        if (backgroundAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryIndigo.copy(alpha = 0.14f * backgroundAlpha),
                                Color(0xFF6366F1).copy(alpha = 0.18f * backgroundAlpha),
                                PrimaryIndigo.copy(alpha = 0.14f * backgroundAlpha)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            PrimaryIndigo.copy(alpha = 0.35f * backgroundAlpha)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )

                // Optional badge
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 6.dp, y = (-4).dp)
                            .size(16.dp)
                            .background(Color(0xFFEF4444), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$badgeCount",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhiteCanvas
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = tab.title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) PrimaryIndigo else Color(0xFF64748B)
            )
        }
    }
}
