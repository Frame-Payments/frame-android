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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.wallet.WalletAPI
import com.framepayments.framesdk_ui.databinding.ViewFrameCheckoutBinding
import com.framepayments.framesdk_ui.databinding.ItemPaymentCardBinding
import com.framepayments.framesdk_ui.viewmodels.AvailableCountries
import com.framepayments.framesdk_ui.viewmodels.FrameCheckoutViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

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

    private lateinit var paymentsClient: PaymentsClient
    private lateinit var googlePayLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var googlePayMerchantId: String? = null
    private var googlePayGateway: String = ""
    private var googlePayGatewayMerchantId: String = ""
    private val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        val activity = (context as? AppCompatActivity)
            ?: throw IllegalArgumentException("FrameCheckoutView must be used in an AppCompatActivity")
        viewModel = ViewModelProvider(activity)[FrameCheckoutViewModel::class.java]

        binding.closeButton.setOnClickListener { (context as Activity).finish() }
//        binding.applePayBtn.setOnClickListener { viewModel.payWithApplePay() }
        binding.googlePayBtn.setOnClickListener { requestGooglePay() }

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

        binding.encryptedCardInput.onCardDataChange = { data -> viewModel.cardData = data }

        val walletEnv = if (FrameNetworking.debugMode)
            WalletConstants.ENVIRONMENT_TEST
        else
            WalletConstants.ENVIRONMENT_PRODUCTION
        paymentsClient = Wallet.getPaymentsClient(
            activity,
            Wallet.WalletOptions.Builder().setEnvironment(walletEnv).build()
        )

        googlePayLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let { handleGooglePaySuccess(it) }
                }
            }
        }

        viewModel.isGooglePayReady.observe(activity) { isReady ->
            binding.googlePayBtn.visibility = if (isReady) View.VISIBLE else View.GONE
            binding.googlePayDivider.visibility = if (isReady) View.VISIBLE else View.GONE
        }

        viewModel.googlePayChargeIntent.observe(activity) { intent ->
            intent?.let { checkoutCallback?.invoke(it) }
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
        googlePayMerchantId: String? = null,
        onCheckout: (ChargeIntent) -> Unit
    ) {
        checkoutCallback = onCheckout
        this.googlePayMerchantId = googlePayMerchantId
        viewModel.loadCustomerPaymentMethods(customerId, paymentAmount)
        binding.payButton.text = "Pay ${CurrencyFormatter.convertCentsToCurrencyString(paymentAmount)}"
        checkGooglePayReadiness()
    }

    private fun checkGooglePayReadiness() {
        viewScope.launch {
            val (config, error) = WalletAPI.getGooglePayConfiguration()
            if (config == null || error != null) {
                viewModel.setGooglePayReadiness(false)
                return@launch
            }
            val isProduction = config.environment.uppercase() == "PRODUCTION"
            googlePayGateway = if (isProduction) config.processor else "example"
            googlePayGatewayMerchantId = if (isProduction) config.processorKey else "exampleGatewayMerchantId"

            val request = IsReadyToPayRequest.fromJson(buildIsReadyToPayRequest().toString()) ?: return@launch
            paymentsClient.isReadyToPay(request).addOnCompleteListener { task ->
                try {
                    viewModel.setGooglePayReadiness(task.getResult(ApiException::class.java))
                } catch (e: ApiException) {
                    viewModel.setGooglePayReadiness(false)
                }
            }
        }
    }

    private fun requestGooglePay() {
        val request = PaymentDataRequest.fromJson(buildPaymentDataRequest()?.toString() ?: return)
        paymentsClient.loadPaymentData(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { handleGooglePaySuccess(it) }
            } else {
                val exception = task.exception
                if (exception is ResolvableApiException) {
                    googlePayLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                }
            }
        }
    }

    private fun handleGooglePaySuccess(paymentData: PaymentData) {
        viewModel.payWithGooglePay(paymentData.toJson())
    }

    private fun buildIsReadyToPayRequest(): JSONObject {
        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "CARD")
                    put("parameters", JSONObject().apply {
                        put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
                        put("allowedCardNetworks", JSONArray(listOf("AMEX", "DISCOVER", "MASTERCARD", "VISA")))
                    })
                })
            })
        }
    }

    private fun buildPaymentDataRequest(): JSONObject? {
        if (googlePayGateway.isEmpty() || googlePayGatewayMerchantId.isEmpty()) return null
        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "CARD")
                    put("parameters", JSONObject().apply {
                        put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
                        put("allowedCardNetworks", JSONArray(listOf("AMEX", "DISCOVER", "MASTERCARD", "VISA")))
                        put("billingAddressRequired", true)
                        put("billingAddressParameters", JSONObject().apply {
                            put("format", "FULL")
                        })
                    })
                    put("tokenizationSpecification", JSONObject().apply {
                        put("type", "PAYMENT_GATEWAY")
                        put("parameters", JSONObject().apply {
                            put("gateway", googlePayGateway)
                            put("gatewayMerchantId", googlePayGatewayMerchantId)
                        })
                    })
                })
            })
            put("transactionInfo", JSONObject().apply {
                put("totalPriceStatus", "FINAL")
                put("totalPrice", String.format("%.2f", viewModel.amount / 100.0))
                put("currencyCode", "USD")
            })
            put("merchantInfo", JSONObject().apply {
                googlePayMerchantId?.let { put("merchantId", it) }
                put("merchantName", "Frame Payments")
            })
            put("emailRequired", true)
        }
    }
}