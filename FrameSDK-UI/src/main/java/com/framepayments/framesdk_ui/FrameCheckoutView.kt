package com.framepayments.framesdk_ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk_ui.databinding.ViewFrameCheckoutBinding
import com.framepayments.framesdk_ui.databinding.ItemPaymentCardBinding
import com.evervault.sdk.input.ui.card.RowsPaymentCard
import com.framepayments.framesdk_ui.viewmodels.AvailableCountries
import com.framepayments.framesdk_ui.viewmodels.FrameCheckoutViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

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
        val activity = (context as? AppCompatActivity)
            ?: throw IllegalArgumentException("FrameCheckoutView must be used in an AppCompatActivity")
        viewModel = ViewModelProvider(activity)[FrameCheckoutViewModel::class.java]

        binding.closeButton.setOnClickListener { (context as Activity).finish() }
//        binding.applePayBtn.setOnClickListener { viewModel.payWithApplePay() }
//        binding.googlePayBtn.setOnClickListener { viewModel.payWithGooglePay() }

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

        // Bindings for customer information
        binding.customerName.doAfterTextChanged { viewModel.customerName.value = it.toString() }
        binding.customerEmail.doAfterTextChanged { viewModel.customerEmail.value = it.toString() }
        binding.customerName.doAfterTextChanged { viewModel.customerName.value = it.toString() }
        binding.address1.doAfterTextChanged { viewModel.customerAddressLine1.value = it.toString() }
        binding.address2.doAfterTextChanged { viewModel.customerAddressLine2.value = it.toString() }
        binding.city.doAfterTextChanged { viewModel.customerCity.value = it.toString() }
        binding.state.doAfterTextChanged { viewModel.customerState.value = it.toString() }
        binding.zip.doAfterTextChanged { viewModel.customerZipCode.value = it.toString() }
        binding.countryInput.setOnClickListener {
            showCountryPicker()
        }

        findViewById<ComposeView>(R.id.evervaultCompose).setContent {
            MaterialTheme {
                RowsPaymentCard(
                    modifier = Modifier.fillMaxWidth(),
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
            itemBinding.paymentCardText.text = "${option.card?.brand.orEmpty().replaceFirstChar { it.uppercase() }} ${option.card?.lastFourDigits.orEmpty()}"
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
    private fun showCountryPicker() {
        val activity = context as? FragmentActivity ?: return
        val view = LayoutInflater.from(context).inflate(R.layout.country_picker_sheet, null)
        val bottomSheetDialog = BottomSheetDialog(activity)
        bottomSheetDialog.setContentView(view)

        val spinner: Spinner = view.findViewById(R.id.countrySpinner)
        val doneButton: TextView = view.findViewById(R.id.doneButton)

        val countries = AvailableCountries.allCountries
        val adapter = ArrayAdapter(
            activity,
            android.R.layout.simple_spinner_item,
            countries.mapNotNull { it?.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(countries.indexOfFirst { it == viewModel.customerCountry })

        doneButton.setOnClickListener {
            val selectedIndex = spinner.selectedItemPosition
            viewModel.customerCountry = countries[selectedIndex]
            binding.countryInput.setText(viewModel.customerCountry.displayName)
            Toast.makeText(activity, "Selected: ${viewModel.customerCountry.displayName}", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
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