package com.mobelio.bill.split.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobelio.bill.split.presentation.components.GradientButton
import com.mobelio.bill.split.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    onCreateSplit: () -> Unit,
    onHistoryClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "home_animation")

    // Multiple floating bubble animations
    val bubble1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble1"
    )

    val bubble2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble2"
    )

    val bubble3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble3"
    )

    // Logo pulsing
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    // Gradient rotation for logo
    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

    // Entry animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundLightStart,
                        Color.White,
                        BackgroundLightEnd
                    )
                )
            )
    ) {
        // Animated background blobs
        AnimatedBlob(
            color = GradientStart.copy(alpha = 0.15f),
            offsetY = bubble1Y,
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-50).dp, y = 100.dp)
                .blur(40.dp)
        )

        AnimatedBlob(
            color = AccentPink.copy(alpha = 0.12f),
            offsetY = bubble2Y,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 200.dp)
                .blur(35.dp)
        )

        AnimatedBlob(
            color = AccentGreen.copy(alpha = 0.1f),
            offsetY = bubble3Y,
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-150).dp)
                .blur(30.dp)
        )

        // Floating decorative icons
        FloatingDecorativeIcon(
            icon = Icons.Outlined.Payments,
            color = AccentOrange,
            offsetY = bubble1Y,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 30.dp, top = 120.dp)
        )

        FloatingDecorativeIcon(
            icon = Icons.Outlined.Groups,
            color = AccentPurple,
            offsetY = bubble2Y,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 40.dp, top = 180.dp)
        )

        FloatingDecorativeIcon(
            icon = Icons.Outlined.Receipt,
            color = AccentGreen,
            offsetY = bubble3Y,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp)
        )

        FloatingDecorativeIcon(
            icon = Icons.Outlined.CreditCard,
            color = AccentBlue,
            offsetY = -bubble1Y,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 25.dp)
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Animated Logo
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800)) +
                        scaleIn(initialScale = 0.5f, animationSpec = tween(800, easing = EaseOutBack))
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating gradient ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(gradientRotation)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        GradientStart,
                                        GradientMiddle,
                                        AccentPink,
                                        AccentOrange,
                                        AccentYellow,
                                        AccentGreen,
                                        AccentBlue,
                                        GradientStart
                                    )
                                )
                            )
                    )

                    // Inner circle with icon
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            GradientStart,
                                            GradientMiddle
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title with gradient
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) +
                        slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = tween(800, delayMillis = 200)
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bill Split",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Split bills easily • Share instantly ✨",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondaryLight,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Feature cards
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 400)) +
                        slideInVertically(
                            initialOffsetY = { 100 },
                            animationSpec = tween(800, delayMillis = 400)
                        )
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(features) { feature ->
                        FeatureCard(
                            icon = feature.icon,
                            title = feature.title,
                            color = feature.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // CTA Button
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 600)) +
                        slideInVertically(
                            initialOffsetY = { 80 },
                            animationSpec = tween(800, delayMillis = 600)
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GradientButton(
                        text = "Create New Split",
                        onClick = onCreateSplit,
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // History button
                    TextButton(
                        onClick = onHistoryClick,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(GradientStart.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = GradientStart,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View History",
                            color = GradientStart,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AnimatedBlob(
    color: Color,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun FloatingDecorativeIcon(
    icon: ImageVector,
    color: Color,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .size(48.dp)
            .shadow(8.dp, CircleShape, ambientColor = color.copy(alpha = 0.3f))
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    color: Color
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "feature_scale"
    )

    Card(
        modifier = Modifier
            .width(110.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { isPressed = !isPressed },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = TextPrimaryLight,
                maxLines = 2
            )
        }
    }
}

private data class Feature(
    val icon: ImageVector,
    val title: String,
    val color: Color
)

private val features = listOf(
    Feature(Icons.Outlined.Groups, "Split with Friends", AccentPurple),
    Feature(Icons.Outlined.AttachMoney, "Multi Currency", AccentGreen),
    Feature(Icons.Outlined.Share, "Quick Share", AccentOrange),
    Feature(Icons.Outlined.History, "Track History", AccentBlue)
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BIllSplitTheme {
        HomeScreen(onCreateSplit = {})
    }
}

