package com.mobelio.bill.split.domain.model

import java.util.UUID

/**
 * Supported currencies for bill splitting
 */
enum class Currency(val code: String, val symbol: String, val displayName: String, val decimalPlaces: Int = 2) {
    GEL("GEL", "₾", "Georgian Lari"),
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    TRY("TRY", "₺", "Turkish Lira"),
    RUB("RUB", "₽", "Russian Ruble");

    companion object {
        fun fromCode(code: String): Currency = entries.find { it.code == code } ?: USD
    }
}

/**
 * Payment method type
 */
enum class PaymentType {
    IBAN,
    CARD
}

/**
 * Contact method for participant
 */
enum class ContactMethod {
    PHONE,
    EMAIL
}

/**
 * Split calculation mode
 */
enum class SplitMode {
    EQUAL,
    MANUAL
}

/**
 * Share channel for sending payment requests
 */
enum class ShareChannel {
    WHATSAPP,
    VIBER,
    SMS,
    EMAIL,
    SHARE_SHEET,
    COPY
}

/**
 * Participant in a bill split
 */
data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val contactValue: String = "", // Phone number or email
    val contactMethod: ContactMethod,
    val amount: Double = 0.0,
    val avatarUri: String? = null,
    val isFromContacts: Boolean = false,
    val isYourself: Boolean = false // Flag to indicate if this is the user themselves
)

/**
 * Payment details (IBAN or Card)
 */
data class PaymentDetails(
    val type: PaymentType = PaymentType.IBAN,
    val value: String = ""
)

/**
 * Complete bill split data
 */
data class BillSplit(
    val id: String = UUID.randomUUID().toString(),
    val currency: Currency = Currency.USD,
    val totalAmount: Double = 0.0,
    val paymentDetails: PaymentDetails = PaymentDetails(),
    val participants: List<Participant> = emptyList(),
    val splitMode: SplitMode = SplitMode.EQUAL,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val includeYourself: Boolean = true, // Whether to include yourself in the split
    val numberOfPeople: Int = 2 // Total number of people splitting (including yourself if applicable)
)

/**
 * Contact from phone
 */
data class PhoneContact(
    val id: String,
    val name: String,
    val phoneNumbers: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val avatarUri: String? = null
)

/**
 * History item for tracking shared splits
 */
data class SplitHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val billSplit: BillSplit,
    val sharedAt: Long = System.currentTimeMillis(),
    val sharedTo: List<SharedParticipant> = emptyList()
)

/**
 * Record of a participant that was shared with
 */
data class SharedParticipant(
    val participant: Participant,
    val sharedVia: ShareChannel,
    val sharedAt: Long = System.currentTimeMillis()
)

