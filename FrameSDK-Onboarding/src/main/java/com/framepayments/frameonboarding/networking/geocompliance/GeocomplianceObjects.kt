package com.framepayments.frameonboarding.networking.geocompliance

import com.google.gson.annotations.SerializedName

enum class GeofenceType {
    @SerializedName("polygon") POLYGON,
    @SerializedName("circle") CIRCLE
}

enum class GeofenceRuleTrigger {
    @SerializedName("enter") ENTER,
    @SerializedName("exit") EXIT,
    @SerializedName("dwell") DWELL
}

enum class GeofenceRuleActionType {
    @SerializedName("block_transaction") BLOCK_TRANSACTION
}

data class GeofenceRule(
    @SerializedName("trigger_on") val triggerOn: GeofenceRuleTrigger,
    @SerializedName("action_type") val actionType: GeofenceRuleActionType
)

data class Geofence(
    val id: String,
    @SerializedName("object") val geofenceObject: String,
    val name: String,
    @SerializedName("geofence_type") val geofenceType: GeofenceType,
    val active: Boolean,
    @SerializedName("geofence_rules") val geofenceRules: List<GeofenceRule>
)

