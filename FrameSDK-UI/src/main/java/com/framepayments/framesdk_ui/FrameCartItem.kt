package com.framepayments.framesdk_ui

/**
 * A single line item displayed in [FrameCartView].
 *
 * @property id Unique identifier for this item.
 * @property title Display name shown in the cart row.
 * @property amountInCents Item price in US cents (e.g. `2500` = $25.00).
 * @property imageUrl URL of the item's thumbnail image.
 */
data class FrameCartItem(
    val id: String,
    val title: String,
    val amountInCents: Int,
    val imageUrl: String
)