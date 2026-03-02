package com.framepayments.frameonboarding.prove

/**
 * Typed errors for [ProveAuthService] for use by the UI layer.
 */
sealed class ProveAuthServiceError : Exception() {
    /** Backend failed to return an auth token (e.g. network or invalid response). */
    data class AuthTokenFailed(val underlying: Throwable) : ProveAuthServiceError()

    /** Backend verify failed (e.g. validation failed or network error). */
    data class VerifyFailed(val underlying: Throwable) : ProveAuthServiceError()

    /** User cancelled or OTP provider returned null. */
    data object Cancelled : ProveAuthServiceError()

    /** Prove SDK reported an error. */
    data class SdkError(val underlying: Throwable) : ProveAuthServiceError()

    /** Unknown or unexpected error. */
    data class Unknown(val underlying: Throwable) : ProveAuthServiceError()

    override val message: String?
        get() = when (this) {
            is AuthTokenFailed -> "Failed to get auth token: ${underlying.message}"
            is VerifyFailed -> "Verification failed: ${underlying.message}"
            is Cancelled -> "Authentication was cancelled."
            is SdkError -> "Prove SDK error: ${underlying.message}"
            is Unknown -> underlying.message
        }
}
