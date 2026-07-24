package com.framepayments.frameonboarding.networking.idv

import com.google.gson.annotations.SerializedName

/**
 * Response from `POST /idv/session`.
 *
 * @property inquiryId The Persona inquiry id (`inq_…`) to launch the mobile SDK against.
 */
data class IdvSessionResponse(
    @SerializedName("inquiry_id") val inquiryId: String?
)

/**
 * Response from `POST /idv/complete` (JSON variant, FRA-5363).
 *
 * @property verified Server-authoritative verification result. Null when the server has not yet
 *   returned the JSON variant (e.g. an older server replying with a non-JSON body), which callers
 *   must treat as "verification pending" rather than verified.
 */
data class IdvCompleteResponse(
    @SerializedName("verified") val verified: Boolean?
)
