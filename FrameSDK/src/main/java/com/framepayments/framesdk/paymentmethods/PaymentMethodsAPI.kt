package com.framepayments.framesdk.paymentmethods

import com.evervault.sdk.Evervault
import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Provides suspend and callback overloads for all payment method API operations.
 *
 * Every function returns a [Pair] whose first element is the parsed response on success and
 * whose second element is a [NetworkingError] on failure; exactly one of the two is non-null
 * at a time.
 */
object PaymentMethodsAPI {
    //MARK: Methods using coroutines

    /**
     * Fetches a paginated list of payment methods.
     *
     * @param page 1-based page number to retrieve, or null for the server default.
     * @param perPage Number of results per page, or null for the server default.
     * @return A pair of the paginated response and a networking error.
     */
    suspend fun getPaymentMethods(page: Int? = null, perPage: Int? = null): Pair<PaymentMethodResponses.ListPaymentMethodsResponse?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethods(perPage = perPage, page = page)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data) }, error)
    }

    /**
     * Fetches a single payment method by its unique identifier.
     *
     * @param paymentMethodId The unique identifier of the payment method to retrieve.
     * @return A pair of the matching payment method and a networking error.
     */
    suspend fun getPaymentMethodWith(paymentMethodId: String): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Fetches all payment methods associated with a specific customer.
     *
     * @param customerId The unique identifier of the customer whose payment methods to retrieve.
     * @return A pair of the list of payment methods and a networking error.
     */
    suspend fun getPaymentMethodsWithCustomer(customerId: String): Pair<List<FrameObjects.PaymentMethod>?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodsWithCustomer(customerId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data }, error)
    }

    /**
     * Fetches all payment methods associated with a specific merchant account.
     *
     * @param accountId The unique identifier of the account whose payment methods to retrieve.
     * @return A pair of the list of payment methods and a networking error.
     */
    suspend fun getPaymentMethodsWithAccount(accountId: String): Pair<List<FrameObjects.PaymentMethod>?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodsWithAccount(accountId)
        val (data, error) = FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data }, error)
    }

    /**
     * Creates a new card payment method, optionally encrypting sensitive fields via Evervault.
     *
     * When [encryptData] is true, [PaymentMethodRequests.CreateCardPaymentMethodRequest.cardNumber]
     * and [PaymentMethodRequests.CreateCardPaymentMethodRequest.cvc] are encrypted before the
     * request is sent.
     *
     * @param request The card details and optional customer or account associations.
     * @param encryptData Whether to encrypt the card number and CVC before transmission. Defaults to true.
     * @return A pair of the created payment method and a networking error.
     */
    suspend fun createCardPaymentMethod(request: PaymentMethodRequests.CreateCardPaymentMethodRequest, encryptData: Boolean = true): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        if (!FrameNetworking.isEvervaultConfigured && encryptData) {
            FrameNetworking.configureEvervault()
        }
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod

        if (encryptData) {
            request.cardNumber = Evervault.shared.encrypt(request.cardNumber) as String
            request.cvc = Evervault.shared.encrypt(request.cvc) as String
        }
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Creates a new ACH bank-account payment method.
     *
     * @param request The ACH bank account details and optional customer or account associations.
     * @return A pair of the created payment method and a networking error.
     */
    suspend fun createACHPaymentMethod(request: PaymentMethodRequests.CreateACHPaymentMethodRequest): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Updates mutable fields on an existing payment method.
     *
     * @param paymentMethodId The unique identifier of the payment method to update.
     * @param request The fields to update (expiration date and/or billing address).
     * @return A pair of the updated payment method and a networking error.
     */
    suspend fun updatePaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.UpdatePaymentMethodRequest): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.UpdatePaymentMethodWith(paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Attaches a payment method to a customer or merchant account.
     *
     * @param paymentMethodId The unique identifier of the payment method to attach.
     * @param request The customer and/or account to attach the payment method to.
     * @return A pair of the updated payment method and a networking error.
     */
    suspend fun attachPaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.AttachPaymentMethodRequest):Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.AttachPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Detaches a payment method from its associated customer or account.
     *
     * @param paymentMethodId The unique identifier of the payment method to detach.
     * @return A pair of the updated payment method and a networking error.
     */
    suspend fun detachPaymentMethodWith(paymentMethodId: String): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.DetachPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null), FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Blocks a payment method, preventing it from being used for new transactions.
     *
     * @param paymentMethodId The unique identifier of the payment method to block.
     * @return A pair of the updated payment method and a networking error.
     */
    suspend fun blockPaymentMethodWith(paymentMethodId: String): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.BlockPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null), FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Unblocks a previously blocked payment method, restoring its ability to be used for transactions.
     *
     * @param paymentMethodId The unique identifier of the payment method to unblock.
     * @return A pair of the updated payment method and a networking error.
     */
    suspend fun unblockPaymentMethodWith(paymentMethodId: String): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.UnblockPaymentMethodWith(paymentMethodId = paymentMethodId)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null), FrameAuthMode.Secret)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Creates a new payment method from a Google Pay wallet token.
     *
     * Sends the request using the merchant's publishable key rather than the secret key.
     *
     * @param request The Google Pay wallet data and optional customer or account associations.
     * @return A pair of the created payment method and a networking error.
     */
    suspend fun createGooglePayPaymentMethod(request: PaymentMethodRequests.CreateGooglePayPaymentMethodRequest): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error)
    }

    /**
     * Creates a new ACH payment method by connecting a bank account via Plaid.
     *
     * @param request The Plaid public token, account identifier, and optional institution metadata.
     * @return A pair of the created payment method and a networking error.
     */
    suspend fun connectPlaidBankAccount(request: PaymentMethodRequests.ConnectPlaidBankAccountRequest): Pair<FrameObjects.PaymentMethod?, NetworkingError?> {
        val endpoint = PaymentMethodEndpoints.ConnectPlaidBankAccount
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(it) }, error)
    }

    //MARK: Methods using callbacks

    /**
     * Fetches a paginated list of payment methods and delivers the result to a callback.
     *
     * @param page 1-based page number to retrieve, or null for the server default.
     * @param perPage Number of results per page, or null for the server default.
     * @param completionHandler Called with the paginated response on success, or a [NetworkingError] on failure.
     */
    fun getPaymentMethods(page: Int? = null, perPage: Int? = null, completionHandler: (PaymentMethodResponses.ListPaymentMethodsResponse?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethods(perPage = perPage, page = page)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data) }, error )
        }
    }

    /**
     * Fetches a single payment method by its unique identifier and delivers the result to a callback.
     *
     * @param paymentMethodId The unique identifier of the payment method to retrieve.
     * @param completionHandler Called with the matching payment method on success, or a [NetworkingError] on failure.
     */
    fun getPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodWith(paymentMethodId = paymentMethodId)

        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    /**
     * Fetches all payment methods for a customer and delivers the result to a callback.
     *
     * @param customerId The unique identifier of the customer whose payment methods to retrieve.
     * @param completionHandler Called with the list of payment methods on success, or a [NetworkingError] on failure.
     */
    fun getPaymentMethodsWithCustomer(customerId: String, completionHandler: (List<FrameObjects.PaymentMethod>?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodsWithCustomer(customerId)
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data }, error )
        }
    }

    /**
     * Fetches all payment methods for a merchant account and delivers the result to a callback.
     *
     * @param accountId The unique identifier of the account whose payment methods to retrieve.
     * @param completionHandler Called with the list of payment methods on success, or a [NetworkingError] on failure.
     */
    fun getPaymentMethodsWithAccount(accountId: String, completionHandler: (List<FrameObjects.PaymentMethod>?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.GetPaymentMethodsWithAccount(accountId)
        FrameNetworking.performDataTask(endpoint, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<PaymentMethodResponses.ListPaymentMethodsResponse>(data)?.data }, error )
        }
    }

    /**
     * Creates a new card payment method on the provided coroutine scope and delivers the result to a callback on the main thread.
     *
     * When [encryptData] is true, the card number and CVC are encrypted via Evervault before
     * transmission. The completion handler is always invoked on the main dispatcher.
     *
     * @param request The card details and optional customer or account associations.
     * @param encryptData Whether to encrypt the card number and CVC before transmission. Defaults to true.
     * @param scope The [CoroutineScope] on which the encryption and network work is launched.
     * @param completionHandler Called on the main thread with the created payment method on success, or a [NetworkingError] on failure.
     */
    fun createCardPaymentMethod(request: PaymentMethodRequests.CreateCardPaymentMethodRequest, encryptData: Boolean = true, scope: CoroutineScope, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        if (!FrameNetworking.isEvervaultConfigured) {
            FrameNetworking.configureEvervault()
        }
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod

        scope.launch(Dispatchers.IO) {
            val encryptedRequest = if (encryptData) request.copy(
                cardNumber = Evervault.shared.encrypt(request.cardNumber) as String,
                cvc = Evervault.shared.encrypt(request.cvc) as String
            ) else request

            FrameNetworking.performDataTaskWithRequest(endpoint, encryptedRequest) { data, error ->
                scope.launch(Dispatchers.Main) {
                    completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
                }
            }
        }
    }

    /**
     * Creates a new ACH payment method and delivers the result to a callback.
     *
     * @param request The ACH bank account details and optional customer or account associations.
     * @param scope The [CoroutineScope] used for the network operation (unused internally but kept for API consistency).
     * @param completionHandler Called with the created payment method on success, or a [NetworkingError] on failure.
     */
    fun createACHPaymentMethod(request: PaymentMethodRequests.CreateACHPaymentMethodRequest, scope: CoroutineScope, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    /**
     * Updates mutable fields on an existing payment method and delivers the result to a callback.
     *
     * @param paymentMethodId The unique identifier of the payment method to update.
     * @param request The fields to update (expiration date and/or billing address).
     * @param completionHandler Called with the updated payment method on success, or a [NetworkingError] on failure.
     */
    fun updatePaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.UpdatePaymentMethodRequest, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.UpdatePaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    /**
     * Attaches a payment method to a customer or merchant account and delivers the result to a callback.
     *
     * @param paymentMethodId The unique identifier of the payment method to attach.
     * @param request The customer and/or account to attach the payment method to.
     * @param completionHandler Called with the updated payment method on success, or a [NetworkingError] on failure.
     */
    fun attachPaymentMethodWith(paymentMethodId: String, request: PaymentMethodRequests.AttachPaymentMethodRequest, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.AttachPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, request, FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    /**
     * Detaches a payment method from its associated customer or account and delivers the result to a callback.
     *
     * @param paymentMethodId The unique identifier of the payment method to detach.
     * @param completionHandler Called with the updated payment method on success, or a [NetworkingError] on failure.
     */
    fun detachPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.DetachPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null), FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    /**
     * Blocks a payment method and delivers the result to a callback.
     *
     * @param paymentMethodId The unique identifier of the payment method to block.
     * @param completionHandler Called with the updated payment method on success, or a [NetworkingError] on failure.
     */
    fun blockPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.BlockPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null), FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    /**
     * Unblocks a previously blocked payment method and delivers the result to a callback.
     *
     * @param paymentMethodId The unique identifier of the payment method to unblock.
     * @param completionHandler Called with the updated payment method on success, or a [NetworkingError] on failure.
     */
    fun unblockPaymentMethodWith(paymentMethodId: String, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.UnblockPaymentMethodWith(paymentMethodId)

        FrameNetworking.performDataTaskWithRequest(endpoint, EmptyRequest(description = null), FrameAuthMode.Secret) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    /**
     * Creates a new payment method from a Google Pay wallet token and delivers the result to a callback.
     *
     * @param request The Google Pay wallet data and optional customer or account associations.
     * @param completionHandler Called with the created payment method on success, or a [NetworkingError] on failure.
     */
    fun createGooglePayPaymentMethod(request: PaymentMethodRequests.CreateGooglePayPaymentMethodRequest, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.CreatePaymentMethod

        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler( data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(data) }, error )
        }
    }

    /**
     * Creates a new ACH payment method by connecting a bank account via Plaid and delivers the result to a callback.
     *
     * @param request The Plaid public token, account identifier, and optional institution metadata.
     * @param completionHandler Called with the created payment method on success, or a [NetworkingError] on failure.
     */
    fun connectPlaidBankAccount(request: PaymentMethodRequests.ConnectPlaidBankAccountRequest, completionHandler: (FrameObjects.PaymentMethod?, NetworkingError?) -> Unit) {
        val endpoint = PaymentMethodEndpoints.ConnectPlaidBankAccount
        FrameNetworking.performDataTaskWithRequest(endpoint, request) { data, error ->
            completionHandler(data?.let { FrameNetworking.parseResponse<FrameObjects.PaymentMethod>(it) }, error)
        }
    }
}
