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
    onUpdateData: (OnboardingData) -> Unit,
    onResult: (OnboardingResult) -> Unit
) {
    when (state.currentStep) {
        OnboardingStep.VerificationWelcome -> {
            UserIdentificationView(
                onContinue = { state.goTo(OnboardingStep.VerifyIdentification) }
            )
        }

        OnboardingStep.VerifyIdentification -> {
            VerifyIdFormScreen(
                onBack = { state.goTo(OnboardingStep.VerificationWelcome) },
                onContinue = { country, idType ->
                    onUpdateData(onboardingData.copy(
                        issuingCountry = country,
                        idType = idType
                    ))
                    state.goTo(OnboardingStep.SelectPaymentMethod)
                }
            )
        }

        OnboardingStep.SelectPaymentMethod -> {
            SelectPaymentMethodScreen(
                savedMethods = listOf(
                    PaymentMethodSummary(id = "pm_1", brand = "VISA", last4 = "2872", exp = "08/29"),
                    PaymentMethodSummary(id = "pm_2", brand = "MASTERCARD", last4 = "3292", exp = "10/27"),
                ),
                selectedId = onboardingData.selectedPaymentMethodId,
                onSelect = { 
                    onUpdateData(onboardingData.copy(selectedPaymentMethodId = it))
                },
                onAddCard = { 
                    // When adding a new card, go directly to add screen
                    // After adding, user will be taken to verify card screen
                    state.goTo(OnboardingStep.AddPaymentMethod)
                },
                onBack = { state.goTo(OnboardingStep.VerifyIdentification) },
                onContinue = { 
                    // Continue if a method is selected or a new one was added
                    if (onboardingData.selectedPaymentMethodId != null || onboardingData.newPaymentMethod != null) {
                        state.goTo(OnboardingStep.VerifyYourCard)
                    }
                }
            )
        }

        OnboardingStep.AddPaymentMethod -> {
            AddPaymentMethodScreen(
                onBack = { state.goTo(OnboardingStep.SelectPaymentMethod) },
                onContinue = { paymentDetails ->
                    onUpdateData(onboardingData.copy(newPaymentMethod = paymentDetails))
                    state.goTo(OnboardingStep.VerifyYourCard)
                }
            )
        }

        OnboardingStep.VerifyYourCard -> {
            VerifyCardScreen(
                phoneLast4 = "3432", // In real implementation, get from onboardingData
                onBack = { state.goTo(OnboardingStep.SelectPaymentMethod) },
                onContinue = { code ->
                    onUpdateData(onboardingData.copy(cardVerificationCode = code))
                    state.goTo(OnboardingStep.SelectPayoutMethod)
                }
            )
        }

        OnboardingStep.SelectPayoutMethod -> {
            SelectPayoutMethodScreen(
                onBack = { state.goTo(OnboardingStep.VerifyYourCard) },
                onContinue = { state.goTo(OnboardingStep.AddPayoutMethod) }
            )
        }

        OnboardingStep.AddPayoutMethod -> {
            AddPayoutMethodScreen(
                onBack = { state.goTo(OnboardingStep.SelectPayoutMethod) },
                onContinue = { payoutDetails ->
                    onUpdateData(onboardingData.copy(newPayoutMethod = payoutDetails))
                    state.goTo(OnboardingStep.UploadDocumentsList)
                }
            )
        }

        OnboardingStep.UploadDocumentsList -> {
            UploadDocumentsScreen(
                frontPhotoComplete = onboardingData.frontPhotoUri != null,
                backPhotoComplete = onboardingData.backPhotoUri != null,
                selfieComplete = onboardingData.selfieUri != null,
                onBack = { state.goTo(OnboardingStep.AddPayoutMethod) },
                onFrontPhotoClick = { state.goTo(OnboardingStep.CaptureFrontPhoto) },
                onBackPhotoClick = { state.goTo(OnboardingStep.CaptureBackPhoto) },
                onSelfieClick = { state.goTo(OnboardingStep.CaptureSelfie) },
                onSubmit = {
                    // All photos are complete, proceed to geolocation
                    state.goTo(OnboardingStep.GeolocationVerification)
                }
            )
        }

        OnboardingStep.CaptureFrontPhoto -> {
            CameraCaptureScreen(
                photoType = PhotoType.FRONT,
                onClose = { state.goTo(OnboardingStep.UploadDocumentsList) },
                onPhotoCaptured = { uri ->
                    onUpdateData(onboardingData.copy(frontPhotoUri = uri))
                    state.goTo(OnboardingStep.ReviewFrontPhoto)
                }
            )
        }

        OnboardingStep.ReviewFrontPhoto -> {
            val photoUri = onboardingData.frontPhotoUri
            if (photoUri == null) {
                // If no photo, go back to capture
                state.goTo(OnboardingStep.CaptureFrontPhoto)
                return
            }
            ReviewPhotoScreen(
                photoUri = photoUri,
                onBack = { state.goTo(OnboardingStep.CaptureFrontPhoto) },
                onUsePhoto = { state.goTo(OnboardingStep.CaptureBackPhoto) },
                onRetake = { 
                    onUpdateData(onboardingData.copy(frontPhotoUri = null))
                    state.goTo(OnboardingStep.CaptureFrontPhoto) 
                }
            )
        }

        OnboardingStep.CaptureBackPhoto -> {
            CameraCaptureScreen(
                photoType = PhotoType.BACK,
                onClose = { state.goTo(OnboardingStep.UploadDocumentsList) },
                onPhotoCaptured = { uri ->
                    onUpdateData(onboardingData.copy(backPhotoUri = uri))
                    state.goTo(OnboardingStep.ReviewBackPhoto)
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
                onBack = { state.goTo(OnboardingStep.CaptureBackPhoto) },
                onUsePhoto = { state.goTo(OnboardingStep.CaptureSelfie) },
                onRetake = { 
                    onUpdateData(onboardingData.copy(backPhotoUri = null))
                    state.goTo(OnboardingStep.CaptureBackPhoto) 
                }
            )
        }

        OnboardingStep.CaptureSelfie -> {
            CameraCaptureScreen(
                photoType = PhotoType.SELFIE,
                onClose = { state.goTo(OnboardingStep.UploadDocumentsList) },
                onPhotoCaptured = { uri ->
                    onUpdateData(onboardingData.copy(selfieUri = uri))
                    state.goTo(OnboardingStep.ReviewSelfie)
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
                onBack = { state.goTo(OnboardingStep.CaptureSelfie) },
                onUsePhoto = { state.goTo(OnboardingStep.GeolocationVerification) },
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
                    onResult(
                        OnboardingResult.Completed(
                            paymentMethodId = onboardingData.selectedPaymentMethodId,
                            onboardingSessionId = "kyc_session"
                        )
                    )
                },
                onDisableVpn = {
                    // In real implementation, guide user to disable VPN
                    onUpdateData(onboardingData.copy(vpnDetected = true))
                }
            )
        }
    }
}