package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameMetadata

object AccountResponses {
    data class ListAccountsResponse(
        val meta: FrameMetadata? = null,
        val data: List<AccountObjects.Account>? = null
    )
}
