package com.framepayments.framesdk_ui.viewmodels

import androidx.lifecycle.*
import com.evervault.sdk.input.model.card.PaymentCardData
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.chargeintents.AuthorizationMode
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.ChargeIntentAPI
import com.framepayments.framesdk.chargeintents.ChargeIntentsRequests
import com.framepayments.framesdk.customers.CustomersAPI
import com.framepayments.framesdk.customers.CustomersRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk_ui.AddressMode
import com.framepayments.framesdk_ui.validation.FieldKey
import com.framepayments.framesdk_ui.validation.ValidationError
import com.framepayments.framesdk_ui.validation.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrameCheckoutViewModel : ViewModel() {

    // Observable customer payment options
    private val _customerPaymentOptions = MutableLiveData<List<FrameObjects.PaymentMethod>?>(null)
    val customerPaymentOptions: LiveData<List<FrameObjects.PaymentMethod>?> = _customerPaymentOptions

    // Customer fields
    val customerName = MutableLiveData("")
    val customerEmail = MutableLiveData("")
    val customerAddressLine1 = MutableLiveData("")
    val customerAddressLine2 = MutableLiveData("")
    val customerCity = MutableLiveData("")
    val customerState = MutableLiveData("")
    var customerCountry: AvailableCountry = AvailableCountries.defaultCountry
    val customerZipCode = MutableLiveData("")

    // Selected payment method and raw card data
    var selectedCustomerPaymentOption: FrameObjects.PaymentMethod? = null
        set(value) {
            field = value
            recomputeUsablePaymentInput()
        }
    var cardData: PaymentCardData = PaymentCardData()
        set(value) {
            field = value
            recomputeUsablePaymentInput()
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
        _hasUsablePaymentInput.postValue(selectedCustomerPaymentOption != null || newCardOk)
    }

    // Address mode + per-field errors
    var addressMode: AddressMode = AddressMode.REQUIRED
    private val _fieldErrors = MutableLiveData<Map<FieldKey, ValidationError>>(emptyMap())
    val fieldErrors: LiveData<Map<FieldKey, ValidationError>> = _fieldErrors

    // Internal tracking
    private var currentCustomerId: String? = null
    internal var amount: Int = 0

    fun loadCustomer(customerId: String?, amount: Int) {
        this.amount = amount
        if (customerId == null) return
        currentCustomerId = customerId

        viewModelScope.launch(Dispatchers.IO) {
            val (customer, _) = CustomersAPI.getCustomerWith(customerId)
            customer ?: return@launch

            withContext(Dispatchers.Main) {
                _customerPaymentOptions.value = customer.paymentMethods
                customerName.value = customer.name
                customerEmail.value = customer.email.orEmpty()

                customer.billingAddress?.let { address ->
                    customerAddressLine1.value = address.addressLine1.orEmpty()
                    customerAddressLine2.value = address.addressLine2.orEmpty()
                    customerCity.value = address.city.orEmpty()
                    customerState.value = address.state.orEmpty()
                    customerZipCode.value = address.postalCode
                    address.country?.let { code ->
                        AvailableCountries.allCountries.firstOrNull {
                            it.alpha2Code.equals(code, ignoreCase = true)
                        }?.let { customerCountry = it }
                    }
                }
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

        if (shouldValidateAddress()) {
            Validators.validateAddressLine1(customerAddressLine1.value)?.let { errors[FieldKey.ADDRESS_LINE_1] = it }
            Validators.validateCity(customerCity.value)?.let { errors[FieldKey.CITY] = it }
            Validators.validateState(customerState.value)?.let { errors[FieldKey.STATE] = it }
            Validators.validateZip(customerZipCode.value)?.let { errors[FieldKey.ZIP] = it }
            Validators.validateCountry(customerCountry.alpha2Code)?.let { errors[FieldKey.COUNTRY] = it }
        }

        return errors
    }

    fun checkoutWithSelectedPaymentMethod(saveMethod: Boolean): LiveData<ChargeIntent?> = liveData(Dispatchers.IO) {
        if (amount == 0) {
            emit(null)
            return@liveData
        }

        val usingSavedCard = selectedCustomerPaymentOption != null
        val errors = validateAll(forSavedCard = usingSavedCard)
        if (errors.isNotEmpty()) {
            _fieldErrors.postValue(errors)
            emit(null)
            return@liveData
        }
        // Clear any prior errors on success.
        _fieldErrors.postValue(emptyMap())

        // Determine or create payment method
        val paymentMethodId = selectedCustomerPaymentOption?.id ?: run {
            val (newPmId, newCustId) = try {
                createPaymentMethod(currentCustomerId)
            } catch (_: Exception) {
                Pair<String?, String?>(null, null)
            }
            currentCustomerId = newCustId
            newPmId
        }

        if (paymentMethodId == null) {
            emit(null)
            return@liveData
        }

        // Build the charge intent request
        val request = ChargeIntentsRequests.CreateChargeIntentRequest(
            amount = amount,
            currency = "usd",
            customer = currentCustomerId,
            description = "",
            paymentMethod = paymentMethodId,
            confirm = true,
            receiptEmail = null,
            authorizationMode = AuthorizationMode.AUTOMATIC,
            customerData = null,
            paymentMethodData = null,
            fraudSignals = null,
            sonarSessionId = FrameNetworking.currentSonarSessionId()
        )

        // Create and emit the intent
        val (intent, _) = ChargeIntentAPI.createChargeIntent(request)
        emit(intent)
    }

    private suspend fun createPaymentMethod(customerId: String? = null): Pair<String?, String?> {
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

        // 1. Create or reuse customer
        val newCustId = customerId ?: run {
            val custReq = CustomersRequests.CreateCustomerRequest(
                billingAddress = billingAddress,
                name = customerName.value.orEmpty(),
                email = customerEmail.value.orEmpty(),
                shippingAddress = null,
                phone = null,
                description = null,
                metadata = null
            )
            val (customer, _) = CustomersAPI.createCustomer(custReq)
            customer?.id.takeIf { it?.isNotEmpty() == true } ?: return Pair(null, null)
        }

        // 2. Create payment method
        val pmReq = PaymentMethodRequests.CreateCardPaymentMethodRequest(
            cardNumber = cardData.card.number,
            expMonth = cardData.card.expMonth,
            expYear = cardData.card.expYear,
            cvc = cardData.card.cvc,
            customer = newCustId,
            billing = billingAddress
        )
        val (pm, _) = PaymentMethodsAPI.createCardPaymentMethod(pmReq, encryptData = false)
        val pmId = pm?.id ?: return Pair(null, newCustId)
        return Pair(pmId, newCustId)
    }
}
