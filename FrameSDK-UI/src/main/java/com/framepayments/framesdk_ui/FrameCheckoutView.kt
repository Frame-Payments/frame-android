package com.framepayments.framesdk_ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk_ui.buttons.FrameGooglePayButton
import com.framepayments.framesdk_ui.databinding.ViewFrameCheckoutBinding
import com.framepayments.framesdk_ui.databinding.ItemPaymentCardBinding
import com.framepayments.framesdk_ui.validation.FieldKey
import com.framepayments.framesdk_ui.validation.ValidationError
import com.framepayments.framesdk_ui.validation.Validators
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

        // Pay button is disabled until the user either selects a saved payment method
        // or enters new card details that pass potential-validity.
        binding.payButton.isEnabled = false
        binding.payButton.alpha = 0.4f
        viewModel.hasUsablePaymentInput.observe(activity) { canPay ->
            binding.payButton.isEnabled = canPay
            binding.payButton.alpha = if (canPay) 1f else 0.4f
        }

        binding.payButton.setOnClickListener {
            binding.checkoutProgressBar.visibility = View.VISIBLE
            binding.payButton.isEnabled = false
            viewModel.checkoutWithSelectedPaymentMethod(binding.saveCard.isChecked)
                .observe(activity) { intent ->
                    binding.checkoutProgressBar.visibility = View.GONE
                    binding.payButton.isEnabled = viewModel.hasUsablePaymentInput.value == true
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

        // Bindings for customer information — also clear errors as the user edits.
        wireField(
            edit = binding.customerName,
            liveData = viewModel.customerName,
            field = FieldKey.NAME,
            validate = { Validators.validateName(it) },
            activity = activity
        )
        wireField(
            edit = binding.customerEmail,
            liveData = viewModel.customerEmail,
            field = FieldKey.EMAIL,
            validate = { Validators.validateEmail(it) },
            activity = activity
        )
        wireField(
            edit = binding.address1,
            liveData = viewModel.customerAddressLine1,
            field = FieldKey.ADDRESS_LINE_1,
            validate = { Validators.validateAddressLine1(it) },
            activity = activity
        )
        wireField(
            edit = binding.address2,
            liveData = viewModel.customerAddressLine2,
            field = null,
            validate = null,
            activity = activity
        )
        wireField(
            edit = binding.city,
            liveData = viewModel.customerCity,
            field = FieldKey.CITY,
            validate = { Validators.validateCity(it) },
            activity = activity
        )
        wireField(
            edit = binding.state,
            liveData = viewModel.customerState,
            field = FieldKey.STATE,
            validate = { Validators.validateState(it) },
            activity = activity
        )
        wireField(
            edit = binding.zip,
            liveData = viewModel.customerZipCode,
            field = FieldKey.ZIP,
            validate = { Validators.validateZip(it) },
            activity = activity
        )
        binding.countryInput.setOnClickListener {
            showCountryPicker()
        }

        binding.countryInput.setText(viewModel.customerCountry.displayName)

        binding.encryptedCardInput.onCardDataChange = { data ->
            viewModel.cardData = data
            viewModel.setError(FieldKey.CARD, Validators.validateCard(data))
        }

        viewModel.fieldErrors.observe(activity) { errors ->
            binding.customerNameLayout.error = errors[FieldKey.NAME]?.let { context.getString(it.messageRes) }
            binding.customerEmailLayout.error = errors[FieldKey.EMAIL]?.let { context.getString(it.messageRes) }
            binding.address1Layout.error = errors[FieldKey.ADDRESS_LINE_1]?.let { context.getString(it.messageRes) }
            binding.cityLayout.error = errors[FieldKey.CITY]?.let { context.getString(it.messageRes) }
            binding.stateLayout.error = errors[FieldKey.STATE]?.let { context.getString(it.messageRes) }
            binding.zipLayout.error = errors[FieldKey.ZIP]?.let { context.getString(it.messageRes) }
            binding.countryInputLayout.error = errors[FieldKey.COUNTRY]?.let { context.getString(it.messageRes) }
            val cardErr = errors[FieldKey.CARD]
            if (cardErr == null) {
                binding.cardErrorText.visibility = View.GONE
                binding.cardErrorText.text = ""
            } else {
                binding.cardErrorText.visibility = View.VISIBLE
                binding.cardErrorText.text = context.getString(cardErr.messageRes)
            }
        }
    }

    private fun wireField(
        edit: com.google.android.material.textfield.TextInputEditText,
        liveData: androidx.lifecycle.MutableLiveData<String>,
        field: FieldKey?,
        validate: ((String?) -> ValidationError?)?,
        activity: AppCompatActivity
    ) {
        edit.doAfterTextChanged {
            val value = it.toString()
            liveData.value = value
            field?.let { key -> viewModel.clearError(key) }
        }
        liveData.observe(activity) { edit.setTextIfDifferent(it) }
        if (field != null && validate != null) {
            edit.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.setError(field, validate(edit.text?.toString()))
                }
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
            viewModel.setError(FieldKey.COUNTRY, Validators.validateCountry(viewModel.customerCountry.alpha2Code))
            Toast.makeText(activity, "Selected: ${viewModel.customerCountry.displayName}", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    @JvmOverloads
    @SuppressLint("SetTextI18n")
    fun configure(
        customerId: String?,
        paymentAmount: Int,
        googlePayMerchantId: String? = null,
        addressMode: AddressMode = AddressMode.REQUIRED,
        onCheckout: (ChargeIntent) -> Unit
    ) {
        checkoutCallback = onCheckout
        viewModel.addressMode = addressMode
        binding.customerAddressContainer.visibility =
            if (addressMode == AddressMode.HIDDEN) View.GONE else View.VISIBLE
        viewModel.loadCustomer(customerId, paymentAmount)
        binding.payButton.text = "Pay ${CurrencyFormatter.convertCentsToCurrencyString(paymentAmount)}"

        binding.googlePayBtn.configure(
            amountCents = paymentAmount,
            customerId = customerId,
            googlePayMerchantId = googlePayMerchantId,
            onResult = { result ->
                when (result) {
                    is FrameGooglePayButton.Result.Success -> checkoutCallback?.invoke(result.chargeIntent)
                    else -> {}
                }
            },
            onReadinessChanged = { isReady ->
                binding.googlePayDivider.visibility = if (isReady) View.VISIBLE else View.GONE
            }
        )
    }
}

private fun com.google.android.material.textfield.TextInputEditText.setTextIfDifferent(value: String?) {
    val newText = value.orEmpty()
    if (text?.toString() != newText) setText(newText)
}
