package com.framepayments.frameonboarding.networking.beneficialowners

import com.framepayments.framesdk.EmptyRequest
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.NetworkingError
import com.google.gson.annotations.SerializedName

internal data class BeneficialOwner(
    val id: String,
    val status: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val email: String? = null,
    val roles: List<String>? = null
)

internal data class BeneficialOwnerListResponse(
    val data: List<BeneficialOwner>? = null
)

/** Create and update body. Null fields are omitted so an update does not send JSON null. */
internal data class BeneficialOwnerWriteRequest(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val email: String? = null,
    val dob: String? = null,
    val ssn: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("phone_country_code") val phoneCountryCode: String? = null,
    @SerializedName("address_line1") val addressLine1: String? = null,
    @SerializedName("address_line2") val addressLine2: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerializedName("postal_code") val postalCode: String? = null,
    val country: String? = null,
    val roles: List<String>? = null,
    val invite: Boolean? = null
)

internal object BeneficialOwnersAPI {
    suspend fun list(accountId: String): Pair<BeneficialOwnerListResponse?, NetworkingError?> {
        if (accountId.isEmpty()) return Pair(null, null)
        val (data, error) = FrameNetworking.performDataTask(BeneficialOwnerEndpoints.Index(accountId))
        return Pair(data?.let { FrameNetworking.parseResponse<BeneficialOwnerListResponse>(it) }, error)
    }

    suspend fun create(
        accountId: String,
        request: BeneficialOwnerWriteRequest
    ): Pair<BeneficialOwner?, NetworkingError?> = write(BeneficialOwnerEndpoints.Create(accountId), request)

    suspend fun update(
        accountId: String,
        ownerId: String,
        request: BeneficialOwnerWriteRequest
    ): Pair<BeneficialOwner?, NetworkingError?> = write(BeneficialOwnerEndpoints.Update(accountId, ownerId), request)

    suspend fun destroy(accountId: String, ownerId: String): NetworkingError? {
        if (accountId.isEmpty() || ownerId.isEmpty()) return null
        val (_, error) = FrameNetworking.performDataTask(BeneficialOwnerEndpoints.Destroy(accountId, ownerId))
        return error
    }

    suspend fun confirm(accountId: String): NetworkingError? {
        if (accountId.isEmpty()) return null
        val (_, error) = FrameNetworking.performDataTaskWithRequest(
            BeneficialOwnerEndpoints.Confirm(accountId),
            EmptyRequest(null)
        )
        return error
    }

    suspend fun resendInvite(accountId: String, ownerId: String): NetworkingError? {
        if (accountId.isEmpty() || ownerId.isEmpty()) return null
        val (_, error) = FrameNetworking.performDataTaskWithRequest(
            BeneficialOwnerEndpoints.ResendInvite(accountId, ownerId),
            EmptyRequest(null)
        )
        return error
    }

    private suspend fun write(
        endpoint: BeneficialOwnerEndpoints,
        request: BeneficialOwnerWriteRequest
    ): Pair<BeneficialOwner?, NetworkingError?> {
        val (data, error) = FrameNetworking.performDataTaskWithRequest(endpoint, request)
        return Pair(data?.let { FrameNetworking.parseResponse<BeneficialOwner>(it) }, error)
    }
}
