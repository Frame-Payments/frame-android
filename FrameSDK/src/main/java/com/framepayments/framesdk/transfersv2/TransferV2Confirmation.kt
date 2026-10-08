package com.framepayments.framesdk.transfersv2

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Result of presenting a 3D Secure challenge. */
enum class FrameThreeDSecureChallengeResult {
    /** The cardholder finished the challenge. The transfer status is read afterward. */
    COMPLETED,
    /** The cardholder failed or dismissed the challenge. */
    FAILED,
    /** The challenge could not be shown. */
    UNAVAILABLE
}

/** Presents a 3D Secure challenge. Implemented by the UI layer. */
interface FrameThreeDSecureChallengePresenting {
    /**
     * Shows [challenge] and returns how it ended.
     */
    suspend fun presentChallenge(challenge: UseFrameSDK): FrameThreeDSecureChallengeResult
}

/**
 * Confirms a V2 transfer from the app, driving a 3D Secure challenge when the API asks for one.
 *
 * Polling timings are part of the contract — see [PollingConfiguration].
 */
class TransferV2Confirmation(
    private val checkoutClientSecret: String? = null,
    private val challengePresenter: FrameThreeDSecureChallengePresenting?,
    private val polling: PollingConfiguration = PollingConfiguration(),
    private val confirmTransfer: suspend (transferId: String) -> TransferV2? = { id ->
        val token = checkoutClientSecret
        if (!token.isNullOrEmpty()) TransfersV2API.confirmTransfer(id, token).first
        else TransfersV2API.confirmTransfer(id).first
    },
    private val loadTransfer: suspend (transferId: String) -> TransferV2? = { id ->
        val token = checkoutClientSecret
        if (!token.isNullOrEmpty()) TransfersV2API.getTransferWith(id, token).first
        else TransfersV2API.getTransferWith(id).first
    },
    private val sleep: suspend (Long) -> Unit = { delay(it) }
) {
    /**
     * How the confirmation polls for a terminal payment status. Defaults match the browser SDK.
     *
     * @property maxAttempts How many times to read the status before giving up. Clamped to at least one read.
     * @property intervalMillis How long to wait between reads, and before the first one.
     */
    data class PollingConfiguration(val maxAttempts: Int = 10, val intervalMillis: Long = 1000) {
        init { require(maxAttempts >= 1) }
    }

    /**
     * Confirms [transferId], completing a 3D Secure challenge if the API requires one.
     *
     * @return The terminal outcome, or [FrameTransferV2Outcome.TimedOut].
     * @throws FrameTransferV2Error
     */
    suspend fun confirm(transferId: String): FrameTransferV2Outcome {
        if (transferId.isEmpty()) throw FrameTransferV2Error.MissingTransfer()

        val transfer = confirmTransfer(transferId) ?: return pollForTerminalOutcome(transferId)

        FrameTransferV2Outcome.terminalOutcome(transfer)?.let { return it }

        val paymentNeeds3DS = transfer.payment?.status == "requires_3d_secure"
            || transfer.payment?.status == "requires_action"
        if (paymentNeeds3DS) {
            presentChallengeIfNeeded(transfer)
        }

        return pollForTerminalOutcome(transferId)
    }

    private suspend fun presentChallengeIfNeeded(transfer: TransferV2) {
        val presenter = challengePresenter
            ?: throw FrameTransferV2Error.ThreeDSecureUnavailable()

        val useFrameSDK = transfer.nextAction?.useFrameSDK
        if (useFrameSDK != null) {
            if (presenter.presentChallenge(useFrameSDK) == FrameThreeDSecureChallengeResult.UNAVAILABLE) {
                throw FrameTransferV2Error.ThreeDSecureUnavailable()
            }
            return
        }

        val redirect = transfer.nextAction?.redirectUrl
        if (!redirect.isNullOrEmpty()) {
            val synthetic = UseFrameSDK(source = "redirect", challengeUrl = redirect)
            if (presenter.presentChallenge(synthetic) == FrameThreeDSecureChallengeResult.UNAVAILABLE) {
                throw FrameTransferV2Error.ThreeDSecureUnavailable()
            }
            return
        }

        throw FrameTransferV2Error.MissingThreeDSecureChallenge()
    }

    private suspend fun pollForTerminalOutcome(transferId: String): FrameTransferV2Outcome {
        sleep(polling.intervalMillis)

        for (attempt in 1..polling.maxAttempts) {
            try {
                val transfer = loadTransfer(transferId)
                if (transfer != null) {
                    FrameTransferV2Outcome.terminalOutcome(transfer)?.let { return it }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt == polling.maxAttempts) {
                    throw FrameTransferV2Error.StatusUnavailable(polling.maxAttempts, e)
                }
            }

            sleep(polling.intervalMillis)
        }

        return FrameTransferV2Outcome.TimedOut
    }
}
