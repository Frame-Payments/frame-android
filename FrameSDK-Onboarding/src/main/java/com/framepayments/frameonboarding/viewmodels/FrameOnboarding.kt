package com.framepayments.frameonboarding.viewmodels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.framepayments.frameonboarding.classes.IdType
import com.framepayments.frameonboarding.classes.OnboardingConfig
import com.framepayments.frameonboarding.classes.OnboardingResult
import com.framepayments.frameonboarding.classes.OnboardingState
import com.framepayments.frameonboarding.classes.OnboardingStep
import com.framepayments.frameonboarding.classes.PaymentMethodSummary
import com.framepayments.frameonboarding.views.PlaceholderScreen
import com.framepayments.frameonboarding.views.UserIdentificationView
import com.framepayments.frameonboarding.views.VerifyIdFormScreen
import com.framepayments.frameonboarding.views.SelectPaymentMethodScreen

@Composable
fun FrameOnboarding(
    config: OnboardingConfig,
    onResult: (OnboardingResult) -> Unit
) {
    val state = remember { OnboardingState() }

    var selectedPaymentMethodId by remember { mutableStateOf<String?>(null) }
    var selectedIdType: IdType
    var selectedCountry: String

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
                    selectedCountry = country
                    selectedIdType = idType
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
                selectedId = selectedPaymentMethodId,
                onSelect = { selectedPaymentMethodId = it },
                onAddCard = { state.goTo(OnboardingStep.AddPaymentMethod) },
                onBack = { state.goTo(OnboardingStep.VerifyIdentification) },
                onContinue = { state.goTo(OnboardingStep.VerifyYourCard) }
            )
        }

        OnboardingStep.AddPaymentMethod -> {
            PlaceholderScreen(
                title = "Add New Payment Method",
                onBack = { state.goTo(OnboardingStep.SelectPaymentMethod) },
                onContinue = { state.goTo(OnboardingStep.VerifyYourCard) }
            )
        }

        OnboardingStep.VerifyYourCard -> {
            PlaceholderScreen(
                title = "Verify Your Card",
                onBack = { state.goTo(OnboardingStep.SelectPaymentMethod) },
                onContinue = { state.goTo(OnboardingStep.UploadDocuments) }
            )
        }

        OnboardingStep.UploadDocuments -> {
            PlaceholderScreen(
                title = "Upload your Documents",
                onBack = { state.goTo(OnboardingStep.SelectPaymentMethod) },
                onContinue = { state.goTo(OnboardingStep.UploadSelfie) }
            )
        }

        OnboardingStep.UploadSelfie -> {
            PlaceholderScreen(
                title = "Upload your ID",
                onBack = { state.goTo(OnboardingStep.UploadDocuments) },
                onContinue = {
                    // fake completion for now
                    onResult(
                        OnboardingResult.Completed(
                            paymentMethodId = "pm_fake",
                            onboardingSessionId = "kyc_fake"
                        )
                    )
                }
            )
        }
    }
}