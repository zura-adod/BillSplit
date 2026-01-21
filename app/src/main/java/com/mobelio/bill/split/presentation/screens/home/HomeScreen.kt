package com.mobelio.bill.split.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobelio.bill.split.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    onCreateSplit: () -> Unit,
    onHistoryClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob_animation")

    // Smooth flowing blob animations
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Individual blob movement offsets for organic feel
    val blob1OffsetX by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1X"
    )

    val blob1OffsetY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1Y"
    )

    val blob2OffsetX by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2X"
    )

    val blob2OffsetY by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2Y"
    )

    val blob3Scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob3Scale"
    )

    val blob4Scale by infiniteTransition.animateFloat(
        initialValue = 1.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob4Scale"
    )

    // Swipe UP state
    var swipeOffset by remember { mutableFloatStateOf(0f) }
    var isSwipeComplete by remember { mutableStateOf(false) }
    val swipeThreshold = -150f // Negative because swiping UP

    val swipeProgress = (swipeOffset / swipeThreshold).coerceIn(0f, 1f)

    // Entry animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Handle swipe completion
    LaunchedEffect(isSwipeComplete) {
        if (isSwipeComplete) {
            delay(300)
            onCreateSplit()
            delay(500)
            isSwipeComplete = false
            swipeOffset = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (swipeOffset <= swipeThreshold) {
                            isSwipeComplete = true
                        } else {
                            swipeOffset = 0f
                        }
                    },
                    onDragCancel = {
                        swipeOffset = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        // Only allow upward swipe (negative values)
                        if (dragAmount < 0 || swipeOffset < 0) {
                            swipeOffset = (swipeOffset + dragAmount).coerceIn(swipeThreshold - 50f, 0f)
                        }
                    }
                )
            }
    ) {
        // Animated Blob Background - Smoother, more organic
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Pink blob - flows smoothly at top right
            drawSmoothBlob(
                center = Offset(
                    width * 0.85f + blob1OffsetX,
                    height * 0.12f + blob1OffsetY
                ),
                baseRadius = width * 0.38f,
                time = time,
                color = BlobPink.copy(alpha = 0.9f),
                complexity = 5,
                waveAmplitude = 0.15f
            )

            // Purple blob - left side, flowing
            drawSmoothBlob(
                center = Offset(
                    width * 0.08f + blob2OffsetX,
                    height * 0.42f + blob2OffsetY
                ),
                baseRadius = width * 0.42f * blob3Scale,
                time = time * 0.8f,
                color = BlobPurple.copy(alpha = 0.85f),
                complexity = 6,
                waveAmplitude = 0.12f
            )

            // Yellow blob - bottom right
            drawSmoothBlob(
                center = Offset(
                    width * 0.92f + blob2OffsetX * 0.5f,
                    height * 0.78f + blob1OffsetY * 0.7f
                ),
                baseRadius = width * 0.32f * blob4Scale,
                time = time * 1.2f,
                color = BlobYellow.copy(alpha = 0.88f),
                complexity = 5,
                waveAmplitude = 0.18f
            )

            // Cyan blob - bottom left
            drawSmoothBlob(
                center = Offset(
                    width * 0.12f + blob1OffsetX * 0.6f,
                    height * 0.88f + blob2OffsetY * 0.5f
                ),
                baseRadius = width * 0.36f,
                time = time * 0.9f,
                color = BlobCyan.copy(alpha = 0.82f),
                complexity = 5,
                waveAmplitude = 0.14f
            )

            // Small accent blob - center area
            drawSmoothBlob(
                center = Offset(
                    width * 0.55f + blob1OffsetX * 0.3f,
                    height * 0.28f + blob2OffsetY * 0.4f
                ),
                baseRadius = width * 0.12f * blob3Scale,
                time = time * 1.5f,
                color = BlobPurple.copy(alpha = 0.5f),
                complexity = 4,
                waveAmplitude = 0.2f
            )
        }

        // Content with swipe offset
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .offset(y = (swipeOffset * 0.3f).dp)
                .alpha(1f - swipeProgress * 0.5f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Title with animation
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) +
                        slideInVertically(initialOffsetY = { -50 }, animationSpec = tween(800, delayMillis = 200))
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Split bills",
                        fontSize = 28.sp,
                        color = TextWhite.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = "with Friends",
                        fontSize = 52.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 56.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.8f))

            // Info cards - NOT clickable, just informational with subtle animations
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) +
                        slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(800, delayMillis = 400))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoCard(
                        icon = Icons.Default.Groups,
                        title = "Equal or Custom Split",
                        subtitle = "Divide expenses any way you want",
                        color = BlobPink,
                        delay = 0
                    )
                    InfoCard(
                        icon = Icons.Default.CurrencyExchange,
                        title = "Multi Currency",
                        subtitle = "Support for GEL, USD, EUR & more",
                        color = BlobCyan,
                        delay = 100
                    )
                    InfoCard(
                        icon = Icons.Default.Share,
                        title = "Instant Share",
                        subtitle = "Send via WhatsApp, Viber, SMS",
                        color = BlobYellow,
                        delay = 200
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Swipe UP indicator
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 600)) +
                        slideInVertically(initialOffsetY = { 80 }, animationSpec = tween(800, delayMillis = 600))
            ) {
                SwipeUpIndicator(
                    progress = swipeProgress,
                    isComplete = isSwipeComplete
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // History link
            AnimatedVisibility(
                visible = isVisible && !isSwipeComplete,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 700))
            ) {
                TextButton(onClick = onHistoryClick) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "View History",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Swipe progress overlay
        if (swipeProgress > 0.3f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BlobPink.copy(alpha = swipeProgress * 0.3f),
                                BlobPurple.copy(alpha = swipeProgress * 0.4f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    delay: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "info_card_$title")

    // Subtle floating animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + delay, easing = EaseInOutSine, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Subtle glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500 + delay, easing = EaseInOutSine, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = floatOffset.dp)
    ) {
        // Glow effect behind
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .blur(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(color.copy(alpha = glowAlpha))
        )

        // Card content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CardGlass.copy(alpha = 0.5f),
                            CardGlass.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with gradient
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                color.copy(alpha = 0.3f),
                                color.copy(alpha = 0.15f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SwipeUpIndicator(
    progress: Float,
    isComplete: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "swipe_hint")

    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = if (!isComplete) bounceOffset.dp else 0.dp)
    ) {
        // Arrow icons pointing up
        if (!isComplete) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-12).dp)
            ) {
                repeat(3) { index ->
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = if (progress > 0.3f) {
                            BlobPink.copy(alpha = 0.5f + index * 0.2f)
                        } else {
                            TextWhite.copy(alpha = arrowAlpha * (0.3f + index * 0.25f))
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Progress indicator or text
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isComplete) {
                        Brush.horizontalGradient(listOf(SuccessGreen, BlobCyan))
                    } else if (progress > 0.3f) {
                        Brush.horizontalGradient(listOf(BlobPink.copy(alpha = 0.3f), BlobPurple.copy(alpha = 0.3f)))
                    } else {
                        Brush.horizontalGradient(listOf(CardGlass.copy(alpha = 0.4f), CardGlass.copy(alpha = 0.3f)))
                    }
                )
                .padding(horizontal = 28.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isComplete) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = when {
                        isComplete -> "Let's Go!"
                        progress > 0.7f -> "Release to start!"
                        progress > 0.3f -> "Keep going..."
                        else -> "Swipe up to start"
                    },
                    color = if (isComplete || progress > 0.3f) Color.White else TextWhite.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Smooth organic blob drawing function
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothBlob(
    center: Offset,
    baseRadius: Float,
    time: Float,
    color: Color,
    complexity: Int = 5,
    waveAmplitude: Float = 0.15f
) {
    val path = Path()
    val points = complexity * 12 // More points for smoother curve
    val angleStep = 360f / points

    for (i in 0 until points) {
        val angle = Math.toRadians((i * angleStep).toDouble())

        // Multiple sine waves for organic shape
        val wave1 = sin(angle * complexity + Math.toRadians(time.toDouble())) * waveAmplitude
        val wave2 = sin(angle * (complexity - 1) + Math.toRadians(time * 0.7).toDouble()) * waveAmplitude * 0.5f
        val wave3 = cos(angle * (complexity + 1) + Math.toRadians(time * 1.3).toDouble()) * waveAmplitude * 0.3f

        val radiusVariation = 1f + wave1.toFloat() + wave2.toFloat() + wave3.toFloat()
        val radius = baseRadius * radiusVariation

        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    path.close()

    drawPath(
        path = path,
        color = color,
        style = Fill
    )
}

