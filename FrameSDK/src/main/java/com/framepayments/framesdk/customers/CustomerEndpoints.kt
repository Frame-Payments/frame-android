package com.framepayments.framesdk.customers

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines all HTTP endpoints used by the customers API.
 *
 * Each case maps to a distinct API route and HTTP method. Construct the appropriate case
 * and pass it to [com.framepayments.framesdk.FrameNetworking] to execute the request.
 */
sealed class CustomerEndpoints : FrameNetworkingEndpoints {

    /** Endpoint for creating a new customer via POST /v1/customers. */
    object CreateCustomer: CustomerEndpoints()

    /**
     * Endpoint for updating an existing customer via PATCH /v1/customers/{customerId}.
     *
     * @property customerId The unique identifier of the customer to update.
     */
    data class UpdateCustomer(val customerId: String): CustomerEndpoints()

    /**
     * Endpoint for retrieving a paginated list of customers via GET /v1/customers.
     *
     * @property perPage The number of results to return per page. Uses the API default when null.
     * @property page The page number to retrieve. Uses the API default when null.
     */
    data class GetCustomers(val perPage: Int? = null, val page: Int? = null): CustomerEndpoints()

    /**
     * Endpoint for fetching a single customer by ID via GET /v1/customers/{customerId}.
     *
     * @property customerId The unique identifier of the customer to retrieve.
     */
    data class GetCustomerWith(val customerId: String): CustomerEndpoints()

    /**
     * Endpoint for searching customers via GET /v1/customers/search.
     *
     * @property q A general-purpose search query string. Pass null to omit.
     * @property email Filters results to customers matching this email address. Pass null to omit.
     * @property createdAfter Unix timestamp; returns only customers created after this time. Pass null to omit.
     * @property page The page number to retrieve. Uses the API default when null.
     * @property perPage The number of results to return per page. Uses the API default when null.
     */
    data class SearchCustomers(val q: String? = null, val email: String? = null, val createdAfter: Int? = null, val page: Int? = null, val perPage: Int? = null): CustomerEndpoints()

    /**
     * Endpoint for deleting a customer via DELETE /v1/customers/{customerId}.
     *
     * @property customerId The unique identifier of the customer to delete.
     */
    data class DeleteCustomer(val customerId: String): CustomerEndpoints()

    /**
     * Endpoint for blocking a customer via POST /v1/customers/{customerId}/block.
     *
     * @property customerId The unique identifier of the customer to block.
     */
    data class BlockCustomerWith(val customerId: String): CustomerEndpoints()

    /**
     * Endpoint for unblocking a customer via POST /v1/customers/{customerId}/unblock.
     *
     * @property customerId The unique identifier of the customer to unblock.
     */
    data class UnblockCustomerWith(val customerId: String): CustomerEndpoints()

    /** Returns the relative URL path for this endpoint. */
    override val endpointURL: String
        get() = when (this) {
            is CreateCustomer, is GetCustomers ->
                "/v1/customers"
            is UpdateCustomer ->
                "/v1/customers/${this.customerId}"
            is DeleteCustomer ->
                "/v1/customers/${this.customerId}"
            is GetCustomerWith ->
                "/v1/customers/${this.customerId}"
            is SearchCustomers ->
                "/v1/customers/search"
            is BlockCustomerWith ->
                "/v1/customers/${this.customerId}/block"
            is UnblockCustomerWith ->
                "/v1/customers/${this.customerId}/unblock"
        }

    /** Returns the HTTP method string for this endpoint. */
    override val httpMethod: String
        get() = when (this) {
            is CreateCustomer, is BlockCustomerWith, is UnblockCustomerWith -> "POST"
            is UpdateCustomer -> "PATCH"
            is DeleteCustomer -> "DELETE"
            else -> "GET"
        }

    /** Returns the URL query parameters for this endpoint, or null if none apply. */
    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetCustomers -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            is SearchCustomers -> {
                val items = mutableListOf<QueryItem>()
                q?.let { items.add(QueryItem("q", it)) }
                email?.let { items.add(QueryItem("email", it)) }
                createdAfter?.let { items.add(QueryItem("created_after", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                items
            }
            else -> null
        }
}
