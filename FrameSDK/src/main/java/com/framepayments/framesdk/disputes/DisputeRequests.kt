package com.framepayments.framesdk.disputes

import com.google.gson.annotations.SerializedName

object DisputeRequests {
    data class UpdateDisputeRequest(
        @SerializedName("shipping_carrier") val shippingCarrier: String? = null,
        @SerializedName("shipping_date") val shippingDate: String? = null,
        @SerializedName("shipping_tracking_number") val shippingTrackingNumber: String? = null,
        @SerializedName("customer_purchase_ip_address") val customerPurchaseIpAddress: String? = null,
        @SerializedName("support_description") val supportDescription: String? = null,
        @SerializedName("refund_refusal_explanation") val refundRefusalExplanation: String? = null,
        @SerializedName("refund_policy") val refundPolicy: String? = null,
        @SerializedName("access_activity_log") val accessActivityLog: String? = null
    )
}
