package com.framepayments.framesdk.transfersv2

import com.framepayments.framesdk.chargeintents.AuthorizationMode
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.ChargeIntentStatus
import com.framepayments.framesdk.chargeintents.FrameThreeDSecureChallengePresenting
import com.framepayments.framesdk.chargeintents.FrameThreeDSecureChallengeResult
import com.framepayments.framesdk.chargeintents.NextAction
import com.framepayments.framesdk.chargeintents.UseFrameSDK
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Confirms a V2 transfer from the app, driving a 3D Secure challenge when the API asks for one.
 *
 * Mirrors [com.framepayments.framesdk.chargeintents.ChargeIntentConfirmation] / Frame.js
 * `confirmTransfer`. Polling timings are part of the contract — see [PollingConfiguration].
 */
class TransferV2Confirmation(
    private val challengePresenter: FrameThreeDSecureChallengePresenting?,
    private val polling: PollingConfiguration = PollingConfiguration(),
    private val confirmTransfer: suspend (transferId: String, clientSecret: String) -> TransferV2? = { id, secret ->
        TransfersV2API.confirmTransfer(id, secret).first
    },
    private val loadTransfer: suspend (transferId: String, clientSecret: String) -> TransferV2? = { id, secret ->
        TransfersV2API.getTransferWith(id, secret).first
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
     * Confirms a V2 transfer, completing a 3D Secure challenge if the API requires one.
     *
     * @param clientSecret The transfer's `client_secret` (`tr_<id>_secret_…` or bridge `ci_…`).
     * @return The terminal outcome, or [FrameTransferV2Outcome.TimedOut].
     * @throws FrameTransferV2Error
     */
    suspend fun confirm(clientSecret: String): FrameTransferV2Outcome {
        val secret = TransferV2ClientSecret(clientSecret)

        val transfer = confirmTransfer(secret.transferId, secret.value)
            ?: return pollForTerminalOutcome(secret)

        FrameTransferV2Outcome.terminalOutcome(transfer)?.let { return it }

        val paymentNeeds3DS = transfer.payment?.status == "requires_3d_secure"
            || transfer.payment?.status == "requires_action"
        if (paymentNeeds3DS) {
            presentChallengeIfNeeded(transfer)
        }

        return pollForTerminalOutcome(secret)
    }

    private suspend fun presentChallengeIfNeeded(transfer: TransferV2) {
        val presenter = challengePresenter
            ?: throw FrameTransferV2Error.ThreeDSecureUnavailable()

        val useFrameSDK = transfer.nextAction?.useFrameSDK
        if (useFrameSDK != null) {
            if (presenter.presentChallenge(useFrameSDK, chargeIntentShell(transfer, useFrameSDK)) ==
                FrameThreeDSecureChallengeResult.UNAVAILABLE
            ) {
                throw FrameTransferV2Error.ThreeDSecureUnavailable()
            }
            return
        }

        val redirect = transfer.nextAction?.redirectUrl
        if (!redirect.isNullOrEmpty()) {
            val synthetic = UseFrameSDK(source = "redirect", challengeUrl = redirect)
            if (presenter.presentChallenge(synthetic, chargeIntentShell(transfer, synthetic)) ==
                FrameThreeDSecureChallengeResult.UNAVAILABLE
            ) {
                throw FrameTransferV2Error.ThreeDSecureUnavailable()
            }
            return
        }

        throw FrameTransferV2Error.MissingThreeDSecureChallenge()
    }

    private fun chargeIntentShell(transfer: TransferV2, challenge: UseFrameSDK): ChargeIntent {
        return ChargeIntent(
            id = transfer.id,
            currency = transfer.amount?.currency ?: "usd",
            customer = null,
            shipping = null,
            status = ChargeIntentStatus.REQUIRES_THREE_D_SECURE,
            description = null,
            amount = transfer.amount?.value,
            created = transfer.created,
            updated = null,
            livemode = transfer.livemode,
            latestCharge = null,
            paymentMethod = null,
            authorizationMode = AuthorizationMode.AUTOMATIC,
            failureDescription = null,
            intentObject = "transfer",
            nextAction = NextAction(type = "use_frame_sdk", useFrameSDK = challenge)
        )
    }

    private suspend fun pollForTerminalOutcome(secret: TransferV2ClientSecret): FrameTransferV2Outcome {
        sleep(polling.intervalMillis)

        for (attempt in 1..polling.maxAttempts) {
            try {
                val transfer = loadTransfer(secret.transferId, secret.value)
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
