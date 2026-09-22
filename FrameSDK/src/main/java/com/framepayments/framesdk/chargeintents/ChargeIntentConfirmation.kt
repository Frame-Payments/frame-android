package com.framepayments.framesdk.chargeintents

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Presents a 3D Secure challenge and reports how it ended. The result is a UI lifecycle signal, not a payment verdict. */
enum class FrameThreeDSecureChallengeResult {
    /** The cardholder finished the challenge. Says nothing about whether the charge succeeded. */
    COMPLETED,

    /** The cardholder failed or abandoned it. The charge may still have settled. */
    FAILED,

    /** The challenge could not be loaded, so it never ran. */
    UNAVAILABLE
}

/** Presents a required 3D Secure challenge and suspends until the cardholder is done with it. */
interface FrameThreeDSecureChallengePresenting {
    /** Presents the challenge and returns once the cardholder is done with it. */
    suspend fun presentChallenge(challenge: UseFrameSDK, intent: ChargeIntent): FrameThreeDSecureChallengeResult
}

/**
 * Confirms a charge intent from the app, driving a 3D Secure challenge when the API asks for one.
 *
 * Mirrors the browser SDK's `confirmCardPayment`: confirm, present a challenge if required,
 * then poll for the verdict. Polling timings are part of the contract — see [PollingConfiguration].
 */
class ChargeIntentConfirmation(
    private val challengePresenter: FrameThreeDSecureChallengePresenting?,
    private val polling: PollingConfiguration = PollingConfiguration(),
    private val confirmIntent: suspend (intentId: String, clientSecret: String) -> ChargeIntent? = { id, secret ->
        ChargeIntentAPI.confirmChargeIntent(id, secret).first
    },
    private val loadIntent: suspend (intentId: String, clientSecret: String) -> ChargeIntent? = { id, secret ->
        ChargeIntentAPI.getChargeIntent(id, secret).first
    },
    private val sleep: suspend (Long) -> Unit = { delay(it) }
) {
    /**
     * How the confirmation polls for a terminal status. Defaults match the browser SDK.
     *
     * @property maxAttempts How many times to read the status before giving up. Clamped to at least one read.
     * @property intervalMillis How long to wait between reads, and before the first one.
     */
    data class PollingConfiguration(val maxAttempts: Int = 10, val intervalMillis: Long = 1000) {
        init { require(maxAttempts >= 1) }
    }

    /**
     * Confirms a charge intent, completing a 3D Secure challenge if the API requires one.
     *
     * @param clientSecret The intent's `client_secret` (`ci_<id>_secret_…`).
     * @return The terminal outcome, or [FrameChargeIntentOutcome.TimedOut].
     * @throws FrameChargeIntentError
     */
    suspend fun confirm(clientSecret: String): FrameChargeIntentOutcome {
        val secret = ChargeIntentClientSecret(clientSecret)

        val intent = confirmIntent(secret.chargeIntentId, secret.value)
            ?: return pollForTerminalOutcome(secret)

        FrameChargeIntentOutcome.terminalOutcome(intent)?.let { return it }

        if (intent.status == ChargeIntentStatus.REQUIRES_THREE_D_SECURE) {
            val challenge = intent.nextAction?.useFrameSDK ?: throw FrameChargeIntentError.MissingThreeDSecureChallenge()
            val presenter = challengePresenter ?: throw FrameChargeIntentError.ThreeDSecureUnavailable()

            if (presenter.presentChallenge(challenge, intent) == FrameThreeDSecureChallengeResult.UNAVAILABLE) {
                throw FrameChargeIntentError.ThreeDSecureUnavailable()
            }
        }

        return pollForTerminalOutcome(secret)
    }

    private suspend fun pollForTerminalOutcome(secret: ChargeIntentClientSecret): FrameChargeIntentOutcome {
        sleep(polling.intervalMillis)

        for (attempt in 1..polling.maxAttempts) {
            try {
                val intent = loadIntent(secret.chargeIntentId, secret.value)
                if (intent != null) {
                    FrameChargeIntentOutcome.terminalOutcome(intent)?.let { return it }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt == polling.maxAttempts) {
                    throw FrameChargeIntentError.StatusUnavailable(polling.maxAttempts, e)
                }
            }

            sleep(polling.intervalMillis)
        }

        return FrameChargeIntentOutcome.TimedOut
    }
}
