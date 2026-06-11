package com.framepayments.framesdk.capabilities

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines all network endpoints for the capabilities API.
 *
 * Each case maps to a specific HTTP method and URL path under `/v1/accounts/{accountId}/capabilities`.
 */
sealed class CapabilityEndpoints : FrameNetworkingEndpoints {

    /**
     * Retrieves all capabilities for the given account (GET).
     *
     * @property accountId The merchant account ID whose capabilities are being fetched.
     */
    data class GetCapabilities(val accountId: String) : CapabilityEndpoints()

    /**
     * Requests one or more capabilities for the given account (POST).
     *
     * @property accountId The merchant account ID to request capabilities for.
     */
    data class RequestCapabilities(val accountId: String) : CapabilityEndpoints()

    /**
     * Retrieves a single capability by name for the given account (GET).
     *
     * @property accountId The merchant account ID that owns the capability.
     * @property name The name identifier of the capability to retrieve.
     */
    data class GetCapabilityWith(val accountId: String, val name: String) : CapabilityEndpoints()

    /**
     * Disables a single capability by name for the given account (DELETE).
     *
     * @property accountId The merchant account ID that owns the capability.
     * @property name The name identifier of the capability to disable.
     */
    data class DisableCapabilityWith(val accountId: String, val name: String) : CapabilityEndpoints()

    /** The relative URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is GetCapabilities -> "/v1/accounts/${this.accountId}/capabilities"
            is RequestCapabilities -> "/v1/accounts/${this.accountId}/capabilities"
            is GetCapabilityWith -> "/v1/accounts/${this.accountId}/capabilities/${this.name}"
            is DisableCapabilityWith -> "/v1/accounts/${this.accountId}/capabilities/${this.name}"
        }

    /** The HTTP method for this endpoint (`GET`, `POST`, or `DELETE`). */
    override val httpMethod: String
        get() = when (this) {
            is RequestCapabilities -> "POST"
            is DisableCapabilityWith -> "DELETE"
            else -> "GET"
        }

    /** Query parameters for this endpoint; always `null` for capability endpoints. */
    override val queryItems: List<QueryItem>? = null
}
