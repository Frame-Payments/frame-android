package com.framepayments.frameonboarding.networking.phoneotpverification

import com.google.gson.annotations.SerializedName

/** Request body types for the phone OTP verification endpoints. */
object PhoneOTPVerificationRequests {
    /**
     * Create phone verification request. `dateOfBirth` must be in `YYYY-MM-DD` format.
     *
     * @property type Verification type; always `"phone"`.
     * @property phoneNumber Customer phone number in E.164 format.
     * @property dateOfBirth Customer date of birth in YYYY-MM-DD format.
     */
    data class Create(
        val type: String = "phone",
        @SerializedName("phone_number") val phoneNumber: String,
        @SerializedName("date_of_birth") val dateOfBirth: String
    )

    /**
     * Confirm OTP code (Twilio path). Omit `code` for the Prove path (sends an empty body).
     *
     * @property code The OTP code entered by the customer.
     */
    data class Confirm(
        val code: String?
    )
}
