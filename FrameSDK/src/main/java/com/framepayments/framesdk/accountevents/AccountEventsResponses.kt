package com.framepayments.framesdk.accountevents

/** Response body for POST /v1/client/account_events. */
data class RecordResponse(
    /** Number of events accepted from the batch. */
    val recorded: Int,
    /** Per-event rejections, by index into the submitted batch. Never retry these. */
    val rejected: List<Rejection>
)

/** One rejected event from a batch, identified by its index in the submitted request. */
data class Rejection(
    val index: Int,
    val name: String,
    val error: String
)
