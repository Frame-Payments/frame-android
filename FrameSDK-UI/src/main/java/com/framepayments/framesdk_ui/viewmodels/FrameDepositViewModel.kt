package com.framepayments.framesdk_ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.framepayments.framesdk_ui.reusable.appendAmountDigit
import com.framepayments.framesdk_ui.reusable.deleteAmountDigit

/** Typed deposit amount in cents. Continue stays disabled outside [minAmount] and [maxAmount]. */
internal class FrameDepositViewModel(
    val minAmount: Int? = null,
    val maxAmount: Int? = null,
) {
    var typedCents: Int by mutableIntStateOf(0)
        private set

    val isAmountInRange: Boolean
        get() {
            if (typedCents <= 0) return false
            if (minAmount != null && typedCents < minAmount) return false
            if (maxAmount != null && typedCents > maxAmount) return false
            return true
        }

    fun replaceTypedCents(cents: Int) {
        typedCents = cents
    }

    fun appendDigit(digit: Int) {
        typedCents = appendAmountDigit(digit, typedCents, maxAmount)
    }

    fun deleteDigit() {
        typedCents = deleteAmountDigit(typedCents)
    }
}
