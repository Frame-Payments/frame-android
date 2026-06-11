package com.framepayments.frameonboarding.classes

import androidx.compose.ui.text.input.KeyboardType

/**
 * Per-country address field format hints used to configure the billing address form.
 *
 * Mirrors iOS `AddressFormat`. Use [AddressFormat.format] to retrieve the format for a
 * specific country; unknown countries fall back to a generic international format.
 *
 * @property stateLabel Localized label for the state/province field (e.g. "State", "Province").
 * @property postalLabel Localized label for the postal code field (e.g. "Zip Code", "Postcode").
 * @property postalKeyboard Keyboard type to use for the postal code input field.
 * @property stateMaxLength Maximum character length for the state field, or null if unconstrained.
 */
data class AddressFormat(
    val stateLabel: String,
    val postalLabel: String,
    val postalKeyboard: KeyboardType,
    val stateMaxLength: Int?
) {
    /** Factory methods and per-country format table. */
    companion object {
        private val DEFAULT = AddressFormat(
            stateLabel = "State",
            postalLabel = "Postal Code",
            postalKeyboard = KeyboardType.Text,
            stateMaxLength = null
        )

        private val formats: Map<String, AddressFormat> = mapOf(
            "US" to AddressFormat("State", "Zip Code", KeyboardType.Number, 2),
            "CA" to AddressFormat("Province", "Postal Code", KeyboardType.Text, 2),
            "GB" to AddressFormat("County", "Postcode", KeyboardType.Text, null),
            "AU" to AddressFormat("State", "Postcode", KeyboardType.Number, null),
            "DE" to AddressFormat("State", "Postal Code", KeyboardType.Number, null),
            "FR" to AddressFormat("Region", "Postal Code", KeyboardType.Number, null),
            "NL" to AddressFormat("Province", "Postcode", KeyboardType.Text, null),
            "JP" to AddressFormat("Prefecture", "Postal Code", KeyboardType.Text, null),
            "MX" to AddressFormat("State", "Postal Code", KeyboardType.Number, null),
            "IN" to AddressFormat("State", "PIN Code", KeyboardType.Number, null),
            "IE" to AddressFormat("County", "Eircode", KeyboardType.Text, null),
            "NZ" to AddressFormat("Region", "Postcode", KeyboardType.Number, null),
            "BR" to AddressFormat("State", "CEP", KeyboardType.Number, null),
            "IT" to AddressFormat("Province", "Postal Code", KeyboardType.Number, null),
            "ES" to AddressFormat("Province", "Postal Code", KeyboardType.Number, null),
            "SG" to AddressFormat("Region", "Postal Code", KeyboardType.Number, null)
        )

        /**
         * Returns the [AddressFormat] for the given country code, falling back to a generic
         * international format for unsupported countries.
         *
         * @param forCountry ISO 3166-1 alpha-2 country code (case-insensitive).
         * @return The country-specific [AddressFormat], or a generic fallback.
         */
        fun format(forCountry: String): AddressFormat =
            formats[forCountry.uppercase()] ?: DEFAULT
    }
}
