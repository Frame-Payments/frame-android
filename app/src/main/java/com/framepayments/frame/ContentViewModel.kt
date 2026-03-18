package com.framepayments.frame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.ChargeIntentAPI
import com.framepayments.framesdk.customers.CustomersAPI
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.refunds.Refund
import com.framepayments.framesdk.refunds.RefundsAPI
import com.framepayments.framesdk.subscriptions.Subscription
import com.framepayments.framesdk.subscriptions.SubscriptionsAPI
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhase
import com.framepayments.framesdk.subscriptionphases.SubscriptionPhasesAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContentUiState(
    val customers: List<FrameObjects.Customer> = emptyList(),
    val paymentMethods: List<FrameObjects.PaymentMethod> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val chargeIntents: List<ChargeIntent> = emptyList(),
    val refunds: List<Refund> = emptyList(),
    val subscriptionPhases: List<SubscriptionPhase> = emptyList(),
)

class ContentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ContentUiState())
    val uiState: StateFlow<ContentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadCustomers()
            loadPaymentMethods()
            loadSubscriptions()
            loadChargeIntents()
            loadRefunds()
            loadSubscriptionPhases()
        }
    }

    private suspend fun loadCustomers() {
        val (response, _) = CustomersAPI.getCustomers()
        _uiState.value = _uiState.value.copy(
            customers = response?.data ?: emptyList()
        )
    }

    private suspend fun loadPaymentMethods() {
        val (response, _) = PaymentMethodsAPI.getPaymentMethods()
        _uiState.value = _uiState.value.copy(
            paymentMethods = response?.data ?: emptyList()
        )
    }

    private suspend fun loadSubscriptions() {
        val (response, _) = SubscriptionsAPI.getSubscriptions(perPage = 50, page = 1)
        _uiState.value = _uiState.value.copy(
            subscriptions = response?.data ?: emptyList()
        )
    }

    private suspend fun loadChargeIntents() {
        val (response, _) = ChargeIntentAPI.getAllChargeIntents(perPage = 50, page = 1)
        _uiState.value = _uiState.value.copy(
            chargeIntents = response?.data ?: emptyList()
        )
    }

    private suspend fun loadRefunds() {
        val (response, _) = RefundsAPI.getRefunds(chargeId = null, chargeIntentId = null, perPage = 50, page = 1)
        _uiState.value = _uiState.value.copy(
            refunds = response?.data ?: emptyList()
        )
    }

    private suspend fun loadSubscriptionPhases() {
        val (subResponse, _) = SubscriptionsAPI.getSubscriptions(perPage = 1, page = 1)
        val firstSub = subResponse?.data?.firstOrNull() ?: return
        val (phaseResponse, _) = SubscriptionPhasesAPI.getSubscriptionPhases(firstSub.id)
        _uiState.value = _uiState.value.copy(
            subscriptionPhases = phaseResponse?.phases ?: emptyList()
        )
    }
}
