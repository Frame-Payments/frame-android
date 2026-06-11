package com.framepayments.framesdk.customeridentity

import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

/**
 * Contains request model types used by customer identity API operations.
 */
object CustomerIdentityRequests {

    /**
     * The request body required to create a new customer identity verification record.
     *
     * All fields are required by the API; none may be omitted.
     *
     * @property address The customer's billing address.
     * @property firstName The customer's first name.
     * @property lastName The customer's last name.
     * @property dateOfBirth The customer's date of birth in `YYYY-MM-DD` format.
     * @property phoneNumber The customer's phone number.
     * @property email The customer's email address.
     * @property ssn The customer's Social Security Number.
     */
    data class CreateCustomerIdentityRequest(
        val address: FrameObjects.BillingAddress,
        @SerializedName("first_name") val firstName: String,
        @SerializedName("last_name") val lastName: String,
        @SerializedName("date_of_birth") val dateOfBirth: String,
        @SerializedName("phone_number") val phoneNumber: String,
        val email: String,
        val ssn: String
    )
}
