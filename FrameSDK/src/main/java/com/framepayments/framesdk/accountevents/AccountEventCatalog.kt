package com.framepayments.framesdk.accountevents

/**
 * The screen/flow an account event happened on. Values must match the shared cross-SDK
 * naming contract in `docs/ACCOUNT_EVENTS.md` (FRA-6548) — do not rename a value's wire
 * string without updating that catalog too.
 */
enum class AccountEventScreen(val apiValue: String) {
    APPLE_PAY("ApplePay"),
    CHECKOUT("Checkout"),
    COMPLIANCE("Compliance"),
    IDENTITY_VERIFICATION("IdentityVerification"),
    ONBOARDING("Onboarding"),
    PAYMENT_METHOD("PaymentMethod"),
    PAYMENT_SHEET("PaymentSheet"),
    PAYOUT_METHOD("PayoutMethod"),
    PERSONAL_INFORMATION("PersonalInformation"),
    PHONE_VERIFICATION("PhoneVerification"),
    TERMS_OF_SERVICE("TermsOfService")
}

/**
 * The wire `name` for an account event. Values must match the shared cross-SDK naming
 * contract in `docs/ACCOUNT_EVENTS.md` (FRA-6548) — do not rename a value's wire string
 * without updating that catalog too.
 */
enum class AccountEventName(val apiValue: String) {
    ONBOARDING_STARTED("onboarding_started"),
    ONBOARDING_STEP_VIEWED("onboarding_step_viewed"),
    ONBOARDING_STEP_COMPLETED("onboarding_step_completed"),
    ONBOARDING_COMPLETED("onboarding_completed"),
    ONBOARDING_DECLINED("onboarding_declined"),
    ONBOARDING_NEEDS_REVIEW("onboarding_needs_review"),
    ONBOARDING_ACTION_REQUIRED("onboarding_action_required"),
    ONBOARDING_CANCELLED("onboarding_cancelled"),
    ONBOARDING_BLOCKED("onboarding_blocked"),
    ONBOARDING_SESSION_START_FAILED("onboarding_session_start_failed"),

    PHONE_VERIFICATION_STARTED("phone_verification_started"),
    PHONE_CODE_SENT("phone_code_sent"),
    PHONE_CODE_SEND_FAILED("phone_code_send_failed"),
    PHONE_CODE_ENTRY_STARTED("phone_code_entry_started"),
    PHONE_VERIFIED("phone_verified"),
    PHONE_CODE_INCORRECT("phone_code_incorrect"),
    PHONE_CODE_ENTRY_CANCELLED("phone_code_entry_cancelled"),
    SILENT_PHONE_AUTH_STARTED("silent_phone_auth_started"),
    SILENT_PHONE_AUTH_COMPLETED("silent_phone_auth_completed"),
    SILENT_PHONE_AUTH_FALLBACK("silent_phone_auth_fallback"),
    SILENT_PHONE_AUTH_FAILED("silent_phone_auth_failed"),

    PROFILE_STEP_STARTED("profile_step_started"),
    PROFILE_UPDATED("profile_updated"),
    PROFILE_UPDATE_FAILED("profile_update_failed"),
    PROFILE_VALIDATION_FAILED("profile_validation_failed"),

    STEP_UP_STARTED("step_up_started"),
    STEP_UP_COMPLETED("step_up_completed"),
    STEP_UP_ALREADY_VERIFIED("step_up_already_verified"),
    STEP_UP_FAILED("step_up_failed"),
    STEP_UP_NEEDS_REVIEW("step_up_needs_review"),
    STEP_UP_DATA_MISMATCH("step_up_data_mismatch"),
    STEP_UP_ESCALATED("step_up_escalated"),
    STEP_UP_DECLINED("step_up_declined"),
    STEP_UP_UNAVAILABLE("step_up_unavailable"),
    STEP_UP_CANCELLED("step_up_cancelled"),

