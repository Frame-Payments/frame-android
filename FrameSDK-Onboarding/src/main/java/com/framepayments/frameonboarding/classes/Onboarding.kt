package com.framepayments.frameonboarding.classes

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class OnboardingState(
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
    data object GeolocationVerification: OnboardingStep()
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
    data object VerificationSubmitted: OnboardingStep()
}

/**
 * Capability names matching the backend and iOS FrameObjects.Capabilities.
 * Used to drive which onboarding steps are shown (capability-driven flow).
 */
enum class Capabilities(val apiValue: String) {
    KYC("kyc"),
    KYC_PREFILL("kyc_prefill"),
    PHONE_VERIFICATION("phone_verification"),
    CREATOR_SHIELD("creator_shield"),
    CARD_VERIFICATION("card_verification"),
    CARD_SEND("card_send"),
    CARD_RECEIVE("card_receive"),
    ADDRESS_VERIFICATION("address_verification"),
    BANK_ACCOUNT_VERIFICATION("bank_account_verification"),
    BANK_ACCOUNT_SEND("bank_account_send"),
    BANK_ACCOUNT_RECEIVE("bank_account_receive"),
    GEO_COMPLIANCE("geo_compliance"),
    AGE_VERIFICATION("age_verification")
}

/**
 */
internal enum class OnboardingFlowSegment(val order: Int) {
    PERSONAL_INFORMATION(0),
    CONFIRM_PAYMENT_METHOD(1),
    CONFIRM_PAYOUT_METHOD(2),
    VERIFICATION_SUBMITTED(3),
    UPLOAD_DOCUMENTS(4)
}

internal fun Capabilities.toFlowSegment(): OnboardingFlowSegment = when (this) {
    Capabilities.KYC, Capabilities.KYC_PREFILL, Capabilities.PHONE_VERIFICATION, Capabilities.CREATOR_SHIELD,
    Capabilities.AGE_VERIFICATION, Capabilities.GEO_COMPLIANCE ->
        OnboardingFlowSegment.PERSONAL_INFORMATION
    Capabilities.CARD_VERIFICATION, Capabilities.CARD_SEND, Capabilities.CARD_RECEIVE, Capabilities.ADDRESS_VERIFICATION ->
        OnboardingFlowSegment.CONFIRM_PAYMENT_METHOD
    Capabilities.BANK_ACCOUNT_VERIFICATION, Capabilities.BANK_ACCOUNT_SEND, Capabilities.BANK_ACCOUNT_RECEIVE ->
        OnboardingFlowSegment.CONFIRM_PAYOUT_METHOD
}

internal fun OnboardingFlowSegment.toSteps(): List<OnboardingStep> = when (this) {
    OnboardingFlowSegment.PERSONAL_INFORMATION -> listOf(
        OnboardingStep.VerificationWelcome,
        OnboardingStep.VerifyIdentification
    )
    OnboardingFlowSegment.CONFIRM_PAYMENT_METHOD -> listOf(
        OnboardingStep.SelectPaymentMethod,
        OnboardingStep.AddPaymentMethod,
        OnboardingStep.VerifyYourCard
    )
    OnboardingFlowSegment.CONFIRM_PAYOUT_METHOD -> listOf(
        OnboardingStep.SelectPayoutMethod,
        OnboardingStep.AddPayoutMethod
    )
    OnboardingFlowSegment.UPLOAD_DOCUMENTS -> listOf(
        OnboardingStep.UploadDocumentsList,
        OnboardingStep.CaptureFrontPhoto,
        OnboardingStep.ReviewFrontPhoto,
        OnboardingStep.CaptureBackPhoto,
        OnboardingStep.ReviewBackPhoto,
        OnboardingStep.CaptureSelfie,
        OnboardingStep.ReviewSelfie
    )
    OnboardingFlowSegment.VERIFICATION_SUBMITTED -> listOf(OnboardingStep.VerificationSubmitted)
}

/**
 * Builds the ordered list of onboarding steps from required capabilities.
 */
