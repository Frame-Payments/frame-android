package com.framepayments.framesdk_ui

class FrameCartViewModel(
    items: List<FrameCartItem>,
    val shippingAmount: Int
) {
    val subtotal: Int = items.sumOf { it.amountInCents }
    val finalTotal: Int = subtotal + shippingAmount
}