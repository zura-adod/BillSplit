package com.mobelio.bill.split.presentation.screens.createsplit

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobelio.bill.split.domain.model.*
import com.mobelio.bill.split.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitScreen(
    viewModel: CreateSplitViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToContactPicker: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Animation - INFINITE blob animations
    val infiniteTransition = rememberInfiniteTransition(label = "blob_anim")

    val blobPhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob_phase1"
    )

    val blobPhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob_phase2"
    )

    val floatX by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatX"
    )

    val floatY by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val blobScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blobScale"
    )

    // Handle navigation
    LaunchedEffect(state.navigateToReview) {
        if (state.navigateToReview) {
            onNavigateToReview()
            viewModel.onEvent(CreateSplitEvent.NavigatedToReview)
        }
    }

    LaunchedEffect(state.navigateToContactPicker) {
        if (state.navigateToContactPicker) {
            onNavigateToContactPicker()
            viewModel.onEvent(CreateSplitEvent.NavigatedToContactPicker)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshParticipantsFromStateHolder()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CreateSplitEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CreateSplitEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                }
                CreateSplitEffect.RequestClipboardPaste -> {
                    val text = clipboardManager.getText()?.text ?: ""
                    viewModel.onPasteResult(text)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated blob background - INFINITE
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top right pink blob with movement
            drawAnimatedBlob(
                center = Offset(width * 1.1f + floatX, height * 0.05f + floatY),
                baseRadius = width * 0.4f * blobScale,
                morphPhase = blobPhase1,
                color = BlobPink.copy(alpha = 0.6f)
            )

            // Bottom left purple blob with movement
            drawAnimatedBlob(
                center = Offset(width * -0.1f + floatX * 0.5f, height * 0.9f + floatY * 0.7f),
                baseRadius = width * 0.35f * blobScale,
                morphPhase = blobPhase2,
                color = BlobPurple.copy(alpha = 0.5f)
            )

            // Small cyan accent blob
            drawAnimatedBlob(
                center = Offset(width * 0.9f + floatY * 0.3f, height * 0.5f + floatX * 0.4f),
                baseRadius = width * 0.15f * blobScale,
                morphPhase = blobPhase1 + blobPhase2,
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
                    text = "Create Split",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )
            }

            // Main content with horizontal paging
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Amount Card
                item {
                    GlassCard {
                        Column {
                            CardHeader(
                                icon = Icons.Outlined.Payments,
                                title = "Amount",
                                color = BlobPink
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            AmountInput(
                                amount = state.totalAmount,
                                currency = state.currency,
                                onAmountChange = { viewModel.onEvent(CreateSplitEvent.TotalAmountChanged(it)) },
                                onCurrencyClick = { viewModel.onEvent(CreateSplitEvent.ShowCurrencyPicker) }
                            )
                        }
                    }
                }

                // Split Config Card
                item {
                    GlassCard {
                        Column {
                            CardHeader(
                                icon = Icons.Outlined.Groups,
                                title = "Split Between",
                                color = BlobPurple
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // People count selector
                            PeopleSelector(
                                count = state.numberOfPeople,
                                onCountChange = { viewModel.onEvent(CreateSplitEvent.NumberOfPeopleChanged(it)) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Include yourself toggle
                            val totalSplitters = if (state.includeYourself) state.numberOfPeople + 1 else state.numberOfPeople
                            ToggleOption(
                                checked = state.includeYourself,
                                onCheckedChange = { viewModel.onEvent(CreateSplitEvent.IncludeYourselfChanged(it)) },
                                label = "Include myself in split",
                                sublabel = if (state.includeYourself)
                                    "Split among ${totalSplitters} people (you + ${state.numberOfPeople} others)"
                                else
                                    "Split among ${state.numberOfPeople} people (you don't pay)"
                            )
                        }
                    }
                }

                // Amount Preview
                if (state.totalAmount.isNotEmpty() && state.numberOfPeople > 0) {
                    item {
                        SplitAmountPreview(
                            totalAmount = state.totalAmount.toDoubleOrNull() ?: 0.0,
                            numberOfPeople = state.numberOfPeople,
                            includeYourself = state.includeYourself,
                            currency = state.currency
                        )
                    }
                }

                // Payment Details Card - only enabled when amount is entered
                item {
                    GlassCard {
                        Column {
                            CardHeader(
                                icon = Icons.Outlined.CreditCard,
                                title = "Payment Details",
                                color = BlobCyan
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            // Payment type toggle
                            PaymentTypeToggle(
                                selectedType = state.paymentType,
                                onTypeChange = { viewModel.onEvent(CreateSplitEvent.PaymentTypeChanged(it)) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Payment value input with inline clear button
                            PaymentInputField(
                                value = state.paymentValue,
                                onValueChange = { viewModel.onEvent(CreateSplitEvent.PaymentValueChanged(it)) },
                                onClear = { viewModel.onEvent(CreateSplitEvent.ClearPaymentValue) },
                                placeholder = if (state.paymentType == PaymentType.IBAN)
                                    "GB82 WEST 1234 5698 7654 32" else "1234 5678 9012 3456",
                                keyboardType = if (state.paymentType == PaymentType.CARD)
                                    KeyboardType.Number else KeyboardType.Text,
                                enabled = state.totalAmount.isNotEmpty()
                            )

                            if (state.totalAmount.isEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Enter amount first",
                                    color = BlobYellow,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SmallActionButton(
                                    text = "Paste",
                                    icon = Icons.Outlined.ContentPaste,
                                    color = BlobCyan,
                                    onClick = { viewModel.onEvent(CreateSplitEvent.PastePaymentValue) },
                                    enabled = state.totalAmount.isNotEmpty(),
                                    modifier = Modifier.weight(1f)
                                )
                                SmallActionButton(
                                    text = "Copy",
                                    icon = Icons.Outlined.ContentCopy,
                                    color = BlobPurple,
                                    onClick = { viewModel.onEvent(CreateSplitEvent.CopyPaymentValue) },
                                    enabled = state.paymentValue.isNotBlank(),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Recipients Card - numberOfPeople is the number of recipients to add
                val requiredRecipients = state.numberOfPeople

                // Recipients Card
                item {
                    GlassCard {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CardHeader(
                                    icon = Icons.Outlined.Send,
                                    title = "Send To",
                                    color = BlobYellow
                                )
                                if (requiredRecipients > 0) {
                                    CountBadge(
                                        current = state.participants.size,
                                        total = requiredRecipients
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Add recipient buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AddRecipientButton(
                                    icon = Icons.Outlined.Contacts,
                                    label = "Contacts",
                                    color = BlobPurple,
                                    onClick = { viewModel.onEvent(CreateSplitEvent.NavigateToContactPicker) },
                                    modifier = Modifier.weight(1f)
                                )
                                AddRecipientButton(
                                    icon = Icons.Outlined.Phone,
                                    label = "Phone",
                                    color = BlobCyan,
                                    onClick = { viewModel.onEvent(CreateSplitEvent.ShowAddPhoneDialog) },
                                    modifier = Modifier.weight(1f)
                                )
                                AddRecipientButton(
                                    icon = Icons.Outlined.Email,
                                    label = "Email",
                                    color = BlobYellow,
                                    onClick = { viewModel.onEvent(CreateSplitEvent.ShowAddEmailDialog) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Participants list
                            if (state.participants.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                state.participants.forEach { participant ->
                                    ParticipantChip(
                                        participant = participant,
                                        onRemove = { viewModel.onEvent(CreateSplitEvent.RemoveParticipant(participant.id)) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                // Note Card
                item {
                    GlassCard {
                        Column {
                            CardHeader(
                                icon = Icons.Outlined.Notes,
                                title = "Note",
                                color = BlobPink
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            DarkTextField(
                                value = state.note,
                                onValueChange = { viewModel.onEvent(CreateSplitEvent.NoteChanged(it)) },
                                placeholder = "What's this for? 🍕",
                                minLines = 2
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // Bottom Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DarkBackground.copy(alpha = 0.9f),
                                DarkBackground
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                GradientActionButton(
                    text = "Review & Share",
                    onClick = { viewModel.onEvent(CreateSplitEvent.ProceedToReview) },
                    enabled = state.participants.isNotEmpty() || state.includeYourself,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Modals
    if (state.showCurrencyPicker) {
        CurrencyPickerSheet(
            selectedCurrency = state.currency,
            onSelect = {
                viewModel.onEvent(CreateSplitEvent.CurrencyChanged(it))
                viewModel.onEvent(CreateSplitEvent.HideCurrencyPicker)
            },
            onDismiss = { viewModel.onEvent(CreateSplitEvent.HideCurrencyPicker) }
        )
    }

    if (state.showAddPhoneDialog) {
        AddContactDialog(
            title = "Add by Phone",
            icon = Icons.Outlined.Phone,
            color = BlobCyan,
            placeholder = "+1 234 567 8900",
            keyboardType = KeyboardType.Phone,
            onDismiss = { viewModel.onEvent(CreateSplitEvent.HideAddPhoneDialog) },
            onConfirm = { contact, name ->
                viewModel.onEvent(CreateSplitEvent.AddPhoneParticipant(contact, name))
            }
        )
    }

    if (state.showAddEmailDialog) {
        AddContactDialog(
            title = "Add by Email",
            icon = Icons.Outlined.Email,
            color = BlobYellow,
            placeholder = "email@example.com",
            keyboardType = KeyboardType.Email,
            onDismiss = { viewModel.onEvent(CreateSplitEvent.HideAddEmailDialog) },
            onConfirm = { contact, name ->
                viewModel.onEvent(CreateSplitEvent.AddEmailParticipant(contact, name))
            }
        )
    }

    state.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CreateSplitEvent.DismissError)
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardGlass.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = TextWhite.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
private fun CardHeader(
    icon: ImageVector,
    title: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = TextWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun AmountInput(
    amount: String,
    currency: Currency,
    onAmountChange: (String) -> Unit,
    onCurrencyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Currency button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(BlobPink.copy(alpha = 0.2f))
                .clickable { onCurrencyClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currency.symbol,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlobPink
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = BlobPink
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Amount input
        BasicTextField(
            value = amount,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    onAmountChange(newValue)
                }
            },
            modifier = Modifier.weight(1f),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (amount.isEmpty()) {
                        Text(
                            "0.00",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun PeopleSelector(
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Number of people",
            color = TextGray,
            fontSize = 14.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { if (count > 2) onCountChange(count - 1) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardGlass)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextWhite)
            }

            Text(
                text = "$count",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BlobPurple,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { if (count < 30) onCountChange(count + 1) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CardGlass)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextWhite)
            }
        }
    }
}

@Composable
private fun ToggleOption(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    sublabel: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (checked) SuccessGreen.copy(alpha = 0.15f)
                else CardGlass.copy(alpha = 0.5f)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (checked) SuccessGreen else TextGray.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = if (checked) SuccessGreen else TextWhite,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = sublabel,
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SplitAmountPreview(
    totalAmount: Double,
    numberOfPeople: Int,  // This is the number of recipients (others)
    includeYourself: Boolean,
    currency: Currency
) {
    // numberOfPeople = number of recipients to send to
    // If includeYourself is true, total splitters = numberOfPeople + 1 (you)
    // If false, total splitters = numberOfPeople (only others pay)
    val totalSplitters = if (includeYourself) numberOfPeople + 1 else numberOfPeople
    val perPerson = if (totalSplitters > 0) totalAmount / totalSplitters else 0.0
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BlobPink, BlobPurple, BlobCyan)
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Each person pays",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = currency.symbol,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatter.format(perPerson),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // People avatars - show totalSplitters count
            Row {
                val colors = listOf(BlobYellow, BlobCyan, BlobPink, BlobPurple)
                val displayCount = minOf(totalSplitters, 4)
                repeat(displayCount) { index ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = (-12 * index).dp)
                            .border(2.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .background(colors[index % colors.size]),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index == 0 && includeYourself) {
                            Text(
                                "You",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                if (totalSplitters > 4) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .offset(x = (-48).dp)
                            .border(2.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+${totalSplitters - 4}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentTypeToggle(
    selectedType: PaymentType,
    onTypeChange: (PaymentType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGlass.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        listOf(PaymentType.IBAN to "IBAN", PaymentType.CARD to "Card").forEach { (type, label) ->
            val selected = selectedType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) BlobCyan.copy(alpha = 0.3f) else Color.Transparent
                    )
                    .clickable { onTypeChange(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) BlobCyan else TextGray,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.5f)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedBorderColor = BlobCyan.copy(alpha = 0.5f),
            unfocusedBorderColor = TextGray.copy(alpha = 0.2f),
            focusedContainerColor = CardGlass.copy(alpha = 0.3f),
            unfocusedContainerColor = CardGlass.copy(alpha = 0.3f),
            cursorColor = BlobCyan
        ),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines,
        singleLine = minLines == 1
    )
}

@Composable
private fun PaymentInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.5f)) },
        trailingIcon = {
            if (value.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            disabledTextColor = TextGray,
            focusedBorderColor = BlobCyan.copy(alpha = 0.5f),
            unfocusedBorderColor = TextGray.copy(alpha = 0.2f),
            disabledBorderColor = TextGray.copy(alpha = 0.1f),
            focusedContainerColor = CardGlass.copy(alpha = 0.3f),
            unfocusedContainerColor = CardGlass.copy(alpha = 0.3f),
            disabledContainerColor = CardGlass.copy(alpha = 0.15f),
            cursorColor = BlobCyan
        ),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
private fun SmallActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) color.copy(alpha = 0.2f) else CardGlass.copy(alpha = 0.3f)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) color else TextGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (enabled) color else TextGray,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun CountBadge(
    current: Int,
    total: Int
) {
    val complete = current >= total
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (complete) SuccessGreen.copy(alpha = 0.2f)
                else BlobYellow.copy(alpha = 0.2f)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$current / $total",
            color = if (complete) SuccessGreen else BlobYellow,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun AddRecipientButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ParticipantChip(
    participant: Participant,
    onRemove: () -> Unit
) {
    val colors = listOf(BlobPink, BlobPurple, BlobCyan, BlobYellow)
    val color = colors[participant.id.hashCode().mod(colors.size).let { if (it < 0) -it else it }]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (participant.name.firstOrNull() ?: participant.contactValue.firstOrNull() ?: '?')
                    .uppercase().toString(),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info - name on top, contact below (truncated)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = participant.name.ifBlank { "Contact" },
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = participant.contactValue,
                color = TextGray,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = ErrorRed.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GradientActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
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
                if (enabled) {
                    Brush.horizontalGradient(
                        colors = listOf(BlobPink, BlobPurple, BlobCyan)
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(CardGlass, CardGlass)
                    )
                }
            )
            .clickable(enabled = enabled) {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = if (enabled) Color.White else TextGray,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (enabled) Color.White else TextGray
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

// Currency Picker Sheet
@Composable
private fun CurrencyPickerSheet(
    selectedCurrency: Currency,
    onSelect: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(DarkSurface)
                    .clickable(enabled = false) { }
                    .padding(24.dp)
            ) {
                // Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextGray.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Currency",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Currency.entries.forEach { currency ->
                    val selected = currency == selectedCurrency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) BlobPink.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable { onSelect(currency) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) BlobPink else CardGlass
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currency.symbol,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else TextWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currency.code,
                                color = if (selected) BlobPink else TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currency.displayName,
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }

                        if (selected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = BlobPink
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Add Contact Dialog
@Composable
private fun AddContactDialog(
    title: String,
    icon: ImageVector,
    color: Color,
    placeholder: String,
    keyboardType: KeyboardType,
    onDismiss: () -> Unit,
    onConfirm: (contact: String, name: String) -> Unit
) {
    var contact by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(DarkSurface)
                    .clickable(enabled = false) { }
                    .padding(24.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                DarkTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    placeholder = placeholder,
                    keyboardType = keyboardType
                )

                Spacer(modifier = Modifier.height(12.dp))

                DarkTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Name (optional)"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextWhite
                        ),
                        border = BorderStroke(1.dp, TextGray.copy(alpha = 0.3f))
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onConfirm(contact, name) },
                        enabled = contact.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = color,
                            disabledContainerColor = CardGlass
                        )
                    ) {
                        Text("Add", color = if (contact.isNotBlank()) Color.White else TextGray)
                    }
                }
            }
        }
    }
}

// Helper function for animated blob drawing
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAnimatedBlob(
    center: Offset,
    baseRadius: Float,
    morphPhase: Float,
    color: Color
) {
    val path = Path()
    val points = 80

    for (i in 0 until points) {
        val angle = (i.toFloat() / points) * 2 * Math.PI

        // Multiple harmonics for organic shape
        val r1 = 1f + 0.2f * sin(angle * 2 + morphPhase).toFloat()
        val r2 = 1f + 0.15f * cos(angle * 3 + morphPhase * 0.7f).toFloat()
        val r3 = 1f + 0.1f * sin(angle * 4 + morphPhase * 1.3f).toFloat()

        val radiusMod = (r1 * r2 * r3) * 0.85f
        val radius = baseRadius * radiusMod

        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    path.close()

    drawPath(path = path, color = color, style = Fill)
}

