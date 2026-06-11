package com.framepayments.framesdk

/**
 * Outcome of a Frame UI flow (checkout, cart, onboarding).
 *
 * - [Completed.id] carries the resource id produced by the flow:
 *   `FrameCheckoutView` / `FrameCartView` → Transfer id.
 *   `OnboardingContainerView` → the selected PaymentMethod id, or empty string if the flow
 *   completed without one.
 * - [Cancelled] is emitted when the user dismisses the surface without completing.
 * - [Failed] is emitted for terminal errors that can't be retried in-flow.
 */
sealed class FrameResult {
    /** Emitted when the user completes the flow. [id] is the resource produced by the flow. */
    data class Completed(val id: String) : FrameResult()

    /** Emitted when the user dismisses the surface without completing. */
    data object Cancelled : FrameResult()

    /**
     * Emitted for terminal errors that cannot be retried in-flow.
     *
     * @property error The underlying exception that caused the failure.
     */
    data class Failed(val error: Throwable) : FrameResult()
}

/**
 * Thrown when [FrameNetworking.initializeWithAPIKey] has not been called before a Frame
 * SDK component is used, or when required configuration values are missing or invalid.
 *
 * @param message Description of the configuration problem.
 */
class FrameConfigurationError(message: String) : RuntimeException(message)
