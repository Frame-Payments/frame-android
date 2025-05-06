package com.framepayments.framesdk.customers

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class CustomersEndpoints : FrameNetworkingEndpoints {
    object CreateCustomer : CustomersEndpoints()
    data class UpdateCustomer(val customerId: String) : CustomersEndpoints()
    data class GetCustomers(val perPage: Int? = null, val page: Int? = null) : CustomersEndpoints()
    data class GetCustomerWith(val customerId: String) : CustomersEndpoints()
    object SearchCustomers  : CustomersEndpoints()
    data class DeleteCustomer(val customerId: String) : CustomersEndpoints()

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
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateCustomer -> "POST"
            is UpdateCustomer -> "PATCH"
            is DeleteCustomer -> "DELETE"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetCustomers -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            else -> null
        }
}