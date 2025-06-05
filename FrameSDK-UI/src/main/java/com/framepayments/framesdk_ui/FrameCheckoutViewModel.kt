package com.framepayments.framesdk_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framepayments.framesdk.FrameObjects.BillingAddress
import com.framepayments.framesdk.FrameObjects.PaymentMethod
import com.framepayments.framesdk.chargeintents.AuthorizationMode
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.chargeintents.ChargeIntentAPI
import com.framepayments.framesdk.chargeintents.ChargeIntentsRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.evervault.sdk.input.model.card.PaymentCardData

class FrameCheckoutViewModel(
    private val paymentAPI: PaymentMethodsAPI = PaymentMethodsAPI(),
    private val chargeAPI: ChargeIntentAPI = ChargeIntentAPI()
) : ViewModel() {

    private val _customerPaymentOptions = MutableStateFlow<List<PaymentMethod>?>(null)

    val customerPaymentOptions: StateFlow<List<PaymentMethod>?> = _customerPaymentOptions
    val customerCountry = MutableStateFlow("United States")
    val customerZipCode = MutableStateFlow("")
    val selectedOption = MutableStateFlow<PaymentMethod?>(null)

    val cardData = MutableStateFlow(PaymentCardData())

    private var customerId: String = ""
    private var amountInCents: Int = 0

    fun loadCustomerPaymentMethods(customerId: String, amount: Int) {
        this.customerId = customerId
        this.amountInCents = amount

        viewModelScope.launch {
            _customerPaymentOptions.value =
                kotlin.runCatching<List<PaymentMethod>?> {
                    paymentAPI.getPaymentMethodsWithCustomer(customerId)
                }.getOrNull()
        }
    }

    /* TODO: Integrate Pay-with-Android / Google Pay */
    fun payWithGooglePay() { /* stub */  }
    fun payWithApplePay()  { /* stub */  }

    fun checkoutWithSelectedPaymentMethod(saveMethod: Boolean) {
        viewModelScope.launch {
            val pm = selectedOption.value ?: createPaymentMethod() ?: return@launch

            val req = ChargeIntentsRequests.CreateChargeIntentRequest(
                amount = amountInCents,
                currency = "USD",
                customer = customerId,
                description = "",
                paymentMethod = pm?.id,
                confirm  = true,
                receiptEmail = null,
                authorizationMode = AuthorizationMode.automatic,
                customerData = null,
                paymentMethodData = null
            )

            runCatching { chargeAPI.createChargeIntent(req) }.onSuccess { /* handle */ }

            if (saveMethod) attachPaymentMethod(pm.id)
        }
    }

    private suspend fun createPaymentMethod(): PaymentMethod? {

        if (customerZipCode.value.length != 5) return null

        val billing = BillingAddress(
            country = customerCountry.value,
            postalCode  = customerZipCode.value
        )

        val req = PaymentMethodRequests.CreatePaymentMethodRequest(
            type = cardData.value.card.type?.brand.orEmpty(),
            cardNumber = cardData.value.card.number,
            expMonth = cardData.value.card.expMonth,
            expYear = cardData.value.card.expYear,
            cvc = cardData.value.card.cvc,
            customer = customerId,
            billing = billing
        )

        return runCatching { paymentAPI.createPaymentMethod(req) }.getOrNull()
    }

    private suspend fun attachPaymentMethod(paymentMethodId: String) {
        val attachReq = PaymentMethodRequests.AttachPaymentMethodRequest(
            customer = customerId
        )

        runCatching { paymentAPI.attachPaymentMethodWith(paymentMethodId, attachReq) }
    }
}