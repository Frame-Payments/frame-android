package com.framepayments.framesdk.subscriptions
import com.google.gson.annotations.SerializedName

data class Subscription (
    val id: String,
    val description: String,
    @SerializedName("current_period_start") val currentPeriodStart: Int,
    @SerializedName("current_period_end") val currentPeriodEnd: Int,
    val livemode: Boolean,
    val plan: SubscriptionPlan?,
    val currency: String,
    val status: String,
    val quantity: Int,
    val customer: String?,
    @SerializedName("default_payment_method") val defaultPaymentMethod: String,
    @SerializedName("object") val subscriptionObject: String,
    val created: Int,
    @SerializedName("start_date") val startDate: Int
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