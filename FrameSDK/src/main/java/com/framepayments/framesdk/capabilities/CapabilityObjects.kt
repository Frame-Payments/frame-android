package com.framepayments.framesdk.capabilities

import com.google.gson.annotations.SerializedName

/** Namespace for data models representing account capability resources. */
object CapabilityObjects {

    /**
     * Describes a single outstanding requirement that must be satisfied before a capability can be enabled.
     *
     * @property id Unique identifier for this requirement.
     * @property object The object type string returned by the API (e.g. `"capability_requirement"`).
     * @property type The category of the requirement (e.g. `"document"`, `"verification"`).
     * @property status Current fulfillment status of the requirement.
     * @property source Optional identifier indicating which system or integration raised the requirement.
     */
    data class CapabilityRequirement(
        val id: String?,
        val `object`: String?,
        val type: String?,
        val status: String?,
        val source: String? = null
    )

    /**
     * Represents a single capability associated with a merchant account.
     *
     * @property id Unique identifier for the capability.
     * @property object The object type string returned by the API (e.g. `"capability"`).
     * @property name The name identifier of the capability (e.g. `"card_payments"`).
     * @property accountId The ID of the merchant account that owns this capability.
     * @property status Current status of the capability (e.g. `"active"`, `"inactive"`, `"pending"`).
     * @property disabledReason Human-readable explanation of why the capability is disabled, if applicable.
     * @property currentlyDue List of requirement keys that must be addressed to activate or maintain the capability.
     * @property created ISO 8601 timestamp when the capability was created.
     * @property updated ISO 8601 timestamp when the capability was last updated.
     * @property disabled Whether the capability has been explicitly disabled.
     */
    data class Capability(
        val id: String?,
        val `object`: String?,
        val name: String?,
        @SerializedName("account_id") val accountId: String?,
        val status: String?,
        @SerializedName("disabled_reason") val disabledReason: String? = null,
        @SerializedName("currently_due") val currentlyDue: List<String>? = null,
        val created: String?,
        val updated: String?,
        val disabled: Boolean? = null
    )
}
