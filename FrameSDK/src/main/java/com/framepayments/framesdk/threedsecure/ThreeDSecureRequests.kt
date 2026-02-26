package com.framepayments.framesdk.threedsecure

import com.google.gson.annotations.SerializedName

object ThreeDSecureRequests {
    data class CreateThreeDSecureVerification(
        @SerializedName("payment_method_id") val paymentMethodId: String
    )
}
