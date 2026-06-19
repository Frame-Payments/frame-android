package com.framepayments.framesdk.capabilities

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/**
 * Provides coroutine-based and callback-based methods for managing account capabilities.
 *
 * All methods require a non-empty [accountId]; calls with an empty ID return `(null, null)`
 * without making a network request.
 */
object CapabilitiesAPI {
    // MARK: Methods using coroutines

    /**
     * Fetches all capabilities for the given account.
     *
     * @param accountId The merchant account ID to retrieve capabilities for.
     * @return A pair containing the list response on success, or a [NetworkingError] on failure.
     */
    suspend fun getCapabilities(accountId: String): Pair<CapabilityResponses.ListCapabilitiesResponse?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = CapabilityEndpoints.GetCapabilities(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CapabilityResponses.ListCapabilitiesResponse>(it) }, error)
    }

    /**
     * Requests one or more capabilities for the given account.
     *
     * @param accountId The merchant account ID to request capabilities for.
     * @param request The request body specifying which capabilities to enable.
     * @return A pair containing the list of newly requested [CapabilityObjects.Capability] objects on
     *   success, or a [NetworkingError] on failure.
     */
    suspend fun requestCapabilities(accountId: String, request: CapabilityRequests.RequestCapabilitiesRequest): Pair<List<CapabilityObjects.Capability>?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val endpoint = CapabilityEndpoints.RequestCapabilities(accountId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        val decoded = data?.let { FrameNetworking.parseListResponse<CapabilityObjects.Capability>(it) }
        return Pair(decoded, error)
    }

    /**
     * Fetches a single capability by name for the given account.
     *
     * @param accountId The merchant account ID that owns the capability.
     * @param name The name identifier of the capability to retrieve.
     * @return A pair containing the [CapabilityObjects.Capability] on success, or a [NetworkingError]
     *   on failure.
     */
    suspend fun getCapabilityWith(accountId: String, name: String): Pair<CapabilityObjects.Capability?, NetworkingError?> {
        if (accountId.isEmpty() || name.isEmpty()) return Pair(null, null)
        val endpoint = CapabilityEndpoints.GetCapabilityWith(accountId, name)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<CapabilityObjects.Capability>(it) }, error)
    }

    /**
     * Disables a single capability by name for the given account.
     *
     * @param accountId The merchant account ID that owns the capability.
     * @param name The name identifier of the capability to disable.
     * @return A pair containing the updated [CapabilityObjects.Capability] on success, or a
     *   [NetworkingError] on failure.
     */
    suspend fun disableCapabilityWith(accountId: String, name: String): Pair<CapabilityObjects.Capability?, NetworkingError?> {
        if (accountId.isEmpty() || name.isEmpty()) return Pair(null, null)
        val endpoint = CapabilityEndpoints.DisableCapabilityWith(accountId, name)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<CapabilityObjects.Capability>(it) }, error)
    }

    // MARK: Methods using callbacks

    /**
     * Fetches all capabilities for the given account and delivers the result via a callback.
     *
     * @param accountId The merchant account ID to retrieve capabilities for.
     * @param completionHandler Invoked with the list response on success, or a [NetworkingError]
     *   on failure.
     */
    fun getCapabilities(accountId: String, completionHandler: (CapabilityResponses.ListCapabilitiesResponse?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = CapabilityEndpoints.GetCapabilities(accountId)
        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CapabilityResponses.ListCapabilitiesResponse>(it) }, error)
        }
    }

    /**
     * Requests one or more capabilities for the given account and delivers the result via a callback.
     *
     * @param accountId The merchant account ID to request capabilities for.
     * @param request The request body specifying which capabilities to enable.
     * @param completionHandler Invoked with the list of newly requested [CapabilityObjects.Capability]
     *   objects on success, or a [NetworkingError] on failure.
     */
    fun requestCapabilities(accountId: String, request: CapabilityRequests.RequestCapabilitiesRequest, completionHandler: (List<CapabilityObjects.Capability>?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty()) return completionHandler(null, null)
        val endpoint = CapabilityEndpoints.RequestCapabilities(accountId)
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseListResponse<CapabilityObjects.Capability>(it) }, error)
        }
    }

    /**
     * Fetches a single capability by name for the given account and delivers the result via a callback.
     *
     * @param accountId The merchant account ID that owns the capability.
     * @param name The name identifier of the capability to retrieve.
     * @param completionHandler Invoked with the [CapabilityObjects.Capability] on success, or a
     *   [NetworkingError] on failure.
     */
    fun getCapabilityWith(accountId: String, name: String, completionHandler: (CapabilityObjects.Capability?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty() || name.isEmpty()) return completionHandler(null, null)
        val endpoint = CapabilityEndpoints.GetCapabilityWith(accountId, name)
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CapabilityObjects.Capability>(it) }, error)
        }
    }

    /**
     * Disables a single capability by name for the given account and delivers the result via a callback.
     *
     * @param accountId The merchant account ID that owns the capability.
     * @param name The name identifier of the capability to disable.
     * @param completionHandler Invoked with the updated [CapabilityObjects.Capability] on success, or a
     *   [NetworkingError] on failure.
     */
    fun disableCapabilityWith(accountId: String, name: String, completionHandler: (CapabilityObjects.Capability?, NetworkingError?) -> Unit) {
        if (accountId.isEmpty() || name.isEmpty()) return completionHandler(null, null)
        val endpoint = CapabilityEndpoints.DisableCapabilityWith(accountId, name)
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CapabilityObjects.Capability>(it) }, error)
        }
    }
}
