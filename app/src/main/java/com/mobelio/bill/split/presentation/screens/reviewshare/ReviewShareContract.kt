package com.mobelio.bill.split.presentation.screens.reviewshare

import com.mobelio.bill.split.domain.model.BillSplit
import com.mobelio.bill.split.domain.model.Participant
import com.mobelio.bill.split.domain.model.ShareChannel

data class ReviewShareState(
    val billSplit: BillSplit = BillSplit(),
    val participantMessages: Map<String, String> = emptyMap(), // Participant ID to message
    val availableChannels: Map<String, Set<ShareChannel>> = emptyMap(), // Per participant
    val isWhatsAppInstalled: Boolean = false,
    val isViberInstalled: Boolean = false,
    val isLoading: Boolean = false,
    val shareCompleted: Boolean = false
)

sealed interface ReviewShareEvent {
    data object LoadData : ReviewShareEvent
    data class ShareToParticipant(
        val participantId: String,
        val channel: ShareChannel
    ) : ReviewShareEvent
    data object ShareAll : ReviewShareEvent
    data object CopyAll : ReviewShareEvent
    data object Finish : ReviewShareEvent
}

sealed interface ReviewShareEffect {
    data class ShareIntent(
        val participant: Participant,
        val message: String,
        val channel: ShareChannel
    ) : ReviewShareEffect
    data class ShareAllIntent(val message: String) : ReviewShareEffect
    data class CopyToClipboard(val text: String) : ReviewShareEffect
    data class ShowToast(val message: String) : ReviewShareEffect
}

