package com.framepayments.framesdk_ui.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.evervault.sdk.input.model.card.PaymentCardData
import com.framepayments.framesdk.FrameCheckoutError
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk.accountevents.AccountEventDetail
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen
import com.framepayments.framesdk.accounts.AccountsAPI
import com.framepayments.framesdk.chargeintents.ChargeIntentConfirmation
import com.framepayments.framesdk.chargeintents.FrameChargeIntentError
import com.framepayments.framesdk.chargeintents.FrameChargeIntentOutcome
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.transfers.Transfer
import com.framepayments.framesdk.transfers.TransferRequests
import com.framepayments.framesdk.transfers.TransferStatus
import com.framepayments.framesdk.transfers.TransfersAPI
import com.framepayments.framesdk_ui.AddressMode
import com.framepayments.framesdk_ui.FrameThreeDSecureChallengePresenter
import com.framepayments.framesdk_ui.snackbar.FrameSnackbarController
import com.framepayments.framesdk_ui.validation.FieldKey
import com.framepayments.framesdk_ui.validation.ValidationError
import com.framepayments.framesdk_ui.validation.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the checkout surface.
 *
 * Holds the observable state for payer details, saved payment methods, field validation errors,
 * and the in-flight action indicator. Call [loadAccountDetails] once after construction to
 * pre-fill name/email and load saved cards. Call [checkoutWithSelectedPaymentMethod] to submit.
 */
class FrameCheckoutViewModel : ViewModel() {

    private val _accountPaymentOptions = MutableLiveData<List<FrameObjects.PaymentMethod>?>(null)
    /** Saved payment methods belonging to the account; null until [loadAccountDetails] completes. */
    val accountPaymentOptions: LiveData<List<FrameObjects.PaymentMethod>?> = _accountPaymentOptions

    private val _didLoadAccountPaymentMethods = MutableLiveData(false)
    /**
     * True once [loadAccountDetails] has finished fetching saved payment methods. The view
     * defers rendering the new-card section until this flips, so returning users don't see
     * the Card/Billing block flash visible before auto-selection of their first saved method.
     */
    val didLoadAccountPaymentMethods: LiveData<Boolean> = _didLoadAccountPaymentMethods

    /** Customer's full name entered in the checkout form. */
    val customerName = MutableLiveData("")
    /** Customer's email address entered in the checkout form. */
    val customerEmail = MutableLiveData("")
    /** Customer's primary street address entered in the checkout form. */
    val customerAddressLine1 = MutableLiveData("")
    /** Customer's secondary address line (apartment, suite, etc.) entered in the checkout form. */
    val customerAddressLine2 = MutableLiveData("")
    /** Customer's city entered in the checkout form. */
    val customerCity = MutableLiveData("")
    /** Customer's state or province entered in the checkout form. */
    val customerState = MutableLiveData("")
    /** Customer's selected country from the checkout country picker. */
    var customerCountry: AvailableCountry = AvailableCountries.defaultCountry
    /** Customer's ZIP or postal code entered in the checkout form. */
    val customerZipCode = MutableLiveData("")

    private val _selectedAccountPaymentOption = MutableLiveData<FrameObjects.PaymentMethod?>(null)
    /** The payment method selected from [accountPaymentOptions]; null when entering a new card. */
    val selectedAccountPaymentOption: LiveData<FrameObjects.PaymentMethod?> = _selectedAccountPaymentOption

    /** Sets [selectedAccountPaymentOption] and recomputes [hasUsablePaymentInput]. */
    fun setSelectedAccountPaymentOption(method: FrameObjects.PaymentMethod?) {
        _selectedAccountPaymentOption.value = method
        AccountEventEmitter.emit(
            AccountEventName.CHECKOUT_PAYMENT_METHOD_SELECTED,
            AccountEventScreen.PAYMENT_SHEET,
            if (method != null) AccountEventDetail.CHECKOUT_SAVED_PAYMENT_METHOD else AccountEventDetail.CHECKOUT_NEW_PAYMENT_METHOD
        )
        recomputeUsablePaymentInput()
    }

    /**
     * Encrypted card data from the Evervault card input. Setting this value recomputes
     * [hasUsablePaymentInput].
     */
    var cardData: PaymentCardData = PaymentCardData()
        set(value) {
            field = value
            recomputeUsablePaymentInput()
        }

