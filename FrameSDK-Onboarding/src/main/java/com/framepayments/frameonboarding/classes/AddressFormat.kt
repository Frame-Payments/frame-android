package com.framepayments.frameonboarding.classes

import androidx.compose.ui.text.input.KeyboardType

/**
 * Per-country address field format hints. 1:1 port of iOS AddressFormat.
 */
data class AddressFormat(
    val stateLabel: String,
    val postalLabel: String,
    val postalKeyboard: KeyboardType,
    val stateMaxLength: Int?
) {
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

        fun format(forCountry: String): AddressFormat =
            formats[forCountry.uppercase()] ?: DEFAULT
    }
}
