package com.framepayments.framesdk.customers
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError

object CustomersAPI {
    //MARK: Methods using coroutines
    suspend fun createCustomer(request: CustomersRequests.CreateCustomerRequest): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.CreateCustomer
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    suspend fun updateCustomer(customerId: String, request: CustomersRequests.UpdateCustomerRequest): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.UpdateCustomer(customerId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    suspend fun getCustomers(page: Int? = null, perPage: Int? = null): Pair<CustomersResponses.ListCustomersResponse?, NetworkingError?> {
        val endpoint = CustomerEndpoints.GetCustomers(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data) }, error)
    }

    suspend fun getCustomerWith(customerId: String): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.GetCustomerWith(customerId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    suspend fun searchCustomers(request: CustomersRequests.SearchCustomersRequest): Pair<List<FrameObjects.Customer>?, NetworkingError?> {
        val endpoint = CustomerEndpoints.SearchCustomers
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data)?.data }, error)
    }

    suspend fun deleteCustomer(customerId: String): Pair<FrameObjects.Customer?, NetworkingError?>? {
        val endpoint = CustomerEndpoints.DeleteCustomer(customerId)
        val (data, error) = FrameNetworking.performDataTask(endpoint)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    suspend fun blockCustomerWith(customerId: String): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.BlockCustomerWith(customerId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    suspend fun unblockCustomerWith(customerId: String): Pair<FrameObjects.Customer?, NetworkingError?> {
        val endpoint = CustomerEndpoints.UnblockCustomerWith(customerId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
    }

    //MARK: Methods using callbacks
    fun createCustomer(request: CustomersRequests.CreateCustomerRequest, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.CreateCustomer

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }

    fun updateCustomer(customerId: String, request: CustomersRequests.UpdateCustomerRequest, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.UpdateCustomer(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }

    fun getCustomers(page: Int? = null, perPage: Int? = null, completionHandler: (CustomersResponses.ListCustomersResponse?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.GetCustomers(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data) }, error)
        }
    }

    fun getCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.GetCustomerWith(customerId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }

    suspend fun searchCustomers(request: CustomersRequests.SearchCustomersRequest, completionHandler: (List<FrameObjects.Customer>?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.SearchCustomers

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data)?.data }, error)
        }
    }

    fun deleteCustomer(customerId: String, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.DeleteCustomer(customerId)

        FrameNetworking.performDataTask(endpoint) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }

    fun blockCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.BlockCustomerWith(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }

    fun unblockCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?, NetworkingError?) -> Unit) {
        val endpoint = CustomerEndpoints.UnblockCustomerWith(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.Customer>(data) }, error)
        }
    }
}
