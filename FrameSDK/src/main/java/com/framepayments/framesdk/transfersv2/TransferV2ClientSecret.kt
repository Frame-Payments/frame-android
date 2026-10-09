package com.framepayments.framesdk.transfersv2

/** Errors raised while confirming a V2 transfer from the app. */
sealed class FrameTransferV2Error(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** Confirm was asked to run without a transfer id. */
    class MissingTransfer :
        FrameTransferV2Error("Confirm was asked to run without a transfer id.")

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
        /** Cardholder-safe failure text, or null when the API gave none. */
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
