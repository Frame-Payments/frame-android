package com.framepayments.frameonboarding.networking.geocompliance

import com.google.gson.annotations.SerializedName

/** Shape type of a geographic compliance fence. */
enum class GeofenceType {
    /** A polygonal fence defined by a set of lat/lng vertices. */
    @SerializedName("polygon") POLYGON,
    /** A circular fence defined by a center point and radius. */
    @SerializedName("circle") CIRCLE
}

/** The device location event that fires a geofence rule. */
enum class GeofenceRuleTrigger {
    /** Fired when the device enters the fence. */
    @SerializedName("enter") ENTER,
    /** Fired when the device exits the fence. */
    @SerializedName("exit") EXIT,
    /** Fired when the device dwells inside the fence. */
    @SerializedName("dwell") DWELL
}

/** The action taken when a [GeofenceRule] is triggered. */
enum class GeofenceRuleActionType {
    /** Block the transaction when the rule fires. */
    @SerializedName("block_transaction") BLOCK_TRANSACTION
}

/**
 * A single rule that pairs a trigger event with a resulting action for a geofence.
 *
 * @property triggerOn The device location event that fires the rule.
 * @property actionType The action applied when the rule fires.
 */
data class GeofenceRule(
    @SerializedName("trigger_on") val triggerOn: GeofenceRuleTrigger?,
    @SerializedName("action_type") val actionType: GeofenceRuleActionType?
)

/**
 * A geographic compliance fence configured on the Frame platform.
 *
 * @property id Unique identifier for this geofence.
 * @property geofenceObject The API object type string (always `"geofence"`).
 * @property name Human-readable name for the fence.
 * @property geofenceType Whether the fence is a [GeofenceType.POLYGON] or [GeofenceType.CIRCLE].
 * @property active Whether the fence is currently enforced.
 * @property geofenceRules Rules that fire when device location intersects this fence.
 */
data class Geofence(
    val id: String?,
    @SerializedName("object") val geofenceObject: String?,
    val name: String?,
    @SerializedName("geofence_type") val geofenceType: GeofenceType?,
    val active: Boolean?,
    @SerializedName("geofence_rules") val geofenceRules: List<GeofenceRule>?
)
