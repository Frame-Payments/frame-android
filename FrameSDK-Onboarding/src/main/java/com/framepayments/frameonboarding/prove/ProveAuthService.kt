package com.framepayments.frameonboarding.prove

import android.content.Context
import com.prove.sdk.proveauth.AuthFinishStep
import com.prove.sdk.proveauth.OtpFinishStep
import com.prove.sdk.proveauth.OtpFinishInput
import com.prove.sdk.proveauth.OtpStartStep
import com.prove.sdk.proveauth.OtpStartInput
import com.prove.sdk.proveauth.ProveAuth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Optional suspend closure the host provides to supply OTP when the SDK falls back to OTP. Return null to cancel.
typealias ProveOtpProvider = suspend () -> String?

// Closure invoked when Prove auth completes. Receives accountId and verificationId from the service.
typealias ProveConfirmHandler = suspend (String, String) -> Unit

// User information returned after successful Prove authentication and backend verify.
data class ProveUserInfo(
    val firstName: String,
    val lastName: String
)

// Typed errors for ProveAuthService for use by the UI layer.
sealed class ProveAuthServiceError : Exception() {
    data class VerifyFailed(val underlying: Throwable) : ProveAuthServiceError()
    data object Cancelled : ProveAuthServiceError()
    data class SdkError(val underlying: Throwable) : ProveAuthServiceError()
    data class Unknown(val underlying: Throwable) : ProveAuthServiceError()

    override val message: String?
        get() = when (this) {
            is VerifyFailed -> "Verification failed: ${underlying.message}"
            is Cancelled -> "Authentication was cancelled."
            is SdkError -> "Prove SDK error: ${underlying.message}"
            is Unknown -> underlying.message
        }
}

/**
 * Standalone service that runs Prove mobile auth and calls [confirmHandler] on success.
 * Pass the auth token from your backend's createVerification call to [authenticateWith].
 *
 * @param context Application context (required by Prove SDK builder).
 * @param accountId Frame account ID passed through to [confirmHandler].
 * @param verificationId Verification ID from createVerification, passed through to [confirmHandler].
 * @param confirmHandler Called on auth success with (accountId, verificationId) to confirm the verification.
 * @param otpProvider Optional: when SDK falls back to OTP, called to get the code. Return null to cancel.
 */
class ProveAuthService(
    private val context: Context,
    private val accountId: String,
    private val verificationId: String,
    private val confirmHandler: ProveConfirmHandler,
    private val otpProvider: ProveOtpProvider? = null
) {
    private var proveAuth: ProveAuth? = null
    private var authFinishStep: AuthFinishStep? = null
    private var otpStartStep: OtpStartStep? = null
    private var otpFinishStep: OtpFinishStep? = null

    /**
     * Runs Prove mobile flow with the auth token from createVerification. Returns true on success
     * after [confirmHandler] completes. Call from a coroutine (e.g. viewModelScope.launch).
     */
    suspend fun authenticateWith(authToken: String): Boolean = withContext(Dispatchers.IO) {
        val resultDeferred = CompletableDeferred<Result<Boolean>>()

        authFinishStep = AuthFinishStep { _ ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    confirmHandler(accountId, verificationId)
                    resultDeferred.complete(Result.success(true))
                } catch (e: Throwable) {
                    resultDeferred.complete(Result.failure(e))
                } finally {
                    releaseRetainedSDKObjects()
                }
            }
        }

        otpStartStep = OtpStartStep { phoneNumberNeeded, _, callback ->
            if (phoneNumberNeeded) {
                callback.onError()
            } else {
                callback.onSuccess(OtpStartInput(""))
            }
        }

        otpFinishStep = OtpFinishStep { _, callback ->
            CoroutineScope(Dispatchers.Main).launch {
                val otp = otpProvider?.invoke()
                if (otp != null) {
                    callback.onSuccess(OtpFinishInput(otp))
                } else {
                    callback.onError()
                }
            }
        }

        proveAuth = ProveAuth.builder()
            .withContext(context)
            .withAuthFinishStep(authFinishStep!!)
            .withOtpFallback(otpStartStep!!, otpFinishStep!!)
            .build()

        try {
            proveAuth!!.authenticate(authToken)
        } catch (e: Throwable) {
            releaseRetainedSDKObjects()
            resultDeferred.complete(Result.failure(e))
        }

        resultDeferred.await().getOrThrow()
    }

    private fun releaseRetainedSDKObjects() {
        proveAuth = null
        authFinishStep = null
        otpStartStep = null
        otpFinishStep = null
    }
}
