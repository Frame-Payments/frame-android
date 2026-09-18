package com.framepayments.framesdk.addresssearch

import com.framepayments.framesdk.AddressSubregions
import com.framepayments.framesdk.FrameObjects

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

        val countryCode = context?.country?.countryCode?.uppercase()

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

        val regionCode = region.regionCode
        if (!regionCode.isNullOrEmpty()) {
            val code = regionCode.substringAfterLast('-')
            if (code.isNotEmpty()) return code.uppercase()
        }

        val name = region.name ?: return null
        if (countryCode == null) return name
        val subregions = AddressSubregions.subregions(forCountry = countryCode) ?: return name

        val match = subregions.firstOrNull { it.name.equals(name, ignoreCase = true) }
        return match?.code ?: name
    }
}
