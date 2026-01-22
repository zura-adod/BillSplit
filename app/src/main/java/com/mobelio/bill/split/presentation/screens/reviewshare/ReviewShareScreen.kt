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
import androidx.compose.ui.draw.blur
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
import kotlin.math.PI
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

    // Infinite blob animations
    val infiniteTransition = rememberInfiniteTransition(label = "blob_anim")

    val blobPhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val blobPhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val floatX by infiniteTransition.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(5000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "floatX"
    )

    val floatY by infiniteTransition.animateFloat(
        initialValue = -15f, targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(4500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "floatY"
    )

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    LaunchedEffect(state.shareCompleted) {
        if (state.shareCompleted) onFinish()
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ReviewShareEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                is ReviewShareEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is ReviewShareEffect.ShareIntent -> {
                    try {
                        context.startActivity(createShareIntent(effect.participant, effect.message, effect.channel))
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
            val w = size.width
            val h = size.height

            drawAnimatedBlob(
                center = Offset(w * 0.85f + floatX, h * 0.08f + floatY),
                baseRadius = w * 0.3f,
                morphPhase = blobPhase1,
                color = SuccessGreen.copy(alpha = 0.4f)
            )

            drawAnimatedBlob(
                center = Offset(w * 0.1f + floatX * 0.5f, h * 0.88f + floatY * 0.7f),
                baseRadius = w * 0.35f,
                morphPhase = blobPhase2,
                color = BlobCyan.copy(alpha = 0.3f)
            )

            drawAnimatedBlob(
                center = Offset(w * 0.9f + floatY * 0.3f, h * 0.5f + floatX * 0.4f),
                baseRadius = w * 0.15f,
                morphPhase = blobPhase1 + blobPhase2,
                color = BlobPurple.copy(alpha = 0.25f)
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
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
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Summary Card
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 50 })
                    ) {
                        SummaryCard(state.billSplit)
                    }
                }

                // Quick Actions
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(initialOffsetY = { 50 })
                    ) {
                        QuickActionsRow(
                            participants = state.billSplit.participants,
                            onShareAllSms = {
                                val phoneParticipants = state.billSplit.participants.filter { it.contactMethod == ContactMethod.PHONE }
                                if (phoneParticipants.isNotEmpty()) {
                                    val phones = phoneParticipants.joinToString(";") { it.contactValue }
                                    // Use phone-only message
                                    val message = viewModel.generatePhoneOnlyMessage()
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:$phones")
                                        putExtra("sms_body", message)
                                    }
                                    try { context.startActivity(intent) }
                                    catch (e: Exception) { Toast.makeText(context, "Could not open SMS app", Toast.LENGTH_SHORT).show() }
                                } else {
                                    Toast.makeText(context, "No phone recipients", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onShareAllEmail = {
                                val emailParticipants = state.billSplit.participants.filter { it.contactMethod == ContactMethod.EMAIL }
                                if (emailParticipants.isNotEmpty()) {
                                    val emails = emailParticipants.map { it.contactValue }.toTypedArray()
                                    // Use email-only message
                                    val message = viewModel.generateEmailOnlyMessage()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "message/rfc822"
                                        putExtra(Intent.EXTRA_EMAIL, emails)
                                        putExtra(Intent.EXTRA_SUBJECT, "Payment Request - Bill Split")
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    }
                                    try { context.startActivity(Intent.createChooser(intent, "Send email")) }
                                    catch (e: Exception) { Toast.makeText(context, "Could not open email app", Toast.LENGTH_SHORT).show() }
                                } else {
                                    Toast.makeText(context, "No email recipients", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyAll = { viewModel.onEvent(ReviewShareEvent.CopyAll) }
                        )
                    }
                }

                // Section header
                item {
                    AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(500, delayMillis = 300))) {
                        Text("Send to Participants", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    }
                }

                // Participant cards
                items(items = state.billSplit.participants, key = { it.id }) { participant ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500, delayMillis = 400)) + slideInHorizontally(initialOffsetX = { 100 })
                    ) {
                        ParticipantShareCard(
                            participant = participant,
                            message = state.participantMessages[participant.id] ?: "",
                            availableChannels = state.availableChannels[participant.id] ?: emptySet(),
                            currency = state.billSplit.currency,
                            onShare = { channel -> viewModel.onEvent(ReviewShareEvent.ShareToParticipant(participant.id, channel)) }
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
                        GradientActionButton(
                            text = "Done",
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
private fun SummaryCard(billSplit: com.mobelio.bill.split.domain.model.BillSplit) {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(BlobPink, BlobPurple, BlobCyan)))
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Total Amount", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(billSplit.currency.symbol, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(formatter.format(billSplit.totalAmount), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryChip(Icons.Outlined.People, "${billSplit.participants.size} people")
                SummaryChip(
                    if (billSplit.paymentDetails.type == PaymentType.IBAN) Icons.Outlined.AccountBalance else Icons.Outlined.CreditCard,
                    billSplit.paymentDetails.type.name
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.15f)).padding(12.dp)
            ) {
                Text(billSplit.paymentDetails.value, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (billSplit.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Notes, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(billSplit.note, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun QuickActionsRow(
    participants: List<Participant>,
    onShareAllSms: () -> Unit,
    onShareAllEmail: () -> Unit,
    onCopyAll: () -> Unit
) {
    val hasPhone = participants.any { it.contactMethod == ContactMethod.PHONE }
    val hasEmail = participants.any { it.contactMethod == ContactMethod.EMAIL }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Show SMS All only if there are phone recipients
        if (hasPhone) {
            ActionButton(Icons.Outlined.Sms, "SMS All", SmsBlue, onShareAllSms, Modifier.weight(1f))
        }
        // Show Email All only if there are email recipients
        if (hasEmail) {
            ActionButton(Icons.Outlined.Email, "Email All", EmailRed, onShareAllEmail, Modifier.weight(1f))
        }
        // Always show Copy All
        ActionButton(Icons.Outlined.ContentCopy, "Copy All", BlobPurple, onCopyAll, Modifier.weight(1f))
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scale")

    Box(
        modifier = modifier.scale(scale).clip(RoundedCornerShape(16.dp)).background(CardGlass.copy(alpha = 0.5f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).clickable { isPressed = true; onClick() }.padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
    LaunchedEffect(isPressed) { if (isPressed) { kotlinx.coroutines.delay(100); isPressed = false } }
}

@Composable
private fun ParticipantShareCard(
    participant: Participant, message: String, availableChannels: Set<ShareChannel>, currency: Currency, onShare: (ShareChannel) -> Unit
) {
    val colors = listOf(BlobPink, BlobPurple, BlobCyan, BlobYellow)
    val color = colors[participant.id.hashCode().mod(colors.size).let { if (it < 0) -it else it }]
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
    var showMessage by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardGlass.copy(alpha = 0.4f))
            .border(1.dp, TextWhite.copy(alpha = 0.1f), RoundedCornerShape(20.dp)).padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Brush.radialGradient(listOf(color, color.copy(alpha = 0.6f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text((participant.name.firstOrNull() ?: participant.contactValue.firstOrNull() ?: '?').uppercase().toString(),
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(participant.name.ifBlank { "Contact" }, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(participant.contactValue, fontSize = 12.sp, color = TextGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("${currency.symbol}${formatter.format(participant.amount)}", color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { showMessage = !showMessage }, modifier = Modifier.align(Alignment.Start)) {
                Icon(if (showMessage) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showMessage) "Hide" else "Preview", color = color, fontSize = 13.sp)
            }

            AnimatedVisibility(visible = showMessage) {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CardGlass.copy(alpha = 0.5f)).padding(12.dp)) {
                    Text(message, fontSize = 12.sp, color = TextWhite, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableChannels.toList()) { channel -> ShareChip(channel) { onShare(channel) } }
            }
        }
    }
}

@Composable
private fun ShareChip(channel: ShareChannel, onClick: () -> Unit) {
    val (icon, label, color) = when (channel) {
        ShareChannel.WHATSAPP -> Triple(Icons.AutoMirrored.Filled.Send, "WhatsApp", WhatsAppGreen)
        ShareChannel.VIBER -> Triple(Icons.AutoMirrored.Filled.Send, "Viber", ViberPurple)
        ShareChannel.SMS -> Triple(Icons.Outlined.Sms, "SMS", SmsBlue)
        ShareChannel.EMAIL -> Triple(Icons.Outlined.Email, "Email", EmailRed)
        ShareChannel.SHARE_SHEET -> Triple(Icons.Outlined.Share, "Share", BlobPurple)
        ShareChannel.COPY -> Triple(Icons.Outlined.ContentCopy, "Copy", TextWhite)
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "chip_scale")

    Box(
        modifier = Modifier.scale(scale).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { isPressed = true; onClick() }.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = color, fontWeight = FontWeight.Medium, fontSize = 12.sp)
        }
    }
    LaunchedEffect(isPressed) { if (isPressed) { kotlinx.coroutines.delay(100); isPressed = false } }
}

@Composable
private fun GradientActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "btn_scale")

    Box(
        modifier = modifier.scale(scale).height(56.dp).clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(SuccessGreen, BlobCyan))).clickable { isPressed = true; onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
    LaunchedEffect(isPressed) { if (isPressed) { kotlinx.coroutines.delay(100); isPressed = false } }
}

private fun createShareIntent(participant: Participant, message: String, channel: ShareChannel): Intent = when (channel) {
    ShareChannel.WHATSAPP -> Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://wa.me/${participant.contactValue.replace(Regex("[^0-9+]"), "")}?text=${URLEncoder.encode(message, "UTF-8")}")
    }
    ShareChannel.VIBER -> Intent(Intent.ACTION_SEND).apply { type = "text/plain"; setPackage("com.viber.voip"); putExtra(Intent.EXTRA_TEXT, message) }
    ShareChannel.SMS -> Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:${participant.contactValue}"); putExtra("sms_body", message) }
    ShareChannel.EMAIL -> Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:${participant.contactValue}")
        putExtra(Intent.EXTRA_SUBJECT, "Payment Request - Bill Split")
        putExtra(Intent.EXTRA_TEXT, message)
    }
    ShareChannel.SHARE_SHEET -> Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message) }
    ShareChannel.COPY -> Intent()
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAnimatedBlob(center: Offset, baseRadius: Float, morphPhase: Float, color: Color) {
    val path = Path()
    for (i in 0 until 80) {
        val angle = (i.toFloat() / 80) * 2 * PI
        val r1 = 1f + 0.2f * sin(angle * 2 + morphPhase).toFloat()
        val r2 = 1f + 0.15f * cos(angle * 3 + morphPhase * 0.7f).toFloat()
        val r3 = 1f + 0.1f * sin(angle * 4 + morphPhase * 1.3f).toFloat()
        val radius = baseRadius * (r1 * r2 * r3) * 0.85f
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

