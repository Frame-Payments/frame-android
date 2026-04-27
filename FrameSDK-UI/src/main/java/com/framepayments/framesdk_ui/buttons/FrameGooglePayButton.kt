package com.framepayments.framesdk_ui.buttons

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.framepayments.framesdk.FrameNetworking
import com.framepayments.framesdk.chargeintents.AuthorizationMode
import com.framepayments.framesdk.chargeintents.ChargeIntent
import com.framepayments.framesdk.chargeintents.ChargeIntentAPI
import com.framepayments.framesdk.chargeintents.ChargeIntentsRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodRequests
import com.framepayments.framesdk.paymentmethods.PaymentMethodsAPI
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
 *     customerId = "cus_12345",
 *     onResult = { result ->
 *         when (result) {
 *             is FrameGooglePayButton.Result.Success -> { /* handle charge intent */ }
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

    sealed class Result {
        data class Success(val chargeIntent: ChargeIntent) : Result()
        data class Failure(val message: String) : Result()
        data object Cancelled : Result()
    }

    private val activity: AppCompatActivity = (context as? AppCompatActivity)
        ?: throw IllegalArgumentException("FrameGooglePayButton must be used in an AppCompatActivity")

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val innerButton = GooglePayButton(context)
    private val paymentsClient: PaymentsClient
    private val googlePayLauncher: ActivityResultLauncher<IntentSenderRequest>

    private var googlePayGateway: String = ""
    private var googlePayGatewayMerchantId: String = ""
    private var googlePayMerchantId: String? = null
    private var amountCents: Int = 0
    private var customerId: String? = null
    private var currencyCode: String = "USD"

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

        googlePayLauncher = activity.registerForActivityResult(
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

    /**
     * Configures the button and checks Google Pay readiness.
     *
     * The button automatically shows/hides based on whether Google Pay is available.
     *
     * @param amountCents The payment amount in cents (e.g., 1000 for $10.00)
     * @param customerId Optional Frame customer ID to associate the payment with
     * @param currencyCode ISO 4217 currency code (default: "USD")
     * @param googlePayMerchantId Optional Google Pay merchant ID override
     * @param onResult Callback invoked with the payment result
     * @param onReadinessChanged Optional callback invoked when Google Pay readiness changes
     */
    fun configure(
        amountCents: Int,
        customerId: String? = null,
        currencyCode: String = "USD",
        googlePayMerchantId: String? = null,
        onResult: (Result) -> Unit,
        onReadinessChanged: ((Boolean) -> Unit)? = null
    ) {
        this.amountCents = amountCents
        this.customerId = customerId
        this.currencyCode = currencyCode
        this.googlePayMerchantId = googlePayMerchantId
        this.onResult = onResult
        this.onReadinessChanged = onReadinessChanged

        checkGooglePayReadiness()
    }

    private fun setReady(isReady: Boolean) {
        visibility = if (isReady) View.VISIBLE else View.GONE
        onReadinessChanged?.invoke(isReady)
    }

    private fun checkGooglePayReadiness() {
        scope.launch {
            val (config, error) = WalletAPI.getGooglePayConfiguration()
            if (config == null || error != null) {
                setReady(false)
                return@launch
            }
            val isProduction = config.environment.uppercase() == "PRODUCTION"
            googlePayGateway = if (isProduction) config.processor else "example"
            googlePayGatewayMerchantId = if (isProduction) config.processorKey else "exampleGatewayMerchantId"

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
            val pmRequest = PaymentMethodRequests.CreateGooglePayPaymentMethodRequest(
                wallet = PaymentMethodRequests.GooglePayWallet(googlePay = walletData),
                customer = customerId
            )

            val (pm, pmError) = PaymentMethodsAPI.createGooglePayPaymentMethod(pmRequest)
            val pmId = pm?.id ?: run {
                withContext(Dispatchers.Main) {
                    onResult?.invoke(Result.Failure(pmError?.message ?: "Failed to create payment method"))
                }
                return@launch
            }

            val ciRequest = ChargeIntentsRequests.CreateChargeIntentRequest(
                amount = amountCents,
                currency = currencyCode.lowercase(),
                customer = customerId,
                description = "",
                paymentMethod = pmId,
                confirm = true,
                receiptEmail = null,
                authorizationMode = AuthorizationMode.AUTOMATIC,
                customerData = null,
                paymentMethodData = null,
                fraudSignals = null,
                sonarSessionId = FrameNetworking.currentSonarSessionId()
            )
            val (intent, ciError) = ChargeIntentAPI.createChargeIntent(ciRequest)

            withContext(Dispatchers.Main) {
                if (intent != null) {
                    onResult?.invoke(Result.Success(intent))
                } else {
                    onResult?.invoke(Result.Failure(ciError?.message ?: "Failed to create charge"))
                }
            }
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
                put("totalPriceStatus", "FINAL")
                put("totalPrice", String.format("%.2f", amountCents / 100.0))
                put("currencyCode", currencyCode)
            })
            put("merchantInfo", JSONObject().apply {
                googlePayMerchantId?.let { put("merchantId", it) }
                put("merchantName", "Frame Payments")
            })
            put("emailRequired", true)
        }
    }
}