    PAYMENT_METHOD_STEP_STARTED("payment_method_step_started"),
    SAVED_PAYMENT_METHOD_SELECTED("saved_payment_method_selected"),
    ADD_PAYMENT_METHOD_STARTED("add_payment_method_started"),
    PAYMENT_METHOD_ADDED("payment_method_added"),
    PAYMENT_METHOD_ADD_FAILED("payment_method_add_failed"),
    CARD_VALIDATION_FAILED("card_validation_failed"),
    BILLING_ADDRESS_UPDATED("billing_address_updated"),
    BILLING_ADDRESS_UPDATE_FAILED("billing_address_update_failed"),
    SAVED_PAYMENT_METHODS_LOAD_FAILED("saved_payment_methods_load_failed"),

    PAYOUT_METHOD_STEP_STARTED("payout_method_step_started"),
    SAVED_PAYOUT_METHOD_SELECTED("saved_payout_method_selected"),
    ADD_PAYOUT_METHOD_STARTED("add_payout_method_started"),
    PAYOUT_METHOD_ADDED("payout_method_added"),
    PAYOUT_METHOD_ADD_FAILED("payout_method_add_failed"),
    BANK_LINK_STARTED("bank_link_started"),
    BANK_LINK_COMPLETED("bank_link_completed"),
    BANK_LINK_CANCELLED("bank_link_cancelled"),
    BANK_LINK_FAILED("bank_link_failed"),
    PAYOUT_METHOD_ELECTED("payout_method_elected"),
    PAYOUT_METHOD_ELECTION_FAILED("payout_method_election_failed"),

    COMPLIANCE_CHECK_STARTED("compliance_check_started"),
    COMPLIANCE_CHECK_PASSED("compliance_check_passed"),
    COMPLIANCE_CHECK_FAILED("compliance_check_failed"),
    COMPLIANCE_CHECK_VPN_DETECTED("compliance_check_vpn_detected"),
    COMPLIANCE_CHECK_VPN_BYPASSED("compliance_check_vpn_bypassed"),

    TERMS_OF_SERVICE_SHOWN("terms_of_service_shown"),
    TERMS_OF_SERVICE_ACCEPTED("terms_of_service_accepted"),
    TERMS_OF_SERVICE_TOKEN_FAILED("terms_of_service_token_failed"),

    CHECKOUT_STARTED("checkout_started"),
    CHECKOUT_PAYMENT_METHOD_SELECTED("checkout_payment_method_selected"),
    CHECKOUT_VALIDATION_FAILED("checkout_validation_failed"),
    CHECKOUT_PAYMENT_STARTED("checkout_payment_started"),
    CARD_TOKENIZED("card_tokenized"),
    CARD_TOKENIZATION_FAILED("card_tokenization_failed"),
    CHECKOUT_PAYMENT_SUCCEEDED("checkout_payment_succeeded"),
    CHECKOUT_PAYMENT_DECLINED("checkout_payment_declined"),
    CHECKOUT_PAYMENT_FAILED("checkout_payment_failed"),
    CHECKOUT_CANCELLED("checkout_cancelled"),
    STEP_UP_CHALLENGE_STARTED("step_up_challenge_started"),
    STEP_UP_CHALLENGE_COMPLETED("step_up_challenge_completed"),
    STEP_UP_CHALLENGE_ABANDONED("step_up_challenge_abandoned"),
    STEP_UP_CHALLENGE_UNAVAILABLE("step_up_challenge_unavailable"),
    CHARGE_INTENT_CONFIRMATION_POLLING_EXHAUSTED("charge_intent_confirmation_polling_exhausted"),

    APPLE_PAY_STARTED("apple_pay_started"),
    APPLE_PAY_UNAVAILABLE("apple_pay_unavailable"),
    APPLE_PAY_AUTHORIZED("apple_pay_authorized"),
    APPLE_PAY_FAILED("apple_pay_failed"),
    APPLE_PAY_CANCELLED("apple_pay_cancelled"),
    APPLE_PAY_CARD_ADDED("apple_pay_card_added"),
    APPLE_PAY_ASSERTION_REJECTED("apple_pay_assertion_rejected"),

