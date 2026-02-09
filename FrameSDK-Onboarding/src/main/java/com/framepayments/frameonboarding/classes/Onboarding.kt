package com.framepayments.frameonboarding.classes

import android.net.Uri
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
    data object SelectPayoutMethod: OnboardingStep()
    data object AddPayoutMethod: OnboardingStep()
    data object UploadDocumentsList: OnboardingStep()
    data object CaptureFrontPhoto: OnboardingStep()
    data object ReviewFrontPhoto: OnboardingStep()
    data object CaptureBackPhoto: OnboardingStep()
    data object ReviewBackPhoto: OnboardingStep()
    data object CaptureSelfie: OnboardingStep()
    data object ReviewSelfie: OnboardingStep()
    data object GeolocationVerification: OnboardingStep()
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

enum class PhotoType {
    FRONT,
    BACK,
    SELFIE
}

enum class GeolocationState {
    CHECKING,
    VERIFIED,
    VPN_DETECTED
}

data class PaymentMethodDetails(
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvc: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String,
    val zipCode: String,
    val useForPayouts: Boolean
)

data class PayoutMethodDetails(
    val routingNumber: String,
    val accountNumber: String,
    val accountType: String
)

data class OnboardingData(
    // Step 1: Identity Verification
    val issuingCountry: String? = null,
    val idType: IdType? = null,
    
    // Step 2: Payment Methods
    val selectedPaymentMethodId: String? = null,
    val newPaymentMethod: PaymentMethodDetails? = null,
    val cardVerificationCode: String? = null,
    
    // Step 3: Payout Methods
    val selectedPayoutMethodId: String? = null,
    val newPayoutMethod: PayoutMethodDetails? = null,
    
    // Step 4: Document Upload
    val frontPhotoUri: Uri? = null,
    val backPhotoUri: Uri? = null,
    val selfieUri: Uri? = null,
    
    // Step 5: Geolocation
    val geolocationVerified: Boolean = false,
    val vpnDetected: Boolean = false
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
