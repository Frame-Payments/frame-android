package com.framepayments.framesdk.subscriptions
import com.google.gson.annotations.SerializedName

data class Subscription (
    val id: String,
    val description: String,
    val currentPeriodStart: Int,
    val currentPeriodEnd: Int,
    val livemode: Boolean,
    val plan: SubscriptionPlan?,
    val currency: String,
    val status: String,
    val quantity: Int,
    val customer: String?,
    val defaultPaymentMethod: String,
    @SerializedName("object") val subscriptionObject: String,
    val created: Int,
    val startDate: Int
)

data class SubscriptionPlan (
    val id: String,
    val interval: String,
    val product: String,
    val amount: Int,
    val currency: String,
    @SerializedName("object") val planObject: String,
    val active: Boolean,
    val created: Int,
    val livemode: Boolean
)