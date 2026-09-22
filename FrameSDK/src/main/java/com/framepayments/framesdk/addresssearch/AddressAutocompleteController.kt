package com.framepayments.framesdk.addresssearch

import com.framepayments.framesdk.FrameObjects
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Drives the suggestion list behind an address field.
 *
 * Autocomplete never blocks manual entry. Every failure path — no token, Mapbox unreachable, an
 * empty result set — clears the suggestions and reports nothing, so a user who is typing simply
 * sees no list rather than an error.
 */
class AddressAutocompleteController(
    private val debounceMillis: Long = 80,
    private val search: suspend (query: String, countryCode: String?) -> List<AddressSuggestion> = { query, country ->
        AddressSearchService.shared.suggest(query, country)
    },
    private val retrieveAddress: suspend (AddressSuggestion) -> FrameObjects.BillingAddress? = { suggestion ->
        AddressSearchService.shared.retrieve(suggestion)
    }
) {
    /** Tuning constants shared by every [AddressAutocompleteController]. */
    companion object {
        /**
         * The shortest query worth a request. Below this the list stays empty and nothing is sent.
         *
         * Two characters rather than three: a street number plus the first letter of the name is
         * already enough for Mapbox to return useful results, and waiting for a third keystroke is
         * the difference the user reads as the list being slow.
         */
        const val MINIMUM_QUERY_LENGTH = 2

        /**
         * The most suggestions the list will hold.
         *
         * Five rows is what fits on a phone between the address field and the form controls below
         * it without scrolling. Mapbox returns up to ten, and a list that long is clipped by the
         * scrolling form it hangs over, leaving a half-drawn row the user cannot reach.
         */
        const val MAXIMUM_SUGGESTIONS = 5
    }

    private val scope = CoroutineScope(Dispatchers.Main.immediate)

    private val _suggestions = MutableStateFlow<List<AddressSuggestion>>(emptyList())

    /** The current suggestion list. Empty while the field is unfocused, below [MINIMUM_QUERY_LENGTH], or between keystrokes and a debounced search resolving. */
    val suggestions: StateFlow<List<AddressSuggestion>> = _suggestions.asStateFlow()

    /**
     * The pending debounce, cancelled whenever new input arrives so a burst of keystrokes
     * collapses into one request. Only the wait is cancelled — a request that already reached
     * the network is left to finish, since killing it is what made the list wait for a pause
     * in typing before it could ever appear.
     */
    private var debounceJob: Job? = null

    /**
     * Counts queries so a slow response for an earlier one cannot overwrite the list with
     * results for text the user has already moved past. Requests can complete out of order.
     */
    private var latestQueryId = 0

    /**
     * Reacts to the user typing: waits out a short debounce, then searches.
     *
     * A new keystroke cancels only the pending wait, never a request already in flight. Results
     * therefore arrive while the user is still typing rather than after they stop, and a
     * response is applied only when it is for the most recent query — so an earlier lookup that
     * resolves late is discarded instead of replacing newer results.
     */
    fun queryChanged(query: String, countryCode: String?) {
        debounceJob?.cancel()

        val trimmed = query.trim()
        if (trimmed.length < MINIMUM_QUERY_LENGTH) {
            latestQueryId++
            _suggestions.value = emptyList()
            return
        }

        latestQueryId++
        val queryId = latestQueryId

        debounceJob = scope.launch {
            delay(debounceMillis)

            val found = withContext(Dispatchers.IO) {
                runCatching { search(trimmed, countryCode) }.getOrDefault(emptyList())
            }

            // Apply only if no newer query has been issued while this one was in flight.
            if (queryId != latestQueryId) return@launch
            _suggestions.value = found.take(MAXIMUM_SUGGESTIONS)
        }
    }

    /** Resolves a picked suggestion, or returns null when it cannot be resolved. */
    suspend fun select(suggestion: AddressSuggestion): FrameObjects.BillingAddress? {
        debounceJob?.cancel()
        // Bumping the ID retires any in-flight search, so a response that lands after the pick
        // cannot repopulate the list the user has just dismissed.
        latestQueryId++
        _suggestions.value = emptyList()
        return runCatching { retrieveAddress(suggestion) }.getOrNull()
    }

    /** Clears the list without sending anything, for dismissing on blur. */
    fun clear() {
        debounceJob?.cancel()
        latestQueryId++
        _suggestions.value = emptyList()
    }
}
