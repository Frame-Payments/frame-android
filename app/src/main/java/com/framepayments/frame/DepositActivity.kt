package com.framepayments.frame

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.framepayments.framesdk_ui.FrameDepositView
import kotlinx.coroutines.launch

/**
 * Example path for deposit: amount screen, then the existing [CheckoutActivity].
 *
 * The host mints `chk_sess_` after the user confirms an amount, because that session locks it.
 */
class DepositActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val accountId = intent.getStringExtra("accountId").orEmpty()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    DepositExample(
                        accountId = accountId,
                        onAccountResolved = { resolved ->
                            setResult(RESULT_CANCELED, Intent().putExtra("accountId", resolved))
                        },
                        onClose = { finish() },
                    )
                }
            }
        }
    }
}

@Composable
private fun DepositExample(
    accountId: String,
    onAccountResolved: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: ContentViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var resolvedAccountId by rememberSaveable { mutableStateOf(accountId) }
    var isContinuing by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    var mismatch by remember { mutableStateOf(false) }
    val demoAlert by viewModel.demoAlert.collectAsState()

    LaunchedEffect(resolvedAccountId) {
        if (resolvedAccountId.isNotBlank()) onAccountResolved(resolvedAccountId)
    }

    val checkoutLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isContinuing = false
        if (result.resultCode == Activity.RESULT_OK) onClose()
    }

    FrameDepositView(
        accountId = resolvedAccountId,
        isContinuing = isContinuing,
        onContinue = { cents ->
            if (isContinuing) return@FrameDepositView
            isContinuing = true
            val thisAttempt = attempt
            scope.launch {
                var launched = false
                try {
                    val secret = viewModel.mintCheckoutClientSecret(
                        resolvedAccountId.takeIf { it.isNotBlank() },
                        amountCents = cents,
                    )
                    viewModel.accountId.value.takeIf { it.isNotBlank() }?.let { resolvedAccountId = it }
                    if (thisAttempt != attempt || secret == null) return@launch
                    if (secret.amountCents != cents) {
                        mismatch = true
                        return@launch
                    }
                    val intent = Intent(context, CheckoutActivity::class.java).apply {
                        putExtra("totalCents", cents)
                        putExtra("accountId", resolvedAccountId)
                        putExtra("checkoutClientSecret", secret.clientSecret)
                        putExtra("checkoutClientSecretExpiresAt", secret.expiresAt)
                        putExtra("checkoutAmountCents", cents)
                        secret.amountCurrency?.let { putExtra("checkoutAmountCurrency", it) }
                    }
                    launched = true
                    checkoutLauncher.launch(intent)
                } finally {
                    if (!launched && thisAttempt == attempt) isContinuing = false
                }
            }
        },
        onCancel = {
            attempt += 1
            onClose()
        },
    )

    if (mismatch) {
        AlertDialog(
            onDismissRequest = { mismatch = false },
            title = { Text("Deposit") },
            text = { Text("Checkout session amount did not match.") },
            confirmButton = {
                TextButton(onClick = { mismatch = false }) { Text("OK") }
            },
        )
    }

    demoAlert?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearDemoAlert() },
            title = { Text(message.title) },
            text = { Text(message.body) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDemoAlert() }) { Text("OK") }
            },
        )
    }
}
