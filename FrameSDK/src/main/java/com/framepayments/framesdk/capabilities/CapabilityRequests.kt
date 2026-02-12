package com.framepayments.framesdk.capabilities

object CapabilityRequests {
    data class RequestCapabilitiesRequest(
        val capabilities: List<String>
    )
}
