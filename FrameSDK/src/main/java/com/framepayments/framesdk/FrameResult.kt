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
    data class Completed(val id: String) : FrameResult()
    data object Cancelled : FrameResult()
    data class Failed(val error: Throwable) : FrameResult()
}

class FrameConfigurationError(message: String) : RuntimeException(message)
