package com.framepayments.framesdk.paymentmethods

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class PaymentMethodEndpoints : FrameNetworkingEndpoints {
    data class GetPaymentMethods(val perPage: Int? = null, val page: Int? = null) : PaymentMethodEndpoints()
    data class GetPaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()
    data class GetPaymentMethodsWithCustomer(val customerId: String) : PaymentMethodEndpoints()
    object CreatePaymentMethod : PaymentMethodEndpoints()
    data class UpdatePaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()
    data class AttachPaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()
    data class DetachPaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetPaymentMethods, is CreatePaymentMethod ->
                "/v1/payment_methods"
            is GetPaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}"
            is UpdatePaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}"
            is GetPaymentMethodsWithCustomer ->
                "/v1/customers/${this.customerId}/payment_methods"
            is AttachPaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}/attach"
            is DetachPaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}/detach"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreatePaymentMethod,
            is AttachPaymentMethodWith,
            is DetachPaymentMethodWith -> "POST"
            is UpdatePaymentMethodWith -> "PATCH"
            else -> "GET"
        }

    override val queryItems: List<QueryItem>?
        get() = when (this) {
            is GetPaymentMethods -> {
                val items = mutableListOf<QueryItem>()
                perPage?.let { items.add(QueryItem("per_page", it.toString())) }
                page?.let { items.add(QueryItem("page", it.toString())) }
                items
            }
            else -> null
        }
}