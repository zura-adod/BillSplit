package com.mobelio.bill.split.presentation.screens.reviewshare

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobelio.bill.split.domain.model.ContactMethod
import com.mobelio.bill.split.domain.model.ShareChannel
import com.mobelio.bill.split.domain.usecase.GenerateMessageUseCase
import com.mobelio.bill.split.presentation.state.BillSplitStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewShareViewModel @Inject constructor(
    private val generateMessageUseCase: GenerateMessageUseCase,
    private val billSplitStateHolder: BillSplitStateHolder,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewShareState())
    val state: StateFlow<ReviewShareState> = _state.asStateFlow()

    private val _effects = Channel<ReviewShareEffect>()
    val effects = _effects.receiveAsFlow()

    init {
        loadData()
    }

    fun onEvent(event: ReviewShareEvent) {
        when (event) {
            ReviewShareEvent.LoadData -> loadData()

            is ReviewShareEvent.ShareToParticipant -> {
                shareToParticipant(event.participantId, event.channel)
            }

            ReviewShareEvent.ShareAll -> shareAll()

            ReviewShareEvent.CopyAll -> copyAll()

            ReviewShareEvent.Finish -> {
                billSplitStateHolder.reset()
                _state.update { it.copy(shareCompleted = true) }
            }
        }
    }

    private fun loadData() {
        val billSplit = billSplitStateHolder.getCurrentSplit()

        // Check installed apps
        val isWhatsAppInstalled = isPackageInstalled("com.whatsapp")
        val isViberInstalled = isPackageInstalled("com.viber.voip")

        // Generate messages for each participant
        val messages = billSplit.participants.associate { participant ->
            participant.id to generateMessageUseCase.execute(
                participant = participant,
                paymentDetails = billSplit.paymentDetails,
                currency = billSplit.currency,
                note = billSplit.note
            )
        }

        // Determine available channels for each participant
        val availableChannels = billSplit.participants.associate { participant ->
            val channels = mutableSetOf<ShareChannel>()

            when (participant.contactMethod) {
                ContactMethod.PHONE -> {
                    channels.add(ShareChannel.SMS)
                    if (isWhatsAppInstalled) channels.add(ShareChannel.WHATSAPP)
                    if (isViberInstalled) channels.add(ShareChannel.VIBER)
                }
                ContactMethod.EMAIL -> {
                    channels.add(ShareChannel.EMAIL)
                }
            }

            channels.add(ShareChannel.SHARE_SHEET)
            channels.add(ShareChannel.COPY)

            participant.id to channels
        }

        _state.update {
            it.copy(
                billSplit = billSplit,
                participantMessages = messages,
                availableChannels = availableChannels,
                isWhatsAppInstalled = isWhatsAppInstalled,
                isViberInstalled = isViberInstalled
            )
        }
    }

    private fun shareToParticipant(participantId: String, channel: ShareChannel) {
        val currentState = _state.value
        val participant = currentState.billSplit.participants.find { it.id == participantId } ?: return
        val message = currentState.participantMessages[participantId] ?: return

        viewModelScope.launch {
            when (channel) {
                ShareChannel.COPY -> {
                    _effects.send(ReviewShareEffect.CopyToClipboard(message))
                    _effects.send(ReviewShareEffect.ShowToast("Message copied"))
                }
                else -> {
                    _effects.send(ReviewShareEffect.ShareIntent(participant, message, channel))
                }
            }
        }
    }

    private fun shareAll() {
        val currentState = _state.value
        val combinedMessage = generateMessageUseCase.generateCombinedMessage(
            participants = currentState.billSplit.participants,
            paymentDetails = currentState.billSplit.paymentDetails,
            currency = currentState.billSplit.currency,
            totalAmount = currentState.billSplit.totalAmount,
            note = currentState.billSplit.note
        )

        viewModelScope.launch {
            _effects.send(ReviewShareEffect.ShareAllIntent(combinedMessage))
        }
    }

    private fun copyAll() {
        val currentState = _state.value
        val combinedMessage = generateMessageUseCase.generateCombinedMessage(
            participants = currentState.billSplit.participants,
            paymentDetails = currentState.billSplit.paymentDetails,
            currency = currentState.billSplit.currency,
            totalAmount = currentState.billSplit.totalAmount,
            note = currentState.billSplit.note
        )

        viewModelScope.launch {
            _effects.send(ReviewShareEffect.CopyToClipboard(combinedMessage))
            _effects.send(ReviewShareEffect.ShowToast("All messages copied"))
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Generate a combined message for Share All functionality
     */
    fun generateCombinedMessage(): String {
        val currentState = _state.value
        return generateMessageUseCase.generateCombinedMessage(
            participants = currentState.billSplit.participants,
            paymentDetails = currentState.billSplit.paymentDetails,
            currency = currentState.billSplit.currency,
            totalAmount = currentState.billSplit.totalAmount,
            note = currentState.billSplit.note
        )
    }

    /**
     * Generate message for phone recipients only (SMS All)
     */
    fun generatePhoneOnlyMessage(): String {
        val currentState = _state.value
        val phoneParticipants = currentState.billSplit.participants.filter {
            it.contactMethod == ContactMethod.PHONE
        }
        return generateMessageUseCase.generateCombinedMessage(
            participants = phoneParticipants,
            paymentDetails = currentState.billSplit.paymentDetails,
            currency = currentState.billSplit.currency,
            totalAmount = currentState.billSplit.totalAmount,
            note = currentState.billSplit.note
        )
    }

    /**
     * Generate message for email recipients only (Email All)
     */
    fun generateEmailOnlyMessage(): String {
        val currentState = _state.value
        val emailParticipants = currentState.billSplit.participants.filter {
            it.contactMethod == ContactMethod.EMAIL
        }
        return generateMessageUseCase.generateCombinedMessage(
            participants = emailParticipants,
            paymentDetails = currentState.billSplit.paymentDetails,
            currency = currentState.billSplit.currency,
            totalAmount = currentState.billSplit.totalAmount,
            note = currentState.billSplit.note
        )
    }
}
