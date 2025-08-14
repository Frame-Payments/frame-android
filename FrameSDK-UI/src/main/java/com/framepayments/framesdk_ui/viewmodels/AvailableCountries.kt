package com.framepayments.framesdk_ui.viewmodels

import java.util.Locale


class AvailableCountries {
    companion object {
        val defaultCountry: AvailableCountry = AvailableCountry(
            alpha2Code = "US",
            displayName = "United States"
        )

        val allCountries: List<AvailableCountry> by lazy {
            Locale.getISOCountries().map { code ->
                val locale = Locale("", code)
                AvailableCountry(code, locale.displayCountry)
            }.sortedBy { it.displayName }
        }
    }
}

data class AvailableCountry(val alpha2Code: String, val displayName: String)