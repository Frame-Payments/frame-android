package com.framepayments.framesdk.customers

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError

/**
 * Provides coroutine-based and callback-based methods for managing customers via the Frame API.
 *
 * Each operation is available in two overloads: a suspend function for use with Kotlin coroutines,
 * and a callback variant for use without coroutines.
 */
object CustomersAPI {
    //MARK: Methods using coroutines

    /**
     * Creates a new customer record.
     *
     * @param request The customer details to submit.
     * @return A pair containing the created [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    suspend fun createCustomer(request: CustomersRequests.CreateCustomerRequest): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.CreateCustomer
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }
        return Pair(decodedResponse, error)
    }

    /**
     * Updates an existing customer record.
     *
     * @param customerId The unique identifier of the customer to update.
     * @param request The updated customer details to apply.
     * @return A pair containing the updated [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    suspend fun updateCustomer(customerId: String, request: CustomersRequests.UpdateCustomerRequest): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.UpdateCustomer(customerId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    /**
     * Retrieves a paginated list of customers.
     *
     * @param page The page number to retrieve. Defaults to the API's first page when null.
     * @param perPage The number of customers per page. Defaults to the API's default page size when null.
     * @return A pair containing a [CustomersResponses.ListCustomersResponse] on success, or a [NetworkingError] on failure.
     */
    suspend fun getCustomers(page: Int? = null, perPage: Int? = null): Pair<CustomersResponses.ListCustomersResponse?, NetworkingError?> {
        val endpoint = CustomerEndpoints.GetCustomers(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data) }, error)
    }

    /**
     * Retrieves a single customer by their unique identifier.
     *
     * @param customerId The unique identifier of the customer to fetch.
     * @return A pair containing the matching [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    suspend fun getCustomerWith(customerId: String): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.GetCustomerWith(customerId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)

        val decodedResponse = data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }
        return Pair(decodedResponse, error)
    }

    /**
     * Searches customers using the criteria specified in the request.
     *
     * @param request The search parameters including optional query string, email, and date filters.
     * @return A pair containing the matching list of [FrameObjects.Customer] objects on success, or a [NetworkingError] on failure.
     */
    suspend fun searchCustomers(request: CustomersRequests.SearchCustomersRequest): Pair<List<FrameObjects.Customer>?, NetworkingError?> {
        val endpoint = CustomerEndpoints.SearchCustomers(request.q, request.email, request.createdAfter, request.page, request.perPage)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data)?.data }, error)
    }

    /**
     * Deletes a customer record by their unique identifier.
     *
     * @param customerId The unique identifier of the customer to delete.
     * @return A pair containing the deleted [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    suspend fun deleteCustomer(customerId: String): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.DeleteCustomer(customerId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    /**
     * Blocks a customer, preventing them from completing transactions.
     *
     * @param customerId The unique identifier of the customer to block.
     * @return A pair containing the updated [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    suspend fun blockCustomerWith(customerId: String): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.BlockCustomerWith(customerId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    /**
     * Unblocks a previously blocked customer, restoring their ability to transact.
     *
     * @param customerId The unique identifier of the customer to unblock.
     * @return A pair containing the updated [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    suspend fun unblockCustomerWith(customerId: String): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.UnblockCustomerWith(customerId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Creates a new customer record and delivers the result to a callback.
     *
     * @param request The customer details to submit.
     * @param completionHandler Invoked with the created [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    fun createCustomer(request: CustomersRequests.CreateCustomerRequest, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.CreateCustomer

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }
            completionHandler(decodedResponse, error)
        }
    }

    /**
     * Updates an existing customer record and delivers the result to a callback.
     *
     * @param customerId The unique identifier of the customer to update.
     * @param request The updated customer details to apply.
     * @param completionHandler Invoked with the updated [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    fun updateCustomer(customerId: String, request: CustomersRequests.UpdateCustomerRequest, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.UpdateCustomer(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }

    /**
     * Retrieves a paginated list of customers and delivers the result to a callback.
     *
     * @param page The page number to retrieve. Defaults to the API's first page when null.
     * @param perPage The number of customers per page. Defaults to the API's default page size when null.
     * @param completionHandler Invoked with a [CustomersResponses.ListCustomersResponse] on success, or a [NetworkingError] on failure.
     */
    fun getCustomers(page: Int? = null, perPage: Int? = null, completionHandler: (CustomersResponses.ListCustomersResponse?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.GetCustomers(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data) }, error)
        }
    }

    /**
     * Retrieves a single customer by their unique identifier and delivers the result to a callback.
     *
     * @param customerId The unique identifier of the customer to fetch.
     * @param completionHandler Invoked with the matching [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    fun getCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.GetCustomerWith(customerId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            val decodedResponse = data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }
            completionHandler(decodedResponse, error)
        }
    }

    /**
     * Searches customers using the criteria specified in the request and delivers results to a callback.
     *
     * @param request The search parameters including optional query string, email, and date filters.
     * @param completionHandler Invoked with the matching list of [FrameObjects.Customer] objects on success, or a [NetworkingError] on failure.
     */
    fun searchCustomers(request: CustomersRequests.SearchCustomersRequest, completionHandler: (List<FrameObjects.Customer>?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.SearchCustomers(request.q, request.email, request.createdAfter, request.page, request.perPage)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data)?.data }, error)
        }
    }

    /**
     * Deletes a customer record by their unique identifier and delivers the result to a callback.
     *
     * @param customerId The unique identifier of the customer to delete.
     * @param completionHandler Invoked with the deleted [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    fun deleteCustomer(customerId: String, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.DeleteCustomer(customerId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }

    /**
     * Blocks a customer and delivers the result to a callback.
     *
     * @param customerId The unique identifier of the customer to block.
     * @param completionHandler Invoked with the updated [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    fun blockCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.BlockCustomerWith(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }

    /**
     * Unblocks a previously blocked customer and delivers the result to a callback.
     *
     * @param customerId The unique identifier of the customer to unblock.
     * @param completionHandler Invoked with the updated [FrameObjects.Customer] on success, or a [NetworkingError] on failure.
     */
    fun unblockCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.UnblockCustomerWith(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }
}
