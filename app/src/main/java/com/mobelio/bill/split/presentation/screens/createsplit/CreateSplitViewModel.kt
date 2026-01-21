package com.mobelio.bill.split.presentation.screens.createsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobelio.bill.split.domain.model.ContactMethod
import com.mobelio.bill.split.domain.model.Participant
import com.mobelio.bill.split.domain.model.PaymentDetails
import com.mobelio.bill.split.domain.model.SplitMode
import com.mobelio.bill.split.domain.usecase.CalculateSplitUseCase
import com.mobelio.bill.split.domain.usecase.ValidationUseCase
import com.mobelio.bill.split.presentation.state.BillSplitStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateSplitViewModel @Inject constructor(
    private val calculateSplitUseCase: CalculateSplitUseCase,
    private val validationUseCase: ValidationUseCase,
    private val billSplitStateHolder: BillSplitStateHolder
) : ViewModel() {

    private val _state = MutableStateFlow(CreateSplitState())
    val state: StateFlow<CreateSplitState> = _state.asStateFlow()

    private val _effects = Channel<CreateSplitEffect>()
    val effects = _effects.receiveAsFlow()

    init {
        // Load any existing state from the state holder
        val existingSplit = billSplitStateHolder.getCurrentSplit()
        _state.update { state ->
            state.copy(
                currency = existingSplit.currency,
                totalAmount = if (existingSplit.totalAmount > 0) existingSplit.totalAmount.toString() else "",
                paymentType = existingSplit.paymentDetails.type,
                paymentValue = existingSplit.paymentDetails.value,
                participants = existingSplit.participants,
                splitMode = existingSplit.splitMode,
                note = existingSplit.note,
                numberOfPeople = existingSplit.numberOfPeople,
                includeYourself = existingSplit.includeYourself
            )
        }
        recalculateSplit()
    }

    fun onEvent(event: CreateSplitEvent) {
        when (event) {
            is CreateSplitEvent.CurrencyChanged -> {
                _state.update { it.copy(currency = event.currency) }
                billSplitStateHolder.updateCurrency(event.currency)
            }

            is CreateSplitEvent.TotalAmountChanged -> {
                _state.update { it.copy(totalAmount = event.amount, totalAmountError = null) }
                recalculateSplit()
            }

            is CreateSplitEvent.PaymentTypeChanged -> {
                _state.update { it.copy(paymentType = event.type, paymentValueError = null) }
                billSplitStateHolder.updatePaymentType(event.type)
            }

            is CreateSplitEvent.PaymentValueChanged -> {
                _state.update { it.copy(paymentValue = event.value, paymentValueError = null) }
                billSplitStateHolder.updatePaymentValue(event.value)
            }

            is CreateSplitEvent.SplitModeChanged -> {
                _state.update { it.copy(splitMode = event.mode) }
                billSplitStateHolder.updateSplitMode(event.mode)
                recalculateSplit()
            }

            is CreateSplitEvent.NoteChanged -> {
                _state.update { it.copy(note = event.note) }
                billSplitStateHolder.updateNote(event.note)
            }

            is CreateSplitEvent.NumberOfPeopleChanged -> {
                _state.update { it.copy(numberOfPeople = event.count) }
                recalculateSplit()
            }

            is CreateSplitEvent.IncludeYourselfChanged -> {
                _state.update { it.copy(includeYourself = event.include) }
                recalculateSplit()
            }

            is CreateSplitEvent.ParticipantAmountChanged -> {
                val amount = event.amount.toDoubleOrNull() ?: 0.0
                updateParticipantAmount(event.participantId, amount)
            }

            is CreateSplitEvent.RemoveParticipant -> {
                removeParticipant(event.participantId)
            }

            CreateSplitEvent.ShowAddPhoneDialog -> {
                _state.update { it.copy(showAddPhoneDialog = true) }
            }

            CreateSplitEvent.HideAddPhoneDialog -> {
                _state.update { it.copy(showAddPhoneDialog = false) }
            }

            is CreateSplitEvent.AddPhoneParticipant -> {
                addPhoneParticipant(event.phone, event.name)
            }

            CreateSplitEvent.ShowAddEmailDialog -> {
                _state.update { it.copy(showAddEmailDialog = true) }
            }

            CreateSplitEvent.HideAddEmailDialog -> {
                _state.update { it.copy(showAddEmailDialog = false) }
            }

            is CreateSplitEvent.AddEmailParticipant -> {
                addEmailParticipant(event.email, event.name)
            }

            CreateSplitEvent.ShowCurrencyPicker -> {
                _state.update { it.copy(showCurrencyPicker = true) }
            }

            CreateSplitEvent.HideCurrencyPicker -> {
                _state.update { it.copy(showCurrencyPicker = false) }
            }

            CreateSplitEvent.ShowPeopleCountPicker -> {
                _state.update { it.copy(showPeopleCountPicker = true) }
            }

            CreateSplitEvent.HidePeopleCountPicker -> {
                _state.update { it.copy(showPeopleCountPicker = false) }
            }

            CreateSplitEvent.NavigateToContactPicker -> {
                _state.update { it.copy(navigateToContactPicker = true) }
            }

            CreateSplitEvent.NavigatedToContactPicker -> {
                _state.update { it.copy(navigateToContactPicker = false) }
            }

            CreateSplitEvent.PastePaymentValue -> {
                viewModelScope.launch {
                    _effects.send(CreateSplitEffect.RequestClipboardPaste)
                }
            }

            CreateSplitEvent.CopyPaymentValue -> {
                viewModelScope.launch {
                    _effects.send(CreateSplitEffect.CopyToClipboard(_state.value.paymentValue))
                    _effects.send(CreateSplitEffect.ShowToast("Copied to clipboard"))
                }
            }

            CreateSplitEvent.NextStep -> {
                val currentStep = _state.value.currentStep
                if (currentStep < 3) {
                    _state.update { it.copy(currentStep = currentStep + 1) }
                }
            }

            CreateSplitEvent.PreviousStep -> {
                val currentStep = _state.value.currentStep
                if (currentStep > 0) {
                    _state.update { it.copy(currentStep = currentStep - 1) }
                }
            }

            is CreateSplitEvent.GoToStep -> {
                _state.update { it.copy(currentStep = event.step) }
            }

            CreateSplitEvent.ProceedToReview -> {
                validateAndProceed()
            }

            CreateSplitEvent.NavigatedToReview -> {
                _state.update { it.copy(navigateToReview = false) }
            }

            CreateSplitEvent.DismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    fun onPasteResult(text: String) {
        _state.update { it.copy(paymentValue = text, paymentValueError = null) }
        billSplitStateHolder.updatePaymentValue(text)
    }

    /**
     * Called when returning from contact picker with new contacts
     */
    fun refreshParticipantsFromStateHolder() {
        val currentSplit = billSplitStateHolder.getCurrentSplit()
        _state.update { it.copy(participants = currentSplit.participants) }
        recalculateSplit()
    }

    private fun addPhoneParticipant(phone: String, name: String) {
        val validation = validationUseCase.validatePhone(phone)
        if (!validation.isValid) {
            _state.update { it.copy(errorMessage = validation.errorMessage) }
            return
        }

        val participant = Participant(
            name = name,
            contactValue = phone,
            contactMethod = ContactMethod.PHONE,
            isFromContacts = false
        )

        _state.update { state ->
            state.copy(
                participants = state.participants + participant,
                showAddPhoneDialog = false
            )
        }
        billSplitStateHolder.addParticipant(participant)
        recalculateSplit()
    }

    private fun addEmailParticipant(email: String, name: String) {
        val validation = validationUseCase.validateEmail(email)
        if (!validation.isValid) {
            _state.update { it.copy(errorMessage = validation.errorMessage) }
            return
        }

        val participant = Participant(
            name = name,
            contactValue = email,
            contactMethod = ContactMethod.EMAIL,
            isFromContacts = false
        )

        _state.update { state ->
            state.copy(
                participants = state.participants + participant,
                showAddEmailDialog = false
            )
        }
        billSplitStateHolder.addParticipant(participant)
        recalculateSplit()
    }

    private fun removeParticipant(participantId: String) {
        _state.update { state ->
            state.copy(participants = state.participants.filter { it.id != participantId })
        }
        billSplitStateHolder.removeParticipant(participantId)
        recalculateSplit()
    }

    private fun updateParticipantAmount(participantId: String, amount: Double) {
        _state.update { state ->
            state.copy(
                participants = state.participants.map { p ->
                    if (p.id == participantId) p.copy(amount = amount) else p
                }
            )
        }
        billSplitStateHolder.updateParticipantAmount(participantId, amount)
        recalculateSplit()
    }

    private fun recalculateSplit() {
        val currentState = _state.value
        val totalAmount = currentState.totalAmount.toDoubleOrNull() ?: 0.0
        val numberOfPeople = currentState.numberOfPeople

        if (totalAmount > 0 && numberOfPeople > 0) {
            val perPerson = totalAmount / numberOfPeople
            val yourShare = if (currentState.includeYourself) perPerson else 0.0

            _state.update { state ->
                state.copy(
                    calculatedAmountPerPerson = perPerson,
                    yourShare = yourShare
                )
            }

            // Update participants with calculated amounts if in EQUAL mode
            if (currentState.splitMode == SplitMode.EQUAL && currentState.participants.isNotEmpty()) {
                val result = calculateSplitUseCase.execute(
                    totalAmount = totalAmount,
                    participants = currentState.participants,
                    splitMode = SplitMode.EQUAL
                )
                _state.update { state ->
                    state.copy(
                        participants = result.participants,
                        hasRoundingAdjustment = result.hasRoundingAdjustment,
                        adjustmentAmount = result.adjustmentApplied
                    )
                }
                billSplitStateHolder.updateParticipants(result.participants)
            }

            billSplitStateHolder.updateTotalAmount(totalAmount)
        }

        // Manual mode sum
        if (currentState.splitMode == SplitMode.MANUAL) {
            val sum = currentState.participants.sumOf { it.amount }
            _state.update { state ->
                state.copy(manualTotalSum = sum)
            }
        }
    }

    private fun validateAndProceed() {
        val currentState = _state.value
        var hasError = false

        // Validate payment details
        val paymentValidation = validationUseCase.validatePaymentDetails(
            currentState.paymentValue,
            currentState.paymentType
        )
        if (!paymentValidation.isValid) {
            _state.update { it.copy(paymentValueError = paymentValidation.errorMessage) }
            hasError = true
        }

        // Validate we have recipients
        val requiredRecipients = if (currentState.includeYourself) currentState.numberOfPeople - 1 else currentState.numberOfPeople
        if (requiredRecipients > 0 && currentState.participants.isEmpty()) {
            _state.update { it.copy(errorMessage = "Please add at least one recipient to send the request") }
            hasError = true
        }

        // Validate amount
        val totalAmount = currentState.totalAmount.toDoubleOrNull()
        if (totalAmount == null || totalAmount <= 0) {
            _state.update { it.copy(totalAmountError = "Please enter a valid total amount") }
            hasError = true
        }

        if (!hasError) {
            // Calculate and set amounts for participants
            val total = currentState.totalAmount.toDoubleOrNull() ?: 0.0
            val perPerson = if (currentState.numberOfPeople > 0) total / currentState.numberOfPeople else 0.0

            val updatedParticipants = currentState.participants.map {
                it.copy(amount = perPerson)
            }

            billSplitStateHolder.updateParticipants(updatedParticipants)
            billSplitStateHolder.updatePaymentDetails(
                PaymentDetails(
                    type = currentState.paymentType,
                    value = currentState.paymentValue
                )
            )
            billSplitStateHolder.updateTotalAmount(total)

            _state.update { it.copy(navigateToReview = true) }
        }
    }
}

