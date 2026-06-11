package com.framepayments.framesdk.capabilities

/** Namespace for response models returned by the capabilities API. */
object CapabilityResponses {

    /**
     * Response model for a paginated list of capabilities associated with a merchant account.
     *
     * @property data The list of [CapabilityObjects.Capability] objects returned by the API.
     */
    data class ListCapabilitiesResponse(
        val data: List<CapabilityObjects.Capability>? = null
    )
}
