package com.mobelio.bill.split.presentation.screens.reviewshare

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobelio.bill.split.domain.model.ContactMethod
import com.mobelio.bill.split.domain.model.Currency
import com.mobelio.bill.split.domain.model.Participant
import com.mobelio.bill.split.domain.model.PaymentType
import com.mobelio.bill.split.domain.model.ShareChannel
import com.mobelio.bill.split.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewShareScreen(
    viewModel: ReviewShareViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Animation
    val infiniteTransition = rememberInfiniteTransition(label = "blob_anim")
    val blobPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob_phase"
    )

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    LaunchedEffect(state.shareCompleted) {
        if (state.shareCompleted) {
            onFinish()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ReviewShareEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                }
                is ReviewShareEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ReviewShareEffect.ShareIntent -> {
                    val intent = createShareIntent(
                        participant = effect.participant,
                        message = effect.message,
                        channel = effect.channel
                    )
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open app", Toast.LENGTH_SHORT).show()
                    }
                }
                is ReviewShareEffect.ShareAllIntent -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.message)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share via"))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated blob background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Green success blob - top
            drawBlobShape(
                center = Offset(width * 0.8f, height * 0.1f),
                baseRadius = width * 0.3f,
                phase = blobPhase,
                color = SuccessGreen.copy(alpha = 0.4f)
            )

            // Cyan blob - bottom
            drawBlobShape(
                center = Offset(width * 0.1f, height * 0.85f),
                baseRadius = width * 0.35f,
                phase = blobPhase * 0.7f,
                color = BlobCyan.copy(alpha = 0.3f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Text(
                    text = "Review & Share",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Summary Card
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 50 })
                    ) {
                        SummaryCard(
                            totalAmount = state.billSplit.totalAmount,
                            currency = state.billSplit.currency,
                            participantCount = state.billSplit.participants.size,
                            paymentType = state.billSplit.paymentDetails.type,
                            paymentValue = state.billSplit.paymentDetails.value,
                            note = state.billSplit.note
                        )
                    }
                }

                // Quick Actions
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(initialOffsetY = { 50 })
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionButton(
                                icon = Icons.Outlined.Share,
                                label = "Share All",
                                color = BlobPink,
                                onClick = { viewModel.onEvent(ReviewShareEvent.ShareAll) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionButton(
                                icon = Icons.Outlined.ContentCopy,
                                label = "Copy All",
                                color = BlobPurple,
                                onClick = { viewModel.onEvent(ReviewShareEvent.CopyAll) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section header
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 300))
                    ) {
                        Text(
                            text = "Send to Participants",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }
                }

                // Participant cards
                items(
                    items = state.billSplit.participants,
                    key = { it.id }
                ) { participant ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 400)) + slideInHorizontally(initialOffsetX = { 100 })
                    ) {
                        ParticipantShareCard(
                            participant = participant,
                            message = state.participantMessages[participant.id] ?: "",
                            availableChannels = state.availableChannels[participant.id] ?: emptySet(),
                            currency = state.billSplit.currency,
                            onShare = { channel ->
                                viewModel.onEvent(ReviewShareEvent.ShareToParticipant(participant.id, channel))
                            }
                        )
                    }
                }

                // Done button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 500)) + scaleIn(initialScale = 0.8f)
                    ) {
                        GradientButton(
                            text = "Done",
                            icon = Icons.Default.Check,
                            onClick = { viewModel.onEvent(ReviewShareEvent.Finish) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalAmount: Double,
    currency: Currency,
    participantCount: Int,
    paymentType: PaymentType,
    paymentValue: String,
    note: String
) {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(BlobPink, BlobPurple, BlobCyan)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Total Amount",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = currency.symbol,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatter.format(totalAmount),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(
                    icon = Icons.Outlined.People,
                    text = "$participantCount people"
                )
                InfoChip(
                    icon = if (paymentType == PaymentType.IBAN) Icons.Outlined.AccountBalance
                    else Icons.Outlined.CreditCard,
                    text = paymentType.name
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment value
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(12.dp)
            ) {
                Text(
                    text = paymentValue,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (note.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Notes,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(CardGlass.copy(alpha = 0.5f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun ParticipantShareCard(
    participant: Participant,
    message: String,
    availableChannels: Set<ShareChannel>,
    currency: Currency,
    onShare: (ShareChannel) -> Unit
) {
    val colors = listOf(BlobPink, BlobPurple, BlobCyan, BlobYellow)
    val color = colors[participant.id.hashCode().mod(colors.size).let { if (it < 0) -it else it }]
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    var showMessage by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardGlass.copy(alpha = 0.4f))
            .border(1.dp, TextWhite.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(color, color.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (participant.name.firstOrNull()
                            ?: participant.contactValue.firstOrNull()
                            ?: '?').uppercase().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = participant.name.ifBlank { "Unknown" },
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (participant.contactMethod == ContactMethod.PHONE)
                                Icons.Outlined.Phone else Icons.Outlined.Email,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = participant.contactValue,
                            fontSize = 13.sp,
                            color = TextGray
                        )
                    }
                }

                // Amount badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${currency.symbol}${formatter.format(participant.amount)}",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message preview toggle
            TextButton(
                onClick = { showMessage = !showMessage },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (showMessage) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (showMessage) "Hide Message" else "Preview Message",
                    color = color
                )
            }

            AnimatedVisibility(visible = showMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardGlass.copy(alpha = 0.5f))
                        .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = TextWhite,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Share buttons
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableChannels.toList()) { channel ->
                    ShareButton(
                        channel = channel,
                        onClick = { onShare(channel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareButton(
    channel: ShareChannel,
    onClick: () -> Unit
) {
    val (icon, label, color) = when (channel) {
        ShareChannel.WHATSAPP -> Triple(Icons.AutoMirrored.Filled.Send, "WhatsApp", WhatsAppGreen)
        ShareChannel.VIBER -> Triple(Icons.AutoMirrored.Filled.Send, "Viber", ViberPurple)
        ShareChannel.SMS -> Triple(Icons.Outlined.Sms, "SMS", SmsBlue)
        ShareChannel.EMAIL -> Triple(Icons.Outlined.Email, "Email", EmailRed)
        ShareChannel.SHARE_SHEET -> Triple(Icons.Outlined.Share, "Share", BlobPurple)
        ShareChannel.COPY -> Triple(Icons.Outlined.ContentCopy, "Copy", TextWhite)
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "share_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = color,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(60.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(SuccessGreen, BlobCyan)
                )
            )
            .clickable {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

private fun createShareIntent(
    participant: Participant,
    message: String,
    channel: ShareChannel
): Intent {
    return when (channel) {
        ShareChannel.WHATSAPP -> {
            val phone = participant.contactValue.replace(Regex("[^0-9+]"), "")
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phone?text=${URLEncoder.encode(message, "UTF-8")}")
            }
        }
        ShareChannel.VIBER -> {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.viber.voip")
                putExtra(Intent.EXTRA_TEXT, message)
            }
        }
        ShareChannel.SMS -> {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${participant.contactValue}")
                putExtra("sms_body", message)
            }
        }
        ShareChannel.EMAIL -> {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${participant.contactValue}")
                putExtra(Intent.EXTRA_SUBJECT, "Payment Request - Bill Split")
                putExtra(Intent.EXTRA_TEXT, message)
            }
        }
        ShareChannel.SHARE_SHEET -> {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
        }
        ShareChannel.COPY -> Intent()
    }
}

// Helper function for blob drawing
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlobShape(
    center: Offset,
    baseRadius: Float,
    phase: Float,
    color: Color,
    blobVariation: Float = 0.3f
) {
    val path = Path()
    val points = 6
    val angleStep = 360f / points

    for (i in 0 until points) {
        val angle = Math.toRadians((i * angleStep + phase).toDouble())
        val variation = sin(angle * 2 + phase * 0.01) * baseRadius * blobVariation
        val radius = baseRadius + variation.toFloat()

        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            val midAngle = Math.toRadians(((i - 0.5f) * angleStep + phase).toDouble())
            val controlRadius = baseRadius * 1.15f
            val controlX = center.x + (controlRadius * cos(midAngle)).toFloat()
            val controlY = center.y + (controlRadius * sin(midAngle)).toFloat()
            path.quadraticBezierTo(controlX, controlY, x, y)
        }
    }

    val firstAngle = Math.toRadians(phase.toDouble())
    val lastMidAngle = Math.toRadians(((points - 0.5f) * angleStep + phase).toDouble())
    val controlRadius = baseRadius * 1.15f
    val controlX = center.x + (controlRadius * cos(lastMidAngle)).toFloat()
    val controlY = center.y + (controlRadius * sin(lastMidAngle)).toFloat()
    val firstX = center.x + (baseRadius * cos(firstAngle)).toFloat()
    val firstY = center.y + (baseRadius * sin(firstAngle)).toFloat()
    path.quadraticBezierTo(controlX, controlY, firstX, firstY)
    path.close()

    drawPath(path = path, color = color, style = Fill)
}

