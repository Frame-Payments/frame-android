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
        /** Reason the account is ineligible to hold this capability, if applicable. */
        @SerializedName("ineligible_reason") val ineligibleReason: String? = null,
        /** Why this capability has not been granted, derived from the latest concluded identity-verification run. */
        val errors: List<CapabilityError>? = null,
        @SerializedName("currently_due") val currentlyDue: List<String>? = null,
        val created: String?,
        val updated: String?,
        val disabled: Boolean? = null
    )

    /**
     * A server-derived conclusion about why a capability has not been granted.
     *
     * @property id Identifier for this derived conclusion.
     * @property object The object type identifier returned by the API.
     * @property code Frame's provider-neutral failure type, e.g. `identity_mismatch`, `verification_rejected`.
     * @property message Display-ready explanation, preferred over client-side copy.
     * @property requirementId The requirement this conclusion is attached to, if any.
     */
    data class CapabilityError(
        val id: String?,
        val `object`: String? = null,
        val code: String? = null,
        val message: String? = null,
        @SerializedName("requirement_id") val requirementId: String? = null
    )

    /** Mapped from the wire string; an unrecognized value degrades to [UNKNOWN]. */
    enum class CapabilityStatus {
        /** Never requested for this account. */
        UNREQUESTED,
        /** Requested, but its requirements are not yet satisfied. */
        PENDING,
        /** Granted and in effect. */
        ACTIVE,
        /** Held but switched off. [Capability.disabledReason] says whether that was risk-borne. */
        DISABLED,
        /** The account type may not hold this capability. */
        INELIGIBLE,
        /** A status this SDK version does not know. */
        UNKNOWN;

        /** Factory for mapping a wire status string to [CapabilityStatus]. */
        companion object {
            /** Maps the wire string to a [CapabilityStatus], degrading anything unrecognized to [UNKNOWN]. */
            fun from(status: String?): CapabilityStatus = when (status) {
                "unrequested" -> UNREQUESTED
                "pending" -> PENDING
                "active" -> ACTIVE
                "disabled" -> DISABLED
                "ineligible" -> INELIGIBLE
                else -> UNKNOWN
            }
        }
    }

    private const val PRODUCT_GRANT_REVOKED_REASON = "product_grant_revoked"

    /** This capability's [status], degrading an unrecognized value to [CapabilityStatus.UNKNOWN]. */
    val Capability.capabilityStatus: CapabilityStatus
        get() = CapabilityStatus.from(status)

    /** Mirrors the server's `Capability#blocks_activation?`. */
    val Capability.isOutstanding: Boolean
        get() = when (capabilityStatus) {
            CapabilityStatus.ACTIVE, CapabilityStatus.UNREQUESTED, CapabilityStatus.INELIGIBLE -> false
            CapabilityStatus.DISABLED -> disabledReason != PRODUCT_GRANT_REVOKED_REASON
            CapabilityStatus.PENDING, CapabilityStatus.UNKNOWN -> true
        }

    /**
     * Whether work listed against this capability can still move it forward. The server blanks
     * `currently_due` only for `ineligible`, so a disabled capability still publishes dead keys.
     */
    val Capability.hasActionableRequirements: Boolean
        get() = when (capabilityStatus) {
            CapabilityStatus.PENDING, CapabilityStatus.UNKNOWN -> true
            CapabilityStatus.ACTIVE, CapabilityStatus.UNREQUESTED,
            CapabilityStatus.DISABLED, CapabilityStatus.INELIGIBLE -> false
        }

    /** `currently_due` keys the applicant can still resolve; a disabled capability publishes dead keys. */
    val Capability.actionableRequirements: List<String>
        get() = if (hasActionableRequirements) currentlyDue.orEmpty() else emptyList()

    /** `currently_due` keys the onboarding flow acts on. */
    object CapabilityRequirementKey {
        /** The backend stepped the account up to a government-ID document check. */
        const val IDENTITY_DOCUMENT = "individual.identity_document"
        /** A KYC run rejected complete-but-wrong details (FRA-6552). */
        const val KYC = "individual.kyc"
    }
}