    ATTESTATION_STARTED("attestation_started"),
    ATTESTATION_COMPLETED("attestation_completed"),
    ATTESTATION_NOT_SUPPORTED("attestation_not_supported"),
    ATTESTATION_FAILED("attestation_failed"),
    ATTESTATION_RESET_AND_RETRY("attestation_reset_and_retry"),
    ATTESTATION_ASSERTION_RETRIED("attestation_assertion_retried"),

    FRAUD_SESSION_STARTED("fraud_session_started"),
    FRAUD_SESSION_REFRESHED("fraud_session_refreshed"),
    FRAUD_SESSION_RECREATED("fraud_session_recreated"),
    SONAR_SESSION_FAILED("sonar_session_failed"),
    FRAUD_SESSION_ADOPTED("fraud_session_adopted")
}

/** Fixed developer-facing `detail` strings reused across account events. Dynamic details stay inline at the call site. */
object AccountEventDetail {
    const val APPLE_PAY_ASSERTION_REJECTED = "attestation-linked failure, triggers an attestation reset"
    const val APPLE_PAY_ADD_TO_OWNER_MODE = "mode: add-to-owner (onboarding wallet-card save, no charge)"
    const val APPLE_PAY_SHEET_DISMISSED_NO_RESULT = "sheet dismissed with no result"
    const val CHECKOUT_PAY_BUTTON_TAPPED = "pay button tapped"
    const val CHECKOUT_SAVED_PAYMENT_METHOD = "saved"
    const val CHECKOUT_NEW_PAYMENT_METHOD = "new"
    const val FRAUD_SESSION_ADOPTED_FROM_ANONYMOUS = "pre-account anonymous session migrated to account-scoped"
    const val FRAUD_SESSION_REFRESH_FELL_BACK_TO_RECREATE = "refresh failed, fell back to creating fresh — self-healing, not a hard failure"
    const val ATTESTATION_NOT_SUPPORTED_REASON = "simulator or unsupported OS version"
    const val ATTESTATION_ONE_TIME_PER_DEVICE = "one-time per device"
    const val ATTESTATION_ASSERTION_RETRIED_CONTEXT = "per-payment assertion, distinct from the one-time attestation above"
    const val STEP_UP_CHALLENGE_IS_3DS = "3DS"
    const val STEP_UP_CHALLENGE_NEVER_LOADED = "challenge page never loaded"
    const val STEP_UP_CHALLENGE_COMPLETED_CONTEXT = "cardholder finished the UI — not itself a verdict"
    const val STEP_UP_CHALLENGE_CARDHOLDER_DISMISSED = "cardholder cancelled/dismissed"
    const val PROVE_PROVIDER = "provider: prove"
    const val PERSONA_PROVIDER = "provider: persona"
    const val PLAID_PROVIDER = "provider: plaid"
    const val PLAID_USER_DISMISSED = "user dismissed Plaid"
    const val BILLING_ADDRESS_ONLY_VERIFICATION_PATH = "address-only verification path"
    const val PAYOUT_METHOD_SET_AS_PRIMARY = "set as primary"
    const val PAYOUT_METHOD_MANUAL_ACH_PATH = "manual/ACH path"
    const val PAYOUT_METHOD_ADD_STARTED_MANUAL_OR_PLAID = "manual or plaid"
    const val ONBOARDING_BLOCKED_NOTHING_ACTIONABLE = "capability outstanding, nothing actionable"
    const val STEP_UP_ALREADY_VERIFIED_SHORT_CIRCUIT = "pre-check short-circuit, Persona never launched"
    const val STEP_UP_CANCELLED_BY_USER = "user closed the verification UI"
    const val STEP_UP_CATEGORY_TERMINAL = "category: terminal"
    const val STEP_UP_CATEGORY_REVIEW = "category: review"
    const val STEP_UP_CATEGORY_RETRIABLE_WITH_NEW_DATA = "category: retriable_with_new_data"
    const val STEP_UP_CATEGORY_STEP_UP_ESCALATED = "category: step_up (e.g. SSN path failed, now needs gov ID)"
    const val STEP_UP_CATEGORY_TRANSIENT_PROVIDER_ERROR = "category: transient / provider_error"
    const val ONBOARDING_COMPLETED_APPROVED = "approved"
}
