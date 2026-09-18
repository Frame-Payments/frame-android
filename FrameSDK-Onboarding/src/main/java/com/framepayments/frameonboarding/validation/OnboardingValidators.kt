package com.framepayments.frameonboarding.validation

import com.framepayments.framesdk.AddressSubregions
import com.framepayments.frameonboarding.classes.AddressFormat

/**
 * Onboarding-only validators that depend on [AddressFormat], an onboarding domain type not
 * available to FrameSDK-UI. All other validators (1:1 ports of iOS [Validators.swift]) live in
 * [com.framepayments.framesdk_ui.validation.Validators] — see that object's kdoc for why
 * FieldKey/ValidationError typing wasn't extended to cover them.
 */
object OnboardingValidators {

    /**
     * Validates [value] against the subregions the given [countryCode] accepts.
     *
     * Countries without a known subregion list are considered valid (returns null).
     *
     * @param value State/province/region string entered by the customer.
     * @param countryCode ISO 3166-1 alpha-2 country code (case-insensitive).
     * @return Null if valid or if the country has no known subregion list, a localized error string otherwise.
     */
    fun validateSubregion(value: String, countryCode: String): String? {
        val label = AddressFormat.format(countryCode).stateLabel
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return "$label is required"
        val codes = AddressSubregions.codes(forCountry = countryCode) ?: return null
        return if (codes.contains(trimmed.uppercase())) null else "Enter a valid 2-letter ${label.lowercase()}"
    }
}
