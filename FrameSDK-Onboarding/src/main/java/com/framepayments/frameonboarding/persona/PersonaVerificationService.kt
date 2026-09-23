package com.framepayments.frameonboarding.persona

import androidx.activity.result.ActivityResultLauncher
import com.withpersona.sdk2.inquiry.Inquiry
import com.withpersona.sdk2.inquiry.InquiryResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Outcome of a Persona government-ID inquiry, as reported by the Persona client SDK.
 *
 * ⚠️ These callback outcomes are **best-effort** and are not authoritative. The Frame server's
 * `/idv/complete` response is the source of truth for whether the customer is actually verified;
 * treat [Completed] here only as a signal to call `/idv/complete`, never as proof of verification.
 */
sealed class PersonaVerificationResult {
    /** The inquiry flow finished. [inquiryId]/[status] come from Persona; confirm via `/idv/complete`. */
    data class Completed(
        /** The Persona inquiry id the flow ran against. */
        val inquiryId: String,
        /** Persona's client-side status string (e.g. "completed"). Advisory only. */
        val status: String?
    ) : PersonaVerificationResult()

    /** The customer abandoned the Persona flow without finishing. */
    data object Cancelled : PersonaVerificationResult()

    /** The Persona SDK reported an error. */
    data class Failure(
        /** Persona's debug message, when available. */
        val message: String?
    ) : PersonaVerificationResult()
}

/**
 * A pre-created Persona inquiry ready to launch, as returned by `POST /idv/session`.
 *
 * @property inquiryId The Persona inquiry id (`inq_…`).
 * @property sessionToken Present only when the server resumed an existing inquiry.
 */
data class PersonaInquiry(
    val inquiryId: String,
    val sessionToken: String?
)

/**
 * Reusable service that launches the Persona mobile SDK against a **pre-created** inquiry id and
 * exposes the outcome as state. Mirrors [com.framepayments.frameonboarding.plaid.PlaidLinkService]
 * (owns a [StateFlow] result; callers react to it) and wraps the SDK's callback in a
 * [CompletableDeferred] like [com.framepayments.frameonboarding.prove.ProveAuthService] so callers
 * get a suspend API.
 *
 * The Persona template and environment live server-side — this launches
 * `Inquiry.fromInquiry(inquiryId)`, never a template.
 *
 * Registration lifecycle: the [ActivityResultLauncher] must be created by the host
 * Activity/Composable via `registerForActivityResult(Inquiry.Contract())` and passed into
 * [awaitResult] (the composable creates the launcher; the view model calls [awaitResult] with it) so
 * it is bound to the lifecycle and survives configuration change. This service does not register the
 * callback itself.
 */
class PersonaVerificationService {

    private val _result = MutableStateFlow<PersonaVerificationResult?>(null)
    /** Latest inquiry outcome, or null before the flow completes. */
    val result: StateFlow<PersonaVerificationResult?> = _result.asStateFlow()

    // Bridges the lifecycle-owned ActivityResult callback back to the suspend caller. Held while a
    // single inquiry is in flight; completed exactly once by [onInquiryResult].
    private var pending: CompletableDeferred<PersonaVerificationResult>? = null

    /**
     * Builds the SDK [Inquiry] for [inquiryId] and launches it through [launcher]. The returned
     * [CompletableDeferred] completes when the host forwards the SDK callback to [onInquiryResult].
     *
     * @param inquiryId Pre-created Persona inquiry id (`inq_…`) from `POST /idv/session`.
     * @param sessionToken Session token from `POST /idv/session`, required to reopen a resumed inquiry.
     * @param launcher A launcher registered via `registerForActivityResult(Inquiry.Contract())`.
     */
    fun launch(
        inquiryId: String,
        sessionToken: String?,
        launcher: ActivityResultLauncher<Inquiry>
    ): CompletableDeferred<PersonaVerificationResult> {
        val deferred = CompletableDeferred<PersonaVerificationResult>()
        pending = deferred
        _result.value = null

        val builder = Inquiry.fromInquiry(inquiryId)
        sessionToken?.let { builder.sessionToken(it) }
        launcher.launch(builder.build())
        return deferred
    }

    /**
     * Launches the inquiry and suspends until the host forwards the SDK callback.
     *
     * @param inquiryId Pre-created Persona inquiry id (`inq_…`).
     * @param sessionToken Session token from `POST /idv/session`, required to reopen a resumed inquiry.
     * @param launcher A launcher registered via `registerForActivityResult(Inquiry.Contract())`.
     */
    suspend fun awaitResult(
        inquiryId: String,
        sessionToken: String?,
        launcher: ActivityResultLauncher<Inquiry>
    ): PersonaVerificationResult = launch(inquiryId, sessionToken, launcher).await()

    /**
     * Maps a Persona [InquiryResponse] to a [PersonaVerificationResult], stores it in [result], and
     * resolves the in-flight [CompletableDeferred] from [launch]. The host wires this into its
     * `registerForActivityResult(Inquiry.Contract()) { onInquiryResult(it) }` callback.
     */
    fun onInquiryResult(response: InquiryResponse) {
        val mapped = when (response) {
            is InquiryResponse.Complete -> PersonaVerificationResult.Completed(
                inquiryId = response.inquiryId,
                status = response.status
            )
            is InquiryResponse.Cancel -> PersonaVerificationResult.Cancelled
            is InquiryResponse.Error -> PersonaVerificationResult.Failure(response.debugMessage)
        }
        _result.value = mapped
        pending?.complete(mapped)
        pending = null
    }

    /** Clears the stored [result] so the UI can reset after handling an outcome. */
    fun clearResult() {
        _result.value = null
    }
}
