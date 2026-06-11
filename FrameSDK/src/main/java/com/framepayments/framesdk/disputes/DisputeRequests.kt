package com.framepayments.framesdk.disputes

import com.google.gson.annotations.SerializedName

/**
 * Contains request model classes used when submitting dispute-related API calls.
 */
object DisputeRequests {

    /**
     * Encapsulates the evidence fields a merchant can supply when updating a dispute.
     *
     * @property shippingCarrier Name of the carrier used to ship the product to the customer.
     * @property shippingDate Date on which the product was shipped to the customer.
     * @property shippingTrackingNumber Tracking number for the shipment delivered to the customer.
     * @property customerPurchaseIpAddress IP address of the customer at the time of purchase.
     * @property supportDescription Description of support interactions with the customer relevant to the dispute.
     * @property refundRefusalExplanation Explanation of why a refund was not issued to the customer.
     * @property refundPolicy The merchant's refund policy as provided to the customer.
     * @property accessActivityLog Log of customer access activity relevant to the dispute.
     */
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
