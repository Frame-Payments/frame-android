package com.framepayments.framesdk_ui.reusable

import androidx.annotation.DrawableRes
import com.framepayments.framesdk_ui.R

/**
 * Returns the drawable resource id for the payment-method icon matching [brand].
 *
 * Lives in FrameSDK-UI rather than the onboarding module so checkout renders the same brand art
 * the onboarding screens do.
 *
 * @param brand Card brand string (e.g. `"visa"`, `"mastercard"`, `"amex"`), or `"BANK"`/`"ACH"`
 *   for a bank payout method. Case-insensitive.
 * @return A `@DrawableRes` id for the brand icon, or a generic card icon for unknown brands.
 */
@DrawableRes
fun cardBrandIcon(brand: String): Int {
    val normalized = brand.lowercase()

    // Payout methods are bank accounts, not cards — without this they fall through to the
    // generic card art and a bank account renders as a credit card.
    if (normalized == "bank" || normalized == "ach") return R.drawable.ic_bank

    // Prefix-matched, like iOS: the API's brand strings vary in spelling ("american-express",
    // "americanex", "master-card"), so exact matching drops most real values to the generic icon.
    // Note: iOS also ships discover/jcb/unionpay art that this module has no asset for yet, so
    // those brands still fall through below.
    return when {
        normalized.startsWith("ame") -> R.drawable.ic_card_amex
        normalized.startsWith("master") -> R.drawable.ic_card_mastercard
        normalized.startsWith("vi") -> R.drawable.ic_card_visa
        else -> R.drawable.ic_card_generic
    }
}
