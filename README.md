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
        FrameNetworking.initializeWithAPIKey(context = this, key="your_api_key_here")
    }
}
```

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

### Obtaining a Google Pay Merchant ID

To accept Google Pay payments, you need a Google Pay Merchant ID:

1. Sign up for a [Google Pay & Wallet Console](https://pay.google.com/business/console/) account
2. Complete the business profile and agree to the Google Pay API Terms of Service
3. Your **Merchant ID** is displayed on the Business Console home page once approved
4. Provide this Merchant ID when configuring Google Pay through the [Frame Payments Dashboard](https://framepayments.com)

### How It Works

- The SDK automatically detects mobile requests and bypasses domain verification required for web integrations
- In development/test mode, Google Pay runs in `TEST` environment with example gateway credentials
- In production, live processor credentials are used automatically based on your merchant configuration

## 🔒 Privacy & Security

- Frame **never stores raw card details** on-device or in your app.  
- All sensitive data is encrypted using [Evervault](https://evervault.com) before transmission.  
- The SDK is PCI-DSS compliant by design.  
- You are responsible for handling customer authentication and complying with local regulations (e.g., SCA, PSD2).  

## 🛠 Support

- [Documentation](https://docs.framepayments.com)  
- [Issues](https://github.com/Frame-Payments/frame-android/issues)
- [Privacy Policy](https://framepayments.com/privacy)
- Email: support@framepayments.com
