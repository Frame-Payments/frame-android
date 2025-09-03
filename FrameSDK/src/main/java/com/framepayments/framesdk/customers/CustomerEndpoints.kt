package com.framepayments.framesdk.customers

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class CustomerEndpoints : FrameNetworkingEndpoints {
    object CreateCustomer: CustomerEndpoints()
    data class UpdateCustomer(val customerId: String): CustomerEndpoints()
    data class GetCustomers(val perPage: Int? = null, val page: Int? = null): CustomerEndpoints()
    data class GetCustomerWith(val customerId: String): CustomerEndpoints()
    object SearchCustomers: CustomerEndpoints()
    data class DeleteCustomer(val customerId: String): CustomerEndpoints()
    data class BlockCustomerWith(val customerId: String): CustomerEndpoints()
    data class UnblockCustomerWith(val customerId: String): CustomerEndpoints()

    override val endpointURL: String
        get() = when (this) {
            CreateCustomer, is GetCustomers ->
                "/v1/customers"
            is UpdateCustomer ->
                "/v1/customers/${this.customerId}"
            is DeleteCustomer ->
                "/v1/customers/${this.customerId}"
            is GetCustomerWith ->
                "/v1/customers/${this.customerId}"
            SearchCustomers ->
                "/v1/customers/search"
            is BlockCustomerWith ->
                "/v1/customers/${this.customerId}/block"
            is UnblockCustomerWith ->
                "/v1/customers/${this.customerId}/unblock"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreateCustomer, is BlockCustomerWith, is UnblockCustomerWith -> "POST"
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