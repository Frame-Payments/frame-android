package com.framepayments.framesdk.addresssearch

import com.framepayments.framesdk.AddressSubregions
import com.framepayments.framesdk.FrameObjects
import java.util.Locale

/** Turns a retrieved Mapbox feature into a [FrameObjects.BillingAddress]. */
internal object AddressSuggestionMapper {
    /**
     * Builds a billing address from a retrieved feature.
     *
     * Address line 2 is never populated: Mapbox does not reliably return apartment or unit, so
     * the field stays as the user left it.
     */
    fun billingAddress(feature: MapboxSearchResponses.RetrieveResponse.Feature): FrameObjects.BillingAddress {
        val properties = feature.properties
        val context = properties.context

        val countryCode = countryCode(context?.country)

        return FrameObjects.BillingAddress(
            city = context?.place?.name,
            country = countryCode,
            state = subregion(context?.region, countryCode),
            postalCode = context?.postcode?.name ?: "",
            addressLine1 = properties.addressLine1 ?: properties.name,
            addressLine2 = null
        )
    }

    /**
     * Resolves the subregion to the form the SDK's validation expects.
     *
     * `validateSubregion` runs against the raw value and matches it against the two-letter
     * codes for countries that enumerate them, so a full name like "California" has to become
     * "CA" here.
     *
     * Mapbox's `region_code` is the level's short code — `US-CA` in some responses, `CA` in
     * others — so the country prefix is dropped when present. When Mapbox sends no code at all,
     * the name is matched against the SDK's own subregion list before falling back to the raw
     * value, which keeps free-text countries working as they do today.
     */
    private fun subregion(region: MapboxSearchResponses.RetrieveResponse.Component?, countryCode: String?): String? {
        if (region == null) return null

        val rawCode = region.regionCode ?: region.regionCodeFull
        if (!rawCode.isNullOrEmpty()) {
            val code = rawCode.substringAfterLast('-')
            if (code.isNotEmpty()) {
                if (countryCode != null) {
                    AddressSubregions.resolve(code, countryCode)?.let { return it }
                }
                return code.uppercase()
            }
        }

        val name = region.name ?: return null
        if (countryCode != null) {
            AddressSubregions.resolve(name, countryCode)?.let { return it }
        }
        AddressSubregions.resolve(name, "US")?.let { return it }
        AddressSubregions.resolve(name, "CA")?.let { return it }
        return name
    }

    private fun countryCode(country: MapboxSearchResponses.RetrieveResponse.Component?): String? {
        val alpha2 = country?.countryCode?.trim().orEmpty()
        if (alpha2.isNotEmpty()) return alpha2.uppercase()
        val raw = country?.countryCodeAlpha3?.trim().orEmpty()
        if (raw.isEmpty()) return null
        if (raw.length == 2) return raw.uppercase()
        return alpha3ToAlpha2[raw.uppercase()]
    }

    private val alpha3ToAlpha2: Map<String, String> by lazy {
        Locale.getISOCountries().mapNotNull { alpha2 ->
            val alpha3 = runCatching { Locale("", alpha2).isO3Country }.getOrNull() ?: return@mapNotNull null
            alpha3.uppercase() to alpha2
        }.toMap()
    }
}
