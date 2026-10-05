package com.framepayments.framesdk_ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk.accounts.AccountObjects
import com.framepayments.framesdk.accountevents.AccountEventEmitter
import com.framepayments.framesdk.accountevents.AccountEventName
import com.framepayments.framesdk.accountevents.AccountEventScreen
import com.framepayments.framesdk_ui.buttons.FrameGooglePayButton
import com.framepayments.framesdk_ui.databinding.ItemPaymentMethodRowBinding
import com.framepayments.framesdk_ui.databinding.ItemPaymentNewRowBinding
import com.framepayments.framesdk_ui.databinding.ViewFrameCheckoutBinding
import com.framepayments.framesdk_ui.reusable.BillingAddressDetailView
import com.framepayments.framesdk_ui.reusable.ValidatedTextField
import com.framepayments.framesdk_ui.reusable.cardBrandIcon
import com.framepayments.framesdk_ui.snackbar.FrameSnackbarController
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.LocalFrameTheme
import com.framepayments.framesdk_ui.validation.FieldKey
import com.framepayments.framesdk_ui.validation.ValidationError
import com.framepayments.framesdk_ui.validation.Validators
import com.framepayments.framesdk_ui.viewmodels.FrameCheckoutViewModel

/**
 * Full-screen checkout surface that collects customer information, billing address, and card
 * details, then creates a Transfer against the supplied account and reports the outcome via
 * [onResult].
 */
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

    /** Callback invoked with the terminal [FrameResult] once the checkout completes, is cancelled, or fails. */
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
        // Held as Compose state so the hosted billing-address form re-themes on a later setTheme.
        composeTheme = theme

        val payColor = theme.colors.primaryButton.toArgb()
        val payTextColor = theme.colors.primaryButtonText.toArgb()
        binding.payButton.setBackgroundColor(payColor)
        binding.payButton.setTextColor(payTextColor)
        binding.encryptedCardInput.setTheme(theme)

        toastBackgroundColor = theme.colors.toastBackground.toArgb()
        toastTextColor = theme.colors.toastText.toArgb()

        applySectionHeaderStyle(binding.customerInformation, theme)
        applySectionHeaderStyle(binding.cardInformation, theme)
        applySectionHeaderStyle(binding.countryRegion, theme)
        applyCheckoutTypefaces()
    }

    private fun applySectionHeaderStyle(view: TextView, theme: FrameTheme) {
        view.setTextColor(theme.colors.textSecondary.toArgb())
        view.textSize = 17f
    }

    private fun applyCheckoutTypefaces() {
        val regular = fontOrNull(R.font.soehne_buch)
        val semiBold = fontOrNull(R.font.soehne_dreiviertelfett)
        val bold = fontOrNull(R.font.soehne_fett)
        regular?.let { binding.saveCard.typeface = it }
        bold?.let { binding.title.typeface = it }
        semiBold?.let { binding.customerInformation.typeface = it }
        semiBold?.let { binding.cardInformation.typeface = it }
        semiBold?.let { binding.countryRegion.typeface = it }
        semiBold?.let { binding.payButton.typeface = it }
    }

    private fun fontOrNull(id: Int): android.graphics.Typeface? =
        try {
            // Prefer View.resources so library-module font ids resolve against this APK merge.
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                resources.getFont(id)
            } else {
                ResourcesCompat.getFont(context, id)
            }
        } catch (_: Exception) {
            // Host/resources can fail to resolve library font ids at inflate time; keep system face.
            null
        }

    private var composeTheme by mutableStateOf(FrameTheme.default(context))
    private var customerNameState by mutableStateOf("")
    private var customerEmailState by mutableStateOf("")
    private var fieldErrorsState by mutableStateOf<Map<FieldKey, ValidationError>>(emptyMap())

    init {
        val activity = (context as? AppCompatActivity)
            ?: throw IllegalArgumentException("FrameCheckoutView must be used in an AppCompatActivity")
        viewModel = ViewModelProvider(activity)[FrameCheckoutViewModel::class.java]

        setTheme(FrameTheme.default(context))

        binding.closeButton.setOnClickListener {
            if (!didFinish) {
                didFinish = true
                AccountEventEmitter.emit(AccountEventName.CHECKOUT_CANCELLED, AccountEventScreen.PAYMENT_SHEET)
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
            viewModel.checkoutWithSelectedPaymentMethod(binding.saveCard.isChecked, context)
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

        // Shown only once the account load has settled, and only when the profile did not supply
        // a usable name and email — otherwise the customer re-types what Frame already has.
        fun refreshCustomerInfoVisibility() {
            val loaded = viewModel.didLoadAccountPaymentMethods.value == true
            val required = viewModel.customerInfoRequired.value != false
            binding.customerInfoContainer.visibility =
                if (loaded && required) View.VISIBLE else View.GONE
        }
        viewModel.customerInfoRequired.observe(activity) { refreshCustomerInfoVisibility() }
        viewModel.didLoadAccountPaymentMethods.observe(activity) { refreshCustomerInfoVisibility() }

        viewModel.customerName.observe(activity) { customerNameState = it.orEmpty() }
        viewModel.customerEmail.observe(activity) { customerEmailState = it.orEmpty() }

        binding.customerInfoCompose.setContent {
            FrameTheme(theme = composeTheme) {
                val theme = LocalFrameTheme.current
                val nameError = fieldErrorsState[FieldKey.NAME]?.let { context.getString(it.messageRes) }
                val emailError = fieldErrorsState[FieldKey.EMAIL]?.let { context.getString(it.messageRes) }
                Column(modifier = Modifier.fillMaxWidth()) {
                    ValidatedTextField(
                        value = customerNameState,
                        onValueChange = {
                            customerNameState = it
                            viewModel.customerName.value = it
                        },
                        prompt = context.getString(R.string.customer_name),
                        error = nameError,
                        onClearError = { viewModel.clearError(FieldKey.NAME) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(theme.spacing.sectionTop))
                    ValidatedTextField(
                        value = customerEmailState,
                        onValueChange = {
                            customerEmailState = it
                            viewModel.customerEmail.value = it
                        },
                        prompt = context.getString(R.string.customer_email),
                        error = emailError,
                        keyboardType = KeyboardType.Email,
                        onClearError = { viewModel.clearError(FieldKey.EMAIL) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        binding.billingAddressCompose.setContent {
            FrameTheme(theme = composeTheme) {
                BillingAddressDetailView(
                    viewModel = viewModel.billingAddress,
                    showHeader = false
                )
            }
        }

        binding.encryptedCardInput.onCardDataChange = { data ->
            viewModel.cardData = data
            viewModel.setError(FieldKey.CARD, Validators.validateCard(data))
        }

        viewModel.fieldErrors.observe(activity) { errors ->
            fieldErrorsState = errors ?: emptyMap()
            val cardErr = errors?.get(FieldKey.CARD)
            if (cardErr == null) {
                binding.cardErrorText.visibility = View.GONE
                binding.cardErrorText.text = ""
            } else {
                binding.cardErrorText.visibility = View.VISIBLE
                binding.cardErrorText.text = context.getString(cardErr.messageRes)
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
                itemBinding.paymentCardIcon.setImageResource(cardBrandIcon(option.card?.brand.orEmpty()))
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

    /**
     * Configure the bundled checkout. The card path creates a `Transfer` (account-scoped),
     * and so does the embedded Google Pay button — both require [accountId]. Callers
     * needing a customer/ChargeIntent flow should use [FrameGooglePayButton] directly.
     *
     * The Google Pay merchant identifier is read from [com.framepayments.framesdk.FrameNetworking.googlePayMerchantId]
     * — pass it once at SDK init. The Google Pay row stays hidden if it isn't configured.
     *
     * @param account Account fetched on the host's backend with `sk_`. Prefills name and email.
     *   When null and the SDK was initialized with a secret key, checkout fetches the account.
     * @param paymentMethods Saved methods fetched on the host's backend with `sk_`. When null and
     *   the SDK was initialized with a secret key, checkout fetches the list.
     */
    @JvmOverloads
    @SuppressLint("SetTextI18n")
    fun configure(
        accountId: String,
        paymentAmount: Int,
        addressMode: AddressMode = AddressMode.REQUIRED,
        account: AccountObjects.Account? = null,
        paymentMethods: List<FrameObjects.PaymentMethod>? = null,
        onResult: (FrameResult) -> Unit,
    ) {
        require(accountId.isNotEmpty()) { "FrameCheckoutView.configure requires a non-empty accountId" }
        this.onResult = onResult
        viewModel.addressMode = addressMode
        binding.customerAddressContainer.visibility =
            if (addressMode == AddressMode.HIDDEN) View.GONE else View.VISIBLE
        viewModel.loadAccountDetails(accountId, paymentAmount, account, paymentMethods)
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
