package com.framepayments.framesdk.capabilities

import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

object CapabilitiesAPI {
    // MARK: Methods using coroutines
    suspend fun getCapabilities(accountId: String): Pair<CapabilityResponses.ListCapabilitiesResponse?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = CapabilityEndpoints.GetCapabilities(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CapabilityResponses.ListCapabilitiesResponse>(it) }, error)
    }

    suspend fun requestCapabilities(accountId: String, request: CapabilityRequests.RequestCapabilitiesRequest): Pair<List<CapabilityObjects.Capability>?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = CapabilityEndpoints.RequestCapabilities(accountId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        val decoded = data?.let { FrameNetworking.parseListResponse<CapabilityObjects.Capability>(it) }
        return Pair(decoded, error)
    }

    suspend fun getCapabilityWith(accountId: String, name: String): Pair<CapabilityObjects.Capability?, NetworkingError?> {
        if (accountId.isEmpty() || name.isEmpty()) return Pair(null, null)
        val endpoint = CapabilityEndpoints.GetCapabilityWith(accountId, name)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CapabilityObjects.Capability>(it) }, error)
    }

    suspend fun disableCapabilityWith(accountId: String, name: String): Pair<CapabilityObjects.Capability?, NetworkingError?> {
        if (accountId.isEmpty() || name.isEmpty()) return Pair(null, null)
        val endpoint = CapabilityEndpoints.DisableCapabilityWith(accountId, name)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CapabilityObjects.Capability>(it) }, error)
    }

    // MARK: Methods using callbacks
    fun getCapabilities(accountId: String, completionHandler: (CapabilityResponses.ListCapabilitiesResponse?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = CapabilityEndpoints.GetCapabilities(accountId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CapabilityResponses.ListCapabilitiesResponse>(it) }, error)
        }
    }

    fun requestCapabilities(accountId: String, request: CapabilityRequests.RequestCapabilitiesRequest, completionHandler: (List<CapabilityObjects.Capability>?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = CapabilityEndpoints.RequestCapabilities(accountId)
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseListResponse<CapabilityObjects.Capability>(it) }, error)
        }
    }

    fun getCapabilityWith(accountId: String, name: String, completionHandler: (CapabilityObjects.Capability?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty() || name.isEmpty()) return completionHandler(null, null)
        val endpoint = CapabilityEndpoints.GetCapabilityWith(accountId, name)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CapabilityObjects.Capability>(it) }, error)
        }
    }

    fun disableCapabilityWith(accountId: String, name: String, completionHandler: (CapabilityObjects.Capability?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty() || name.isEmpty()) return completionHandler(null, null)
        val endpoint = CapabilityEndpoints.DisableCapabilityWith(accountId, name)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CapabilityObjects.Capability>(it) }, error)
        }
    }
}
