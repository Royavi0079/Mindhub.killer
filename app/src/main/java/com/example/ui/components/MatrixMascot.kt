package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.HapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * A beautiful, hardware-accelerated 2D animated mascot character ("Matrix Bot").
 * Built entirely with vector draw operations on a Canvas for optimal performance.
 * Supports:
 * - Smooth idle floating & breathing (InfiniteTransition)
 * - Dynamic periodic blinking eyes
 * - Tap-to-jump tactile animation with sound/haptic feedback
 * - Floating sparkles & reaction bubbles
 */
@Composable
fun MatrixMascot(
    modifier: Modifier = Modifier,
    isKidsMode: Boolean = false,
    onSpeechBubbleText: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Jump & Spin trigger state
    var isJumping by remember { mutableStateOf(false) }
    var sparkCount by remember { mutableIntStateOf(0) }

    // Jumping offset animation
    val jumpOffset by animateFloatAsState(
        targetValue = if (isJumping) -48f else 0f,
        animationSpec = if (isJumping) {
            spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "jump_offset"
    )

    // Spin/Scale animation during jump
    val mascotScale by animateFloatAsState(
        targetValue = if (isJumping) 1.15f else 1.0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "mascot_scale"
    )

    // Infinite breathing float
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_breathing")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    // Eye blinking trigger
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((3000..6000).random().toLong())
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }

    // Interactive Tap handler
    fun triggerJumpCheer() {
        if (!isJumping) {
            HapticManager.performTileSlide(context)
            isJumping = true
            sparkCount = (sparkCount + 1) % 5
            coroutineScope.launch {
                delay(400)
                isJumping = false
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Speech Bubble left/above
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isKidsMode) Color(0xFFF0FDF4) else Color(0xFFEFF6FF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .widthIn(max = 200.dp)
                .padding(end = 12.dp)
        ) {
            Text(
                text = onSpeechBubbleText ?: if (isKidsMode) {
                    listOf(
                        "Dino power! Tap me to jump! 🦖",
                        "You are doing awesome! 🌟",
                        "Let's solve more puzzles! 🎨",
                        "Super job, young adventurer! 🏆"
                    ).random()
                } else {
                    listOf(
                        "Ready to test your focus? 🧠",
                        "Try a customized app background in profile! 🎨",
                        "Double your rewards with a quick video! 🪙",
                        "Let's climb the leaderboard! ⚡"
                    ).random()
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isKidsMode) Color(0xFF16A34A) else Color(0xFF2563EB),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        // Mascot 2D Drawing Container
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { triggerJumpCheer() }
                )
                .testTag("animated_mascot_avatar"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw Floor Shadow under mascot
                val shadowWidth = 50f - (jumpOffset / 3f)
                val shadowAlpha = (0.25f + (jumpOffset / 120f)).coerceIn(0.05f, 0.35f)
                drawOval(
                    color = Color.Black.copy(alpha = shadowAlpha),
                    topLeft = Offset(canvasWidth / 2f - shadowWidth / 2f, canvasHeight - 16f),
                    size = Size(shadowWidth, 8f)
                )

                // Mascot Y Position offset (floating breathing + jump hop)
                val activeY = (canvasHeight / 2f - 40f) + floatOffset + jumpOffset

                // Mascot Body Gradient Colors
                val primaryColor = if (isKidsMode) Color(0xFF10B981) else Color(0xFF4F46E5)
                val accentColor = if (isKidsMode) Color(0xFF34D399) else Color(0xFF818CF8)
                val glowOutline = if (isKidsMode) Color(0xFFA7F3D0) else Color(0xFFC7D2FE)

                // Draw cute round body capsule
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor, primaryColor)
                    ),
                    topLeft = Offset(canvasWidth / 2f - 30f, activeY),
                    size = Size(60f, 68f) * mascotScale,
                    cornerRadius = CornerRadius(24f, 24f)
                )

                // Draw glowing outer border
                drawRoundRect(
                    color = glowOutline.copy(alpha = 0.8f),
                    topLeft = Offset(canvasWidth / 2f - 30f, activeY),
                    size = Size(60f, 68f) * mascotScale,
                    cornerRadius = CornerRadius(24f, 24f),
                    style = Stroke(width = 3.5f)
                )

                // Draw digital eye screen plate (black faceplate)
                drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(canvasWidth / 2f - 22f, activeY + 14f),
                    size = Size(44f, 26f) * mascotScale,
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Draw expressive cyan glowing digital eyes
                val eyeWidth = 8f
                val eyeHeight = if (isBlinking) 2f else 12f
                val leftEyeX = canvasWidth / 2f - 13f
                val rightEyeX = canvasWidth / 2f + 5f
                val eyeY = activeY + 22f + (if (isBlinking) 5f else 0f)

                // Left Eye
                drawRoundRect(
                    color = Color(0xFF22D3EE), // Cyan glow
                    topLeft = Offset(leftEyeX, eyeY),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Right Eye
                drawRoundRect(
                    color = Color(0xFF22D3EE),
                    topLeft = Offset(rightEyeX, eyeY),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Draw friendly digital smile/beeping indicator
                if (!isBlinking) {
                    drawArc(
                        color = Color(0xFF22D3EE),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(canvasWidth / 2f - 4f, activeY + 26f),
                        size = Size(8f, 6f),
                        style = Stroke(width = 1.5f)
                    )
                }

                // Draw cute little blushing cheeks
                drawCircle(
                    color = Color(0xFFF43F5E).copy(alpha = 0.6f),
                    center = Offset(canvasWidth / 2f - 18f, activeY + 28f),
                    radius = 3f
                )
                drawCircle(
                    color = Color(0xFFF43F5E).copy(alpha = 0.6f),
                    center = Offset(canvasWidth / 2f + 18f, activeY + 28f),
                    radius = 3f
                )

                // Draw sparkles if mascot has jumped recently
                if (isJumping) {
                    drawCircle(
                        color = Color(0xFFFDE047),
                        center = Offset(canvasWidth / 2f - 35f, activeY - 10f),
                        radius = 4f
                    )
                    drawCircle(
                        color = Color(0xFFFDE047),
                        center = Offset(canvasWidth / 2f + 35f, activeY - 5f),
                        radius = 5f
                    )
                    drawCircle(
                        color = Color(0xFF22D3EE),
                        center = Offset(canvasWidth / 2f, activeY - 20f),
                        radius = 3f
                    )
                }
            }
        }
    }
}
