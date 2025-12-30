package com.framepayments.frameonboarding.classes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class OnboardingState(
    startStep: OnboardingStep = OnboardingStep.VerificationWelcome
) {
    var currentStep by mutableStateOf(startStep)
        private set

    fun goTo(step: OnboardingStep) {
        currentStep = step
    }
}

sealed class OnboardingStep {
    data object VerificationWelcome: OnboardingStep()
    data object VerifyIdentification: OnboardingStep()
    data object SelectPaymentMethod: OnboardingStep()
    data object AddPaymentMethod: OnboardingStep()
    data object VerifyYourCard: OnboardingStep()
    data object UploadDocuments: OnboardingStep()
    data object UploadSelfie: OnboardingStep()
}

sealed class OnboardingResult {
    data object Cancelled : OnboardingResult()
    data class Completed(
        val paymentMethodId: String?,
        val onboardingSessionId: String?
    ) : OnboardingResult()
}

data class OnboardingConfig(
    val customerId: String
)

enum class IdType(val displayName: String) {
    DRIVERS_LICENSE("Driver’s License"),
    PASSPORT("Passport"),
    ID_CARD("ID Card")
}

data class PaymentMethodSummary(
    val id: String,
    val brand: String,
    val last4: String,
    val exp: String
)

interface OnboardingCoordinator {
    val config: OnboardingConfig

    /** host app calls this to start */
    fun start()

    /** SDK calls this when user finishes or exits */
    fun finish(result: OnboardingResult)

    /** SDK calls this to move between screens */
    fun goTo(step: OnboardingStep)

    /** host app can call to close */
    fun cancel() = finish(OnboardingResult.Cancelled)
}
