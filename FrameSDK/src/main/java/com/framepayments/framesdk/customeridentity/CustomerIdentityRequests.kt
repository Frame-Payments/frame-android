package com.framepayments.framesdk.customeridentity

import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

object CustomerIdentityRequests {
    data class CreateCustomerIdentityRequest(
        @SerializedName("billing_address") val address: FrameObjects.BillingAddress,
        @SerializedName("first_name") val firstName: String,
        @SerializedName("last_name") val lastName: String,
        @SerializedName("date_of_birth") val dateOfBirth: String,
        @SerializedName("phone_number") val phoneNumber: String,
        val email: String,
        val ssn: String
    )
}