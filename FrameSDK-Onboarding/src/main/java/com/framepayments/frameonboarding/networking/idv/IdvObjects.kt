package com.framepayments.frameonboarding.networking.idv

import com.google.gson.annotations.SerializedName

/**
 * Response from `POST /idv/session`.
 *
 * @property inquiryId The Persona inquiry id (`inq_…`) to launch the mobile SDK against.
 * @property sessionToken Present only when the server resumed an existing inquiry; Persona will
 *   not reopen a resumed inquiry without it.
 */
data class IdvSessionResponse(
    @SerializedName("inquiry_id") val inquiryId: String?,
    @SerializedName("session_token") val sessionToken: String? = null
)

/**
 * Response from `POST /idv/complete` (JSON variant, FRA-5363).
 *
 * @property verified Server-authoritative verification result. Null when the server has not yet
 *   returned the JSON variant (e.g. an older server replying with a non-JSON body), which callers
 *   must treat as "verification pending" rather than verified.
 * @property status The KYC run's own status (e.g. `declined`, `needs_review`), used when
 *   [category] is absent.
 * @property failureType Provider-neutral failure type. Not currently surfaced in copy.
 * @property category Remediation category (`terminal`, `review`, `retriable_with_new_data`,
 *   `step_up`, `transient`); decides the failure message and event when present.
 * @property retriable Whether retrying as-is can succeed. Not currently surfaced in copy.
 */
data class IdvCompleteResponse(
    @SerializedName("verified") val verified: Boolean?,
    @SerializedName("status") val status: String? = null,
    @SerializedName("failure_type") val failureType: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("retriable") val retriable: Boolean? = null
)
