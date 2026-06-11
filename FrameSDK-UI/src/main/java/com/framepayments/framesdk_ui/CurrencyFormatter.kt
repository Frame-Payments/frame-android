package com.framepayments.framesdk_ui

import java.text.NumberFormat
import java.util.Locale

/** Formats integer cent amounts as USD currency strings for display in Frame UI components. */
object CurrencyFormatter {
    private val nf = NumberFormat.getCurrencyInstance(Locale.US)

    /**
     * Converts a cent-denominated integer to a formatted USD currency string.
     *
     * @param cents Amount in US cents (e.g. `2500` → `"$25.00"`).
     * @return Formatted currency string suitable for display.
     */
    fun convertCentsToCurrencyString(cents: Int): String {
        return nf.format(cents / 100.0)
    }
}