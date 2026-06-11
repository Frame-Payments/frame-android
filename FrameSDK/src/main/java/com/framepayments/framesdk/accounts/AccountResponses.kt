package com.framepayments.framesdk.accounts

import com.framepayments.framesdk.FrameMetadata
import com.google.gson.annotations.SerializedName

/** Namespace for account API response types. */
object AccountResponses {

    /**
     * Paginated list of accounts returned by [AccountsAPI.getAccounts].
     *
     * @property meta Pagination metadata.
     * @property data The list of accounts on the current page.
     */
    data class ListAccountsResponse(
        val meta: FrameMetadata? = null,
        val data: List<AccountObjects.Account>? = null
    )

    /**
     * Paginated list of accounts returned by [AccountsAPI.searchAccounts].
     *
     * @property meta Pagination metadata.
     * @property data The list of matching accounts.
     */
    data class SearchAccountsResponse(
        val meta: FrameMetadata? = null,
        val data: List<AccountObjects.Account>? = null
    )

    /**
     * Response containing a Plaid Link token for initiating a bank-account connection.
     *
     * @property linkToken The short-lived token passed to the Plaid Link SDK.
     */
    data class PlaidLinkTokenResponse(
        @SerializedName("link_token") val linkToken: String?
    )
}
