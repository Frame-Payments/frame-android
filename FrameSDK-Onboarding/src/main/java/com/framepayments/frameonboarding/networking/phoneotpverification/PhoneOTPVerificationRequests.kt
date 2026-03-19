package com.framepayments.frameonboarding.networking.phoneotpverification

import com.google.gson.annotations.SerializedName

object PhoneOTPVerificationRequests {
    /**
     * Create phone verification request. dateOfBirth must be YYYY-MM-DD.
     */
    data class Create(
        val type: String = "phone",
        @SerializedName("phone_number") val phoneNumber: String,
        @SerializedName("date_of_birth") val dateOfBirth: String
    )
}
