package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameMetadata
import com.google.gson.annotations.SerializedName

object AccountResponses {
    data class ListAccountsResponse(
        val meta: FrameMetadata? = null,
        val data: List<AccountObjects.Account>? = null
    )

    data class SearchAccountsResponse(
        val meta: FrameMetadata? = null,
        val data: List<AccountObjects.Account>? = null
    )

    data class PlaidLinkTokenResponse(
        @SerializedName("link_token") val linkToken: String
    )
}