    /**
     * Clears field errors that only apply to the new-card flow. Call when the customer selects
     * a saved payment method so stale validation messages don't linger behind the collapsed
     * Card/Billing sections.
     */
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

    private val _hasUsablePaymentInput = MutableLiveData(false)
    /**
     * True when the customer has either selected a saved payment method or entered card details
     * that pass Evervault's potential-validity check. Drives the pay button's enabled state.
     */
    val hasUsablePaymentInput: LiveData<Boolean> = _hasUsablePaymentInput

    private fun recomputeUsablePaymentInput() {
        // Evervault's `isPotentiallyValid` returns true for an empty card, so we
        // also require a non-empty card number before treating new-card input as usable.
        val newCardOk = cardData.card.number.isNotEmpty() && cardData.isPotentiallyValid
        _hasUsablePaymentInput.postValue(_selectedAccountPaymentOption.value != null || newCardOk)
    }

    /** Controls whether the billing address section is required, optional, or hidden. */
    var addressMode: AddressMode = AddressMode.REQUIRED
    private val _fieldErrors = MutableLiveData<Map<FieldKey, ValidationError>>(emptyMap())
    /** Per-field validation errors surfaced to the UI; empty map when no errors are present. */
    val fieldErrors: LiveData<Map<FieldKey, ValidationError>> = _fieldErrors

    // Internal tracking
    private var currentAccountId: String? = null
    internal var amount: Int = 0

    private val _isPerformingAction = MutableLiveData(false)
    /**
     * True while the checkout submit is in-flight. Drives the pay button's disabled state and
     * the in-button progress indicator. Re-entrant submit calls bail out immediately.
     */
    val isPerformingAction: LiveData<Boolean> = _isPerformingAction

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
        // Checkout is often the first surface a host shows, so this may be the SDK's first
        // sighting of the account — publish it before emitting, or the event is dropped.
        FrameNetworking.setAccountIdIfUnset(accountId)
        AccountEventEmitter.emit(AccountEventName.CHECKOUT_STARTED, AccountEventScreen.PAYMENT_SHEET)

