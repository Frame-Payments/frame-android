package com.framepayments.framesdk.capabilities

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class CapabilityEndpoints : FrameNetworkingEndpoints {
    data class GetCapabilities(val accountId: String) : CapabilityEndpoints()
    data class RequestCapabilities(val accountId: String) : CapabilityEndpoints()
    data class GetCapabilityWith(val accountId: String, val name: String) : CapabilityEndpoints()
    data class DisableCapabilityWith(val accountId: String, val name: String) : CapabilityEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetCapabilities -> "/v1/accounts/${this.accountId}/capabilities"
            is RequestCapabilities -> "/v1/accounts/${this.accountId}/capabilities"
            is GetCapabilityWith -> "/v1/accounts/${this.accountId}/capabilities/${this.name}"
            is DisableCapabilityWith -> "/v1/accounts/${this.accountId}/capabilities/${this.name}"
        }

    override val httpMethod: String
        get() = when (this) {
            is RequestCapabilities -> "POST"
            is DisableCapabilityWith -> "DELETE"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>? = null
}
