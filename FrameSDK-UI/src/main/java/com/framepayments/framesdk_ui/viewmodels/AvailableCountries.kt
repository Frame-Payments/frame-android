package com.framepayments.framesdk_ui.viewmodels

import java.util.Locale


/**
 * Provides the full list of selectable countries for the checkout country picker.
 */
class AvailableCountries {
    /** Static country list and default selection for the checkout country picker. */
    companion object {
        /** Pre-selected country used when no prior selection exists (United States). */
        val defaultCountry: AvailableCountry = AvailableCountry(
            alpha2Code = "US",
            displayName = "United States"
        )

        /**
         * Lazily built list of all ISO 3166-1 countries sorted by display name in the
         * device's default locale.
         */
        val allCountries: List<AvailableCountry> by lazy {
            Locale.getISOCountries().map { code ->
                val locale = Locale.Builder().setRegion(code).build()
                AvailableCountry(code, locale.displayCountry)
            }.sortedBy { it.displayName }
        }
    }
}

/**
 * A country entry used in the checkout country picker.
 *
 * @property alpha2Code ISO 3166-1 alpha-2 code (e.g. "US", "GB").
 * @property displayName Localized country name shown to the customer.
 */
data class AvailableCountry(val alpha2Code: String, val displayName: String)