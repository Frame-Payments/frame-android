package com.framepayments.framesdk.subscriptions
import com.google.gson.annotations.SerializedName

/**
 * Represents a recurring billing subscription for a customer.
 *
 * @property id Unique identifier for the subscription.
 * @property description Optional description of the subscription.
 * @property currentPeriodStart Unix timestamp marking the start of the current billing period.
 * @property currentPeriodEnd Unix timestamp marking the end of the current billing period.
 * @property livemode Whether the subscription exists in live mode (true) or test mode (false).
 * @property plan The billing plan associated with this subscription.
 * @property currency Three-letter ISO currency code used for billing.
 * @property status Current status of the subscription (e.g., "active", "cancelled", "past_due").
 * @property quantity Number of units of the plan included in this subscription.
 * @property customer ID of the customer this subscription belongs to.
 * @property defaultPaymentMethod ID of the payment method used for recurring charges.
 * @property subscriptionObject String identifier for the object type, always "subscription".
 * @property created Unix timestamp of when the subscription was created.
 * @property startDate Unix timestamp of when the subscription billing began.
 */
data class Subscription (
    val id: String?,
    val description: String?,
    @SerializedName("current_period_start") val currentPeriodStart: Int?,
    @SerializedName("current_period_end") val currentPeriodEnd: Int?,
    val livemode: Boolean?,
    val plan: SubscriptionPlan?,
    val currency: String?,
    val status: String?,
    val quantity: Int?,
    val customer: String?,
    @SerializedName("default_payment_method") val defaultPaymentMethod: String?,
    @SerializedName("object") val subscriptionObject: String?,
    val created: Int?,
    @SerializedName("start_date") val startDate: Int?
)

/**
 * Represents the billing plan attached to a subscription.
 *
 * @property id Unique identifier for the plan.
 * @property interval Billing interval for the plan (e.g., "month", "year").
 * @property product ID of the product this plan is associated with.
 * @property amount Billing amount per interval in the smallest currency unit (e.g., cents).
 * @property currency Three-letter ISO currency code for the plan amount.
 * @property planObject String identifier for the object type, always "plan".
 * @property active Whether the plan is currently available for new subscriptions.
 * @property created Unix timestamp of when the plan was created.
 * @property livemode Whether the plan exists in live mode (true) or test mode (false).
 */
data class SubscriptionPlan (
    val id: String?,
    val interval: String?,
    val product: String?,
    val amount: Int?,
    val currency: String?,
    @SerializedName("object") val planObject: String?,
    val active: Boolean?,
    val created: Int?,
    val livemode: Boolean?
)
