package com.framepayments.framesdk.capabilities

object CapabilityResponses {
    data class ListCapabilitiesResponse(
        val data: List<CapabilityObjects.Capability>? = null
    )
}
