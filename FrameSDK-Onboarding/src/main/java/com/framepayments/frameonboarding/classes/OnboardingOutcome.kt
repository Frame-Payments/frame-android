package com.framepayments.frameonboarding.classes

import com.framepayments.framesdk.capabilities.CapabilityObjects
import com.framepayments.framesdk.capabilities.CapabilityObjects.isOutstanding

/** How onboarding actually ended for the applicant. Reaching the last step is not the same as passing verification. */
sealed class OnboardingOutcome {
    /** Every required capability is granted. */
    data object Approved : OnboardingOutcome()

    /** Outstanding, but no decision has been reached — a run under manual review counts here. */
    data object PendingReview : OnboardingOutcome()

    /** Verification did not pass and retrying cannot change that. */
    data class Declined(
        /** Display-ready explanation from the API, or null if it gave none. */
        val message: String?
    ) : OnboardingOutcome()

    /** Verification did not pass, but the applicant can still act on it. */
    data class ActionRequired(
        /** Display-ready explanation from the API, or null if it gave none. */
        val message: String?
    ) : OnboardingOutcome()

    /** Whether this outcome should be reported to the host as a successful onboarding. */
    val isSuccess: Boolean
        get() = this is Approved

    /** Factory for resolving an [OnboardingOutcome] from an account's capabilities. */
    companion object {
        private val categoriesByFailureType: Map<String, String> = mapOf(
            "identity_mismatch" to "retriable_with_new_data",
            "identity_not_found" to "step_up",
            "verification_rejected" to "terminal",
            "review_pending" to "review",
            "provider_error" to "transient",
            "signals_unavailable" to "transient",
            "unclassified" to "unclassified"
        )

        /**
         * A required capability missing from the response is not a failure signal — the server
         * silently skips capabilities gated on a merchant switch that is off, so judging on what
         * came back is the only sound read.
         */
        fun resolve(
            capabilities: List<CapabilityObjects.Capability>,
            required: List<Capabilities>
        ): OnboardingOutcome {
            // Capabilities were never fetched (or the account lookup failed) — nothing outstanding
            // is not the same as nothing to check, so this must not read as Approved.
            if (capabilities.isEmpty() && required.isNotEmpty()) return PendingReview

            val requiredNames = capabilitiesWithDependencies(required).map { it.apiValue }.toSet()
            val relevant = if (requiredNames.isEmpty()) capabilities else capabilities.filter { requiredNames.contains(it.name) }
            val outstanding = relevant.filter { it.isOutstanding }
            if (outstanding.isEmpty()) return Approved

            var fallback: OnboardingOutcome = PendingReview
            for (capability in outstanding) {
                val error = capability.errors?.firstOrNull() ?: continue
                val code = error.code ?: continue
                when (categoriesByFailureType[code]) {
                    "terminal" -> return Declined(error.message)
                    "retriable_with_new_data", "step_up" -> fallback = ActionRequired(error.message)
                    else -> continue
                }
            }
            return fallback
        }
    }
}
