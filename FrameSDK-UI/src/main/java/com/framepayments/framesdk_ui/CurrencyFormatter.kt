package com.framepayments.framesdk_ui

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val nf = NumberFormat.getCurrencyInstance(Locale.US)

    fun convertCentsToCurrencyString(cents: Int): String {
        return nf.format(cents / 100.0)
    }
}