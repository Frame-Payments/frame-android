package com.framepayments.framesdk.threedsecure

import com.google.gson.annotations.SerializedName

/**
 * Contains request payload models for the 3D Secure Verifications API.
 */
object ThreeDSecureRequests {

    /**
     * Request payload for creating a new 3D Secure verification intent.
     *
     * @property paymentMethodId Identifier of the payment method to run 3D Secure verification against.
     */
    data class CreateThreeDSecureVerification(
        @SerializedName("payment_method_id") val paymentMethodId: String
    )
}
