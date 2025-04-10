package com.framepayments.framesdk.customers

import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.paymentmethods.PaymentMethod
import com.google.gson.annotations.SerializedName

data class Customer(
    val id: String,
    val created: Int,
    val updated: Int?,
    val livemode: Boolean,
    val name: String,
    val phone: String?,
    val email: String?,
    val description: String?,
    @SerializedName("object") val customerObject: String?,
    @SerializedName("shipping_address") val shippingAddress: FrameObjects.BillingAddress?,
    @SerializedName("billing_address") val billingAddress: FrameObjects.BillingAddress?,
    @SerializedName("payment_methods") val paymentMethods: List<PaymentMethod>?
)