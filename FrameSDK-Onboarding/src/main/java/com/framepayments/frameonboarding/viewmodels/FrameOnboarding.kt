package com.framepayments.frameonboarding.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import android.net.Uri
import androidx.compose.runtime.*
import com.framepayments.frameonboarding.classes.*
import com.framepayments.frameonboarding.views.*

@Composable
fun FrameOnboarding(
    config: OnboardingConfig,
    onboardingData: OnboardingData,
    state: OnboardingState,
    orderedSteps: List<OnboardingStep>,
    savedPaymentMethods: List<PaymentMethodSummary> = emptyList(),
    savedPayoutMethods: List<PaymentMethodSummary> = emptyList(),
    onResend3DS: () -> Unit = {},
    onUpdateData: (OnboardingData) -> Unit,
    onResult: (OnboardingResult) -> Unit
) {
    val moveNext: () -> Unit = {
        val i = orderedSteps.indexOf(state.currentStep)
        if (i < 0 || i >= orderedSteps.size - 1) {
            onResult(
                OnboardingResult.Completed(
                    paymentMethodId = onboardingData.selectedPaymentMethodId,
                    onboardingSessionId = "kyc_session"
                )
            )
        } else {
            state.goTo(orderedSteps[i + 1])
        }
    }
    val moveBack: () -> Unit = {
        val i = orderedSteps.indexOf(state.currentStep)
        if (i > 0) state.goTo(orderedSteps[i - 1])
    }

    when (state.currentStep) {
        OnboardingStep.VerificationWelcome -> {
            UserIdentificationView(
                onContinue = moveNext
            )
        }

        OnboardingStep.VerifyIdentification -> {
            VerifyIdFormScreen(
                accountId = config.accountId ?: config.customerId,
                requiresDateOfBirth = config.requiredCapabilities.contains(Capabilities.KYC_PREFILL),
                onBack = moveBack,
                onContinue = { country, idType ->
                    onUpdateData(onboardingData.copy(
                        issuingCountry = country,
                        idType = idType
                    ))
                    moveNext()
                }
            )
        }

        OnboardingStep.SelectPaymentMethod -> {
            SelectPaymentMethodScreen(
                savedMethods = savedPaymentMethods.ifEmpty {
                    listOf(
                        PaymentMethodSummary(id = "pm_1", brand = "VISA", last4 = "2872", exp = "08/29"),
                        PaymentMethodSummary(id = "pm_2", brand = "MASTERCARD", last4 = "3292", exp = "10/27"),
                    )
                },
                selectedId = onboardingData.selectedPaymentMethodId,
                onSelect = {
                    onUpdateData(onboardingData.copy(selectedPaymentMethodId = it))
                },
                onAddCard = moveNext,
                onBack = moveBack,
                onContinue = {
                    if (onboardingData.selectedPaymentMethodId != null || onboardingData.newPaymentMethod != null) {
                        moveNext()
                    }
                }
            )
        }

        OnboardingStep.AddPaymentMethod -> {
            AddPaymentMethodScreen(
                onBack = moveBack,
                onContinue = { paymentDetails ->
                    onUpdateData(onboardingData.copy(newPaymentMethod = paymentDetails))
                    moveNext()
                }
            )
        }

        OnboardingStep.VerifyYourCard -> {
            VerifyCardScreen(
                onBack = moveBack,
                onResendCode = onResend3DS,
                onContinue = { code ->
                    onUpdateData(onboardingData.copy(cardVerificationCode = code))
                    moveNext()
                }
            )
        }

        OnboardingStep.SelectPayoutMethod -> {
            SelectPayoutMethodScreen(
                savedMethods = savedPayoutMethods,
                selectedId = onboardingData.selectedPayoutMethodId,
                onSelect = { onUpdateData(onboardingData.copy(selectedPayoutMethodId = it)) },
                onAddPayout = moveNext,
                onBack = moveBack,
                onContinue = moveNext
            )
        }

        OnboardingStep.AddPayoutMethod -> {
            AddPayoutMethodScreen(
                onBack = moveBack,
                onContinue = { payoutDetails ->
                    onUpdateData(onboardingData.copy(newPayoutMethod = payoutDetails))
                    moveNext()
                }
            )
        }

        OnboardingStep.UploadDocumentsList -> {
            UploadDocumentsScreen(
                frontPhotoComplete = onboardingData.frontPhotoUri != null,
                backPhotoComplete = onboardingData.backPhotoUri != null,
                selfieComplete = onboardingData.selfieUri != null,
                onBack = moveBack,
                onFrontPhotoClick = { state.goTo(OnboardingStep.CaptureFrontPhoto) },
                onBackPhotoClick = { state.goTo(OnboardingStep.CaptureBackPhoto) },
                onSelfieClick = { state.goTo(OnboardingStep.CaptureSelfie) },
                onSubmit = moveNext
            )
        }

        OnboardingStep.CaptureFrontPhoto -> {
            CameraCaptureScreen(
                photoType = PhotoType.FRONT,
                onClose = moveBack,
                onPhotoCaptured = { uri ->
                    onUpdateData(onboardingData.copy(frontPhotoUri = uri))
                    moveNext()
                }
            )
        }

        OnboardingStep.ReviewFrontPhoto -> {
            val photoUri = onboardingData.frontPhotoUri
            if (photoUri == null) {
                state.goTo(OnboardingStep.CaptureFrontPhoto)
                return
            }
            ReviewPhotoScreen(
                photoUri = photoUri,
                onBack = moveBack,
                onUsePhoto = moveNext,
                onRetake = {
                    onUpdateData(onboardingData.copy(frontPhotoUri = null))
                    state.goTo(OnboardingStep.CaptureFrontPhoto)
                }
            )
        }

        OnboardingStep.CaptureBackPhoto -> {
            CameraCaptureScreen(
                photoType = PhotoType.BACK,
                onClose = moveBack,
                onPhotoCaptured = { uri ->
                    onUpdateData(onboardingData.copy(backPhotoUri = uri))
                    moveNext()
                }
            )
        }

        OnboardingStep.ReviewBackPhoto -> {
            val photoUri = onboardingData.backPhotoUri
            if (photoUri == null) {
                state.goTo(OnboardingStep.CaptureBackPhoto)
                return
            }
            ReviewPhotoScreen(
                photoUri = photoUri,
                onBack = moveBack,
                onUsePhoto = moveNext,
                onRetake = {
                    onUpdateData(onboardingData.copy(backPhotoUri = null))
                    state.goTo(OnboardingStep.CaptureBackPhoto)
                }
            )
        }

        OnboardingStep.CaptureSelfie -> {
            CameraCaptureScreen(
                photoType = PhotoType.SELFIE,
                onClose = moveBack,
                onPhotoCaptured = { uri ->
                    onUpdateData(onboardingData.copy(selfieUri = uri))
                    moveNext()
                }
            )
        }

        OnboardingStep.ReviewSelfie -> {
            val photoUri = onboardingData.selfieUri
            if (photoUri == null) {
                state.goTo(OnboardingStep.CaptureSelfie)
                return
            }
            ReviewPhotoScreen(
                photoUri = photoUri,
                onBack = moveBack,
                onUsePhoto = moveNext,
                onRetake = {
                    onUpdateData(onboardingData.copy(selfieUri = null))
                    state.goTo(OnboardingStep.CaptureSelfie)
                }
            )
        }

        OnboardingStep.GeolocationVerification -> {
            GeolocationVerificationScreen(
                onContinue = {
                    onUpdateData(onboardingData.copy(geolocationVerified = true))
                    moveNext()
                },
                onDisableVpn = {
                    onUpdateData(onboardingData.copy(vpnDetected = true))
                }
            )
        }

        OnboardingStep.VerificationSubmitted -> {
            VerificationSubmittedScreen(onDone = moveNext)
        }
    }
}