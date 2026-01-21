package com.mobelio.bill.split.domain.usecase

import com.mobelio.bill.split.domain.model.PaymentType
import javax.inject.Inject

/**
 * Use case for validating payment details and other inputs
 */
class ValidationUseCase @Inject constructor() {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validatePaymentDetails(value: String, type: PaymentType): ValidationResult {
        if (value.isBlank()) {
            return ValidationResult(false, "Payment details cannot be empty")
        }

        return when (type) {
            PaymentType.IBAN -> validateIban(value)
            PaymentType.CARD -> validateCard(value)
        }
    }

    private fun validateIban(iban: String): ValidationResult {
        val cleanIban = iban.replace(" ", "").uppercase()

        // Basic IBAN validation - length between 15-34, alphanumeric
        if (cleanIban.length < 15 || cleanIban.length > 34) {
            return ValidationResult(false, "IBAN must be between 15-34 characters")
        }

        if (!cleanIban.matches(Regex("^[A-Z]{2}[0-9]{2}[A-Z0-9]+$"))) {
            return ValidationResult(false, "Invalid IBAN format")
        }

        return ValidationResult(true)
    }

    private fun validateCard(cardNumber: String): ValidationResult {
        val cleanCard = cardNumber.replace(" ", "").replace("-", "")

        // Basic card validation - digits only, length 13-19
        if (!cleanCard.matches(Regex("^[0-9]+$"))) {
            return ValidationResult(false, "Card number must contain only digits")
        }

        if (cleanCard.length < 13 || cleanCard.length > 19) {
            return ValidationResult(false, "Card number must be between 13-19 digits")
        }

        return ValidationResult(true)
    }

    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) {
            return ValidationResult(false, "Phone number cannot be empty")
        }

        // Clean phone number - allow +, digits, spaces, dashes
        val cleanPhone = phone.replace(Regex("[\\s\\-()]"), "")

        // Check for valid characters
        if (!cleanPhone.matches(Regex("^\\+?[0-9]+$"))) {
            return ValidationResult(false, "Invalid phone number format")
        }

        // Minimum 9 digits (excluding +)
        val digitsOnly = cleanPhone.replace("+", "")
        if (digitsOnly.length < 9) {
            return ValidationResult(false, "Phone number must have at least 9 digits")
        }

        return ValidationResult(true)
    }

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Email cannot be empty")
        }

        // Basic email pattern validation
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!email.matches(emailPattern)) {
            return ValidationResult(false, "Invalid email format")
        }

        return ValidationResult(true)
    }

    fun validateAmount(amount: Double): ValidationResult {
        if (amount <= 0) {
            return ValidationResult(false, "Amount must be greater than 0")
        }
        return ValidationResult(true)
    }
}

