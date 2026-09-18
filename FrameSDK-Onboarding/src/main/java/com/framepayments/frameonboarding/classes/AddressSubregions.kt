package com.framepayments.frameonboarding.classes

/**
 * A state, province, or territory that the Frame API accepts for a given country.
 *
 * @property code The subregion's code as submitted to the API (e.g. "TX", "ON").
 * @property name The subregion's full display name (e.g. "Texas", "Ontario").
 */
data class AddressSubregion(
    val code: String,
    val name: String
)

/**
 * The subregions (states / provinces / territories) the Frame API accepts for a country.
 * Mirrors iOS `AddressSubregions`.
 */
object AddressSubregions {

    /** The subregions accepted for United States addresses. */
    val unitedStates: List<AddressSubregion> = listOf(
        AddressSubregion("AL", "Alabama"),
        AddressSubregion("AK", "Alaska"),
        AddressSubregion("AZ", "Arizona"),
        AddressSubregion("AR", "Arkansas"),
        AddressSubregion("CA", "California"),
        AddressSubregion("CO", "Colorado"),
        AddressSubregion("CT", "Connecticut"),
        AddressSubregion("DE", "Delaware"),
        AddressSubregion("DC", "District of Columbia"),
        AddressSubregion("FL", "Florida"),
        AddressSubregion("GA", "Georgia"),
        AddressSubregion("HI", "Hawaii"),
        AddressSubregion("ID", "Idaho"),
        AddressSubregion("IL", "Illinois"),
        AddressSubregion("IN", "Indiana"),
        AddressSubregion("IA", "Iowa"),
        AddressSubregion("KS", "Kansas"),
        AddressSubregion("KY", "Kentucky"),
        AddressSubregion("LA", "Louisiana"),
        AddressSubregion("ME", "Maine"),
        AddressSubregion("MD", "Maryland"),
        AddressSubregion("MA", "Massachusetts"),
        AddressSubregion("MI", "Michigan"),
        AddressSubregion("MN", "Minnesota"),
        AddressSubregion("MS", "Mississippi"),
        AddressSubregion("MO", "Missouri"),
        AddressSubregion("MT", "Montana"),
        AddressSubregion("NE", "Nebraska"),
        AddressSubregion("NV", "Nevada"),
        AddressSubregion("NH", "New Hampshire"),
        AddressSubregion("NJ", "New Jersey"),
        AddressSubregion("NM", "New Mexico"),
        AddressSubregion("NY", "New York"),
        AddressSubregion("NC", "North Carolina"),
        AddressSubregion("ND", "North Dakota"),
        AddressSubregion("OH", "Ohio"),
        AddressSubregion("OK", "Oklahoma"),
        AddressSubregion("OR", "Oregon"),
        AddressSubregion("PA", "Pennsylvania"),
        AddressSubregion("RI", "Rhode Island"),
        AddressSubregion("SC", "South Carolina"),
        AddressSubregion("SD", "South Dakota"),
        AddressSubregion("TN", "Tennessee"),
        AddressSubregion("TX", "Texas"),
        AddressSubregion("UT", "Utah"),
        AddressSubregion("VT", "Vermont"),
        AddressSubregion("VA", "Virginia"),
        AddressSubregion("WA", "Washington"),
        AddressSubregion("WV", "West Virginia"),
        AddressSubregion("WI", "Wisconsin"),
        AddressSubregion("WY", "Wyoming"),
        AddressSubregion("AS", "American Samoa"),
        AddressSubregion("GU", "Guam"),
        AddressSubregion("MP", "Northern Mariana Islands"),
        AddressSubregion("PR", "Puerto Rico"),
        AddressSubregion("VI", "U.S. Virgin Islands")
    )

    /** The subregions accepted for Canadian addresses — 10 provinces and 3 territories. */
    val canada: List<AddressSubregion> = listOf(
        AddressSubregion("AB", "Alberta"),
        AddressSubregion("BC", "British Columbia"),
        AddressSubregion("MB", "Manitoba"),
        AddressSubregion("NB", "New Brunswick"),
        AddressSubregion("NL", "Newfoundland and Labrador"),
        AddressSubregion("NT", "Northwest Territories"),
        AddressSubregion("NS", "Nova Scotia"),
        AddressSubregion("NU", "Nunavut"),
        AddressSubregion("ON", "Ontario"),
        AddressSubregion("PE", "Prince Edward Island"),
        AddressSubregion("QC", "Quebec"),
        AddressSubregion("SK", "Saskatchewan"),
        AddressSubregion("YT", "Yukon")
    )

    private val byCountry: Map<String, List<AddressSubregion>> = mapOf(
        "US" to unitedStates,
        "CA" to canada
    )

    /**
     * Returns the subregions for a country, or null when its subregion is unvalidated free text.
     *
     * @param forCountry ISO 3166-1 alpha-2 country code (case-insensitive); blank defaults to US.
     */
    fun subregions(forCountry: String): List<AddressSubregion>? {
        val trimmed = forCountry.trim()
        if (trimmed.isEmpty()) return unitedStates
        return byCountry[trimmed.uppercase()]
    }

    /** Returns the accepted subregion codes for a country, or null when the country is unvalidated. */
    fun codes(forCountry: String): Set<String>? =
        subregions(forCountry)?.map { it.code }?.toSet()

    /** Looks up a subregion by its code within a country's list. */
    fun subregion(forCode: String, countryCode: String): AddressSubregion? {
        val needle = forCode.trim().uppercase()
        return subregions(countryCode)?.firstOrNull { it.code == needle }
    }

    /** Trims a subregion, upcasing it only for countries whose subregions are validated as codes. */
    fun normalize(value: String, countryCode: String): String {
        val trimmed = value.trim()
        return if (subregions(countryCode) == null) trimmed else trimmed.uppercase()
    }
}
