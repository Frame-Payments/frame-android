package com.framepayments.framesdk.transfersv2

/**
 * A V2 transfer's `client_secret`, split into the pieces the API expects separately.
 *
 * Accepts `tr_<id>_secret_<token>` (preferred) and `ci_<id>_secret_<token>` during the bridge
 * while V2 payments may still mint ChargeIntent-shaped secrets.
 *
 * The confirm and retrieve calls need the bare resource id for the URL path and the full
 * secret for the request body.
 */
class TransferV2ClientSecret(
    /** The secret exactly as issued. A credential authorizing one confirmation: never log it. */
    val value: String
) {
    /** The bare transfer resource id, for the URL path. */
    val transferId: String

    init {
        val prefix = when {
            value.startsWith("tr_") -> "tr_"
            value.startsWith("ci_") -> "ci_"
            else -> throw FrameTransferV2Error.InvalidClientSecret()
        }

        val marker = value.indexOf("_secret_")
        // Both parts are required: an id with no "_secret_" marker, and a marker with nothing
        // after it, would otherwise pass and fail later at the API instead.
        if (marker <= prefix.length || marker + "_secret_".length >= value.length) {
            throw FrameTransferV2Error.InvalidClientSecret()
        }
        transferId = value.substring(0, marker).removePrefix(prefix)
    }
}

/** Errors raised while confirming a V2 transfer from the app. */
sealed class FrameTransferV2Error(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** The string is not a recognised transfer `client_secret`. */
    class InvalidClientSecret :
        FrameTransferV2Error("The string is not a transfer client_secret (tr_<id>_secret_... or ci_<id>_secret_...).")

    /** The transfer requires 3D Secure but carried no challenge session to present. */
    class MissingThreeDSecureChallenge :
        FrameTransferV2Error("The transfer requires 3D Secure but carried no challenge session to present.")

    /** The challenge never ran. Distinct from the cardholder failing one that did, and retryable. */
    class ThreeDSecureUnavailable(cause: Throwable? = null) :
        FrameTransferV2Error("The 3D Secure challenge could not be presented.", cause)

    /** The status could not be read after every attempt, so the transfer's state is unknown. */
    class StatusUnavailable(
        /** How many read attempts were made before giving up. */
        val attempts: Int,
        cause: Throwable? = null
    ) : FrameTransferV2Error("The transfer's status could not be read after $attempts attempts.", cause)
}

/**
 * The result of confirming a V2 transfer from the app.
 */
sealed class FrameTransferV2Outcome {
    /** Payment succeeded, or authorized and awaiting a merchant-initiated capture. */
    data class Succeeded(
        /** The transfer in its settled state. */
        val transfer: TransferV2
    ) : FrameTransferV2Outcome()

    /** A terminal payment failure. */
    data class Failed(
        /** The transfer in its failed state. */
        val transfer: TransferV2,
        val message: String?
    ) : FrameTransferV2Outcome()

    /** Every attempt returned a non-terminal status. The payment may still settle. */
    data object TimedOut : FrameTransferV2Outcome()

    /** Factory for deriving a terminal [FrameTransferV2Outcome] from a [TransferV2]. */
    companion object {
        /** The terminal outcome for [transfer]'s current payment/top-level status, or null while in flight. */
        fun terminalOutcome(transfer: TransferV2): FrameTransferV2Outcome? {
            when (transfer.payment?.status) {
                "succeeded", "requires_capture" -> return Succeeded(transfer)
                "failed", "canceled" -> return Failed(transfer, transfer.payment?.failureReason)
            }
            return when (transfer.status) {
                TransferV2Status.COMPLETED -> Succeeded(transfer)
                TransferV2Status.FAILED, TransferV2Status.CANCELED ->
                    Failed(transfer, transfer.payment?.failureReason)
                else -> null
            }
        }
    }
}
