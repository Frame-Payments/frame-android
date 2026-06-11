package com.framepayments.frameonboarding.reusable

import androidx.annotation.DrawableRes
import com.framepayments.frameonboarding.R

/**
 * Returns the drawable resource id for the card brand icon matching [brand].
 *
 * @param brand Card brand string (e.g. `"visa"`, `"mastercard"`, `"amex"`). Case-insensitive.
 * @return A `@DrawableRes` id for the brand icon, or a generic card icon for unknown brands.
 */
@DrawableRes
fun cardBrandIcon(brand: String): Int {
    return when (brand.uppercase()) {
        "VISA" -> R.drawable.ic_card_visa
        "MASTERCARD" -> R.drawable.ic_card_mastercard
        "AMEX", "AMERICAN_EXPRESS" -> R.drawable.ic_card_amex
        else -> R.drawable.ic_card_generic
    }
}