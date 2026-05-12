package com.framepayments.framesdk_ui.viewmodels

import androidx.lifecycle.*
import com.evervault.sdk.input.model.card.PaymentCardData
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.transfers.Transfer
import com.framepayments.framesdk.transfers.TransferRequests
import com.framepayments.framesdk.transfers.TransfersAPI
import com.framepayments.framesdk_ui.AddressMode
import com.framepayments.framesdk_ui.validation.FieldKey
import com.framepayments.framesdk_ui.validation.ValidationError
import com.framepayments.framesdk_ui.validation.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrameCheckoutViewModel : ViewModel() {

    // Observable account payment options
    private val _accountPaymentOptions = MutableLiveData<List<FrameObjects.PaymentMethod>?>(null)
    val accountPaymentOptions: LiveData<List<FrameObjects.PaymentMethod>?> = _accountPaymentOptions

    /// True once `loadAccountDetails` has finished fetching saved payment methods. The view
    /// defers rendering the new-card section until this flips, so returning users don't see
    /// the Card/Billing block flash visible before the auto-selection of their first saved method.
    private val _didLoadAccountPaymentMethods = MutableLiveData(false)
    val didLoadAccountPaymentMethods: LiveData<Boolean> = _didLoadAccountPaymentMethods

    // Customer-facing fields (payer details entered at checkout)
    val customerName = MutableLiveData("")
    val customerEmail = MutableLiveData("")
    val customerAddressLine1 = MutableLiveData("")
    val customerAddressLine2 = MutableLiveData("")
    val customerCity = MutableLiveData("")
    val customerState = MutableLiveData("")
    var customerCountry: AvailableCountry = AvailableCountries.defaultCountry
    val customerZipCode = MutableLiveData("")

    // Selected payment method (LiveData so the view can react to selection changes).
    private val _selectedAccountPaymentOption = MutableLiveData<FrameObjects.PaymentMethod?>(null)
    val selectedAccountPaymentOption: LiveData<FrameObjects.PaymentMethod?> = _selectedAccountPaymentOption

    fun setSelectedAccountPaymentOption(method: FrameObjects.PaymentMethod?) {
        _selectedAccountPaymentOption.value = method
        recomputeUsablePaymentInput()
    }

    var cardData: PaymentCardData = PaymentCardData()
        set(value) {
            field = value
            recomputeUsablePaymentInput()
        }

    /// Clear field errors that only apply to the new-card flow. Called when the user
    /// selects a saved payment method so stale validation messages don't linger
    /// behind the collapsed Card/Billing sections.
    fun clearNewCardFieldErrors() {
        val current = _fieldErrors.value.orEmpty().toMutableMap()
        current.remove(FieldKey.CARD)
        current.remove(FieldKey.ADDRESS_LINE_1)
        current.remove(FieldKey.CITY)
        current.remove(FieldKey.STATE)
        current.remove(FieldKey.ZIP)
        current.remove(FieldKey.COUNTRY)
        _fieldErrors.value = current
    }

    /**
     * True when the user has either selected a saved payment method
     * or entered card details that pass Evervault's potential-validity check.
     */
    private val _hasUsablePaymentInput = MutableLiveData(false)
    val hasUsablePaymentInput: LiveData<Boolean> = _hasUsablePaymentInput

    private fun recomputeUsablePaymentInput() {
        // Evervault's `isPotentiallyValid` returns true for an empty card, so we
        // also require a non-empty card number before treating new-card input as usable.
        val newCardOk = cardData.card.number.isNotEmpty() && cardData.isPotentiallyValid
        _hasUsablePaymentInput.postValue(_selectedAccountPaymentOption.value != null || newCardOk)
    }

    // Address mode + per-field errors
    var addressMode: AddressMode = AddressMode.REQUIRED
    private val _fieldErrors = MutableLiveData<Map<FieldKey, ValidationError>>(emptyMap())
    val fieldErrors: LiveData<Map<FieldKey, ValidationError>> = _fieldErrors

    // Internal tracking
    private var currentAccountId: String? = null
    internal var amount: Int = 0

    /// True while the checkout submit is in flight. Drives the pay button's disabled state and
    /// the in-button progress indicator. Re-entrant submit calls bail out immediately.
    private val _isPerformingAction = MutableLiveData(false)
    val isPerformingAction: LiveData<Boolean> = _isPerformingAction

    /**
     * Set when the last checkout submit failed with a network/server error. The view
     * observes this to display a banner; cleared on every new submit.
     */
    private val _checkoutError = MutableLiveData<String?>(null)
    val checkoutError: LiveData<String?> = _checkoutError

    /**
     * Fetches the account, prefills the customer name + email fields from its
     * individual profile (matches iOS `loadAccountDetails`), then loads the saved
     * payment methods so the user can pick one instead of entering a new card.
     *
     * [accountId] is required because the bundled checkout's pay button creates a
     * Transfer, which is account-scoped.
     */
    fun loadAccountDetails(accountId: String, amount: Int) {
        require(accountId.isNotEmpty()) { "FrameCheckoutViewModel.loadAccountDetails requires a non-empty accountId" }
        this.amount = amount
        currentAccountId = accountId

        viewModelScope.launch(Dispatchers.IO) {
            val (account, _) = AccountsAPI.getAccountWith(accountId)
            val individual = account?.profile?.individual
            if (individual != null) {
                val firstName = individual.name?.firstName.orEmpty()
                val lastName = individual.name?.lastName.orEmpty()
                val composedName = listOf(firstName, lastName)
                    .filter { it.isNotEmpty() }
                    .joinToString(" ")
                val composedEmail = individual.email.orEmpty()
                withContext(Dispatchers.Main) {
                    if (composedName.isNotEmpty()) customerName.value = composedName
                    if (composedEmail.isNotEmpty()) customerEmail.value = composedEmail
                }
            }

            val (paymentMethods, _) = PaymentMethodsAPI.getPaymentMethodsWithAccount(accountId)
            withContext(Dispatchers.Main) {
                _accountPaymentOptions.value = paymentMethods
                if (_selectedAccountPaymentOption.value == null &&
                    cardData.card.number.isEmpty() &&
                    !paymentMethods.isNullOrEmpty()
                ) {
                    setSelectedAccountPaymentOption(paymentMethods.first())
                }
                _didLoadAccountPaymentMethods.value = true
            }
        }
    }

    fun setError(key: FieldKey, error: ValidationError?) {
        val current = _fieldErrors.value.orEmpty().toMutableMap()
        if (error == null) current.remove(key) else current[key] = error
        _fieldErrors.postValue(current)
    }

    fun clearError(key: FieldKey) {
        val current = _fieldErrors.value.orEmpty()
        if (!current.containsKey(key)) return
        _fieldErrors.postValue(current - key)
    }

    private fun hasAnyAddressInput(): Boolean =
        !customerAddressLine1.value.isNullOrEmpty() ||
            !customerAddressLine2.value.isNullOrEmpty() ||
            !customerCity.value.isNullOrEmpty() ||
            !customerState.value.isNullOrEmpty() ||
            !customerZipCode.value.isNullOrEmpty()

    private fun shouldValidateAddress(): Boolean = when (addressMode) {
        AddressMode.REQUIRED -> true
        AddressMode.OPTIONAL -> hasAnyAddressInput()
        AddressMode.HIDDEN -> false
    }

    /**
     * Run all validations and return the resulting error map.
     * When [forSavedCard] is true, the new-card field is not validated.
     */
    fun validateAll(forSavedCard: Boolean): Map<FieldKey, ValidationError> {
        val errors = mutableMapOf<FieldKey, ValidationError>()

        Validators.validateName(customerName.value)?.let { errors[FieldKey.NAME] = it }
        Validators.validateEmail(customerEmail.value)?.let { errors[FieldKey.EMAIL] = it }

        if (!forSavedCard) {
            Validators.validateCard(cardData)?.let { errors[FieldKey.CARD] = it }
        }

        if (!forSavedCard && shouldValidateAddress()) {
            Validators.validateAddressLine1(customerAddressLine1.value)?.let { errors[FieldKey.ADDRESS_LINE_1] = it }
            Validators.validateCity(customerCity.value)?.let { errors[FieldKey.CITY] = it }
            Validators.validateState(customerState.value)?.let { errors[FieldKey.STATE] = it }
            Validators.validateZip(customerZipCode.value)?.let { errors[FieldKey.ZIP] = it }
            Validators.validateCountry(customerCountry.alpha2Code)?.let { errors[FieldKey.COUNTRY] = it }
        }

        return errors
    }

    fun checkoutWithSelectedPaymentMethod(saveMethod: Boolean): LiveData<Transfer?> = liveData(Dispatchers.IO) {
        if (amount == 0) {
            emit(null)
            return@liveData
        }
        val accountId = currentAccountId
        if (accountId.isNullOrEmpty()) {
            emit(null)
            return@liveData
        }
        if (_isPerformingAction.value == true) {
            emit(null)
            return@liveData
        }
        _isPerformingAction.postValue(true)
        _checkoutError.postValue(null)
        try {
            val usingSavedCard = _selectedAccountPaymentOption.value != null
            val errors = validateAll(forSavedCard = usingSavedCard)
            if (errors.isNotEmpty()) {
                _fieldErrors.postValue(errors)
                emit(null)
                return@liveData
            }
            // Clear any prior errors on success.
            _fieldErrors.postValue(emptyMap())

            // Determine or create payment method
            val paymentMethodId = _selectedAccountPaymentOption.value?.id ?: run {
                val (pmId, pmError) = createPaymentMethod(accountId)
                if (pmError != null) {
                    surfaceCheckoutError(pmError)
                    emit(null)
                    return@liveData
                }
                pmId
            }

            if (paymentMethodId == null) {
                emit(null)
                return@liveData
            }

            // Build the transfer request (charge flow against the account)
            val request = TransferRequests.CreateTransferRequest(
                amount = amount,
                accountId = accountId,
                currency = "usd",
                sourcePaymentMethodId = paymentMethodId,
                destinationPaymentMethodId = null,
                description = null,
                metadata = null
            )

            val (transfer, transferError) = TransfersAPI.createTransfer(request)
            if (transferError != null) {
                surfaceCheckoutError(transferError)
            }
            emit(transfer)
        } finally {
            _isPerformingAction.postValue(false)
        }
    }

    /// Only retryable server-side errors (e.g. card declined) are surfaced inline so the
    /// payer sees the decline reason verbatim from the server. Decode/network/unknown
    /// errors are intentionally silent — they're terminal and don't have a payer-friendly
    /// message; hosts should rely on the `null` emission to decide what to do next.
    private fun surfaceCheckoutError(error: com.framepayments.framesdk.NetworkingError) {
        if (error is com.framepayments.framesdk.NetworkingError.ServerError) {
            val message = extractCheckoutErrorMessage(error.errorDescription)
            if (message.isNotEmpty()) {
                _checkoutError.postValue(message)
            }
        }
    }

    /// Extract a user-facing message from the raw error-envelope JSON the server sends back.
    /// Shape: `{"status":N,"error":"...","code":"...","error_details":{"message":"...","data":...}}`.
    /// Preference: `error_details.message` → `error` → the raw string as-is.
    private fun extractCheckoutErrorMessage(raw: String): String {
        if (raw.isEmpty()) return raw
        try {
            val envelope = org.json.JSONObject(raw)
            val details = envelope.optJSONObject("error_details")
            val message = details?.optString("message").orEmpty()
            if (message.isNotEmpty()) return message
            val error = envelope.optString("error")
            if (error.isNotEmpty()) return error
        } catch (_: Exception) {
            // fall through to raw
        }
        return raw
    }

    /// Returns the new payment method's id paired with any underlying networking error
    /// from the create-payment-method call. The caller is responsible for surfacing the
    /// error to the user via [_checkoutError] rather than discarding it.
    private suspend fun createPaymentMethod(accountId: String): Pair<String?, com.framepayments.framesdk.NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)

        val billingAddress: FrameObjects.BillingAddress? = if (shouldValidateAddress()) {
            FrameObjects.BillingAddress(
                city = customerCity.value.orEmpty(),
                country = customerCountry.alpha2Code,
                state = customerState.value.orEmpty(),
                postalCode = customerZipCode.value.orEmpty(),
                addressLine1 = customerAddressLine1.value.orEmpty(),
                addressLine2 = customerAddressLine2.value.orEmpty()
            )
        } else null

        val pmReq = PaymentMethodRequests.CreateCardPaymentMethodRequest(
            cardNumber = cardData.card.number,
            expMonth = cardData.card.expMonth,
            expYear = cardData.card.expYear,
            cvc = cardData.card.cvc,
            customer = null,
            account = accountId,
            billing = billingAddress
        )
        val (pm, pmError) = PaymentMethodsAPI.createCardPaymentMethod(pmReq, encryptData = false)
        return Pair(pm?.id, pmError)
    }
}
