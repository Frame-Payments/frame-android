package com.framepayments.framesdk.addresssearch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddressSuggestionMapperTest {

    private fun feature(
        address: String? = "1 Market Street",
        city: String? = null,
        regionName: String? = null,
        regionCode: String? = null,
        regionCodeFull: String? = null,
        countryCode: String? = null,
        countryCodeAlpha3: String? = null,
        postcode: String? = null,
        name: String? = null
    ): MapboxSearchResponses.RetrieveResponse.Feature {
        val region = if (regionName != null || regionCode != null || regionCodeFull != null) {
            MapboxSearchResponses.RetrieveResponse.Component(
                name = regionName,
                regionCode = regionCode,
                regionCodeFull = regionCodeFull,
                countryCode = null,
                countryCodeAlpha3 = null
            )
        } else null
        val country = if (countryCode != null || countryCodeAlpha3 != null) {
            MapboxSearchResponses.RetrieveResponse.Component(
                name = null,
                regionCode = null,
                countryCode = countryCode,
                countryCodeAlpha3 = countryCodeAlpha3
            )
        } else null
        val context = if (city != null || region != null || postcode != null || country != null) {
            MapboxSearchResponses.RetrieveResponse.Context(
                place = city?.let {
                    MapboxSearchResponses.RetrieveResponse.Component(
                        name = it,
                        regionCode = null,
                        countryCode = null
                    )
                },
                region = region,
                postcode = postcode?.let {
                    MapboxSearchResponses.RetrieveResponse.Component(
                        name = it,
                        regionCode = null,
                        countryCode = null
                    )
                },
                country = country
            )
        } else null
        return MapboxSearchResponses.RetrieveResponse.Feature(
            properties = MapboxSearchResponses.RetrieveResponse.Properties(
                name = name,
                addressLine1 = address,
                context = context
            )
        )
    }

    @Test
    fun usesRegionCodeFullWhenRegionCodeIsAbsent() {
        val address = AddressSuggestionMapper.billingAddress(
            feature(
                regionName = "California",
                regionCodeFull = "US-CA",
                countryCode = "us"
            )
        )
        assertEquals("CA", address.state)
    }

    @Test
    fun resolvesStateNameWhenCountryCodeIsMissing() {
        val address = AddressSuggestionMapper.billingAddress(
            feature(
                address = "1 Congress Avenue",
                city = "Austin",
                regionName = "Texas",
                postcode = "78701"
            )
        )
        assertEquals("TX", address.state)
    }

    @Test
    fun readsCountryFromAlpha3Code() {
        val address = AddressSuggestionMapper.billingAddress(
            feature(
                address = "1 Congress Avenue",
                regionName = "Texas",
                countryCodeAlpha3 = "USA"
            )
        )
        assertEquals("US", address.country)
        assertEquals("TX", address.state)
    }

    @Test
    fun readsCountryFromOtherAlpha3Codes() {
        val address = AddressSuggestionMapper.billingAddress(
            feature(
                address = "10 Downing Street",
                city = "London",
                countryCodeAlpha3 = "GBR"
            )
        )
        assertEquals("GB", address.country)
    }

    @Test
    fun missingContextYieldsAddressLineOnly() {
        val address = AddressSuggestionMapper.billingAddress(feature(address = "Somewhere"))
        assertEquals("Somewhere", address.addressLine1)
        assertNull(address.city)
        assertNull(address.state)
        assertNull(address.country)
        assertEquals("", address.postalCode)
    }
}
