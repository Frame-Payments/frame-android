package com.framepayments.framesdk.customers
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects

object CustomersAPI {
    //MARK: Methods using coroutines
    suspend fun createCustomer(request: CustomersRequests.CreateCustomerRequest): FrameObjects.Customer? {
        val endpoint = CustomerEndpoints.CreateCustomer
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.Customer>(data)
        }
        return null
    }

    suspend fun updateCustomer(customerId: String, request: CustomersRequests.UpdateCustomerRequest): FrameObjects.Customer? {
        val endpoint = CustomerEndpoints.UpdateCustomer(customerId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.Customer>(data)
        }
        return null
    }

    suspend fun getCustomers(page: Int? = null, perPage: Int? = null): List<FrameObjects.Customer>? {
        val endpoint = CustomerEndpoints.GetCustomer(perPage = perPage, page = page)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data)?.data
        }
        return null
    }

    suspend fun getCustomerWith(customerId: String): FrameObjects.Customer? {
        val endpoint = CustomerEndpoints.GetCustomerWith(customerId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.Customer>(data)
        }
        return null
    }

    suspend fun searchCustomers(request: CustomersRequests.SearchCustomersRequest): List<FrameObjects.Customer>? {
        val endpoint = CustomerEndpoints.SearchCustomers
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, request)

        if (data != null) {
            return FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data)?.data
        }
        return null
    }

    suspend fun deleteCustomer(customerId: String): FrameObjects.Customer? {
        val endpoint = CustomerEndpoints.DeleteCustomer(customerId)
        val (data, _) = FrameNetworking.performDataTask(endpoint)

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.Customer>(data)
        }
        return null
    }

    suspend fun blockCustomerWith(customerId: String): FrameObjects.Customer? {
        val endpoint = CustomerEndpoints.BlockCustomerWith(customerId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.Customer>(data)
        }
        return null
    }

    suspend fun unblockCustomerWith(customerId: String): FrameObjects.Customer? {
        val endpoint = CustomerEndpoints.UnblockCustomerWith(customerId)
        val (data, _) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null))

        if (data != null) {
            return FrameNetworking.parseResponse<FrameObjects.Customer>(data)
        }
        return null
    }

    //MARK: Methods using callbacks
    fun createCustomer(request: CustomersRequests.CreateCustomerRequest, completionHandler: (FrameObjects.Customer?) -> Unit) {
        val endpoint = CustomerEndpoints.CreateCustomer

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.Customer>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun updateCustomer(customerId: String, request: CustomersRequests.UpdateCustomerRequest, completionHandler: (FrameObjects.Customer?) -> Unit) {
        val endpoint = CustomerEndpoints.UpdateCustomer(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.Customer>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun getCustomers(page: Int? = null, perPage: Int? = null, completionHandler: (List<FrameObjects.Customer>?) -> Unit) {
        val endpoint = CustomerEndpoints.GetCustomer(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data)?.data)
            } else {
                completionHandler(null)
            }
        }
    }

    fun getCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?) -> Unit) {
        val endpoint = CustomerEndpoints.GetCustomerWith(customerId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.Customer>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    suspend fun searchCustomers(request: CustomersRequests.SearchCustomersRequest, completionHandler: (List<FrameObjects.Customer>?) -> Unit) {
        val endpoint = CustomerEndpoints.SearchCustomers

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<CustomersResponses.ListCustomersResponse>(data)?.data)
            } else {
                completionHandler(null)
            }
        }
    }

    fun deleteCustomer(customerId: String, completionHandler: (FrameObjects.Customer?) -> Unit) {
        val endpoint = CustomerEndpoints.DeleteCustomer(customerId)

        FrameNetworking.performDataTask(endpoint) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.Customer>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun blockCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?) -> Unit) {
        val endpoint = CustomerEndpoints.BlockCustomerWith(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.Customer>(data))
            } else {
                completionHandler(null)
            }
        }
    }

    fun unblockCustomerWith(customerId: String, completionHandler: (FrameObjects.Customer?) -> Unit) {
        val endpoint = CustomerEndpoints.UnblockCustomerWith(customerId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(null)) { data, response, error ->
            if (data != null) {
                completionHandler(FrameNetworking.parseResponse<FrameObjects.Customer>(data))
            } else {
                completionHandler(null)
            }
        }
    }
}
