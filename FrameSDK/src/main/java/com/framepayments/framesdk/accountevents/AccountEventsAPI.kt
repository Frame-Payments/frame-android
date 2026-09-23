package com.framepayments.framesdk.accountevents

import com.framepayments.framesdk.FrameAuthMode
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError

/** Calls the account events endpoint. Internal only — the SDK emits its own diagnostic events via [AccountEventQueue]. */
internal object AccountEventsAPI {
    /**
     * Submits a batch of events to the backend.
     *
     * Authenticates with [FrameAuthMode.PublishableOnly] regardless of any active onboarding
     * session: the endpoint only ever accepts `pk_`, and the event already names the end-user
     * account by `account_id` in the body.
     */
    suspend fun record(events: List<AccountEventsRequests.Event>): Pair<RecordResponse?, NetworkingError?> {
        val request = AccountEventsRequests.RecordRequest(events)
        val (data, error) = FrameNetworking.performDataTaskWithRequest(
            AccountEventsEndpoints.Record,
            request,
            FrameAuthMode.PublishableOnly
        )
        return Pair(data?.let { FrameNetworking.parseResponse<RecordResponse>(it) }, error)
    }
}
