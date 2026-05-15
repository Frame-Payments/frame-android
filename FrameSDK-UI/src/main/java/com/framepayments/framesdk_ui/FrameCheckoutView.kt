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
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk_ui.buttons.FrameGooglePayButton
import com.framepayments.framesdk_ui.databinding.ViewFrameCheckoutBinding
import com.framepayments.framesdk_ui.databinding.ItemPaymentMethodRowBinding
import com.framepayments.framesdk_ui.databinding.ItemPaymentNewRowBinding
import com.framepayments.framesdk_ui.snackbar.FrameSnackbarController
import com.framepayments.framesdk_ui.theme.FrameTheme
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

    var onResult: ((FrameResult) -> Unit)? = null

    /**
     * Set to true once a terminal [FrameResult] has been emitted (Completed / Failed). Used to
     * suppress a duplicate [FrameResult.Cancelled] emission on the close-button path that also
     * calls `Activity.finish()`.
     */
    private var didFinish: Boolean = false

    /// Current snackbar colors, read at emission time so [setTheme] takes effect on the next toast
    /// without requiring re-observation of the controller.
    private var toastBackgroundColor: Int =
        com.framepayments.framesdk_ui.theme.FrameColors.defaults(context).toastBackground.toArgb()
    private var toastTextColor: Int =
        com.framepayments.framesdk_ui.theme.FrameColors.defaults(context).toastText.toArgb()

    /**
     * Apply a [FrameTheme] to this checkout. Tints the pay button, forwards the primary-button
     * color to the embedded [EncryptedPaymentCardInput], and updates the colors used by the
     * transport-error snackbar. Call after construction; safe to call multiple times.
     */
    fun setTheme(theme: FrameTheme) {
        val payColor = theme.colors.primaryButton.toArgb()
        val payTextColor = theme.colors.primaryButtonText.toArgb()
        binding.payButton.setBackgroundColor(payColor)
        binding.payButton.setTextColor(payTextColor)
        binding.encryptedCardInput.accentColor = theme.colors.primaryButton

        toastBackgroundColor = theme.colors.toastBackground.toArgb()
        toastTextColor = theme.colors.toastText.toArgb()
    }

    init {
        val activity = (context as? AppCompatActivity)
            ?: throw IllegalArgumentException("FrameCheckoutView must be used in an AppCompatActivity")
        viewModel = ViewModelProvider(activity)[FrameCheckoutViewModel::class.java]

        binding.closeButton.setOnClickListener {
            if (!didFinish) {
                didFinish = true
                onResult?.invoke(FrameResult.Cancelled)
            }
            (context as Activity).finish()
        }

        // Surface transport-error snackbars emitted by the Google Pay button (and any future
        // Frame surface) without intruding on the inline server-validation error UI below.
        // Suppliers re-read the latest theme on each emission so `setTheme(...)` is live.
        FrameSnackbarController.observeWithSnackbar(
            lifecycleOwner = activity,
            anchorView = this,
            backgroundColor = { toastBackgroundColor },
            textColor = { toastTextColor },
        )

        // Pay button is disabled until the user either selects a saved payment method
        // or enters new card details that pass potential-validity. It's also force-disabled
        // while a checkout submit is in flight (driven by viewModel.isPerformingAction).
        binding.payButton.isEnabled = false
        binding.payButton.alpha = 0.4f

        fun refreshPayButtonState() {
            val canPay = viewModel.hasUsablePaymentInput.value == true
            val loading = viewModel.isPerformingAction.value == true
            binding.payButton.isEnabled = canPay && !loading
            binding.payButton.alpha = if (canPay && !loading) 1f else 0.4f
        }

        viewModel.hasUsablePaymentInput.observe(activity) { refreshPayButtonState() }
        viewModel.isPerformingAction.observe(activity) { loading ->
            binding.checkoutProgressBar.visibility = if (loading == true) View.VISIBLE else View.GONE
            refreshPayButtonState()
        }

        binding.payButton.setOnClickListener {
            viewModel.checkoutWithSelectedPaymentMethod(binding.saveCard.isChecked)
                .observe(activity) { transfer ->
                    val id = transfer?.id ?: return@observe
                    didFinish = true
                    onResult?.invoke(FrameResult.Completed(id))
                }
        }

        // The list always renders — even when there are no saved methods — because the
        // "Enter New Payment Method" row is part of the same container and is always
        // available as a selectable option.
        fun refreshNewCardVisibility() {
            val loaded = viewModel.didLoadAccountPaymentMethods.value == true
            val selected = viewModel.selectedAccountPaymentOption.value
            binding.newCardContainer.visibility =
                if (loaded && selected == null) View.VISIBLE else View.GONE
        }
        viewModel.accountPaymentOptions.observe(activity) { list ->
            renderPaymentOptions(list ?: emptyList(), viewModel.selectedAccountPaymentOption.value)
        }
        viewModel.selectedAccountPaymentOption.observe(activity) { selected ->
            renderPaymentOptions(viewModel.accountPaymentOptions.value ?: emptyList(), selected)
            refreshNewCardVisibility()
        }
        viewModel.didLoadAccountPaymentMethods.observe(activity) { refreshNewCardVisibility() }

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
    private fun renderPaymentOptions(
        options: List<FrameObjects.PaymentMethod>,
        selected: FrameObjects.PaymentMethod?
    ) {
        binding.paymentOptionsContainer.removeAllViews()
        options.forEach { option ->
            val itemBinding = ItemPaymentMethodRowBinding.inflate(
                LayoutInflater.from(context),
                binding.paymentOptionsContainer,
                false
            )
            val isACH = option.type == FrameObjects.PaymentMethodType.ACH
            if (isACH) {
                itemBinding.paymentCardIcon.setImageResource(R.drawable.ic_bank)
                itemBinding.paymentCardPrimary.text =
                    "•••• ${option.ach?.lastFour.orEmpty()}"
                val accountType = option.ach?.accountType?.name?.lowercase()
                    ?.replaceFirstChar { it.uppercase() }.orEmpty()
                itemBinding.paymentCardSecondary.text =
                    if (accountType.isEmpty()) "Account" else "$accountType Account"
            } else {
                itemBinding.paymentCardIcon.setImageResource(R.drawable.ic_card)
                itemBinding.paymentCardPrimary.text =
                    "•••• ${option.card?.lastFourDigits.orEmpty()}"
                itemBinding.paymentCardSecondary.text =
                    "Exp. ${option.card?.expirationMonth.orEmpty()}/${option.card?.expirationYear.orEmpty()}"
            }
            val isSelected = option == selected
            itemBinding.paymentCardRadio.isChecked = isSelected
            // Selected payment cards get a high-contrast border vs the surface; the
            // tokens below adapt automatically in dark mode via values-night/colors.xml.
            val color = if (isSelected)
                ContextCompat.getColor(context, R.color.frame_text_primary)
            else
                ContextCompat.getColor(context, R.color.frame_surface_stroke)
            itemBinding.paymentCardContainer.strokeColor = color

            itemBinding.root.setOnClickListener {
                viewModel.setSelectedAccountPaymentOption(option)
                viewModel.clearNewCardFieldErrors()
            }
            binding.paymentOptionsContainer.addView(itemBinding.root)
        }

        // Always append the "Enter New Payment Method" row. Active when nothing else is selected.
        val newRowBinding = ItemPaymentNewRowBinding.inflate(
            LayoutInflater.from(context),
            binding.paymentOptionsContainer,
            false
        )
        val newIsSelected = selected == null
        newRowBinding.paymentCardRadio.isChecked = newIsSelected
        val newColor = if (newIsSelected)
            ContextCompat.getColor(context, R.color.frame_text_primary)
        else
            ContextCompat.getColor(context, R.color.frame_surface_stroke)
        newRowBinding.paymentCardContainer.strokeColor = newColor
        newRowBinding.root.setOnClickListener {
            viewModel.setSelectedAccountPaymentOption(null)
        }
        binding.paymentOptionsContainer.addView(newRowBinding.root)
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

    /**
     * Configure the bundled checkout. The card path creates a `Transfer` (account-scoped),
     * and so does the embedded Google Pay button — both require [accountId]. Callers
     * needing a customer/ChargeIntent flow should use [FrameGooglePayButton] directly.
     *
     * The Google Pay merchant identifier is read from [com.framepayments.framesdk.FrameNetworking.googlePayMerchantId]
     * — pass it once at SDK init. The Google Pay row stays hidden if it isn't configured.
     */
    @JvmOverloads
    @SuppressLint("SetTextI18n")
    fun configure(
        accountId: String,
        paymentAmount: Int,
        addressMode: AddressMode = AddressMode.REQUIRED,
        onResult: (FrameResult) -> Unit,
    ) {
        require(accountId.isNotEmpty()) { "FrameCheckoutView.configure requires a non-empty accountId" }
        this.onResult = onResult
        viewModel.addressMode = addressMode
        binding.customerAddressContainer.visibility =
            if (addressMode == AddressMode.HIDDEN) View.GONE else View.VISIBLE
        viewModel.loadAccountDetails(accountId, paymentAmount)
        binding.payButton.text = "Pay ${CurrencyFormatter.convertCentsToCurrencyString(paymentAmount)}"

        binding.googlePayBtn.configure(
            amountCents = paymentAmount,
            owner = FrameGooglePayButton.Owner.Account(accountId),
            onResult = { gpResult ->
                when (gpResult) {
                    is FrameGooglePayButton.Result.Success -> {
                        // Bundled checkout is always account-scoped, so `gpResult.id` is a Transfer id.
                        didFinish = true
                        onResult.invoke(FrameResult.Completed(gpResult.id))
                    }
                    is FrameGooglePayButton.Result.Failure -> {
                        // Keep the checkout open so the user can retry Google Pay or fall through
                        // to card entry. Transport errors already toasted from inside the button;
                        // non-transport failures surface a generic message here.
                        FrameSnackbarController.emit("Error: Google Pay could not complete. Please try again or use a card.")
                    }
                    is FrameGooglePayButton.Result.Cancelled -> {
                        // User backed out of the Google Pay sheet — they're still in checkout.
                    }
                    is FrameGooglePayButton.Result.PaymentMethodCreated -> {
                        // Not produced in `Charge` mode (which is what the bundled checkout uses).
                    }
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
