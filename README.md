# Frame Android SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.framepayments/framesdk.svg?label=Maven%20Central)](https://search.maven.org/artifact/com.framepayments/framesdk)
[![Maven Central](https://img.shields.io/maven-central/v/com.framepayments/framesdk_ui.svg?label=Maven%20Central)](https://search.maven.org/artifact/com.framepayments/framesdk_ui)  

The Frame Android SDK simplifies the process of creating a seamless payment experience within your Android app. It offers versatile, pre-designed UI components that enable you to effortlessly gather payment details from your users. Additionally, it provides access to the underlying APIs that drive these components, allowing you to design fully customized payment workflows tailored to your app's needs.

## 📋 Requirements

- **Minimum Android Version**: 26 (Android 8.0, Oreo)  
- **Kotlin**: 2.2+  
- A valid **Frame API key** from your [Frame Payments Dashboard](https://framepayments.com)


## ✨ Features

- **Reusable UI Components** We have built two ready-to-use UI components for your payment to make it easy to allow customers to check out with your products and enter their payment details with encryption.

- **Pre Packaged API Calls** This SDK has built in support for all available Frame API calls. Each API endpoint has it's own initialization-less class that you can call directly within your code and supports either async/await or completion handlers. See our API Documentation here.

- **Payment Card Encryption** Frame supports payment card encryption using Evervault. It's integrated directly into our CREATE payment method API call to ensure all data transmitted is encrypted before hitting our servers.

## 🚀 Getting Started


1. **Add dependency** in your `build.gradle` (app-level):

```gradle
dependencies {
    implementation "com.framepayments:framesdk:<latest-version>"
    implementation "com.framepayments:framesdk_ui:<latest-version>"
}
```

2. **Initialize Frame** in your `Application` class:

```kotlin
import com.framepayments.framesdk.FrameNetworking

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FrameNetworking.initializeWithAPIKey(
            context = this,
            secretKey = "sk_your_secret_key_here",
            publishableKey = "pk_your_publishable_key_here",
            googlePayMerchantId = "BCR2DN4T...your-merchant-id...", // optional, see Google Pay section
            debug = false
        )
    }
}
```

> **Google Pay merchant ID:** Pass it once to `initializeWithAPIKey` and every Frame surface — `FrameGooglePayButton`, `FrameCheckoutView`, the onboarding wallet attach button — picks it up automatically. Skip it if you don't intend to surface Google Pay; the button hides itself when not configured.

## 📖 Examples

### 1. Load Customer Payment Methods
```kotlin
viewModel.loadCustomerPaymentMethods(
    customerId = "cus_12345",
    amount = 1000
)
```

### 2. Present Checkout View
```kotlin
val checkoutView = FrameCheckoutView(context)
checkoutView.amount = 1000
checkoutView.customerId = "cus_12345"
```

### 3. Refund a Charge
```kotlin
val (refund, error) = RefundsAPI.createRefund(request: { 
    chargeId = "ch_67890",
    amount = 500
  }
)
```

## 🎨 Theming

Every Frame UI surface — checkout, cart, the full onboarding flow, and the encrypted card input — reads its colors, typography, and corner radii from a single immutable `FrameTheme` data class. Override the tokens you care about; the rest keep their defaults. The same token set exists on iOS, so one brand spec drives both platforms.

### What's customizable

`FrameTheme` is a data class with three sections, each a data class itself so you can use Kotlin's `copy()` for partial overrides:

| Section | Tokens |
| --- | --- |
| **`colors`** (15) | `primaryButton`, `primaryButtonText`, `secondaryButton`, `secondaryButtonText`, `disabledButton`, `disabledButtonStroke`, `disabledButtonText`, `surface`, `surfaceStroke`, `textPrimary`, `textSecondary`, `error`, `onboardingHeaderBackground`, `onboardingProgressFilledOnBrand`, `onboardingProgressEmptyOnBrand` |
| **`fonts`** (8) | `title`, `heading`, `headline`, `body`, `bodySmall`, `label`, `caption`, `button` (`androidx.compose.ui.text.TextStyle` for each) |
| **`radii`** (3) | `small` (8dp), `medium` (10dp), `large` (16dp) |

### Defaults & dark mode

`FrameTheme.default()` reads colors from `frame_*` resources shipped in `FrameSDK-UI`. Dark variants live in [`values-night/colors.xml`](FrameSDK-UI/src/main/res/values-night/colors.xml), so toggling the system theme swaps colors automatically — including inside the `FrameCheckoutView`, `FrameCartView`, and the embedded Evervault card input. **You don't need to do anything special for dark mode**; it works out of the box.

Font defaults derive from `MaterialTheme.typography` with weight overrides applied (e.g., `heading` → `headlineMedium` + `SemiBold`) so headlines render at the same visual weight as iOS.

### Building a custom theme

Use Kotlin's built-in `copy()` to override only what you need:

```kotlin
import com.framepayments.framesdk_ui.theme.FrameTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val brandTheme = FrameTheme.default().let { base ->
    base.copy(
        colors = base.colors.copy(
            primaryButton = Color(0xFF7B61FF),
            primaryButtonText = Color.White,
            onboardingHeaderBackground = Color(0xFF7B61FF)
        ),
        radii = base.radii.copy(medium = 14.dp)
    )
}
```

Anything you don't touch (fonts, untouched colors, untouched radii) keeps the SDK default and continues to track dark mode automatically.

### Applying the theme

#### Option A — via `OnboardingConfig` (recommended for the onboarding flow)

`OnboardingConfig` has an optional `theme` field. The onboarding container wraps its content in your theme automatically:

```kotlin
OnboardingContainerView(
    config = OnboardingConfig(
        requiredCapabilities = listOf(Capabilities.KYC),
        theme = brandTheme   // null → FrameTheme.default()
    ),
    onResult = { /* ... */ }
)
```

### Configuring Screen Visibility

`OnboardingConfig` exposes two booleans to control which bookend screens are shown:

| Field | Default | Description |
|---|---|---|
| `showIntroScreen` | `true` | Show the "Verify Your Identity" welcome screen before the first step. Set to `false` to skip it and open directly on the first capability step. |
| `showCompletionScreen` | `true` | Show the "Verification Submitted" confirmation screen after the last step. Set to `false` to complete the flow immediately and finish without the final screen. |

```kotlin
// Skip both bookend screens — jump straight into the flow and complete silently
OnboardingContainerView(
    config = OnboardingConfig(
        requiredCapabilities = listOf(Capabilities.KYC_PREFILL, Capabilities.BANK_ACCOUNT_VERIFICATION),
        showIntroScreen = false,
        showCompletionScreen = false
    ),
    onResult = { result ->
        // fires as soon as the last step is done
    }
)
```

#### Option B — `FrameTheme { ... }` composable (for any Compose tree)

Wrap any Compose UI that hosts SDK Composables — useful when you compose your own checkout flow on top of the SDK's primitives:

```kotlin
FrameTheme(theme = brandTheme) {
    MyCheckoutScreen() // every SDK Composable inside reads brandTheme
}
```

#### Option C — `setTheme()` on Views-based components

`FrameCheckoutView`, `FrameCartView`, and `EncryptedPaymentCardInput` are Android `View`s, so they expose imperative `setTheme(...)`:

```kotlin
val cart = findViewById<FrameCartView>(R.id.cart)
cart.setTheme(brandTheme)
cart.configure(customerId, items, shippingCents) { total -> /* ... */ }
```

`setTheme` can be called before or after `configure()` — calling it later applies the new theme to the already-configured view.

### Reading the theme in your own Composables

Inside any Composable hosted under `FrameTheme { ... }` (or under `OnboardingContainerView`), read the active theme via `LocalFrameTheme.current`:

```kotlin
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

@Composable
fun MyBrandedHeader() {
    val theme = LocalFrameTheme.current
    Text(
        text = "Welcome",
        color = theme.colors.textPrimary,
        style = theme.fonts.heading
    )
}
```

> **⚠️ Heads-up:** `LocalFrameTheme.current` throws if no theme is provided. This is intentional — silent fallbacks let custom themes drift unnoticed. If you see "FrameTheme not provided", wrap the call site in `FrameTheme { ... }`.

### Previewing in light + dark

Use the `@FrameThemePreviews` multi-preview annotation to render Android Studio previews in both light and dark side-by-side:

```kotlin
import com.framepayments.framesdk_ui.theme.FrameTheme
import com.framepayments.framesdk_ui.theme.FrameThemePreviews

@FrameThemePreviews
@Composable
private fun MyBrandedHeaderPreview() {
    FrameTheme {
        MyBrandedHeader()
    }
}
```

### Reusable themed components

When you build your own checkout / payout UI, these reusable Composables ship with the SDK and consume `LocalFrameTheme` automatically — no extra wiring required:

- **`ContinueButton(text, style = ContinueButtonStyle.PRIMARY)`** — primary action button. Pass `ContinueButtonStyle.SECONDARY` for the outlined-brand variant.
- **`ValidatedTextField`** / **`PhoneNumberTextField`** — themed inputs with inline error rendering.
- **`PaymentCardForm`** — manual card-entry form (no encryption; use `EncryptedPaymentCardInput` for the Evervault-backed input).
- **`BankAccountDetailView`** / **`BillingAddressDetailView`** / **`CustomerInformationView`** — preassembled themed forms bound to view models.
- **`CountryPickerSheet`** — modal country picker, OFAC-restricted countries filtered by default.
- **`FramePaymentButton(paymentOption = PaymentButtonOption.GOOGLE)`** — branded Google Pay pill (visual only; for the full charge flow use `FrameGooglePayButton`).
- **`TermsOfServiceView`** — themed legalese with clickable Privacy Policy / Terms links.

### Theme contract — enforced in CI

A pre-merge lint blocks any code or layout from drifting away from the theme system. The lint catches:

- `Color(0x…)`, `Color.White`, `Color.Black`, `Color.Gray`, `Color.Red`, `Color.Transparent` literals in SDK Compose code
- `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `MaterialTheme.shapes.*` reads (use `LocalFrameTheme.current.…` instead)
- `RoundedCornerShape(N.dp)` literals (use `theme.radii.{small,medium,large}`)
- `fontSize = N.sp` literals (use `style = theme.fonts.<token>`)
- Hardcoded hex colors and `@android:color/{white,black}` references in SDK XML layouts

Run `./scripts/theme-lint.sh` locally; it runs in CI as a fast-fail gate before any build job.

### Working example

A live custom-theme integration is in [`PlaygroundScreen.kt`](app/src/main/java/com/framepayments/frame/PlaygroundScreen.kt) — runs an orange-themed onboarding flow against the demo's playground.

## 💳 Google Pay

The Frame Android SDK supports Google Pay as a payment method for merchants using FluidPay or Coinflow as their processor.

Google Pay setup is a three-part process: get a merchant ID from Google, declare the wallet capability in your `AndroidManifest.xml`, pass the merchant ID to the SDK at init. Once those are done, **let us know** (see [Enabling Google Pay on your account](#enabling-google-pay-on-your-account)) and we'll flip the feature on for your business.

### 1. Obtain a Google Pay merchant ID

1. Sign up for a [Google Pay & Wallet Console](https://pay.google.com/business/console/) account.
2. Complete the business profile and agree to the Google Pay API Terms of Service.
3. Your **Merchant ID** (looks like `BCR2DN4T…`) is displayed on the Business Console home page once approved.

### 2. Declare the Google Pay capability in your manifest

Add the following inside `<application>` in your app's `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.gms.wallet.api.enabled"
    android:value="true" />
```

Without this entry, the Google Pay button stays hidden on the user's device — the Wallet API is opted-out by default.

### 3. Pass the merchant ID to the SDK at init

`FrameNetworking` is the single source of truth for the Google Pay merchant ID. Pass it once when you initialize the SDK and every Frame surface — `FrameGooglePayButton`, the bundled `FrameCheckoutView`, the onboarding wallet attach button — will use it automatically.

```kotlin
FrameNetworking.initializeWithAPIKey(
    context = this,
    secretKey = "sk_your_secret_key_here",
    publishableKey = "pk_your_publishable_key_here",
    googlePayMerchantId = "BCR2DN4T...your-merchant-id..."
)
```

### Enabling Google Pay on your account

Once steps 1–3 are complete, contact Frame at [support@framepayments.com](mailto:support@framepayments.com) (or via your [Frame Payments Dashboard](https://framepayments.com)) and we'll enable Google Pay on your account. Google Pay charges won't succeed until this is done on our side.

### How it works

- The SDK automatically detects mobile requests and bypasses the domain verification step required for web integrations.
- In development/test mode (`debug = true` at init), Google Pay runs in `ENVIRONMENT_TEST` with example gateway credentials.
- In production, live processor credentials are used automatically based on your merchant configuration.

### Result types

Checkout flows resolve through the shared `FrameResult` sealed class:

```kotlin
sealed class FrameResult {
    data class Completed(val id: String) : FrameResult()   // Transfer id for checkout/cart
    data object Cancelled : FrameResult()
    data class Failed(val error: Throwable) : FrameResult()
}
```

## 🔒 Privacy & Security

- Frame **never stores raw card details** on-device or in your app.  
- All sensitive data is encrypted using [Evervault](https://evervault.com) before transmission.  
- The SDK is PCI-DSS compliant by design.  
- You are responsible for handling customer authentication and complying with local regulations (e.g., SCA, PSD2).  

### Backup & device transfer

The SDK persists two SharedPreferences files that should not travel with Auto Backup or device-to-device transfer:

- `config_store_encrypted` — sealed by an AndroidKeyStore master key that stays on the original device. Restoring it elsewhere leaves the SDK unable to decrypt its own config.
- `sonar_sessions` — server-issued, device-scoped charge-session IDs that become stale on restore.

If your host app sets `android:allowBackup="true"`, reference the SDK's rule files from your `<application>`:

```xml
<application
    android:allowBackup="true"
    android:fullBackupContent="@xml/frame_sdk_backup_rules"
    android:dataExtractionRules="@xml/frame_sdk_data_extraction_rules">
```

If you already maintain your own backup rule files, merge the `<exclude>` entries from [`frame_sdk_backup_rules.xml`](FrameSDK/src/main/res/xml/frame_sdk_backup_rules.xml) and [`frame_sdk_data_extraction_rules.xml`](FrameSDK/src/main/res/xml/frame_sdk_data_extraction_rules.xml) into them. If your app uses `android:allowBackup="false"` (like the playground in this repo), no action is needed.

## 🛠 Support

- [Documentation](https://docs.framepayments.com)  
- [Issues](https://github.com/Frame-Payments/frame-android/issues)
- [Privacy Policy](https://framepayments.com/privacy)
- Email: support@framepayments.com
