package com.framepayments.framesdk_ui.viewmodels

import com.framepayments.framesdk_ui.FrameCartItem

/**
 * ViewModel for the cart summary surface.
 *
 * Computes totals from a list of [FrameCartItem]s plus a flat shipping amount.
 *
 * @property shippingAmount Shipping cost in cents added to the item subtotal.
 * @property subtotal Sum of all item amounts in cents.
 * @property finalTotal Total amount in cents (subtotal + shipping) passed to the payment flow.
 */
class FrameCartViewModel(
    items: List<FrameCartItem>,
    val shippingAmount: Int
) {
    val subtotal: Int = items.sumOf { it.amountInCents }
    val finalTotal: Int = subtotal + shippingAmount
}