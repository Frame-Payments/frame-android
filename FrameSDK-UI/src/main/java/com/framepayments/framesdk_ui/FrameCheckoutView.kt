package com.framepayments.framesdk_ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk_ui.databinding.ViewFrameCheckoutBinding
import com.framepayments.framesdk_ui.databinding.ItemPaymentCardBinding
import com.evervault.sdk.input.ui.card.RowsPaymentCard

class FrameCheckoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Inflate with ViewBinding
    private val binding: ViewFrameCheckoutBinding = ViewFrameCheckoutBinding.inflate(
        LayoutInflater.from(context), this, true
    )
    private val viewModel: FrameCheckoutViewModel

    var checkoutCallback: ((ChargeIntent) -> Unit)? = null

    init {
        // Acquire ViewModel
        val activity = (context as? AppCompatActivity)
            ?: throw IllegalArgumentException("FrameCheckoutView must be used in an AppCompatActivity")
        viewModel = ViewModelProvider(activity)[FrameCheckoutViewModel::class.java]

        // Wire up UI
        binding.closeButton.setOnClickListener { (context as Activity).finish() }

        binding.applePayBtn.setOnClickListener { viewModel.payWithApplePay() }
        binding.googlePayBtn.setOnClickListener { viewModel.payWithGooglePay() }

        binding.payButton.setOnClickListener {
            binding.checkoutProgressBar.visibility = View.VISIBLE
            binding.payButton.isEnabled = false
            viewModel.checkoutWithSelectedPaymentMethod(binding.saveCard.isChecked)
                .observe(activity) { intent ->
                    binding.checkoutProgressBar.visibility = View.GONE
                    binding.payButton.isEnabled = true
                    intent?.let { checkoutCallback?.invoke(it) }
                }
        }

        viewModel.customerPaymentOptions.observe(activity) { list ->
            if (list.isNullOrEmpty()) {
                binding.existingPaymentOptionsScrollView.visibility = View.GONE
            } else {
                binding.existingPaymentOptionsScrollView.visibility = View.VISIBLE
                renderPaymentOptions(list)
            }
        }

        // Two-way bindings
        binding.zip.doAfterTextChanged { viewModel.customerZipCode.value = it.toString() }

        binding.countryRegion.setOnClickListener {
            // TODO: Change Country Drawer when other countries are supported.
        }

        findViewById<ComposeView>(R.id.evervaultCompose).setContent {
            MaterialTheme {
                RowsPaymentCard(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    onDataChange = { data -> viewModel.cardData = data }
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderPaymentOptions(options: List<FrameObjects.PaymentMethod>) {
        binding.paymentOptionsContainer.removeAllViews()
        options.forEach { option ->
            val itemBinding = ItemPaymentCardBinding.inflate(
                LayoutInflater.from(context),
                binding.paymentOptionsContainer,
                false
            )
            itemBinding.paymentCardText.text = "${option.card?.brand.orEmpty()} ${option.card?.lastFourDigits.orEmpty()}"
            val color = if (viewModel.selectedCustomerPaymentOption == option)
                ContextCompat.getColor(context, R.color.black)
            else
                ContextCompat.getColor(context, R.color.divider)
            itemBinding.paymentCardContainer.strokeColor = color

            itemBinding.root.setOnClickListener {
                viewModel.selectedCustomerPaymentOption = option
                renderPaymentOptions(options)
            }
            binding.paymentOptionsContainer.addView(itemBinding.root)
        }
    }

    @SuppressLint("SetTextI18n")
    fun configure(
        customerId: String?,
        paymentAmount: Int,
        onCheckout: (ChargeIntent) -> Unit
    ) {
        checkoutCallback = onCheckout
        viewModel.loadCustomerPaymentMethods(customerId.toString(), paymentAmount)
        binding.payButton.text = "Pay ${CurrencyFormatter.convertCentsToCurrencyString(paymentAmount)}"
    }
}