        viewModelScope.launch(Dispatchers.IO) {
            val (account, accountError) = AccountsAPI.getAccountWith(accountId)
            reportError(accountError)
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

            val (paymentMethods, paymentMethodsError) = PaymentMethodsAPI.getPaymentMethodsWithAccount(accountId)
            if (paymentMethodsError != null) {
                AccountEventEmitter.emit(
                    AccountEventName.SAVED_PAYMENT_METHODS_LOAD_FAILED,
                    AccountEventScreen.PAYMENT_SHEET,
                    "$paymentMethodsError"
                )
            }
            reportError(paymentMethodsError)
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

    /**
     * Sets or clears the validation error for [key]. Passing `null` for [error] removes the entry.
     *
     * @param key The form field whose error state should be updated.
     * @param error The validation error to display, or null to clear the current error.
     */
    fun setError(key: FieldKey, error: ValidationError?) {
        val current = _fieldErrors.value.orEmpty().toMutableMap()
        if (error == null) current.remove(key) else current[key] = error
        _fieldErrors.postValue(current)
    }

    /**
     * Removes the validation error for [key] if one is present; no-op otherwise.
     *
     * @param key The form field whose error should be cleared.
     */
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

    /**
     * Validates inputs and submits the checkout.
     *
     * If a saved payment method is selected, the new-card fields are skipped during validation.
     * Creates the payment method if needed, then creates a Transfer against [currentAccountId].
     * Emits `null` on validation failure, a missing account id, or a networking error.
     *
     * @param saveMethod Whether to persist the new card as a saved payment method (currently unused).
     * @return [LiveData] that emits the created [Transfer] on success, or null on failure.
     */
    fun checkoutWithSelectedPaymentMethod(saveMethod: Boolean, context: Context): LiveData<Transfer?> = liveData(Dispatchers.IO) {
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
        AccountEventEmitter.emit(
            AccountEventName.CHECKOUT_PAYMENT_STARTED,
            AccountEventScreen.PAYMENT_SHEET,
            AccountEventDetail.CHECKOUT_PAY_BUTTON_TAPPED
        )
        try {
            val usingSavedCard = _selectedAccountPaymentOption.value != null
            val errors = validateAll(forSavedCard = usingSavedCard)
            if (errors.isNotEmpty()) {
                AccountEventEmitter.emit(
                    AccountEventName.CHECKOUT_VALIDATION_FAILED,
                    AccountEventScreen.PAYMENT_SHEET,
                    errors.keys.joinToString(", ") { "$it" }
                )
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
                    AccountEventEmitter.emit(
                        AccountEventName.CHECKOUT_PAYMENT_FAILED,
                        AccountEventScreen.PAYMENT_SHEET,
                        "$pmError"
                    )
                    reportError(pmError)
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
                AccountEventEmitter.emit(
                    AccountEventName.CHECKOUT_PAYMENT_FAILED,
                    AccountEventScreen.PAYMENT_SHEET,
                    "$transferError"
                )
                reportError(transferError)
                emit(null)
                return@liveData
            }
            if (transfer == null) {
                emit(null)
                return@liveData
            }

            if (transfer.status != TransferStatus.REQUIRES_CONFIRMATION && transfer.status != TransferStatus.REQUIRES_THREE_D_SECURE) {
                AccountEventEmitter.emit(AccountEventName.CHECKOUT_PAYMENT_SUCCEEDED, AccountEventScreen.PAYMENT_SHEET)
                emit(transfer)
                return@liveData
            }

            emit(completeThreeDSecure(transfer, context))
        } finally {
            _isPerformingAction.postValue(false)
        }
    }

    /**
     * Confirms a transfer the API held back, running a 3D Secure challenge if the confirm asks
     * for one, and reports the charge's real outcome.
     */
    private suspend fun completeThreeDSecure(transfer: Transfer, context: Context): Transfer? {
        val clientSecret = transfer.clientSecret ?: run {
            FrameSnackbarController.emit(FrameCheckoutError.ThreeDSecureUnavailable().toastMessage())
            return null
        }

        val confirmation = ChargeIntentConfirmation(challengePresenter = FrameThreeDSecureChallengePresenter(context))

        return try {
            when (val outcome = confirmation.confirm(clientSecret)) {
                is FrameChargeIntentOutcome.Succeeded -> {
                    AccountEventEmitter.emit(AccountEventName.CHECKOUT_PAYMENT_SUCCEEDED, AccountEventScreen.PAYMENT_SHEET)
                    transfer
                }
                is FrameChargeIntentOutcome.Failed -> {
                    val declined = FrameCheckoutError.Declined(outcome.reason?.message)
                    AccountEventEmitter.emit(
                        AccountEventName.CHECKOUT_PAYMENT_DECLINED,
                        AccountEventScreen.PAYMENT_SHEET,
                        declined.toastMessage()
                    )
                    FrameSnackbarController.emit(declined.toastMessage())
                    null
                }
                is FrameChargeIntentOutcome.TimedOut -> {
                    // The charge may still settle, so this is not reported as a decline.
                    AccountEventEmitter.emit(
                        AccountEventName.CHECKOUT_PAYMENT_FAILED,
                        AccountEventScreen.PAYMENT_SHEET,
                        "$outcome"
                    )
                    FrameSnackbarController.emit(FrameCheckoutError.Unresolved().toastMessage())
                    null
                }
            }
        } catch (e: FrameChargeIntentError) {
            AccountEventEmitter.emit(AccountEventName.CHECKOUT_PAYMENT_FAILED, AccountEventScreen.PAYMENT_SHEET, "$e")
            FrameSnackbarController.emit(FrameCheckoutError.ThreeDSecureUnavailable().toastMessage())
            null
        }
    }

    /// Emit a snackbar for any [error]. Server errors surface the parsed `error_details.message`
    /// from the Frame envelope; transport errors use the generic fallback. No-op for `null`.
    private fun reportError(error: NetworkingError?) {
        if (error != null) {
            FrameSnackbarController.emit(error.toastMessage())
        }
    }

    /// Returns the new payment method's id paired with any underlying networking error
    /// from the create-payment-method call. The caller is responsible for surfacing the
    /// error to the user via the snackbar rather than discarding it.
    private suspend fun createPaymentMethod(accountId: String): Pair<String?, NetworkingError?> {
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
        if (pmError != null) {
            AccountEventEmitter.emit(
                AccountEventName.CARD_TOKENIZATION_FAILED,
                AccountEventScreen.PAYMENT_SHEET,
                "$pmError"
            )
        } else {
            AccountEventEmitter.emit(AccountEventName.CARD_TOKENIZED, AccountEventScreen.PAYMENT_SHEET)
        }
        return Pair(pm?.id, pmError)
    }
}
