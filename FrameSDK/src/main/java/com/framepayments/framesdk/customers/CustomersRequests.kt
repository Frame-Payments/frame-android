package com.framepayments.framesdk.customers
import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

object CustomersRequests {
    data class CreateCustomerRequest(
        @SerializedName("billing_address") val billingAddress: FrameObjects.BillingAddress?,
        @SerializedName("shipping_address") val shippingAddress: FrameObjects.BillingAddress?,
        val name: String?,
        val phone: String?,
        val email: String?,
        val description: String?,
        val metadata: Map<String, String>?
    )

    data class UpdateCustomerRequest(
        @SerializedName("billing_address") val billingAddress: FrameObjects.BillingAddress?,
        @SerializedName("shipping_address") val shippingAddress: FrameObjects.BillingAddress?,
        val name: String?,
        val phone: String?,
        val email: String?,
        val description: String?,
        val metadata: Map<String, String>?
    )

    data class SearchCustomersRequest(
        val name: String?,
        val phone: String?,
        val email: String?,
        @SerializedName("created_before") val createdBefore: Int?,
        @SerializedName("created_after") val createdAfter: Int?
    )
}