package com.mobelio.bill.split.presentation.state

import com.mobelio.bill.split.domain.model.BillSplit
import com.mobelio.bill.split.domain.model.Currency
import com.mobelio.bill.split.domain.model.Participant
import com.mobelio.bill.split.domain.model.PaymentDetails
import com.mobelio.bill.split.domain.model.PaymentType
import com.mobelio.bill.split.domain.model.SplitMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared state holder for bill split data across screens
 */
@Singleton
class BillSplitStateHolder @Inject constructor() {

    private val _billSplit = MutableStateFlow(BillSplit())
    val billSplit: StateFlow<BillSplit> = _billSplit.asStateFlow()

    fun updateCurrency(currency: Currency) {
        _billSplit.update { it.copy(currency = currency) }
    }

    fun updateTotalAmount(amount: Double) {
        _billSplit.update { it.copy(totalAmount = amount) }
    }

    fun updatePaymentDetails(paymentDetails: PaymentDetails) {
        _billSplit.update { it.copy(paymentDetails = paymentDetails) }
    }

    fun updatePaymentType(type: PaymentType) {
        _billSplit.update {
            it.copy(paymentDetails = it.paymentDetails.copy(type = type))
        }
    }

    fun updatePaymentValue(value: String) {
        _billSplit.update {
            it.copy(paymentDetails = it.paymentDetails.copy(value = value))
        }
    }

    fun updateSplitMode(mode: SplitMode) {
        _billSplit.update { it.copy(splitMode = mode) }
    }

    fun updateNote(note: String) {
        _billSplit.update { it.copy(note = note) }
    }

    fun addParticipant(participant: Participant) {
        _billSplit.update {
            it.copy(participants = it.participants + participant)
        }
    }

    fun addParticipants(participants: List<Participant>) {
        _billSplit.update {
            it.copy(participants = it.participants + participants)
        }
    }

    fun removeParticipant(participantId: String) {
        _billSplit.update {
            it.copy(participants = it.participants.filter { p -> p.id != participantId })
        }
    }

    fun updateParticipant(participant: Participant) {
        _billSplit.update { split ->
            split.copy(
                participants = split.participants.map {
                    if (it.id == participant.id) participant else it
                }
            )
        }
    }

    fun updateParticipantAmount(participantId: String, amount: Double) {
        _billSplit.update { split ->
            split.copy(
                participants = split.participants.map {
                    if (it.id == participantId) it.copy(amount = amount) else it
                }
            )
        }
    }

    fun updateParticipants(participants: List<Participant>) {
        _billSplit.update { it.copy(participants = participants) }
    }

    fun reset() {
        _billSplit.value = BillSplit()
    }

    fun getCurrentSplit(): BillSplit = _billSplit.value
}

