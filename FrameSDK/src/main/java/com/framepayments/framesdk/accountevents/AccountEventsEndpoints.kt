package com.framepayments.framesdk.accountevents

import com.framepayments.framesdk.FrameNetworkingEndpoints
import com.framepayments.framesdk.QueryItem

sealed class AccountEventsEndpoints : FrameNetworkingEndpoints {

    /** Records a batch of account events. Resolves to POST /v1/client/account_events. */
    object Record : AccountEventsEndpoints()

    override val endpointURL: String
        get() = "/v1/client/account_events"

    override val httpMethod: String
        get() = "POST"

    override val queryItems: List<QueryItem>?
        get() = null
}
