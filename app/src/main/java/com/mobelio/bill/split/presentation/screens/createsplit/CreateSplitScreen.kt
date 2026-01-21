package com.mobelio.bill.split.presentation.screens.createsplit

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.mobelio.bill.split.presentation.components.*
import com.mobelio.bill.split.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

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

    // Refresh participants when returning from contact picker
    LaunchedEffect(Unit) {
        viewModel.refreshParticipantsFromStateHolder()
    }

    // Handle effects
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
        // Background decorations
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = 100.dp)
                .blur(60.dp)
                .background(GradientStart.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 200.dp)
                .blur(50.dp)
                .background(AccentPink.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        "Create Split",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Main content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Amount Section
                item {
                    AmountSection(
                        totalAmount = state.totalAmount,
                        currency = state.currency,
                        error = state.totalAmountError,
                        onAmountChanged = { viewModel.onEvent(CreateSplitEvent.TotalAmountChanged(it)) },
                        onCurrencyClick = { viewModel.onEvent(CreateSplitEvent.ShowCurrencyPicker) }
                    )
                }

                // Split Configuration
                item {
                    SplitConfigSection(
                        numberOfPeople = state.numberOfPeople,
                        includeYourself = state.includeYourself,
                        totalAmount = state.totalAmount.toDoubleOrNull() ?: 0.0,
                        currency = state.currency,
                        onPeopleCountClick = { viewModel.onEvent(CreateSplitEvent.ShowPeopleCountPicker) },
                        onIncludeYourselfChanged = { viewModel.onEvent(CreateSplitEvent.IncludeYourselfChanged(it)) }
                    )
                }

                // Split Preview
                if (state.totalAmount.isNotEmpty() && state.numberOfPeople > 0) {
                    item {
                        SplitPreviewCard(
                            totalAmount = state.totalAmount.toDoubleOrNull() ?: 0.0,
                            numberOfPeople = state.numberOfPeople,
                            includeYourself = state.includeYourself,
                            currency = state.currency
                        )
                    }
                }

                // Payment Details
                item {
                    PaymentSection(
                        paymentType = state.paymentType,
                        paymentValue = state.paymentValue,
                        error = state.paymentValueError,
                        onTypeChanged = { viewModel.onEvent(CreateSplitEvent.PaymentTypeChanged(it)) },
                        onValueChanged = { viewModel.onEvent(CreateSplitEvent.PaymentValueChanged(it)) },
                        onPaste = { viewModel.onEvent(CreateSplitEvent.PastePaymentValue) },
                        onCopy = { viewModel.onEvent(CreateSplitEvent.CopyPaymentValue) }
                    )
                }

                // Participants Section
                item {
                    ParticipantsSection(
                        participants = state.participants,
                        requiredCount = if (state.includeYourself) state.numberOfPeople - 1 else state.numberOfPeople,
                        currency = state.currency,
                        splitMode = state.splitMode,
                        onAddFromContacts = { viewModel.onEvent(CreateSplitEvent.NavigateToContactPicker) },
                        onAddPhone = { viewModel.onEvent(CreateSplitEvent.ShowAddPhoneDialog) },
                        onAddEmail = { viewModel.onEvent(CreateSplitEvent.ShowAddEmailDialog) },
                        onRemove = { viewModel.onEvent(CreateSplitEvent.RemoveParticipant(it)) },
                        onAmountChanged = { id, amount ->
                            viewModel.onEvent(CreateSplitEvent.ParticipantAmountChanged(id, amount))
                        }
                    )
                }

                // Note
                item {
                    NoteSection(
                        note = state.note,
                        onNoteChanged = { viewModel.onEvent(CreateSplitEvent.NoteChanged(it)) }
                    )
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            // Bottom Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                GradientButton(
                    text = "Review & Share",
                    onClick = { viewModel.onEvent(CreateSplitEvent.ProceedToReview) },
                    icon = Icons.Default.ArrowForward,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.participants.isNotEmpty() || state.includeYourself
                )
            }
        }
    }

    // Currency Picker Modal
    if (state.showCurrencyPicker) {
        CurrencyPickerModal(
            selectedCurrency = state.currency,
            onCurrencySelected = {
                viewModel.onEvent(CreateSplitEvent.CurrencyChanged(it))
                viewModel.onEvent(CreateSplitEvent.HideCurrencyPicker)
            },
            onDismiss = { viewModel.onEvent(CreateSplitEvent.HideCurrencyPicker) }
        )
    }

    // People Count Picker Modal
    if (state.showPeopleCountPicker) {
        PeopleCountPickerModal(
            currentCount = state.numberOfPeople,
            onCountSelected = {
                viewModel.onEvent(CreateSplitEvent.NumberOfPeopleChanged(it))
                viewModel.onEvent(CreateSplitEvent.HidePeopleCountPicker)
            },
            onDismiss = { viewModel.onEvent(CreateSplitEvent.HidePeopleCountPicker) }
        )
    }

    // Add Phone Dialog
    if (state.showAddPhoneDialog) {
        AddParticipantModal(
            title = "Add by Phone",
            icon = Icons.Outlined.Phone,
            color = AccentGreen,
            contactLabel = "Phone Number",
            contactPlaceholder = "+1 234 567 8900",
            keyboardType = KeyboardType.Phone,
            onDismiss = { viewModel.onEvent(CreateSplitEvent.HideAddPhoneDialog) },
            onConfirm = { contact, name ->
                viewModel.onEvent(CreateSplitEvent.AddPhoneParticipant(contact, name))
            }
        )
    }

    // Add Email Dialog
    if (state.showAddEmailDialog) {
        AddParticipantModal(
            title = "Add by Email",
            icon = Icons.Outlined.Email,
            color = AccentOrange,
            contactLabel = "Email Address",
            contactPlaceholder = "email@example.com",
            keyboardType = KeyboardType.Email,
            onDismiss = { viewModel.onEvent(CreateSplitEvent.HideAddEmailDialog) },
            onConfirm = { contact, name ->
                viewModel.onEvent(CreateSplitEvent.AddEmailParticipant(contact, name))
            }
        )
    }

    // Error snackbar
    state.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CreateSplitEvent.DismissError)
        }
    }
}

