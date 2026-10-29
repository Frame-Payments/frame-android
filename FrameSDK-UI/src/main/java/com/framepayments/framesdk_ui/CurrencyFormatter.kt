package com.framepayments.framesdk_ui

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.max
import kotlin.math.pow

/** Formats integer cent amounts as currency strings for display in Frame UI components. */
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

    /**
     * Formats [cents] as a currency string for [currencyCode] without changing the shared USD formatter.
     *
     * @param cents Amount in the currency's smallest unit.
     * @param currencyCode ISO 4217 code (for example `usd`).
     * @return A currency string for that code.
     */
    fun convertCentsToCurrencyString(cents: Int, currencyCode: String): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        val code = currencyCode.trim().ifEmpty { "USD" }.uppercase()
        val currency = try {
            Currency.getInstance(code)
        } catch (_: IllegalArgumentException) {
            Currency.getInstance("USD")
        }
        formatter.currency = currency
        val digits = max(currency.defaultFractionDigits, 0)
        formatter.minimumFractionDigits = digits
        formatter.maximumFractionDigits = digits
        val total = cents / 10.0.pow(digits.toDouble())
        return formatter.format(total)
    }
}
