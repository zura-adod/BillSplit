package com.mobelio.bill.split.presentation.screens.home

import android.widget.Toast
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobelio.bill.split.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    onCreateSplit: () -> Unit,
    onHistoryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Swipe state
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isNavigating by remember { mutableStateOf(false) }
    val swipeThreshold = screenHeight * 0.15f

    // Animated swipe progress
    val animatedOffset by animateFloatAsState(
        targetValue = if (isNavigating) -screenHeight else dragOffset,
        animationSpec = if (isNavigating) {
            tween(400, easing = FastOutSlowInEasing)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        },
        finishedListener = {
            if (isNavigating) {
                onCreateSplit()
            }
        },
        label = "swipe_offset"
    )

    // Entry animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // INFINITE blob animations
    val infiniteTransition = rememberInfiniteTransition(label = "infinite_blobs")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    // Blob position animations for floating effect
    val floatX1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX1"
    )

    val floatY1 by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY1"
    )

    val floatX2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX2"
    )

    val floatY2 by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY2"
    )

    // Scale animations for breathing effect
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.02f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )

    // Swipe indicator bounce
    val indicatorBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val swipeProgress = (-animatedOffset / swipeThreshold).coerceIn(0f, 1.5f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -swipeThreshold && !isNavigating) {
                            isNavigating = true
                        } else {
                            dragOffset = 0f
                        }
                    },
                    onDragCancel = { dragOffset = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (!isNavigating) {
                            dragOffset = (dragOffset + dragAmount).coerceIn(-screenHeight * 0.5f, 50f)
                        }
                    }
                )
            }
    ) {
        // Animated Blob Background - INFINITE ANIMATION
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(1f + swipeProgress * 0.1f)
        ) {
            val w = size.width
            val h = size.height

            // Pink blob - top right
            drawAnimatedBlob(
                center = Offset(w * 0.82f + floatX1, h * 0.08f + floatY1),
                size = Size(w * 0.55f * scale1, h * 0.22f * scale1),
                morphPhase = phase1,
                color = BlobPink
            )

            // Purple blob - left side
            drawAnimatedBlob(
                center = Offset(w * 0.05f + floatX2, h * 0.38f + floatY2),
                size = Size(w * 0.5f * scale2, h * 0.28f * scale2),
                morphPhase = phase2,
                color = BlobPurple
            )

            // Yellow blob - bottom right
            drawAnimatedBlob(
                center = Offset(w * 0.88f + floatX1 * 0.5f, h * 0.72f + floatY1 * 0.7f),
                size = Size(w * 0.42f * scale1, h * 0.18f * scale1),
                morphPhase = phase3,
                color = BlobYellow
            )

            // Cyan blob - bottom left
            drawAnimatedBlob(
                center = Offset(w * 0.12f + floatX2 * 0.6f, h * 0.85f + floatY2 * 0.5f),
                size = Size(w * 0.48f * scale2, h * 0.2f * scale2),
                morphPhase = phase1 + phase2,
                color = BlobCyan
            )

            // Small purple accent blob
            drawAnimatedBlob(
                center = Offset(w * 0.6f + floatX1 * 0.3f, h * 0.25f + floatY2 * 0.4f),
                size = Size(w * 0.18f * scale1, h * 0.08f * scale1),
                morphPhase = phase2 * 1.5f,
                color = BlobPurple.copy(alpha = 0.5f)
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = with(density) { animatedOffset.toDp() })
                .alpha(1f - (swipeProgress * 0.3f).coerceIn(0f, 0.5f))
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Title
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = 100)) +
                        slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(600, delayMillis = 100))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Split bills",
                        fontSize = 26.sp,
                        color = TextWhite.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "with Friends",
                        fontSize = 54.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 58.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.6f))

            // Info Banners with rounded corners
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = 300)) +
                        slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(600, delayMillis = 300))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    InfoBanner(
                        icon = Icons.Default.Groups,
                        title = "Equal or Custom Split",
                        description = "Divide expenses any way you want",
                        accentColor = BlobPink,
                        floatDelay = 0
                    )
                    InfoBanner(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Multi Currency",
                        description = "Support for GEL, USD, EUR & more",
                        accentColor = BlobCyan,
                        floatDelay = 150
                    )
                    InfoBanner(
                        icon = Icons.Default.Share,
                        title = "Instant Share",
                        description = "Send via WhatsApp, Viber, SMS",
                        accentColor = BlobYellow,
                        floatDelay = 300
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Swipe Up Indicator - CENTERED
            AnimatedVisibility(
                visible = isVisible && !isNavigating,
                enter = fadeIn(tween(600, delayMillis = 500)) +
                        slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(600, delayMillis = 500)),
                exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { -100 })
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SwipeUpIndicator(
                        progress = swipeProgress,
                        bounceOffset = indicatorBounce
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History Button
            AnimatedVisibility(
                visible = isVisible && !isNavigating,
                enter = fadeIn(tween(400, delayMillis = 600)),
                exit = fadeOut(tween(150))
            ) {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "History coming soon!", Toast.LENGTH_SHORT).show()
                        onHistoryClick()
                    }
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View History", color = TextGray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Swipe overlay
        if (swipeProgress > 0.2f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BlobPink.copy(alpha = swipeProgress * 0.15f),
                                BlobPurple.copy(alpha = swipeProgress * 0.25f)
                            )
                        )
                    )
            )
        }

        // Navigation overlay
        AnimatedVisibility(
            visible = isNavigating,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkBackground, BlobPurple.copy(alpha = 0.3f), DarkBackground)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BlobPink, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun InfoBanner(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    floatDelay: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_$title")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500 + floatDelay, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + floatDelay, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = floatOffset.dp)
    ) {
        // Glow shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 6.dp)
                .clip(RoundedCornerShape(22.dp))
                .blur(24.dp)
                .background(accentColor.copy(alpha = glowAlpha))
        )

        // Card with ROUNDED icon container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CardGlass.copy(alpha = 0.55f),
                            CardGlass.copy(alpha = 0.35f)
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with ROUNDED corners
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.35f),
                                accentColor.copy(alpha = 0.15f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
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
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    color = TextGray,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun SwipeUpIndicator(
    progress: Float,
    bounceOffset: Float
) {
    val bounce = if (progress < 0.1f) bounceOffset * 12f else 0f

    Column(
        modifier = Modifier.offset(y = (-bounce).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arrows
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-14).dp)
        ) {
            listOf(0.3f, 0.5f, 0.8f).forEachIndexed { index, baseAlpha ->
                val alpha = if (progress > 0.3f) (baseAlpha + progress * 0.3f).coerceAtMost(1f)
                else baseAlpha + bounceOffset * 0.2f
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = if (progress > 0.5f) BlobPink.copy(alpha = alpha) else TextWhite.copy(alpha = alpha),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(
                    when {
                        progress > 0.8f -> Brush.horizontalGradient(listOf(SuccessGreen, BlobCyan))
                        progress > 0.3f -> Brush.horizontalGradient(listOf(BlobPink.copy(alpha = 0.4f), BlobPurple.copy(alpha = 0.4f)))
                        else -> Brush.horizontalGradient(listOf(CardGlass.copy(alpha = 0.5f), CardGlass.copy(alpha = 0.35f)))
                    }
                )
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                text = when {
                    progress > 0.8f -> "Release now!"
                    progress > 0.3f -> "Keep going..."
                    else -> "Swipe up to start"
                },
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

// Draw animated organic blob
private fun DrawScope.drawAnimatedBlob(
    center: Offset,
    size: Size,
    morphPhase: Float,
    color: Color
) {
    val path = Path()
    val points = 80

    for (i in 0 until points) {
        val angle = (i.toFloat() / points) * 2 * PI

        val r1 = 1f + 0.2f * sin(angle * 2 + morphPhase).toFloat()
        val r2 = 1f + 0.15f * cos(angle * 3 + morphPhase * 0.7f).toFloat()
        val r3 = 1f + 0.1f * sin(angle * 4 + morphPhase * 1.3f).toFloat()

        val radiusMod = (r1 * r2 * r3) * 0.85f

        val rx = size.width * 0.5f * radiusMod
        val ry = size.height * 0.5f * radiusMod

        val x = center.x + rx * cos(angle).toFloat()
        val y = center.y + ry * sin(angle).toFloat()

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    path.close()
    drawPath(path = path, color = color, style = Fill)
}

