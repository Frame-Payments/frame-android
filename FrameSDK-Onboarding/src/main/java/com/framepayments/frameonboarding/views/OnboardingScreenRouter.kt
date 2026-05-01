package com.framepayments.frameonboarding.views

import android.content.Context
import androidx.compose.runtime.Composable
import com.framepayments.frameonboarding.classes.Capabilities
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.PhotoType
import com.framepayments.frameonboarding.classes.OnboardingData
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.frameonboarding.viewmodels.OnboardingField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
internal fun OnboardingScreenRouter(
    viewModel: FrameOnboardingViewModel,
    config: OnboardingConfig,
    savedPaymentMethods: List<PaymentMethodSummary>,
    savedPayoutMethods: List<PaymentMethodSummary>,
    onboardingData: OnboardingData,
    context: Context
) {
    when (viewModel.navigationState.currentStep) {
        OnboardingStep.VerificationWelcome -> {
            OnboardingIntroView(
                onContinue = { viewModel.moveNext() }
            )
        }

        OnboardingStep.VerifyIdentification -> {
            UserIdentificationView(
                viewModel = viewModel,
                requiresDateOfBirth = config.requiredCapabilities.contains(Capabilities.KYC_PREFILL),
                showTermsOfService = config.requiredCapabilities.contains(Capabilities.GEO_COMPLIANCE),
                onBack = { viewModel.moveBack() }
            )
        }

        OnboardingStep.GeolocationVerification -> {
            GeolocationVerificationScreen(
                accountId = onboardingData.resolvedAccountId,
                onContinue = { viewModel.moveNext() },
                onDisableVpn = { viewModel.moveNext() }
            )
        }

        OnboardingStep.SelectPaymentMethod -> {
            SelectPaymentMethodScreen(
                savedMethods = savedPaymentMethods,
                selectedId = onboardingData.selectedPaymentMethodId,
                onSelect = { viewModel.onPaymentMethodSelected(it) },
                onAddCard = { viewModel.moveNext() },
                onBack = { viewModel.moveBack() },
                onContinue = {
                    if (onboardingData.selectedPaymentMethodId != null) {
                        viewModel.moveNext()
                    }
                }
            )
        }

        OnboardingStep.AddPaymentMethod -> {
            AddPaymentMethodScreen(
                viewModel = viewModel,
                onBack = { viewModel.moveBack() }
            )
        }

        OnboardingStep.VerifyYourCard -> {
            VerifyCardScreen(
                showResendCode = true,
                onBack = { viewModel.moveBack() },
                onResendCode = { viewModel.resend3DS() },
                onContinue = { viewModel.moveNext() }
            )
        }

        OnboardingStep.SelectPayoutMethod -> {
            SelectPayoutMethodScreen(
                savedMethods = savedPayoutMethods,
                selectedId = onboardingData.selectedPayoutMethodId,
                onSelect = { viewModel.onPayoutMethodSelected(it) },
                onAddPayout = { viewModel.moveNext() },
                onBack = { viewModel.moveBack() },
                onContinue = { viewModel.moveNext() }
            )
        }

        OnboardingStep.AddPayoutMethod -> {
            AddPayoutMethodScreen(
                viewModel = viewModel,
                onBack = { viewModel.moveBack() }
            )
        }

        OnboardingStep.UploadDocumentsList -> {
            val fieldErrors by viewModel.fieldErrors.collectAsState()
            UploadDocumentsScreen(
                frontPhotoComplete = onboardingData.frontPhotoUri != null,
                backPhotoComplete = onboardingData.backPhotoUri != null,
                selfieComplete = onboardingData.selfieUri != null,
                frontError = fieldErrors[OnboardingField.DOC_FRONT],
                backError = fieldErrors[OnboardingField.DOC_BACK],
                selfieError = fieldErrors[OnboardingField.DOC_SELFIE],
                onBack = { viewModel.moveBack() },
                onFrontPhotoClick = { viewModel.navigationState.goTo(OnboardingStep.CaptureFrontPhoto) },
                onBackPhotoClick = { viewModel.navigationState.goTo(OnboardingStep.CaptureBackPhoto) },
                onSelfieClick = { viewModel.navigationState.goTo(OnboardingStep.CaptureSelfie) },
                onSubmit = {
                    if (viewModel.validateAllDocs()) {
                        viewModel.uploadIdentificationDocumentsThenContinue(context)
                    }
                }
            )
        }

        OnboardingStep.CaptureFrontPhoto -> {
            CameraCaptureScreen(
                photoType = PhotoType.FRONT,
                onClose = { viewModel.moveBack() },
                onPhotoCaptured = { uri ->
                    viewModel.onFrontPhotoSelected(uri)
                    viewModel.moveNext()
                }
            )
        }

        OnboardingStep.ReviewFrontPhoto -> {
            val photoUri = onboardingData.frontPhotoUri
            if (photoUri == null) {
                viewModel.navigationState.goTo(OnboardingStep.CaptureFrontPhoto)
                return
            }
            ReviewPhotoScreen(
                photoUri = photoUri,
                onBack = { viewModel.moveBack() },
                onUsePhoto = { viewModel.moveNext() },
                onRetake = {
                    viewModel.onFrontPhotoSelected(null)
                    viewModel.navigationState.goTo(OnboardingStep.CaptureFrontPhoto)
                }
            )
        }

        OnboardingStep.CaptureBackPhoto -> {
            CameraCaptureScreen(
                photoType = PhotoType.BACK,
                onClose = { viewModel.moveBack() },
                onPhotoCaptured = { uri ->
                    viewModel.onBackPhotoSelected(uri)
                    viewModel.moveNext()
                }
            )
        }

        OnboardingStep.ReviewBackPhoto -> {
            val photoUri = onboardingData.backPhotoUri
            if (photoUri == null) {
                viewModel.navigationState.goTo(OnboardingStep.CaptureBackPhoto)
                return
            }
            ReviewPhotoScreen(
                photoUri = photoUri,
                onBack = { viewModel.moveBack() },
                onUsePhoto = { viewModel.moveNext() },
                onRetake = {
                    viewModel.onBackPhotoSelected(null)
                    viewModel.navigationState.goTo(OnboardingStep.CaptureBackPhoto)
                }
            )
        }

        OnboardingStep.CaptureSelfie -> {
            CameraCaptureScreen(
                photoType = PhotoType.SELFIE,
                onClose = { viewModel.moveBack() },
                onPhotoCaptured = { uri ->
                    viewModel.onSelfieSelected(uri)
                    viewModel.moveNext()
                }
            )
        }

        OnboardingStep.ReviewSelfie -> {
            val photoUri = onboardingData.selfieUri
            if (photoUri == null) {
                viewModel.navigationState.goTo(OnboardingStep.CaptureSelfie)
                return
            }
            ReviewPhotoScreen(
                photoUri = photoUri,
                onBack = { viewModel.moveBack() },
                onUsePhoto = { viewModel.moveNext() },
                onRetake = {
                    viewModel.onSelfieSelected(null)
                    viewModel.navigationState.goTo(OnboardingStep.CaptureSelfie)
                }
            )
        }

        OnboardingStep.VerificationSubmitted -> {
            VerificationSubmittedScreen(onDone = { viewModel.moveNext() })
        }
    }
}
