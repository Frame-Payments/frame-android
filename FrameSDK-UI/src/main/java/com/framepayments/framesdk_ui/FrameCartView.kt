package com.framepayments.framesdk_ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.framepayments.framesdk_ui.databinding.ItemCartBinding
import com.framepayments.framesdk_ui.databinding.ViewFrameCartBinding
import com.framepayments.framesdk.FrameObjects

class FrameCartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding =
        ViewFrameCartBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var viewModel: FrameCartViewModel
    private var listener: ((Int) -> Unit)? = null

    fun configure(
        customerId: String?,
        items: List<FrameCartItem>,
        shippingCents: Int,
        onCheckout: (Int) -> Unit
    ) {
        listener = onCheckout
        viewModel = FrameCartViewModel(items, shippingCents)

        // title
        binding.cartTitle.text = context.getString(R.string.cart_title)

        // list
        binding.itemsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CartAdapter(items)
        }

        // summary rows
        binding.subtotalLabel.text = context.getString(R.string.subtotal)
        binding.subtotalValue.text = CurrencyFormatter.convertCentsToCurrencyString(viewModel.subtotal)
        binding.shippingLabel.text = context.getString(R.string.shipping)
        binding.shippingValue.text = CurrencyFormatter.convertCentsToCurrencyString(viewModel.shippingAmount)
        binding.totalLabel.text = context.getString(R.string.total)
        binding.totalValue.text = CurrencyFormatter.convertCentsToCurrencyString(viewModel.finalTotal)

        // button
        binding.checkoutButton.setOnClickListener {
            listener?.invoke(viewModel.finalTotal)
        }
    }

    /** View-holder / adapter */
    private inner class CartAdapter(private val data: List<FrameCartItem>) :
        RecyclerView.Adapter<CartAdapter.Holder>() {

        inner class Holder(val row: ItemCartBinding) : RecyclerView.ViewHolder(row.root)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): Holder {
            val row = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return Holder(row)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = data[position]
            holder.row.itemTitle.text = item.title
            holder.row.itemAmount.text = CurrencyFormatter.convertCentsToCurrencyString(item.amountInCents)
            holder.row.itemImage.load(item.imageUrl)
        }
    }
}