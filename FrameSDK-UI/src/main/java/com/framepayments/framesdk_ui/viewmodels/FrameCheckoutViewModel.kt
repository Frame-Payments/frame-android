package com.framepayments.framesdk_ui.viewmodels

import androidx.lifecycle.*
import com.evervault.sdk.input.model.card.PaymentCardData
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.chargeintents.AuthorizationMode
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.ChargeIntentAPI
import com.framepayments.framesdk.chargeintents.ChargeIntentsRequests
import com.framepayments.framesdk.customers.CustomersAPI
import com.framepayments.framesdk.customers.CustomersRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
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
    var cardData: PaymentCardData = PaymentCardData()

    // Internal tracking
    private var currentCustomerId: String? = null
    private var amount: Int = 0

    fun loadCustomerPaymentMethods(customerId: String?, amount: Int) {
        this.amount = amount
        if (customerId == null) return
        currentCustomerId = customerId

        viewModelScope.launch(Dispatchers.IO) {
            val (customer, error) = CustomersAPI.getCustomerWith(customerId).also {
                println("Customer response: $it")
            }
            withContext(Dispatchers.Main) {
                _customerPaymentOptions.value = customer?.paymentMethods
                customerName.value = customer?.name ?: ""
                customerEmail.value = customer?.email ?: ""

            }
        }
    }

    fun payWithApplePay() {
        // TODO: implement Apple Pay flow
    }

    fun payWithGooglePay() {
        // TODO: implement Google Pay flow
    }

    fun checkoutWithSelectedPaymentMethod(saveMethod: Boolean): LiveData<ChargeIntent?> = liveData(Dispatchers.IO) {
        if (amount == 0) {
            emit(null)
            return@liveData
        }

        // Determine or create payment method
        val paymentMethodId = selectedCustomerPaymentOption?.id ?: run {
            if (customerZipCode.value?.length != 5) {
                emit(null)
                return@liveData
            }
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
            paymentMethodData = null
        )

        // Create and emit the intent
        val (intent, error) = ChargeIntentAPI.createChargeIntent(request)
        emit(intent)
    }

    private suspend fun createPaymentMethod(customerId: String? = null): Pair<String?, String?> {
        if (customerZipCode.value.isNullOrEmpty() ||
            !cardData.isPotentiallyValid
        ) return Pair(null, null)

        val billingAddress = FrameObjects.BillingAddress(
            city = customerCity.value.orEmpty(),
            country = customerCountry.alpha2Code,
            state = customerState.value.orEmpty(),
            postalCode = customerZipCode.value.orEmpty(),
            addressLine1 = customerAddressLine1.value.orEmpty(),
            addressLine2 = customerAddressLine2.value.orEmpty()
        )

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
            val (customer, error) = CustomersAPI.createCustomer(custReq)
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
        val (pm, methodError) = PaymentMethodsAPI.createCardPaymentMethod(pmReq, encryptData = false)
        val pmId = pm?.id ?: return Pair(null, null)
        return Pair(pmId, newCustId)
    }
}