@Composable
private fun AmountSection(
    totalAmount: String,
    currency: Currency,
    error: String?,
    onAmountChanged: (String) -> Unit,
    onCurrencyClick: () -> Unit
) {
    AnimatedCard {
        Column {
            SectionHeader(
                title = "Total Amount",
                icon = Icons.Outlined.Payments,
                color = GradientStart
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency selector
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(GradientStart.copy(alpha = 0.1f))
                        .clickable { onCurrencyClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currency.symbol,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientStart
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = GradientStart
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Amount input
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            onAmountChanged(newValue)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("0.00", color = TextSecondaryLight.copy(alpha = 0.5f))
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GradientStart,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = error,
                    color = ErrorRed,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SplitConfigSection(
    numberOfPeople: Int,
    includeYourself: Boolean,
    totalAmount: Double,
    currency: Currency,
    onPeopleCountClick: () -> Unit,
    onIncludeYourselfChanged: (Boolean) -> Unit
) {
    AnimatedCard {
        Column {
            SectionHeader(
                title = "Split Between",
                icon = Icons.Outlined.Groups,
                color = AccentPurple
            )

            Spacer(modifier = Modifier.height(16.dp))

            // People count selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Number of people",
                    color = TextSecondaryLight,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentPurple.copy(alpha = 0.1f))
                        .clickable { onPeopleCountClick() }
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$numberOfPeople",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.People,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Include yourself toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (includeYourself) AccentGreen.copy(alpha = 0.1f)
                        else Color.Gray.copy(alpha = 0.05f)
                    )
                    .clickable { onIncludeYourselfChanged(!includeYourself) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (includeYourself) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (includeYourself) AccentGreen else TextSecondaryLight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Include myself",
                        fontWeight = FontWeight.Medium,
                        color = if (includeYourself) AccentGreen else TextPrimaryLight
                    )
                    Text(
                        text = "I'm also part of this split",
                        fontSize = 12.sp,
                        color = TextSecondaryLight
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitPreviewCard(
    totalAmount: Double,
    numberOfPeople: Int,
    includeYourself: Boolean,
    currency: Currency
) {
    val perPerson = if (numberOfPeople > 0) totalAmount / numberOfPeople else 0.0
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = GradientStart.copy(alpha = 0.2f),
                spotColor = GradientMiddle.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientMiddle,
                        AccentPink
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Each person pays",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currency.symbol,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatter.format(perPerson),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                repeat(minOf(numberOfPeople, 5)) { index ->
                    val colors = listOf(AccentYellow, AccentGreen, AccentBlue, AccentOrange, AccentPink)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .offset(x = (-8 * index).dp)
                            .border(2.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .background(colors[index % colors.size]),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index == 0 && includeYourself) {
                            Text("Me", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                if (numberOfPeople > 5) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .offset(x = (-40).dp)
                            .border(2.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+${numberOfPeople - 5}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSection(
    paymentType: PaymentType,
    paymentValue: String,
    error: String?,
    onTypeChanged: (PaymentType) -> Unit,
    onValueChanged: (String) -> Unit,
    onPaste: () -> Unit,
    onCopy: () -> Unit
) {
    AnimatedCard {
        Column {
            SectionHeader(
                title = "Payment Details",
                icon = Icons.Outlined.CreditCard,
                color = AccentBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Payment type tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
                    .padding(4.dp)
            ) {
                PaymentTypeTab(
                    text = "IBAN",
                    icon = Icons.Outlined.AccountBalance,
                    selected = paymentType == PaymentType.IBAN,
                    onClick = { onTypeChanged(PaymentType.IBAN) },
                    modifier = Modifier.weight(1f)
                )
                PaymentTypeTab(
                    text = "Card",
                    icon = Icons.Outlined.CreditCard,
                    selected = paymentType == PaymentType.CARD,
                    onClick = { onTypeChanged(PaymentType.CARD) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = paymentValue,
                onValueChange = onValueChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (paymentType == PaymentType.IBAN) "GB82 WEST 1234 5698 7654 32"
                        else "1234 5678 9012 3456"
                    )
                },
                isError = error != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (paymentType == PaymentType.CARD) KeyboardType.Number
                    else KeyboardType.Text
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = error, color = ErrorRed, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    text = "Paste",
                    icon = Icons.Outlined.ContentPaste,
                    color = AccentBlue,
                    onClick = onPaste,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    text = "Copy",
                    icon = Icons.Outlined.ContentCopy,
                    color = AccentPurple,
                    onClick = onCopy,
                    enabled = paymentValue.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PaymentTypeTab(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tab_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) Color.White else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) AccentBlue else TextSecondaryLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) AccentBlue else TextSecondaryLight
            )
        }
    }
}

@Composable
private fun ActionChip(
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
                if (enabled) color.copy(alpha = 0.1f)
                else Color.Gray.copy(alpha = 0.1f)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) color else TextSecondaryLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                color = if (enabled) color else TextSecondaryLight
            )
        }
    }
}

@Composable
private fun ParticipantsSection(
    participants: List<Participant>,
    requiredCount: Int,
    currency: Currency,
    splitMode: SplitMode,
    onAddFromContacts: () -> Unit,
    onAddPhone: () -> Unit,
    onAddEmail: () -> Unit,
    onRemove: (String) -> Unit,
    onAmountChanged: (String, String) -> Unit
) {
    AnimatedCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Send Request To",
                    icon = Icons.Outlined.Send,
                    color = AccentOrange,
                    modifier = Modifier.weight(1f)
                )
                if (requiredCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (participants.size >= requiredCount) AccentGreen.copy(alpha = 0.2f)
                                else AccentOrange.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${participants.size}/$requiredCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (participants.size >= requiredCount) AccentGreen else AccentOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AddButton(
                    text = "Contacts",
                    icon = Icons.Outlined.Contacts,
                    color = AccentPurple,
                    onClick = onAddFromContacts,
                    modifier = Modifier.weight(1f)
                )
                AddButton(
                    text = "Phone",
                    icon = Icons.Outlined.Phone,
                    color = AccentGreen,
                    onClick = onAddPhone,
                    modifier = Modifier.weight(1f)
                )
                AddButton(
                    text = "Email",
                    icon = Icons.Outlined.Email,
                    color = AccentOrange,
                    onClick = onAddEmail,
                    modifier = Modifier.weight(1f)
                )
            }

            if (participants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                participants.forEach { participant ->
                    ParticipantCard(
                        participant = participant,
                        currency = currency,
                        showAmountInput = splitMode == SplitMode.MANUAL,
                        onRemove = { onRemove(participant.id) },
                        onAmountChanged = { onAmountChanged(participant.id, it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AddButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "add_btn_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun ParticipantCard(
    participant: Participant,
    currency: Currency,
    showAmountInput: Boolean,
    onRemove: () -> Unit,
    onAmountChanged: (String) -> Unit
) {
    val colors = listOf(AccentPurple, AccentGreen, AccentOrange, AccentBlue, AccentPink)
    val color = colors[participant.id.hashCode().mod(colors.size).let { if (it < 0) -it else it }]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (participant.name.firstOrNull() ?: participant.contactValue.firstOrNull() ?: '?').uppercase().toString(),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = participant.name.ifBlank { "Unknown" },
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (participant.contactMethod == ContactMethod.PHONE)
                        Icons.Outlined.Phone else Icons.Outlined.Email,
                    contentDescription = null,
                    tint = TextSecondaryLight,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = participant.contactValue,
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
            }
        }

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Remove",
                tint = ErrorRed,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NoteSection(
    note: String,
    onNoteChanged: (String) -> Unit
) {
    AnimatedCard {
        Column {
            SectionHeader(
                title = "Note (Optional)",
                icon = Icons.Outlined.Notes,
                color = AccentPink
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Dinner at restaurant 🍕") },
                minLines = 2,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPink,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// MODALS

@Composable
private fun CurrencyPickerModal(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
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
                    .background(Color.White)
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
                        .background(Color.Gray.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Currency",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Currency.entries.forEach { currency ->
                    CurrencyItem(
                        currency = currency,
                        selected = currency == selectedCurrency,
                        onClick = { onCurrencySelected(currency) }
                    )
                    if (currency != Currency.entries.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CurrencyItem(
    currency: Currency,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "currency_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) GradientStart.copy(alpha = 0.1f)
                else Color.Gray.copy(alpha = 0.05f)
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) GradientStart else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (selected) GradientStart else Color.Gray.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currency.symbol,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else TextPrimaryLight
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.code,
                fontWeight = FontWeight.Bold,
                color = if (selected) GradientStart else TextPrimaryLight
            )
            Text(
                text = currency.name,
                fontSize = 12.sp,
                color = TextSecondaryLight
            )
        }

        if (selected) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = GradientStart
            )
        }
    }
}

@Composable
private fun PeopleCountPickerModal(
    currentCount: Int,
    onCountSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
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
                    .background(Color.White)
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
                        .background(Color.Gray.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Number of People",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Grid of numbers
                val numbers = (2..10).toList()
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(numbers) { number ->
                        NumberCircle(
                            number = number,
                            selected = number == currentCount,
                            onClick = { onCountSelected(number) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun NumberCircle(
    number: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "number_scale"
    )

    val colors = listOf(AccentPurple, AccentGreen, AccentOrange, AccentBlue, AccentPink, GradientStart, AccentYellow, ErrorRed, GradientMiddle)
    val color = colors[(number - 2) % colors.size]

    Box(
        modifier = Modifier
            .size(60.dp)
            .scale(scale)
            .shadow(
                elevation = if (selected) 12.dp else 4.dp,
                shape = CircleShape,
                ambientColor = color.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(
                if (selected) color else Color.White
            )
            .border(
                width = if (selected) 0.dp else 2.dp,
                color = if (selected) Color.Transparent else color.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else color
        )
    }
}

@Composable
private fun AddParticipantModal(
    title: String,
    icon: ImageVector,
    color: Color,
    contactLabel: String,
    contactPlaceholder: String,
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
                .background(Color.Black.copy(alpha = 0.5f))
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
                    .background(Color.White)
                    .clickable(enabled = false) { }
                    .padding(24.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contact input
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(contactLabel) },
                    placeholder = { Text(contactPlaceholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = color,
                        focusedLabelColor = color
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name (Optional)") },
                    placeholder = { Text("e.g., John") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = color,
                        focusedLabelColor = color
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onConfirm(contact, name) },
                        enabled = contact.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = color
                        )
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

