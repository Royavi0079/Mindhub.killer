package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldStar
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Data structure for simulating floating and bursting celebration confetti particles.
 */
private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val particleType: ParticleType,
    val wobbleSpeed: Float,
    val wobbleAmplitude: Float
)

private enum class ParticleType {
    RECTANGLE,
    CIRCLE,
    STAR,
    DIAMOND
}

/**
 * Full-screen Celebratory Lottie-style Compose transition & Confetti celebration overlay.
 * Triggers upon solving any puzzle or answering a challenge correctly.
 */
@Composable
fun CorrectAnswerCelebrationOverlay(
    xpEarned: Int = 100,
    coinsEarned: Int = 50,
    starsCount: Int = 3,
    titleText: String = "Correct Answer! 🎉",
    subtitleText: String = "Brilliant thinking! Brain mastery points awarded.",
    is2xClaimed: Boolean = false,
    onClaim2xReward: (() -> Unit)? = null,
    onWatchLevelBonusVideo: (() -> Unit)? = null,
    onRateFeedback: (() -> Unit)? = null,
    onDismissOrContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration_infinite")
    val sunburstRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Animation states
    val progress = remember { Animatable(0f) }
    var star1Visible by remember { mutableStateOf(false) }
    var star2Visible by remember { mutableStateOf(false) }
    var star3Visible by remember { mutableStateOf(false) }

    val animatedXp by animateIntAsState(
        targetValue = if (progress.value > 0.4f) xpEarned else 0,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "animated_xp"
    )

    val animatedCoins by animateIntAsState(
        targetValue = if (progress.value > 0.5f) coinsEarned else 0,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "animated_coins"
    )

    // Generate crisp, lightweight confetti particles (28 particles for optimal 60/120fps performance)
    val particles = remember {
        val colors = listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber Gold
            Color(0xFFEC4899), // Rose Pink
            Color(0xFF06B6D4), // Cyan
            Color(0xFF8B5CF6)  // Purple
        )
        val rnd = Random(42)
        List(28) {
            val angle = rnd.nextFloat() * 2f * PI.toFloat()
            val speed = rnd.nextFloat() * 500f + 250f
            ConfettiParticle(
                initialX = 0.5f + (rnd.nextFloat() - 0.5f) * 0.2f,
                initialY = 0.45f + (rnd.nextFloat() - 0.5f) * 0.1f,
                velocityX = cos(angle) * speed,
                velocityY = sin(angle) * speed - 150f,
                color = colors[rnd.nextInt(colors.size)],
                size = rnd.nextFloat() * 10f + 8f,
                rotationSpeed = (rnd.nextFloat() - 0.5f) * 450f,
                particleType = if (rnd.nextBoolean()) ParticleType.RECTANGLE else ParticleType.CIRCLE,
                wobbleSpeed = rnd.nextFloat() * 6f + 3f,
                wobbleAmplitude = rnd.nextFloat() * 12f + 4f
            )
        }
    }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
            )
        }
        delay(300)
        star1Visible = true
        delay(200)
        star2Visible = true
        delay(200)
        star3Visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismissOrContinue
            )
            .testTag("celebration_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Confetti Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = progress.value
            val canvasW = size.width
            val canvasH = size.height

            particles.forEach { p ->
                val gravity = 980f * t * t * 0.5f
                val currentX = (p.initialX * canvasW) + (p.velocityX * t * 0.6f) + (sin(t * p.wobbleSpeed) * p.wobbleAmplitude)
                val currentY = (p.initialY * canvasH) + (p.velocityY * t * 0.6f) + gravity
                val alpha = (1f - (t * 0.85f)).coerceIn(0f, 1f)
                val currentRotation = p.rotationSpeed * t

                if (currentY in 0f..canvasH && currentX in 0f..canvasW) {
                    rotate(degrees = currentRotation, pivot = Offset(currentX, currentY)) {
                        when (p.particleType) {
                            ParticleType.RECTANGLE -> {
                                drawRoundRect(
                                    color = p.color.copy(alpha = alpha),
                                    topLeft = Offset(currentX - p.size / 2, currentY - p.size / 4),
                                    size = Size(p.size, p.size / 2),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                            ParticleType.CIRCLE -> {
                                drawCircle(
                                    color = p.color.copy(alpha = alpha),
                                    radius = p.size / 2.5f,
                                    center = Offset(currentX, currentY)
                                )
                            }
                            ParticleType.STAR -> {
                                drawStarParticle(
                                    center = Offset(currentX, currentY),
                                    radius = p.size / 1.8f,
                                    color = p.color.copy(alpha = alpha)
                                )
                            }
                            ParticleType.DIAMOND -> {
                                drawDiamondParticle(
                                    center = Offset(currentX, currentY),
                                    radius = p.size / 2f,
                                    color = p.color.copy(alpha = alpha)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Center Animated Celebration Card
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialScale = 0.3f
            ) + fadeIn(animationSpec = tween(300))
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCanvas),
                border = BorderStroke(2.dp, PrimaryIndigo.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(16.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* prevent inner card tap from dismissing unintentionally */ }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing Badge Icon with Rotating Sunburst Behind
                    Box(
                        modifier = Modifier.size(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Sunburst Rays
                        Canvas(
                            modifier = Modifier
                                .size(96.dp)
                                .rotate(sunburstRotation)
                        ) {
                            val rayCount = 12
                            val center = Offset(size.width / 2, size.height / 2)
                            val rayLength = size.width / 2
                            for (i in 0 until rayCount) {
                                val rayAngle = (i * 360f / rayCount) * (PI.toFloat() / 180f)
                                val endX = center.x + cos(rayAngle) * rayLength
                                val endY = center.y + sin(rayAngle) * rayLength
                                drawLine(
                                    color = Color(0xFFFEF08A).copy(alpha = 0.5f),
                                    start = center,
                                    end = Offset(endX, endY),
                                    strokeWidth = 6f
                                )
                            }
                        }

                        // Central Trophy Shield
                        Surface(
                            shape = CircleShape,
                            color = PrimaryIndigo,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Success",
                                    tint = GoldStar,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title & Subtitle
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Star Rating Burst with Staggered Spring Entry
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedStar(visible = star1Visible, ratingIndex = 1, isEarned = starsCount >= 1)
                        AnimatedStar(visible = star2Visible, ratingIndex = 2, isEarned = starsCount >= 2)
                        AnimatedStar(visible = star3Visible, ratingIndex = 3, isEarned = starsCount >= 3)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Animated Reward Chips (+XP, +Coins)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEDD5),
                            border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "XP",
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+$animatedXp XP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9A3412)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF9C3),
                            border = BorderStroke(1.dp, Color(0xFFFEF08A))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = GoldStar,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (is2xClaimed) "+${animatedCoins * 2} Coins (2X)" else "+$animatedCoins Coins",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF854D0E)
                                )
                            }
                        }
                    }

                    if (is2xClaimed) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, GoldStar)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "👑", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2X REWARD MULTIPLIER APPLIED!",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2X Reward Claim Button (if available and not yet claimed)
                    if (onClaim2xReward != null && !is2xClaimed) {
                        Button(
                            onClick = onClaim2xReward,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD97706)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("claim_2x_reward_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "2X Reward",
                                    tint = Color(0xFFFEF08A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Claim 2X Double Reward 📺",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WhiteCanvas
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Level Video Bonus Button (for extra coins per level)
                    if (onWatchLevelBonusVideo != null) {
                        Button(
                            onClick = onWatchLevelBonusVideo,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("watch_level_bonus_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "💰", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Watch Video for +100 Extra Coins",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteCanvas
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Rate & Feedback Button
                    if (onRateFeedback != null) {
                        OutlinedButton(
                            onClick = onRateFeedback,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.2.dp, PrimaryIndigo.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("celebration_rate_feedback_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "⭐", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rate Puzzle Set (+50 XP)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Next Challenge / Continue Button
                    Button(
                        onClick = onDismissOrContinue,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (onClaim2xReward != null && !is2xClaimed) PrimaryIndigo.copy(alpha = 0.9f) else PrimaryIndigo
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("celebration_continue_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Next Challenge 🚀",
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
}

/**
 * Animated individual star with scale-up pop and shine physics
 */
@Composable
private fun AnimatedStar(
    visible: Boolean,
    ratingIndex: Int,
    isEarned: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "star_scale_$ratingIndex"
    )

    Surface(
        shape = CircleShape,
        color = if (isEarned) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, if (isEarned) GoldStar else SlateBorder),
        modifier = Modifier
            .size(46.dp)
            .scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star $ratingIndex",
                tint = if (isEarned) GoldStar else Color(0xFFCBD5E1),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

private fun DrawScope.drawStarParticle(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val points = 5
    val innerRadius = radius * 0.45f
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = (i * PI / points) - (PI / 2)
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color, style = Fill)
}

private fun DrawScope.drawDiamondParticle(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + radius * 0.7f, center.y)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - radius * 0.7f, center.y)
        close()
    }
    drawPath(path = path, color = color, style = Fill)
}
