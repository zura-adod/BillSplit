package com.mobelio.bill.split.presentation.screens.createsplit

import com.mobelio.bill.split.domain.model.Currency
import com.mobelio.bill.split.domain.model.Participant
import com.mobelio.bill.split.domain.model.PaymentType
import com.mobelio.bill.split.domain.model.SplitMode

/**
 * UI State for Create Split Screen
 */
data class CreateSplitState(
    val currency: Currency = Currency.USD,
    val totalAmount: String = "",
    val paymentType: PaymentType = PaymentType.IBAN,
    val paymentValue: String = "",
    val participants: List<Participant> = emptyList(),
    val splitMode: SplitMode = SplitMode.EQUAL,
    val note: String = "",

    // Split configuration
    val numberOfPeople: Int = 2, // Total people including yourself
    val includeYourself: Boolean = true, // Whether you're part of the split

    // Validation states
    val totalAmountError: String? = null,
    val paymentValueError: String? = null,

    // Dialog/Modal states
    val showAddPhoneDialog: Boolean = false,
    val showAddEmailDialog: Boolean = false,
    val showCurrencyPicker: Boolean = false,
    val showPeopleCountPicker: Boolean = false,

    // Calculated values
    val calculatedAmountPerPerson: Double = 0.0,
    val yourShare: Double = 0.0,
    val hasRoundingAdjustment: Boolean = false,
    val adjustmentAmount: Double = 0.0,
    val manualTotalSum: Double = 0.0,
    val manualTotalDifference: Double = 0.0,

    // Loading/navigation
    val isLoading: Boolean = false,
    val navigateToReview: Boolean = false,
    val navigateToContactPicker: Boolean = false,

    // Error
    val errorMessage: String? = null,

    // Current step for wizard-like flow
    val currentStep: Int = 0 // 0: Amount, 1: Split, 2: Payment, 3: Participants
)

/**
 * Events/Intents from UI
 */
sealed interface CreateSplitEvent {
    data class CurrencyChanged(val currency: Currency) : CreateSplitEvent
    data class TotalAmountChanged(val amount: String) : CreateSplitEvent
    data class PaymentTypeChanged(val type: PaymentType) : CreateSplitEvent
    data class PaymentValueChanged(val value: String) : CreateSplitEvent
    data class SplitModeChanged(val mode: SplitMode) : CreateSplitEvent
    data class NoteChanged(val note: String) : CreateSplitEvent

    // Split configuration
    data class NumberOfPeopleChanged(val count: Int) : CreateSplitEvent
    data class IncludeYourselfChanged(val include: Boolean) : CreateSplitEvent

    data class ParticipantAmountChanged(val participantId: String, val amount: String) : CreateSplitEvent
    data class RemoveParticipant(val participantId: String) : CreateSplitEvent

    data object ShowAddPhoneDialog : CreateSplitEvent
    data object HideAddPhoneDialog : CreateSplitEvent
    data class AddPhoneParticipant(val phone: String, val name: String) : CreateSplitEvent

    data object ShowAddEmailDialog : CreateSplitEvent
    data object HideAddEmailDialog : CreateSplitEvent
    data class AddEmailParticipant(val email: String, val name: String) : CreateSplitEvent

    data object ShowCurrencyPicker : CreateSplitEvent
    data object HideCurrencyPicker : CreateSplitEvent

    data object ShowPeopleCountPicker : CreateSplitEvent
    data object HidePeopleCountPicker : CreateSplitEvent

    data object NavigateToContactPicker : CreateSplitEvent
    data object NavigatedToContactPicker : CreateSplitEvent

    data object PastePaymentValue : CreateSplitEvent
    data object CopyPaymentValue : CreateSplitEvent
    data object ClearPaymentValue : CreateSplitEvent

    // Step navigation
    data object NextStep : CreateSplitEvent
    data object PreviousStep : CreateSplitEvent
    data class GoToStep(val step: Int) : CreateSplitEvent

    data object ProceedToReview : CreateSplitEvent
    data object NavigatedToReview : CreateSplitEvent

    data object DismissError : CreateSplitEvent
}

/**
 * Side effects
 */
sealed interface CreateSplitEffect {
    data class ShowToast(val message: String) : CreateSplitEffect
    data class CopyToClipboard(val text: String) : CreateSplitEffect
    data object RequestClipboardPaste : CreateSplitEffect
}

