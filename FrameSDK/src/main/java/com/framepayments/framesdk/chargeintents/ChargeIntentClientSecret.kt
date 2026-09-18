package com.framepayments.framesdk.chargeintents

/**
 * A charge intent's `client_secret`, split into the pieces the API expects separately.
 *
 * The confirm and retrieve calls need the bare resource id for the URL path and the full
 * secret for the request body. The `ci_` prefix belongs to the secret, not to the charge
 * intent, and a URL built from the unstripped string 404s.
 */
class ChargeIntentClientSecret(
    /** The secret exactly as issued. A credential authorizing one confirmation: never log it. */
    val value: String
) {
    /** The bare charge-intent resource id, for the URL path. */
    val chargeIntentId: String

    init {
        if (!value.startsWith("ci_")) {
            throw FrameChargeIntentError.InvalidClientSecret()
        }

        val marker = value.indexOf("_secret_")
        val withoutSecret = if (marker > "ci_".length) value.substring(0, marker) else value
        val id = withoutSecret.removePrefix("ci_")
        if (id.isEmpty()) {
            throw FrameChargeIntentError.InvalidClientSecret()
        }
        chargeIntentId = id
    }
}

/** Errors raised while confirming a charge intent from the app. */
sealed class FrameChargeIntentError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** The string is not a charge-intent `client_secret` (`ci_<id>_secret_...`). */
    class InvalidClientSecret : FrameChargeIntentError("The string is not a charge-intent client_secret (ci_<id>_secret_...).")

    /** The intent requires 3D Secure but carried no challenge session to present. */
    class MissingThreeDSecureChallenge : FrameChargeIntentError("The intent requires 3D Secure but carried no challenge session to present.")

    /** The challenge never ran. Distinct from the cardholder failing one that did, and retryable. */
    class ThreeDSecureUnavailable(cause: Throwable? = null) :
        FrameChargeIntentError("The 3D Secure challenge could not be presented.", cause)

    /** The status could not be read after every attempt, so the charge's state is unknown. */
    class StatusUnavailable(
        /** How many read attempts were made before giving up. */
        val attempts: Int,
        cause: Throwable? = null
    ) : FrameChargeIntentError("The charge intent's status could not be read after $attempts attempts.", cause)
}

/**
 * Why a charge failed, as reported by the API's `latest_charge`.
 *
 * @property code A machine-readable failure code (e.g. `"card_declined"`).
 * @property message A human-readable message safe to show the cardholder.
 */
data class FrameChargeFailureReason(val code: String?, val message: String?) {
    /** Factory for deriving a [FrameChargeFailureReason] from a failed [ChargeIntent]. */
    companion object {
        /** Returns null for a terminal failure that carried no reason at all, which the API does send. */
        fun from(intent: ChargeIntent): FrameChargeFailureReason? {
            val code = intent.latestCharge?.failureCode
            val message = intent.latestCharge?.failureMessage ?: intent.failureDescription
            if (code == null && message == null) return null
            return FrameChargeFailureReason(code, message)
        }
    }
}

/**
 * The result of confirming a charge intent from the app.
 */
sealed class FrameChargeIntentOutcome {
    /** Captured, or authorized and awaiting a merchant-initiated capture. */
    data class Succeeded(
        /** The intent in its settled state. */
        val intent: ChargeIntent
    ) : FrameChargeIntentOutcome()

    /** A terminal failure. [reason] is absent when the API gave no `latest_charge`. */
    data class Failed(
        /** The intent in its failed state. */
        val intent: ChargeIntent,
        val reason: FrameChargeFailureReason?
    ) : FrameChargeIntentOutcome()

    /** Every attempt returned a non-terminal status. The charge may still settle. */
    data object TimedOut : FrameChargeIntentOutcome()

    /** Factory for deriving a terminal [FrameChargeIntentOutcome] from a [ChargeIntent]'s status. */
    companion object {
        /** The terminal outcome for [intent]'s current status, or null while it is still in flight. */
        fun terminalOutcome(intent: ChargeIntent): FrameChargeIntentOutcome? = when (intent.status) {
            ChargeIntentStatus.SUCCEEDED, ChargeIntentStatus.REQUIRES_CAPTURE -> Succeeded(intent)
            ChargeIntentStatus.FAILED -> Failed(intent, FrameChargeFailureReason.from(intent))
            else -> null
        }
    }
}
