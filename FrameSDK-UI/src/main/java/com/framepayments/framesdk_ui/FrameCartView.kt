package com.framepayments.framesdk_ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.framepayments.framesdk_ui.databinding.ItemCartBinding
import com.framepayments.framesdk_ui.databinding.ViewFrameCartBinding
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.toCartAppearance
import com.framepayments.framesdk_ui.viewmodels.FrameCartViewModel

class FrameCartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding =
        ViewFrameCartBinding.inflate(LayoutInflater.from(context), this, true)

    private lateinit var viewModel: FrameCartViewModel
    private var listener: ((Int) -> Unit)? = null
    private var appearance: FrameCartAppearance? = null
    private var explicitAppearance: FrameCartAppearance? = null
    private var theme: FrameTheme? = null

    /**
     * Apply a [FrameTheme] to this cart. The theme is converted to a [FrameCartAppearance]
     * and merged with any explicit appearance previously passed to [configure] (the explicit
     * appearance wins on conflict). Safe to call before or after [configure]; takes effect
     * immediately if [configure] has already run.
     */
    fun setTheme(theme: FrameTheme) {
        this.theme = theme
        if (::viewModel.isInitialized) {
            appearance = theme.toCartAppearance(overlay = explicitAppearance)
            applyAppearance()
            binding.itemsList.adapter?.notifyDataSetChanged()
        }
    }

    /**
     * Configure the cart. [accountId] is not consumed directly by the cart UI, but is
     * required because the cart flow always feeds into [FrameCheckoutView], which is
     * account-scoped — surfacing the requirement here forces callers to acknowledge it
     * before mounting the cart instead of crashing later at checkout time.
     */
    fun configure(
        @Suppress("UNUSED_PARAMETER") accountId: String,
        items: List<FrameCartItem>,
        shippingCents: Int,
        onCheckout: (Int) -> Unit,
        appearance: FrameCartAppearance? = null
    ) {
        require(accountId.isNotEmpty()) { "FrameCartView.configure requires a non-empty accountId" }
        explicitAppearance = appearance
        this.appearance = theme?.toCartAppearance(overlay = appearance) ?: appearance
        listener = onCheckout
        viewModel = FrameCartViewModel(items, shippingCents)

        binding.itemsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CartAdapter(items, this@FrameCartView.appearance?.cartItemHeightPx)
        }

        applyAppearance()

        binding.checkoutButton.setOnClickListener {
            listener?.invoke(viewModel.finalTotal)
        }
    }

    private fun applyAppearance() {
        val appearance = this.appearance

        binding.cartTitle.text = appearance?.cartTitle ?: context.getString(R.string.cart_title)
        appearance?.cartTitleColor?.let { binding.cartTitle.setTextColor(it) }
        appearance?.cartTitleSizeSp?.let { binding.cartTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        binding.cartTitle.setTypeface(binding.cartTitle.typeface, appearance?.cartTitleTypeface ?: Typeface.BOLD)

        appearance?.subtitle?.let { subtitle ->
            binding.cartSubtitle.visibility = android.view.View.VISIBLE
            binding.cartSubtitle.text = subtitle
            appearance.subtitleColor?.let { binding.cartSubtitle.setTextColor(it) }
            appearance.subtitleSizeSp?.let { binding.cartSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        } ?: run { binding.cartSubtitle.visibility = android.view.View.GONE }

        appearance?.backgroundColor?.let { binding.cartRoot.setBackgroundColor(it) }

        binding.subtotalLabel.text = context.getString(R.string.subtotal)
        appearance?.auxiliaryLabelColor?.let { binding.subtotalLabel.setTextColor(it) }
        appearance?.auxiliaryLabelSizeSp?.let { binding.subtotalLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        binding.subtotalValue.text = CurrencyFormatter.convertCentsToCurrencyString(viewModel.subtotal)
        appearance?.auxiliaryLabelColor?.let { binding.subtotalValue.setTextColor(it) }
        appearance?.auxiliaryLabelSizeSp?.let { binding.subtotalValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }

        binding.shippingLabel.text = context.getString(R.string.shipping)
        appearance?.auxiliaryLabelColor?.let { binding.shippingLabel.setTextColor(it) }
        appearance?.auxiliaryLabelSizeSp?.let { binding.shippingLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        binding.shippingValue.text = CurrencyFormatter.convertCentsToCurrencyString(viewModel.shippingAmount)
        appearance?.auxiliaryLabelColor?.let { binding.shippingValue.setTextColor(it) }
        appearance?.auxiliaryLabelSizeSp?.let { binding.shippingValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }

        binding.totalLabel.text = context.getString(R.string.total)
        appearance?.totalColor?.let { binding.totalLabel.setTextColor(it) }
        appearance?.totalSizeSp?.let { binding.totalLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        binding.totalLabel.setTypeface(binding.totalLabel.typeface, appearance?.totalTypeface ?: Typeface.BOLD)
        binding.totalValue.text = CurrencyFormatter.convertCentsToCurrencyString(viewModel.finalTotal)
        appearance?.totalColor?.let { binding.totalValue.setTextColor(it) }
        appearance?.totalSizeSp?.let { binding.totalValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
        binding.totalValue.setTypeface(binding.totalValue.typeface, appearance?.totalTypeface ?: Typeface.BOLD)

        binding.checkoutButton.text = appearance?.checkoutButtonTitle ?: context.getString(R.string.checkout)
        appearance?.checkoutButtonBackgroundColor?.let { binding.checkoutButton.setBackgroundColor(it) }
        appearance?.checkoutButtonTextColor?.let { binding.checkoutButton.setTextColor(it) }
        appearance?.checkoutButtonTextSizeSp?.let { binding.checkoutButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
    }

    /** View-holder / adapter */
    private inner class CartAdapter(
        private val data: List<FrameCartItem>,
        private val itemHeightPx: Int?
    ) : RecyclerView.Adapter<CartAdapter.Holder>() {

        inner class Holder(val row: ItemCartBinding) : RecyclerView.ViewHolder(row.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val row = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemHeightPx?.let { px ->
                row.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, px)
            }
            return Holder(row)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = data[position]
            holder.row.itemTitle.text = item.title
            appearance?.cartItemColor?.let { holder.row.itemTitle.setTextColor(it) }
            appearance?.cartItemSizeSp?.let { holder.row.itemTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
            holder.row.itemAmount.text = CurrencyFormatter.convertCentsToCurrencyString(item.amountInCents)
            appearance?.cartItemColor?.let { holder.row.itemAmount.setTextColor(it) }
            appearance?.cartItemSizeSp?.let { holder.row.itemAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
            appearance?.cartItemBackgroundColor?.let { holder.row.root.setBackgroundColor(it) }
            itemHeightPx?.let { px ->
                holder.row.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, px)
            }
            holder.row.itemImage.load(item.imageUrl)
        }
    }
}