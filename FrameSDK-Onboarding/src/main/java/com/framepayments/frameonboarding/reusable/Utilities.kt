package com.framepayments.frameonboarding.reusable

import androidx.annotation.DrawableRes
import com.framepayments.frameonboarding.R

@DrawableRes
fun cardBrandIcon(brand: String): Int {
    return when (brand.uppercase()) {
        "VISA" -> R.drawable.ic_card_visa
        "MASTERCARD" -> R.drawable.ic_card_mastercard
        "AMEX", "AMERICAN_EXPRESS" -> R.drawable.ic_card_amex
        else -> R.drawable.ic_card_generic
    }
}