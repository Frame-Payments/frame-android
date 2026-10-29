package com.framepayments.frameonboarding.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.framepayments.framesdk.FrameObjects
import com.framepayments.framesdk.FrameResult
import com.framepayments.framesdk.NetworkingError
import com.framepayments.framesdk_ui.CurrencyFormatter
import com.framepayments.framesdk_ui.reusable.appendAmountDigit
import com.framepayments.framesdk_ui.reusable.deleteAmountDigit

/** Amount and destination state for [com.framepayments.frameonboarding.views.FramePayoutView]. */
internal class FramePayoutViewModel(
    availableAmount: Int,
    currencyCode: String = "usd",
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    var availableAmount: Int by mutableIntStateOf(maxOf(0, availableAmount))
        private set
    private var currencyCode: String by mutableStateOf(
        currencyCode.trim().ifEmpty { "usd" },
    )

    var typedCents: Int by mutableIntStateOf(0)
        private set
    var payoutMethods: List<FrameObjects.PaymentMethod> by mutableStateOf(emptyList())
        private set
    var selectedPaymentMethodId: String? by mutableStateOf(null)
        private set
    var isCreating: Boolean by mutableStateOf(false)
    var isLoadingMethods: Boolean by mutableStateOf(false)
    var didLoadMethods: Boolean by mutableStateOf(false)
        private set
    var didFinish: Boolean by mutableStateOf(false)
        private set
    var submitIdempotencyKey: String? = savedState[IDEM_KEY]
        private set(value) {
            field = value
            savedState[IDEM_KEY] = value
        }
    var submitIdempotencyFingerprint: String? = savedState[IDEM_FINGERPRINT]
        private set(value) {
            field = value
            savedState[IDEM_FINGERPRINT] = value
        }

    var onResult: ((FrameResult) -> Unit)? = null

    val isAmountInRange: Boolean
        get() = typedCents > 0 && typedCents <= availableAmount

    val selectedPaymentMethod: FrameObjects.PaymentMethod?
        get() = selectedPaymentMethodId?.let { id -> payoutMethods.firstOrNull { it.id == id } }

    val canConfirm: Boolean
        get() = isAmountInRange && selectedPaymentMethod != null && !isLoadingMethods

    val amountText: String
        get() = formattedAmount(typedCents)

    val availableText: String
        get() = formattedAmount(availableAmount)

    fun formattedAmount(cents: Int): String =
        CurrencyFormatter.convertCentsToCurrencyString(cents, currencyCode)

    fun updateAvailableAmount(amount: Int, currencyCode: String? = null) {
        availableAmount = maxOf(0, amount)
        if (!currencyCode.isNullOrEmpty()) {
            this.currencyCode = currencyCode
        }
        if (typedCents > availableAmount) {
            typedCents = availableAmount
        }
    }

    fun isQuickAmountAvailable(cents: Int): Boolean = cents > 0 && cents <= availableAmount

    fun applyQuickAmount(cents: Int) {
        if (!isQuickAmountAvailable(cents)) return
        typedCents = cents
    }

    fun replaceTypedCents(cents: Int) {
        typedCents = cents
    }

    fun appendDigit(digit: Int) {
        typedCents = appendAmountDigit(digit, typedCents, availableAmount)
    }

    fun deleteDigit() {
        typedCents = deleteAmountDigit(typedCents)
    }

    fun applyLoadedMethods(methods: List<FrameObjects.PaymentMethod>, primaryId: String?) {
        val banks = methods.filter { it.ach != null }
        payoutMethods = banks
        didLoadMethods = true
        val current = selectedPaymentMethodId
        if (current != null && banks.any { it.id == current }) return
        if (primaryId != null && banks.any { it.id == primaryId }) {
            selectedPaymentMethodId = primaryId
            return
        }
        selectedPaymentMethodId = banks.singleOrNull()?.id
    }

    fun selectElected(id: String) {
        if (id.isEmpty()) return
        selectedPaymentMethodId = id
    }

    fun rememberIdempotencyKey(cents: Int, methodId: String): String {
        val fingerprint = "$cents|$methodId"
        if (submitIdempotencyFingerprint != fingerprint || submitIdempotencyKey == null) {
            submitIdempotencyKey = java.util.UUID.randomUUID().toString()
            submitIdempotencyFingerprint = fingerprint
        }
        return submitIdempotencyKey!!
    }

    fun rotateIdempotencyKeyIfNeeded(error: NetworkingError) {
        if (!shouldRotateIdempotency(error)) return
        submitIdempotencyKey = null
        submitIdempotencyFingerprint = null
    }

    fun finish(result: FrameResult) {
        if (didFinish) return
        didFinish = true
        if (result is FrameResult.Completed) {
            submitIdempotencyKey = null
            submitIdempotencyFingerprint = null
        }
        onResult?.invoke(result)
    }

    override fun onCleared() {
        if (!didFinish && !isCreating) {
            didFinish = true
            onResult?.invoke(FrameResult.Cancelled)
        }
    }

    companion object {
        val quickAmounts = listOf(1_000, 2_500, 5_000, 10_000)
        internal const val IDEM_KEY = "submitIdempotencyKey"
        internal const val IDEM_FINGERPRINT = "submitIdempotencyFingerprint"

        fun shouldRotateIdempotency(error: NetworkingError): Boolean {
            val code = (error as? NetworkingError.ServerError)?.statusCode ?: return false
            if (code == 408 || code == 429) return false
            if (code in 500..599) return false
            return code in 400..499
        }
    }
}
