package com.mobelio.bill.split.domain.usecase

import com.mobelio.bill.split.domain.model.BillSplit
import com.mobelio.bill.split.domain.model.Participant
import com.mobelio.bill.split.domain.model.SplitMode
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Use case for calculating split amounts
 */
class CalculateSplitUseCase @Inject constructor() {

    /**
     * Calculate split amounts for participants
     * @return Updated list of participants with calculated amounts
     */
    fun execute(
        totalAmount: Double,
        participants: List<Participant>,
        splitMode: SplitMode
    ): SplitResult {
        if (participants.isEmpty()) {
            return SplitResult(
                participants = emptyList(),
                totalCalculated = 0.0,
                adjustmentApplied = 0.0,
                hasRoundingAdjustment = false
            )
        }

        return when (splitMode) {
            SplitMode.EQUAL -> calculateEqualSplit(totalAmount, participants)
            SplitMode.MANUAL -> calculateManualSplit(participants)
        }
    }

    private fun calculateEqualSplit(
        totalAmount: Double,
        participants: List<Participant>
    ): SplitResult {
        val count = participants.size
        if (count == 0) return SplitResult(emptyList(), 0.0, 0.0, false)

        val totalBD = BigDecimal(totalAmount).setScale(2, RoundingMode.HALF_UP)
        val countBD = BigDecimal(count)

        // Calculate base amount per person
        val baseAmount = totalBD.divide(countBD, 2, RoundingMode.DOWN)

        // Calculate the remainder that needs to be distributed
        val sumOfBase = baseAmount.multiply(countBD)
        val remainder = totalBD.subtract(sumOfBase)

        // Distribute amounts - first participant gets any rounding difference
        val updatedParticipants = participants.mapIndexed { index, participant ->
            val amount = if (index == 0) {
                baseAmount.add(remainder).toDouble()
            } else {
                baseAmount.toDouble()
            }
            participant.copy(amount = amount)
        }

        return SplitResult(
            participants = updatedParticipants,
            totalCalculated = totalAmount,
            adjustmentApplied = remainder.toDouble(),
            hasRoundingAdjustment = remainder.compareTo(BigDecimal.ZERO) != 0
        )
    }

    private fun calculateManualSplit(participants: List<Participant>): SplitResult {
        val totalCalculated = participants.sumOf { it.amount }
        return SplitResult(
            participants = participants,
            totalCalculated = totalCalculated,
            adjustmentApplied = 0.0,
            hasRoundingAdjustment = false
        )
    }
}

data class SplitResult(
    val participants: List<Participant>,
    val totalCalculated: Double,
    val adjustmentApplied: Double,
    val hasRoundingAdjustment: Boolean
)

