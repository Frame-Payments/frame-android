package com.framepayments.framesdk_ui.buttons

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk_ui.snackbar.FrameSnackbarController
import com.framepayments.framesdk.chargeintents.AuthorizationMode
import com.framepayments.framesdk.chargeintents.ChargeIntentAPI
import com.framepayments.framesdk.chargeintents.ChargeIntentsRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
import com.framepayments.framesdk.transfers.TransferRequests
import com.framepayments.framesdk.transfers.TransfersAPI
import com.framepayments.framesdk.wallet.WalletAPI
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * A standalone, reusable Google Pay button that handles the full payment flow.
 *
 * Drop this into any layout to add Google Pay support. It automatically fetches
 * wallet configuration, checks device readiness, and handles the payment flow
 * through to charge intent creation.
 *
 * Usage:
 * ```kotlin
 * val googlePayButton = findViewById<FrameGooglePayButton>(R.id.googlePayButton)
 * googlePayButton.configure(
 *     amountCents = 1000,
 *     owner = FrameGooglePayButton.Owner.Account("acc_12345"),
 *     // or: owner = FrameGooglePayButton.Owner.Customer("cus_12345") for a ChargeIntent flow
 *     onResult = { result ->
 *         when (result) {
 *             is FrameGooglePayButton.Result.Success -> {
 *                 // result.id is a Transfer id (account owner) or ChargeIntent id (customer owner)
 *             }
 *             is FrameGooglePayButton.Result.Failure -> { /* handle error */ }
 *             is FrameGooglePayButton.Result.Cancelled -> { /* user cancelled */ }
 *         }
 *     },
 *     onReadinessChanged = { isReady -> /* optionally show/hide surrounding UI */ }
 * )
 * ```
 */
class FrameGooglePayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * Identifies who owns the resulting payment method and which downstream resource
     * the Charge mode creates:
     *  - [Customer] → creates a [ChargeIntent]; [Result.Success.id] is the ChargeIntent id.
     *  - [Account]  → creates a [Transfer];     [Result.Success.id] is the Transfer id.
     * Callers infer the resource type from the owner they passed in.
     */
    sealed class Owner {
        data class Customer(val id: String) : Owner()
        data class Account(val id: String) : Owner()
    }

    sealed class Result {
        /**
         * Charge mode: payment authorized and the downstream resource was created.
         * `id` is a ChargeIntent id when the owner was [Owner.Customer], or a Transfer id
         * when the owner was [Owner.Account].
         */
        data class Success(val id: String) : Result()
        /** AddToOwner mode: wallet card was attached to the customer/account as a PaymentMethod. */
        data class PaymentMethodCreated(val paymentMethod: FrameObjects.PaymentMethod) : Result()
        data class Failure(val message: String) : Result()
        data object Cancelled : Result()
    }

    /// Drives whether the Google Pay sheet completes by creating a charge (`Charge`) or
    /// only attaches the wallet card to the customer/account (`AddToOwner`).
    sealed class Mode {
        data class Charge(
            val amountCents: Int,
            val currencyCode: String = "USD",
            val owner: Owner
        ) : Mode()
        data class AddToOwner(val customerId: String?, val accountId: String?) : Mode()
    }

    private val activity: ComponentActivity = context.findComponentActivity()
        ?: throw IllegalArgumentException("FrameGooglePayButton must be hosted by a ComponentActivity (e.g. AppCompatActivity, ComposeActivity)")

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val innerButton = GooglePayButton(context)
    private val paymentsClient: PaymentsClient
    private val googlePayLauncher: ActivityResultLauncher<IntentSenderRequest>

    private var googlePayGateway: String = ""
    private var googlePayGatewayMerchantId: String = ""
    private var mode: Mode = Mode.Charge(amountCents = 0, owner = Owner.Account(""))

    private var onResult: ((Result) -> Unit)? = null
    private var onReadinessChanged: ((Boolean) -> Unit)? = null

    init {
        addView(innerButton, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        visibility = View.GONE

        innerButton.setOnClickListener { requestGooglePay() }

        val walletEnv = if (FrameNetworking.debugMode)
            WalletConstants.ENVIRONMENT_TEST
        else
            WalletConstants.ENVIRONMENT_PRODUCTION
        paymentsClient = Wallet.getPaymentsClient(
            activity,
            Wallet.WalletOptions.Builder().setEnvironment(walletEnv).build()
        )

        // `ActivityResultRegistry.register(key, ...)` is the lifecycle-free overload —
        // safe to call after the activity has reached RESUMED, which happens when this
        // View is constructed lazily inside a Compose `AndroidView` factory.
        // (`activity.registerForActivityResult(...)` enforces a pre-STARTED registration
        // and crashes here.) Each instance gets a unique key so multiple buttons hosted
        // by the same activity don't collide on the same launcher slot.
        val registryKey = "FrameGooglePayButton#${System.identityHashCode(this)}"
        googlePayLauncher = activity.activityResultRegistry.register(
            registryKey,
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    result.data?.let { intent ->
                        PaymentData.getFromIntent(intent)?.let { handleGooglePaySuccess(it) }
                    } ?: onResult?.invoke(Result.Failure("No payment data returned"))
                }
                Activity.RESULT_CANCELED -> onResult?.invoke(Result.Cancelled)
                else -> onResult?.invoke(Result.Failure("Google Pay failed with code ${result.resultCode}"))
            }
        }
    }

    override fun onDetachedFromWindow() {
        // The lifecycle-free `register(...)` overload doesn't auto-unregister on
        // activity destroy. Clean up the launcher slot when this View leaves the tree
        // so we don't leak the registry entry across recomposition / navigation.
        googlePayLauncher.unregister()
        scope.cancel()
        super.onDetachedFromWindow()
    }

    /**
     * Configures the button for a Charge flow and checks Google Pay readiness.
     *
     * The button automatically shows/hides based on whether Google Pay is available. The Google
     * Pay merchant identifier is read from [com.framepayments.framesdk.FrameNetworking.googlePayMerchantId]
     * — pass it once at SDK init. The button stays hidden if it isn't configured.
     *
     * @param amountCents The payment amount in cents (e.g., 1000 for $10.00)
     * @param owner Customer or account that owns the resulting payment method and charge.
     *              Customer owners produce a ChargeIntent id; account owners produce a Transfer id.
     * @param currencyCode ISO 4217 currency code (default: "USD")
     * @param onResult Callback invoked with the payment result
     * @param onReadinessChanged Optional callback invoked when Google Pay readiness changes
     */
    fun configure(
        amountCents: Int,
        owner: Owner,
        currencyCode: String = "USD",
        onResult: (Result) -> Unit,
        onReadinessChanged: ((Boolean) -> Unit)? = null
    ) {
        configure(
            mode = Mode.Charge(amountCents = amountCents, currencyCode = currencyCode, owner = owner),
            onResult = onResult,
            onReadinessChanged = onReadinessChanged
        )
    }

    /// Mode-aware configure. Use `Mode.Charge` for the existing checkout flow or
    /// `Mode.AddToOwner` to attach a Google Pay wallet card to a customer/account without
    /// creating a charge intent.
    fun configure(
        mode: Mode,
        onResult: (Result) -> Unit,
        onReadinessChanged: ((Boolean) -> Unit)? = null
    ) {
        this.mode = mode
        this.onResult = onResult
        this.onReadinessChanged = onReadinessChanged

        checkGooglePayReadiness()
    }

    private fun setReady(isReady: Boolean) {
        visibility = if (isReady) View.VISIBLE else View.GONE
        onReadinessChanged?.invoke(isReady)
    }

    private fun checkGooglePayReadiness() {
        if (FrameNetworking.googlePayMerchantId.isNullOrEmpty()) {
            setReady(false)
            return
        }
        scope.launch {
            val (config, error) = WalletAPI.getGooglePayConfiguration()
            if (config == null || error != null) {
                setReady(false)
                return@launch
            }
            val isProduction = config.environment?.uppercase() == "PRODUCTION"
            googlePayGateway = if (isProduction) config.processor ?: "example" else "example"
            googlePayGatewayMerchantId = if (isProduction) config.processorKey ?: "exampleGatewayMerchantId" else "exampleGatewayMerchantId"

            val request = IsReadyToPayRequest.fromJson(buildIsReadyToPayRequest().toString()) ?: run {
                setReady(false)
                return@launch
            }
            paymentsClient.isReadyToPay(request).addOnCompleteListener { task ->
                try {
                    setReady(task.getResult(ApiException::class.java))
                } catch (e: ApiException) {
                    setReady(false)
                }
            }
        }
    }

    private fun requestGooglePay() {
        val requestJson = buildPaymentDataRequest() ?: run {
            onResult?.invoke(Result.Failure("Unable to build Google Pay request"))
            return
        }
        val request = PaymentDataRequest.fromJson(requestJson.toString())
        paymentsClient.loadPaymentData(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { handleGooglePaySuccess(it) }
            } else {
                val exception = task.exception
                if (exception is ResolvableApiException) {
                    googlePayLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                } else {
                    onResult?.invoke(Result.Failure(exception?.message ?: "Google Pay failed"))
                }
            }
        }
    }

    private fun handleGooglePaySuccess(paymentData: PaymentData) {
        scope.launch(Dispatchers.IO) {
            val json = JSONObject(paymentData.toJson())

            val email: String? = if (json.has("email")) json.getString("email") else null
            val apiVersion = json.optInt("apiVersion", 2)
            val apiVersionMinor = json.optInt("apiVersionMinor", 0)
            val paymentMethodDataJson = json.optJSONObject("paymentMethodData")
                ?: run {
                    withContext(Dispatchers.Main) {
                        onResult?.invoke(Result.Failure("Invalid Google Pay response"))
                    }
                    return@launch
                }
            val paymentMethodData = jsonObjectToMap(paymentMethodDataJson)

            val walletData = PaymentMethodRequests.GooglePayWalletData(
                apiVersion = apiVersion,
                apiVersionMinor = apiVersionMinor,
                email = email,
                paymentMethodData = paymentMethodData
            )

            val ownerCustomer: String? = when (val m = mode) {
                is Mode.Charge -> (m.owner as? Owner.Customer)?.id
                is Mode.AddToOwner -> m.customerId
            }
            val ownerAccount: String? = when (val m = mode) {
                is Mode.Charge -> (m.owner as? Owner.Account)?.id
                is Mode.AddToOwner -> m.accountId
            }

            val pmRequest = PaymentMethodRequests.CreateGooglePayPaymentMethodRequest(
                wallet = PaymentMethodRequests.GooglePayWallet(googlePay = walletData),
                customer = ownerCustomer,
                account = ownerAccount
            )

            val (pm, pmError) = PaymentMethodsAPI.createGooglePayPaymentMethod(pmRequest)
            val resolvedPaymentMethod = pm ?: run {
                reportError(pmError)
                withContext(Dispatchers.Main) {
                    onResult?.invoke(Result.Failure(pmError?.message ?: "Failed to create payment method"))
                }
                return@launch
            }

            when (val m = mode) {
                is Mode.AddToOwner -> {
                    withContext(Dispatchers.Main) {
                        onResult?.invoke(Result.PaymentMethodCreated(resolvedPaymentMethod))
                    }
                    return@launch
                }
                is Mode.Charge -> when (val o = m.owner) {
                    is Owner.Customer -> {
                        // Customer owner → create a ChargeIntent. `Result.Success.id` is the ChargeIntent id.
                        val ciRequest = ChargeIntentsRequests.CreateChargeIntentRequest(
                            amount = m.amountCents,
                            currency = m.currencyCode.lowercase(),
                            customer = o.id,
                            description = null,
                            confirm = true,
                            paymentMethod = resolvedPaymentMethod.id,
                            receiptEmail = null,
                            authorizationMode = AuthorizationMode.AUTOMATIC,
                            customerData = null,
                            paymentMethodData = null
                        )
                        val (intent, ciError) = ChargeIntentAPI.createChargeIntent(ciRequest)
                        if (intent == null) reportError(ciError)
                        withContext(Dispatchers.Main) {
                            val id = intent?.id
                            if (id != null) {
                                onResult?.invoke(Result.Success(id))
                            } else {
                                onResult?.invoke(Result.Failure(ciError?.message ?: "Failed to create charge intent"))
                            }
                        }
                        return@launch
                    }
                    is Owner.Account -> {
                        // Account owner → create a Transfer. `Result.Success.id` is the Transfer id.
                        val transferRequest = TransferRequests.CreateTransferRequest(
                            amount = m.amountCents,
                            accountId = o.id,
                            currency = m.currencyCode.lowercase(),
                            sourcePaymentMethodId = resolvedPaymentMethod.id
                        )
                        val (transfer, transferError) = TransfersAPI.createTransfer(transferRequest)
                        if (transfer == null) reportError(transferError)
                        withContext(Dispatchers.Main) {
                            val id = transfer?.id
                            if (id != null) {
                                onResult?.invoke(Result.Success(id))
                            } else {
                                onResult?.invoke(Result.Failure(transferError?.message ?: "Failed to create transfer"))
                            }
                        }
                        return@launch
                    }
                }
            }
        }
    }

    /**
     * Surface a networking failure via the cross-surface toast controller. Server errors emit
     * the parsed `error_details.message`; transport errors use the generic fallback.
     */
    private fun reportError(error: NetworkingError?) {
        if (error != null) {
            FrameSnackbarController.emit(error.toastMessage())
        }
    }

    private fun jsonObjectToMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            map[key] = when (value) {
                is JSONObject -> jsonObjectToMap(value)
                is JSONArray -> jsonArrayToList(value)
                else -> value
            }
        }
        return map
    }

    private fun jsonArrayToList(array: JSONArray): List<Any> {
        val list = mutableListOf<Any>()
        for (i in 0 until array.length()) {
            val value = array.get(i)
            list.add(
                when (value) {
                    is JSONObject -> jsonObjectToMap(value)
                    is JSONArray -> jsonArrayToList(value)
                    else -> value
                }
            )
        }
        return list
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
                when (val m = mode) {
                    is Mode.Charge -> {
                        put("totalPriceStatus", "FINAL")
                        put("totalPrice", String.format("%.2f", m.amountCents / 100.0))
                        put("currencyCode", m.currencyCode)
                    }
                    is Mode.AddToOwner -> {
                        // Wallet-only: no charge happens, but Google Pay still requires
                        // transactionInfo. NOT_CURRENTLY_KNOWN omits the price from the sheet.
                        put("totalPriceStatus", "NOT_CURRENTLY_KNOWN")
                        put("currencyCode", "USD")
                    }
                }
            })
            put("merchantInfo", JSONObject().apply {
                FrameNetworking.googlePayMerchantId?.let { put("merchantId", it) }
                put("merchantName", "Frame Payments")
            })
            put("emailRequired", true)
        }
    }
}

/// Walks the [ContextWrapper] chain to find the host [ComponentActivity]. Compose's
/// `LocalContext.current` (and therefore the context handed to `AndroidView` factories)
/// is typically a themed wrapper rather than the activity itself, so a direct
/// `context as ComponentActivity` cast crashes on otherwise-valid hosts.
private fun Context.findComponentActivity(): ComponentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
