package com.framepayments.framesdk.capabilities

/** Namespace for request body models used by the capabilities API. */
object CapabilityRequests {

    /**
     * Request body for enabling one or more capabilities on a merchant account.
     *
     * @property capabilities List of capability name identifiers to request (e.g. `["card_payments"]`).
     */
    data class RequestCapabilitiesRequest(
        val capabilities: List<String>
    )
}
