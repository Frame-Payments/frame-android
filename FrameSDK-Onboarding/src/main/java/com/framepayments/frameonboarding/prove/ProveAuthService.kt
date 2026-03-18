package com.framepayments.frameonboarding.prove

import android.content.Context
import com.prove.sdk.proveauth.AuthFinishStep
import com.prove.sdk.proveauth.OtpFinishStep
import com.prove.sdk.proveauth.OtpFinishStepCallback
import com.prove.sdk.proveauth.OtpFinishInput
import com.prove.sdk.proveauth.OtpStartStep
import com.prove.sdk.proveauth.OtpStartStepCallback
import com.prove.sdk.proveauth.OtpStartInput
import com.prove.sdk.proveauth.ProveAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Standalone service that runs Prove mobile auth (phone + DOB) and returns user info.
 * Runs [ProveAuth.authenticate] on [Dispatchers.IO] (blocking); uses [ProveAuthBackend] for token and verify.
 *
 * @param context Application context (required by Prove SDK builder).
 * @param backend Backend that provides auth token and verify.
 * @param otpProvider Optional: when SDK falls back to OTP, this is called to get the code. Return null to cancel.
 */
class ProveAuthService(
    private val context: Context,
    private val backend: ProveAuthBackend,
    private val otpProvider: (() -> String?)? = null
) {
    /**
     * Runs Prove mobile flow with phone and DOB; returns user info on success.
     * Call from a coroutine (e.g. viewModelScope.launch).
     */
    suspend fun authenticate(phoneNumber: String, dateOfBirth: String): ProveUserInfo =
        withContext(Dispatchers.IO) {
            val authToken = backend.getAuthToken(
                phoneNumber = phoneNumber,
                dateOfBirth = dateOfBirth,
                flowType = "mobile"
            )

            val resultDeferred = kotlinx.coroutines.CompletableDeferred<Result<ProveUserInfo>>()

            val finishStep = AuthFinishStep { authId ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val info = backend.verify(authId)
                        resultDeferred.complete(Result.success(info))
                    } catch (e: Throwable) {
                        resultDeferred.complete(Result.failure(e))
                    }
                }
            }

            val otpStartStep = OtpStartStep { phoneNumberNeeded, _, callback ->
                if (phoneNumberNeeded) {
                    callback.onError()
                } else {
                    callback.onSuccess(OtpStartInput(""))
                }
            }

            val otpFinishStep = OtpFinishStep { _, callback ->
                val otp = otpProvider?.invoke()
                if (otp != null) {
                    callback.onSuccess(OtpFinishInput(otp))
                } else {
                    callback.onError()
                }
            }

            val proveAuth = ProveAuth.builder()
                .withContext(context)
                .withAuthFinishStep(finishStep)
                .withOtpFallback(otpStartStep, otpFinishStep)
                .build()

            try {
                proveAuth.authenticate(authToken)
            } catch (e: Throwable) {
                resultDeferred.complete(Result.failure(e))
            }

            resultDeferred.await().getOrThrow()
        }
}
