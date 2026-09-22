package com.framepayments.framesdk.addresssearch

import com.google.gson.annotations.SerializedName

/**
 * Wire shapes for the Mapbox Search Box `/suggest` and `/retrieve` endpoints.
 *
 * These are decoded with a dedicated Gson instance rather than [com.framepayments.framesdk.FrameNetworking.gson],
 * because they are Mapbox's shapes rather than Frame's and must not follow Frame's conventions.
 */
internal object MapboxSearchResponses {
    data class SuggestResponse(
        val suggestions: List<Suggestion>
    ) {
        data class Suggestion(
            @SerializedName("mapbox_id") val mapboxId: String,
            val name: String,
            @SerializedName("place_formatted") val placeFormatted: String?
        )
    }

    data class RetrieveResponse(
        val features: List<Feature>
    ) {
        data class Feature(
            val properties: Properties
        )

        data class Properties(
            val name: String?,
            @SerializedName("address") val addressLine1: String?,
            val context: Context?
        )

        /** Mapbox nests each administrative level under its own key rather than returning a flat address. */
        data class Context(
            val place: Component?,
            val region: Component?,
            val postcode: Component?,
            val country: Component?
        )

        data class Component(
            val name: String?,
            /** The short code for the level, e.g. `US-CA` for a region or `US` for a country. Absent for levels that have no code, such as a city. */
            @SerializedName("region_code") val regionCode: String?,
            @SerializedName("country_code") val countryCode: String?
        )
    }
}
