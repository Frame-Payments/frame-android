package com.framepayments.framesdk_ui.viewmodels

import com.framepayments.framesdk_ui.FrameCartItem

class FrameCartViewModel(
    items: List<FrameCartItem>,
    val shippingAmount: Int
) {
    val subtotal: Int = items.sumOf { it.amountInCents }
    val finalTotal: Int = subtotal + shippingAmount
}