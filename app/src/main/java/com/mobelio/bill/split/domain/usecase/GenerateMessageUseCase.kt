package com.mobelio.bill.split.domain.usecase

import com.mobelio.bill.split.domain.model.Currency
import com.mobelio.bill.split.domain.model.Participant
import com.mobelio.bill.split.domain.model.PaymentDetails
import com.mobelio.bill.split.domain.model.PaymentType
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Use case for generating payment request messages
 */
class GenerateMessageUseCase @Inject constructor() {

    fun execute(
        participant: Participant,
        paymentDetails: PaymentDetails,
        currency: Currency,
        note: String = "",
        includeGreeting: Boolean = true,
        includeFooter: Boolean = true
    ): String {
        val builder = StringBuilder()

        // Greeting
        if (includeGreeting) {
            val greeting = if (participant.name.isNotBlank()) {
                "Hi ${participant.name} 👋"
            } else {
                "Hi! 👋"
            }
            builder.appendLine(greeting)
            builder.appendLine()
        }

        // Amount
        val formattedAmount = formatAmount(participant.amount, currency)
        builder.appendLine("Your part: $formattedAmount")
        builder.appendLine()

        // Payment details
        val paymentLabel = when (paymentDetails.type) {
            PaymentType.IBAN -> "IBAN"
            PaymentType.CARD -> "Card"
        }
        builder.appendLine("Please transfer to:")
        builder.appendLine("$paymentLabel: ${paymentDetails.value}")

        // Note
        if (note.isNotBlank()) {
            builder.appendLine()
            builder.appendLine("Note: $note")
        }

        // Footer
        if (includeFooter) {
            builder.appendLine()
            builder.appendLine("Sent via Bill Split app 💰")
        }

        return builder.toString().trim()
    }

    fun generateShortMessage(
        participant: Participant,
        paymentDetails: PaymentDetails,
        currency: Currency,
        note: String = ""
    ): String {
        // SMS-friendly short version
        val formattedAmount = formatAmount(participant.amount, currency)
        val paymentLabel = when (paymentDetails.type) {
            PaymentType.IBAN -> "IBAN"
            PaymentType.CARD -> "Card"
        }

        val builder = StringBuilder()
        builder.append("Amount: $formattedAmount\n")
        builder.append("$paymentLabel: ${paymentDetails.value}")

        if (note.isNotBlank()) {
            builder.append("\n$note")
        }

        return builder.toString()
    }

    fun generateCombinedMessage(
        participants: List<Participant>,
        paymentDetails: PaymentDetails,
        currency: Currency,
        totalAmount: Double,
        note: String = ""
    ): String {
        val builder = StringBuilder()

        builder.appendLine("📝 Bill Split Summary")
        builder.appendLine()
        builder.appendLine("Total: ${formatAmount(totalAmount, currency)}")
        builder.appendLine()

        val paymentLabel = when (paymentDetails.type) {
            PaymentType.IBAN -> "IBAN"
            PaymentType.CARD -> "Card"
        }
        builder.appendLine("$paymentLabel: ${paymentDetails.value}")
        builder.appendLine()

        builder.appendLine("Participants:")
        participants.forEach { participant ->
            val name = participant.name.ifBlank { participant.contactValue }
            builder.appendLine("• $name: ${formatAmount(participant.amount, currency)}")
        }

        if (note.isNotBlank()) {
            builder.appendLine()
            builder.appendLine("Note: $note")
        }

        builder.appendLine()
        builder.appendLine("Sent via Bill Split app 💰")

        return builder.toString().trim()
    }

    private fun formatAmount(amount: Double, currency: Currency): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = currency.decimalPlaces
            maximumFractionDigits = currency.decimalPlaces
        }
        return "${formatter.format(amount)} ${currency.symbol}"
    }
}

