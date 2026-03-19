package com.framepayments.frameonboarding.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.threedsecure.ThreeDSecureRequests
import com.framepayments.framesdk.threedsecure.ThreeDSecureVerificationsAPI
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingData
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingState
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.computeFlowSegments
import com.framepayments.frameonboarding.classes.computeOrderedSteps
import com.framepayments.frameonboarding.viewmodels.FrameOnboarding

@Composable
fun OnboardingContainerView(
    config: OnboardingConfig,
    onResult: (OnboardingResult) -> Unit
) {
    var onboardingData by remember { mutableStateOf(OnboardingData()) }
    val flowSegments = remember(config.requiredCapabilities) { computeFlowSegments(config.requiredCapabilities) }
    val orderedSteps = remember(config.requiredCapabilities) { computeOrderedSteps(config.requiredCapabilities) }
    val state = remember(orderedSteps) { OnboardingState(orderedSteps.first()) }
    var savedPaymentMethods by remember { mutableStateOf<List<PaymentMethodSummary>>(emptyList()) }
    var savedPayoutMethods by remember { mutableStateOf<List<PaymentMethodSummary>>(emptyList()) }
    var threeDSVerificationId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.currentStep, onboardingData.selectedPaymentMethodId) {
        if (
            state.currentStep == OnboardingStep.VerifyYourCard &&
            config.requiredCapabilities.contains(Capabilities.CARD_VERIFICATION)
        ) {
            val paymentMethodId = onboardingData.selectedPaymentMethodId ?: return@LaunchedEffect
            val request = ThreeDSecureRequests.CreateThreeDSecureVerification(
                paymentMethodId = paymentMethodId
            )
            val (verification, networkError, _) = ThreeDSecureVerificationsAPI.create3DSecureVerification(request)
            if (networkError != null) {
                onResult(OnboardingResult.Error("Failed to initialize card verification. Please try again."))
                return@LaunchedEffect
            }
            threeDSVerificationId = verification?.id
        }
    }

    val resend3DS: () -> Unit = {
        scope.launch {
            threeDSVerificationId?.let { id ->
                ThreeDSecureVerificationsAPI.resend3DSecureVerification(id)
            }
        }
    }

    LaunchedEffect(config.accountId) {
        val customerOrAccountId = config.accountId ?: return@LaunchedEffect
        val (list, _) = PaymentMethodsAPI.getPaymentMethodsWithCustomer(customerOrAccountId)
        savedPaymentMethods = list
            ?.mapNotNull { pm ->
                pm.card?.let { c ->
                    PaymentMethodSummary(
                        id = pm.id,
                        brand = c.brand.uppercase(),
                        last4 = c.lastFourDigits,
                        exp = "${c.expirationMonth}/${c.expirationYear.takeLast(2)}"
                    )
                }
            }
            ?: emptyList()
        savedPayoutMethods = list
            ?.filter { it.ach != null }
            ?.map { pm ->
                PaymentMethodSummary(
                    id = pm.id,
                    brand = "BANK",
                    last4 = pm.ach?.lastFour ?: "",
                    exp = ""
                )
            }
            ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ProgressIndicator(
            currentStep = state.currentStep,
            flowSegments = flowSegments,
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.weight(1f)) {
            FrameOnboarding(
                config = config,
                onboardingData = onboardingData,
                state = state,
                orderedSteps = orderedSteps,
                savedPaymentMethods = savedPaymentMethods,
                savedPayoutMethods = savedPayoutMethods,
                onResend3DS = resend3DS,
                onUpdateData = { updatedData ->
                    onboardingData = updatedData
                },
                onResult = onResult
            )
        }
    }
}
