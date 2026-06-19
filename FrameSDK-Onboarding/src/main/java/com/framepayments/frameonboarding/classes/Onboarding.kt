package com.framepayments.frameonboarding.classes

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.framepayments.framesdk_ui.theme.FrameTheme

internal class OnboardingState(
    startStep: OnboardingStep = OnboardingStep.VerificationWelcome
) {
    var currentStep by mutableStateOf(startStep)
        private set

    fun goTo(step: OnboardingStep) {
        currentStep = step
    }
}

/**
 * Represents a single screen in the onboarding flow.
 *
 * The ordered list of steps is computed from [OnboardingConfig.requiredCapabilities] by
 * [com.framepayments.frameonboarding.classes.computeOrderedSteps]. Merchants do not create
 * or navigate steps directly; use [OnboardingContainerView] to drive the full flow.
 */
sealed class OnboardingStep {
    /** Introductory welcome screen shown at the start of the flow. */
    data object VerificationWelcome: OnboardingStep()
    /** Screen collecting personal information and phone/DOB for identity verification. */
    data object VerifyIdentification: OnboardingStep()
    /** Screen checking the customer's geolocation for compliance. */
    data object GeolocationVerification: OnboardingStep()
    /** Screen listing the customer's existing payment methods for selection. */
    data object SelectPaymentMethod: OnboardingStep()
    /** Screen for adding a new payment Card. */
    data object AddPaymentMethod: OnboardingStep()
    /** Screen for verifying the customer's Card via 3D Secure. */
    data object VerifyYourCard: OnboardingStep()
    /** Screen listing the customer's existing payout methods for selection. */
    data object SelectPayoutMethod: OnboardingStep()
    /** Screen for adding a new payout method (bank account). */
    data object AddPayoutMethod: OnboardingStep()
    /** Screen listing the identity documents the customer must upload. */
    data object UploadDocumentsList: OnboardingStep()
    /** Camera screen for capturing the front of the customer's ID document. */
    data object CaptureFrontPhoto: OnboardingStep()
    /** Review screen for the front-of-ID photo before submission. */
    data object ReviewFrontPhoto: OnboardingStep()
    /** Camera screen for capturing the back of the customer's ID document. */
    data object CaptureBackPhoto: OnboardingStep()
    /** Review screen for the back-of-ID photo before submission. */
    data object ReviewBackPhoto: OnboardingStep()
    /** Camera screen for capturing a selfie. */
    data object CaptureSelfie: OnboardingStep()
    /** Review screen for the selfie photo before submission. */
    data object ReviewSelfie: OnboardingStep()
    /** Confirmation screen shown after all verification data has been submitted. */
    data object VerificationSubmitted: OnboardingStep()
}

/**
 * Capability names that map to backend capability strings and drive the onboarding step sequence.
 *
 * Pass one or more values in [OnboardingConfig.requiredCapabilities] to control which screens
 * appear in the flow. Mirrors the iOS `FrameObjects.Capabilities` enum.
 *
 * @property apiValue The backend API string for this capability.
 */
enum class Capabilities(val apiValue: String) {
    /** Know Your Customer identity verification. */
    KYC("kyc"),
    /** KYC with pre-filled identity data via Prove. */
    KYC_PREFILL("kyc_prefill"),
    /** Phone number verification via OTP. */
    PHONE_VERIFICATION("phone_verification"),
    /** Creator Shield fraud-protection capability. */
    CREATOR_SHIELD("creator_shield"),
    /** Card ownership verification via 3D Secure. */
    CARD_VERIFICATION("card_verification"),
    /** Ability to send funds from a Card. */
    CARD_SEND("card_send"),
    /** Ability to receive funds to a Card. */
    CARD_RECEIVE("card_receive"),
    /** Billing address verification. */
    ADDRESS_VERIFICATION("address_verification"),
    /** Bank account ownership verification. */
    BANK_ACCOUNT_VERIFICATION("bank_account_verification"),
    /** Ability to send funds from a bank account. */
    BANK_ACCOUNT_SEND("bank_account_send"),
    /** Ability to receive funds to a bank account. */
    BANK_ACCOUNT_RECEIVE("bank_account_receive"),
    /** Geographic compliance check. */
    GEO_COMPLIANCE("geo_compliance"),
    /** Age verification (must be 18+). */
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

/**
 * Terminal outcome of an onboarding session delivered to the merchant via the
 * [OnboardingContainerView] `onResult` callback.
 */
sealed class OnboardingResult {
    /** The customer dismissed the onboarding flow before completing all steps. */
    data object Cancelled : OnboardingResult()

    /**
     * All required capability steps were completed.
     *
     * @property paymentMethodId ID of the payment method added during the flow, if any.
     */
    data class Completed(
        val paymentMethodId: String?
    ) : OnboardingResult()

    /**
     * The onboarding flow encountered an unrecoverable error.
     *
     * @property message Human-readable description of the failure.
     */
    data class Failed(val message: String) : OnboardingResult()
}

/**
 * Configuration for an onboarding session passed to [OnboardingContainerView].
 *
 * @property accountId Pre-existing Frame account ID to resume onboarding for, or null to create
 *   a new account during the flow.
 * @property clientSecret Onboarding-session token (`onb_sess_…`) minted by your backend. When
 *   provided, every onboarding request is authenticated with it, scoping the flow to a single
 *   account. Leave null only for legacy integrations that authenticate via the configured keys.
 * @property requiredCapabilities Capabilities the customer must satisfy; determines which
 *   onboarding steps are shown.
 * @property skipInitNetwork When true, suppresses network calls during ViewModel init (for
 *   Compose previews and design tools).
 * @property theme Optional [FrameTheme] applied to all onboarding screens. Defaults to
 *   [FrameTheme.default] when null.
 * @property showIntroScreen When false, the "Verify Your Identity" welcome screen is omitted
 *   and the first capability-driven step is shown immediately.
 * @property showCompletionScreen When false, the "Verification Submitted" screen is omitted
 *   and the flow completes immediately after the last capability step.
 */
data class OnboardingConfig(
    val accountId: String? = null,
    val clientSecret: String? = null,
    val requiredCapabilities: List<Capabilities> = emptyList(),
    val skipInitNetwork: Boolean = false,
    val theme: FrameTheme? = null,
    val showIntroScreen: Boolean = true,
    val showCompletionScreen: Boolean = true,
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

/**
 * Mutable draft state for the manual card entry form used when Evervault UI is unavailable.
 *
 * @property cardNumber Raw card number digits as entered by the customer.
 * @property expiryMonth Two-digit expiration month (e.g. "01").
 * @property expiryYear Two- or four-digit expiration year (e.g. "26" or "2026").
 * @property cvc Three- or four-digit card security code.
 * @property useForPayouts When true, the customer intends to use this card for payouts as well.
 */
data class PaymentCardDraft(
    val cardNumber: String = "",
    val expiryMonth: String = "",
    val expiryYear: String = "",
    val cvc: String = "",
    val useForPayouts: Boolean = false
)

/**
 * Mutable draft state for the ACH bank account form.
 *
 * @property routingNumber US 9-digit ABA routing number.
 * @property accountNumber Bank account number (4–17 digits).
 * @property accountTypeLabel Human-readable account type selected by the customer ("Checking" or "Savings").
 */
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
