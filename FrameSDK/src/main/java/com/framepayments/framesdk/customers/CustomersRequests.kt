package com.framepayments.framesdk.customers
import com.google.gson.annotations.SerializedName
import com.framepayments.framesdk.FrameObjects

/**
 * Contains request body models for customer API operations.
 */
object CustomersRequests {

    /**
     * Request body for creating a new customer.
     *
     * @property billingAddress The customer's billing address. Pass null to omit.
     * @property shippingAddress The customer's shipping address. Pass null to omit.
     * @property name The customer's full name. Pass null to omit.
     * @property phone The customer's phone number. Pass null to omit.
     * @property email The customer's email address. Pass null to omit.
     * @property description An optional description or note about the customer. Pass null to omit.
     * @property metadata A map of merchant-defined key-value pairs to attach to the customer. Pass null to omit.
     */
    data class CreateCustomerRequest(
        @SerializedName("billing_address") val billingAddress: FrameObjects.BillingAddress?,
        @SerializedName("shipping_address") val shippingAddress: FrameObjects.BillingAddress?,
        val name: String?,
        val phone: String?,
        val email: String?,
        val description: String?,
        val metadata: Map<String, String>?
    )

    /**
     * Request body for updating an existing customer.
     *
     * Only fields that should be changed need to be provided; pass null for any field to leave it unchanged.
     *
     * @property billingAddress The updated billing address. Pass null to leave unchanged.
     * @property shippingAddress The updated shipping address. Pass null to leave unchanged.
     * @property name The updated full name. Pass null to leave unchanged.
     * @property phone The updated phone number. Pass null to leave unchanged.
     * @property email The updated email address. Pass null to leave unchanged.
     * @property description The updated description or note. Pass null to leave unchanged.
     * @property metadata The updated map of merchant-defined key-value pairs. Pass null to leave unchanged.
     */
    data class UpdateCustomerRequest(
        @SerializedName("billing_address") val billingAddress: FrameObjects.BillingAddress?,
        @SerializedName("shipping_address") val shippingAddress: FrameObjects.BillingAddress?,
        val name: String?,
        val phone: String?,
        val email: String?,
        val description: String?,
        val metadata: Map<String, String>?
    )

    /**
     * Parameters for searching customers.
     *
     * All fields are optional; provide only the criteria relevant to the search.
     *
     * @property q A general-purpose search query string. Defaults to null.
     * @property email Filters results to customers matching this email address. Defaults to null.
     * @property createdAfter Unix timestamp; returns only customers created after this time. Defaults to null.
     * @property page The page number to retrieve. Defaults to null, which uses the API's first page.
     * @property perPage The number of results per page. Defaults to null, which uses the API's default page size.
     */
    data class SearchCustomersRequest(
        val q: String? = null,
        val email: String? = null,
        @SerializedName("created_after") val createdAfter: Int? = null,
        val page: Int? = null,
        @SerializedName("per_page") val perPage: Int? = null
    )
}
