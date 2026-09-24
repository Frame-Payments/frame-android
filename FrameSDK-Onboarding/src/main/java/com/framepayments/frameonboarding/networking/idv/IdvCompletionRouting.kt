package com.framepayments.frameonboarding.networking.idv

import com.framepayments.framesdk.accountevents.AccountEventDetail
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen

/**
 * Maps `POST /idv/complete` category/status onto catalog events and applicant-facing copy.
 * Mirrors Frame-iOS `OnboardingContainerViewModel.emitStepUpFailure` / `idvFailureMessage`.
 */
internal object IdvCompletionRouting {

    fun emitStepUpFailure(completion: IdvCompleteResponse?) {
        when (completion?.category) {
            "terminal" -> AccountEventEmitter.emit(
                AccountEventName.STEP_UP_DECLINED, AccountEventScreen.IDENTITY_VERIFICATION,
                detail = AccountEventDetail.STEP_UP_CATEGORY_TERMINAL
            )
            "review" -> AccountEventEmitter.emit(
                AccountEventName.STEP_UP_NEEDS_REVIEW, AccountEventScreen.IDENTITY_VERIFICATION,
                detail = AccountEventDetail.STEP_UP_CATEGORY_REVIEW
            )
            "retriable_with_new_data" -> AccountEventEmitter.emit(
                AccountEventName.STEP_UP_DATA_MISMATCH, AccountEventScreen.IDENTITY_VERIFICATION,
                detail = AccountEventDetail.STEP_UP_CATEGORY_RETRIABLE_WITH_NEW_DATA
            )
            "step_up" -> AccountEventEmitter.emit(
                AccountEventName.STEP_UP_ESCALATED, AccountEventScreen.IDENTITY_VERIFICATION,
                detail = AccountEventDetail.STEP_UP_CATEGORY_STEP_UP_ESCALATED
            )
            "transient" -> AccountEventEmitter.emit(
                AccountEventName.STEP_UP_UNAVAILABLE, AccountEventScreen.IDENTITY_VERIFICATION,
                detail = AccountEventDetail.STEP_UP_CATEGORY_TRANSIENT_PROVIDER_ERROR
            )
            else -> when (completion?.status) {
                "declined", "failed" -> AccountEventEmitter.emit(
                    AccountEventName.STEP_UP_DECLINED, AccountEventScreen.IDENTITY_VERIFICATION,
                    detail = AccountEventDetail.STEP_UP_CATEGORY_TERMINAL
                )
                "needs_review" -> AccountEventEmitter.emit(
                    AccountEventName.STEP_UP_NEEDS_REVIEW, AccountEventScreen.IDENTITY_VERIFICATION,
                    detail = AccountEventDetail.STEP_UP_CATEGORY_REVIEW
                )
                else -> AccountEventEmitter.emit(
                    AccountEventName.STEP_UP_FAILED, AccountEventScreen.IDENTITY_VERIFICATION,
                    detail = "generic bucket — status: ${completion?.status ?: "unknown"}"
                )
            }
        }
    }

    fun idvFailureMessage(completion: IdvCompleteResponse?, ssnAllowed: Boolean = true): String {
        when (completion?.category) {
            "terminal" -> return "We couldn't verify your identity. Please contact support if you think this is a mistake."
            "review" -> return "Your verification is in review. We'll be in touch once it's complete."
            "retriable_with_new_data" -> return "Some of your details didn't match. Please check them and try again."
            "step_up" -> return "We need a government ID to finish verifying your identity."
            "transient" -> return "We couldn't complete the check just now. Please try again."
            else -> Unit
        }
        return when (completion?.status) {
            "declined", "failed" -> "We couldn't verify your identity. Please contact support if you think this is a mistake."
            "needs_review" -> "Your verification is in review. We'll be in touch once it's complete."
            else -> genericFailureMessage(ssnAllowed)
        }
    }

    fun genericFailureMessage(ssnAllowed: Boolean): String =
        if (ssnAllowed) {
            "We couldn't verify your identity. Please try again or enter your Social Security Number."
        } else {
            "We couldn't verify your identity. Please try again."
        }
}