internal fun computeFlowSegments(requiredCapabilities: List<Capabilities>): List<OnboardingFlowSegment> {
    if (requiredCapabilities.isEmpty()) {
        return listOf(
            OnboardingFlowSegment.PERSONAL_INFORMATION,
            OnboardingFlowSegment.VERIFICATION_SUBMITTED
        )
    }
    val segmentSet = requiredCapabilities.map { it.toFlowSegment() }.toMutableSet()
    return (segmentSet.sortedBy { it.order } + OnboardingFlowSegment.VERIFICATION_SUBMITTED).distinct()
}

internal fun computeOrderedSteps(requiredCapabilities: List<Capabilities>): List<OnboardingStep> {
    val needsCardVerification = requiredCapabilities.contains(Capabilities.CARD_VERIFICATION)
    return computeFlowSegments(requiredCapabilities).flatMap { segment ->
        when (segment) {
            OnboardingFlowSegment.CONFIRM_PAYMENT_METHOD -> {
                if (needsCardVerification) {
                    segment.toSteps()
                } else {
                    listOf(OnboardingStep.SelectPaymentMethod, OnboardingStep.AddPaymentMethod)
                }
            }
            else -> segment.toSteps()
        }
    }
}

internal fun OnboardingStep.toFlowSegment(): OnboardingFlowSegment = when (this) {
    OnboardingStep.VerificationWelcome,
    OnboardingStep.VerifyIdentification,
    OnboardingStep.GeolocationVerification -> OnboardingFlowSegment.PERSONAL_INFORMATION
    OnboardingStep.SelectPaymentMethod,
    OnboardingStep.AddPaymentMethod,
    OnboardingStep.VerifyYourCard -> OnboardingFlowSegment.CONFIRM_PAYMENT_METHOD
    OnboardingStep.SelectPayoutMethod,
    OnboardingStep.AddPayoutMethod -> OnboardingFlowSegment.CONFIRM_PAYOUT_METHOD
    OnboardingStep.UploadDocumentsList,
    OnboardingStep.CaptureFrontPhoto,
    OnboardingStep.ReviewFrontPhoto,
    OnboardingStep.CaptureBackPhoto,
    OnboardingStep.ReviewBackPhoto,
    OnboardingStep.CaptureSelfie,
    OnboardingStep.ReviewSelfie -> OnboardingFlowSegment.UPLOAD_DOCUMENTS
    OnboardingStep.VerificationSubmitted -> OnboardingFlowSegment.VERIFICATION_SUBMITTED
}

sealed class OnboardingResult {
    data object Cancelled : OnboardingResult()
    data class Completed(
        val paymentMethodId: String?
    ) : OnboardingResult()
    data class Error(val message: String) : OnboardingResult()
}

data class OnboardingConfig(
    val accountId: String? = null,
    val requiredCapabilities: List<Capabilities> = emptyList(),
    /**
     * When true, [FrameOnboardingViewModel] skips network work in `init` (Compose Preview / design tools).
     */
    val skipInitNetwork: Boolean = false
)

internal data class PaymentMethodSummary(
    val id: String,
    val brand: String,
    val last4: String,
    val exp: String
)

internal enum class PhotoType {
    FRONT,
    BACK,
    SELFIE
}

data class PaymentCardDraft(
    val cardNumber: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val cvc: String = "",
    val useForPayouts: Boolean = false
)

data class BankAccountDraft(
    val routingNumber: String = "",
    val accountNumber: String = "",
    val accountTypeLabel: String = "Checking"
)

internal data class OnboardingData(
    // Step 1: Payment Methods
    val selectedPaymentMethodId: String? = null,
    val cardVerificationCode: String? = null,

    // Step 3: Payout Methods
    val selectedPayoutMethodId: String? = null,

    // Step 4: Document Upload
    val frontPhotoUri: Uri? = null,
    val backPhotoUri: Uri? = null,
    val selfieUri: Uri? = null,

    // Personal information (collected in UserIdentificationView)
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val ssnLast4: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val stateCode: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val phoneNumber: String? = null,
    // IDs set after API calls
    val customerIdentityId: String? = null,
    val resolvedAccountId: String? = null,
)

internal interface OnboardingCoordinator {
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
