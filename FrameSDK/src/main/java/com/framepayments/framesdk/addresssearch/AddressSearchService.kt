package com.framepayments.framesdk.addresssearch

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.configurations.ConfigurationAPI
import com.framepayments.framesdk.configurations.ConfigurationResponses
import com.framepayments.framesdk.configurations.SecureConfigurationStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import java.util.UUID

/**
 * Errors raised while searching for an address.
 *
 * Autocomplete is an accelerator rather than a gate, so callers are expected to fall back to
 * manual entry on any of these rather than surfacing them to the user.
 */
internal sealed class AddressSearchError : Exception() {
    /** No Mapbox token is available from either the Frame API or local cache. */
    object Unavailable : AddressSearchError()

    /** Mapbox rejected the request or the response could not be decoded. */
    object RequestFailed : AddressSearchError()
}

/**
 * Looks up addresses through the Mapbox Search Box API.
 *
 * The access token is served by Frame's configuration API rather than embedded in the SDK, so
 * it can be rotated without a release. Requests go straight to Mapbox rather than through
 * [FrameNetworking], which only builds URLs against Frame's own host.
 */
internal class AddressSearchService {
    companion object {
        val shared = AddressSearchService()
        private const val MAXIMUM_SUGGESTIONS = 5
    }

    private val gson = Gson()
    private val mutex = Mutex()
    private var cachedToken: String? = null

    /**
     * Groups a sequence of keystrokes with the retrieve that ends it, which is how Mapbox bills
     * a search. A fresh token per session would bill every keystroke as its own lookup.
     */
    private var sessionToken: String = UUID.randomUUID().toString()

    private suspend fun fetchToken(): String? {
        val fromApi = runCatching { ConfigurationAPI.getMapboxConfiguration() }.getOrNull()
        if (fromApi?.accessToken?.isNotEmpty() == true && !fromApi.hasExpired) return fromApi.accessToken

        val cached: ConfigurationResponses.GetMapboxConfigurationResponse? =
            SecureConfigurationStorage.retrieve(FrameNetworking.getContext(), "mapbox")
        // Don't serve an expired token — spending a request on one known to be dead just
        // trades an expiry-aware failure now for a 401 later.
        if (cached?.accessToken?.isNotEmpty() == true && !cached.hasExpired) return cached.accessToken

        return null
    }

    private suspend fun token(): String {
        mutex.withLock { cachedToken }?.let { return it }
        val fetched = fetchToken() ?: throw AddressSearchError.Unavailable
        mutex.withLock { cachedToken = fetched }
        return fetched
    }

    /**
     * Returns the addresses matching a partial query, restricted to one country.
     *
     * @param query What the user has typed so far.
     * @param countryCode ISO 3166-1 alpha-2 code the results are limited to, so a checkout
     *   locked to one country does not surface addresses from another.
     */
    suspend fun suggest(query: String, countryCode: String?): List<AddressSuggestion> {
        val urlBuilder = "https://api.mapbox.com/search/searchbox/v1/suggest".toHttpUrlOrNull()
            ?.newBuilder()
            ?.addQueryParameter("q", query)
            ?.addQueryParameter("session_token", sessionToken)
            ?.addQueryParameter("types", "address")
            // Ask for only what the list shows rather than Mapbox's default of ten.
            ?.addQueryParameter("limit", MAXIMUM_SUGGESTIONS.toString())
            ?.addQueryParameter("access_token", token())
            ?: throw AddressSearchError.RequestFailed

        if (!countryCode.isNullOrEmpty()) {
            urlBuilder.addQueryParameter("country", countryCode.lowercase())
        }

        val decoded: MapboxSearchResponses.SuggestResponse = perform(urlBuilder.build().toString())
        return decoded.suggestions.map {
            AddressSuggestion(id = it.mapboxId, title = it.name, subtitle = it.placeFormatted ?: "")
        }
    }

    /**
     * Resolves a suggestion into a full address.
     *
     * Ends the billing session: the next [suggest] starts a new one.
     */
    suspend fun retrieve(suggestion: AddressSuggestion): FrameObjects.BillingAddress {
        val url = "https://api.mapbox.com/search/searchbox/v1/retrieve/${suggestion.id}".toHttpUrlOrNull()
            ?.newBuilder()
            ?.addQueryParameter("session_token", sessionToken)
            ?.addQueryParameter("access_token", token())
            ?.build()
            ?: throw AddressSearchError.RequestFailed

        val decoded: MapboxSearchResponses.RetrieveResponse = perform(url.toString())
        val feature = decoded.features.firstOrNull() ?: throw AddressSearchError.RequestFailed

        sessionToken = UUID.randomUUID().toString()
        return AddressSuggestionMapper.billingAddress(feature)
    }

    private suspend inline fun <reified T> perform(url: String): T = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = FrameNetworking.okHttpClient.newCall(request).execute()
            response.use {
                if (!it.isSuccessful) {
                    // A rejected token is worth dropping: the next call refetches rather than
                    // repeating a request that cannot succeed.
                    if (it.code == 401 || it.code == 403) {
                        mutex.withLock { cachedToken = null }
                    }
                    throw AddressSearchError.RequestFailed
                }
                val body = it.body?.string() ?: throw AddressSearchError.RequestFailed
                gson.fromJson(body, T::class.java) ?: throw AddressSearchError.RequestFailed
            }
        } catch (e: AddressSearchError) {
            throw e
        } catch (e: Exception) {
            throw AddressSearchError.RequestFailed
        }
    }
}
