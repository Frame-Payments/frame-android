package com.framepayments.framesdk.invoicelineitems

/**
 * Namespace for request models used by [InvoiceLineItemsAPI].
 */
object InvoiceLineItemRequests {

    /**
     * Request body for creating a new invoice line item.
     *
     * @property product The identifier of the product to associate with this line item.
     * @property quantity The number of units to include. Must be greater than 0.
     */
    data class CreateLineItemRequest(
        val product: String,
        val quantity: Int // Note: Must be greater than 0
    )

    /**
     * Request body for updating an existing invoice line item.
     *
     * Only non-null fields are applied to the existing line item.
     *
     * @property product The updated product identifier, or null to leave unchanged.
     * @property quantity The updated unit quantity. Must be greater than 0 when provided.
     */
    data class UpdateLineItemRequest(
        val product: String?,
        val quantity: Int? // Note: Must be greater than 0
    )
}
