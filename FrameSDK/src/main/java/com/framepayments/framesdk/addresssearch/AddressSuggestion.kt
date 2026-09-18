package com.framepayments.framesdk.addresssearch

/**
 * One address the user can pick from the autocomplete list.
 *
 * A suggestion carries only what the list needs to draw a row plus the identifier used to
 * retrieve the full address. Mapbox returns the components on the retrieve call, not on
 * suggest, so a suggestion alone cannot fill a form.
 *
 * @property id Identifies the suggestion within its search session.
 * @property title The first line of the row, typically the street address.
 * @property subtitle The second line of the row, typically city, state, and country.
 */
data class AddressSuggestion(
    val id: String,
    val title: String,
    val subtitle: String
)
