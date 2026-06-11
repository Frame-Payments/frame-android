package com.framepayments.framesdk.paymentmethods

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

/**
 * Defines every HTTP endpoint used by [PaymentMethodsAPI].
 *
 * Each case encapsulates the path parameters required to construct the endpoint URL, the HTTP
 * method, and any query string items. Merchants interact with these indirectly through
 * [PaymentMethodsAPI] and do not need to instantiate cases directly.
 */
sealed class PaymentMethodEndpoints : FrameNetworkingEndpoints {

    /**
     * Fetches a paginated list of all payment methods.
     *
     * @property perPage Number of results to return per page.
     * @property page 1-based page number to retrieve.
     */
    data class GetPaymentMethods(val perPage: Int? = null, val page: Int? = null) : PaymentMethodEndpoints()

    /**
     * Fetches a single payment method by its unique identifier.
     *
     * @property paymentMethodId The unique identifier of the payment method to retrieve.
     */
    data class GetPaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()

    /**
     * Fetches all payment methods associated with a specific customer.
     *
     * @property customerId The unique identifier of the customer whose payment methods to retrieve.
     */
    data class GetPaymentMethodsWithCustomer(val customerId: String) : PaymentMethodEndpoints()

    /**
     * Fetches all payment methods associated with a specific merchant account.
     *
     * @property accountId The unique identifier of the account whose payment methods to retrieve.
     */
    data class GetPaymentMethodsWithAccount(val accountId: String) : PaymentMethodEndpoints()

    /**
     * Creates a new payment method (card, ACH, or Google Pay).
     */
    object CreatePaymentMethod : PaymentMethodEndpoints()

    /**
     * Updates mutable fields on an existing payment method.
     *
     * @property paymentMethodId The unique identifier of the payment method to update.
     */
    data class UpdatePaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()

    /**
     * Attaches a payment method to a customer or merchant account.
     *
     * @property paymentMethodId The unique identifier of the payment method to attach.
     */
    data class AttachPaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()

    /**
     * Detaches a payment method from its associated customer or account.
     *
     * @property paymentMethodId The unique identifier of the payment method to detach.
     */
    data class DetachPaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()

    /**
     * Blocks a payment method, preventing it from being used for new transactions.
     *
     * @property paymentMethodId The unique identifier of the payment method to block.
     */
    data class BlockPaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()

    /**
     * Unblocks a previously blocked payment method, restoring its ability to be used for transactions.
     *
     * @property paymentMethodId The unique identifier of the payment method to unblock.
     */
    data class UnblockPaymentMethodWith(val paymentMethodId: String) : PaymentMethodEndpoints()

    /**
     * Creates an ACH payment method by exchanging a Plaid public token for a connected bank account.
     */
    object ConnectPlaidBankAccount : PaymentMethodEndpoints()

    override val endpointURL: String
        get() = when (this) {
            is GetPaymentMethods, is CreatePaymentMethod -> "/v1/payment_methods"
            is ConnectPlaidBankAccount -> "/v1/payment_methods/connect_plaid"
            is GetPaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}"
            is UpdatePaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}"
            is GetPaymentMethodsWithCustomer ->
                "/v1/customers/${this.customerId}/payment_methods"
            is GetPaymentMethodsWithAccount ->
                "/v1/accounts/${this.accountId}/payment_methods"
            is AttachPaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}/attach"
            is DetachPaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}/detach"
            is BlockPaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}/block"
            is UnblockPaymentMethodWith ->
                "/v1/payment_methods/${this.paymentMethodId}/unblock"
        }

    override val httpMethod: String
        get() = when (this) {
            is CreatePaymentMethod, is AttachPaymentMethodWith, is DetachPaymentMethodWith, is BlockPaymentMethodWith, is UnblockPaymentMethodWith, is ConnectPlaidBankAccount -> "POST"
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
