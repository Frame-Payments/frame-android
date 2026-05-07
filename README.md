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

The SDK ships with a `FrameTheme` system that lets you customize the colors, fonts, and corner radii used by every Frame UI component (checkout, cart, onboarding flow). The same set of tokens exists on iOS, so a single brand spec drives both platforms.

`FrameTheme` has three sections:

- **`colors`** — 15 tokens (`primaryButton`, `primaryButtonText`, `secondaryButton`, `secondaryButtonText`, `disabledButton`, `disabledButtonStroke`, `disabledButtonText`, `surface`, `surfaceStroke`, `textPrimary`, `textSecondary`, `error`, `onboardingHeaderBackground`, `onboardingProgressFilledOnBrand`, `onboardingProgressEmptyOnBrand`).
- **`fonts`** — 8 `TextStyle` tokens (`title`, `heading`, `headline`, `body`, `bodySmall`, `label`, `caption`, `button`).
- **`radii`** — 3 corner-radius tokens (`small` 8dp, `medium` 10dp, `large` 16dp).

### Defaults & dark mode

`FrameTheme.default()` reads its colors from the `frame_*` color resources shipped in `FrameSDK-UI`. Dark variants live in `values-night/colors.xml`, so toggling the system theme swaps colors automatically with no code changes. Font defaults map to the host app's `MaterialTheme.typography` slots with weight overrides applied for visual parity with iOS.

### Customizing the theme

Use Kotlin's `copy()` to override only the tokens you care about. Everything you don't touch keeps its default:

```kotlin
val brandTheme = FrameTheme.default().let { base ->
    base.copy(
        colors = base.colors.copy(
            primaryButton = Color(0xFF7B61FF),
            primaryButtonText = Color.White
        ),
        radii = base.radii.copy(medium = 14.dp)
    )
}
```

### Applying the theme — onboarding (Compose)

Pass the theme through `OnboardingConfig`. The container wraps content automatically:

```kotlin
OnboardingContainerView(
    config = OnboardingConfig(
        requiredCapabilities = listOf(Capabilities.KYC),
        theme = brandTheme
    ),
    onResult = { /* ... */ }
)
```

Or wrap your own UI hierarchy with the `FrameTheme { ... }` composable for fine-grained control:

```kotlin
FrameTheme(theme = brandTheme) {
    MyCheckoutFlow()  // any SDK Composables inside read brandTheme
}
```

### Applying the theme — checkout / cart (Views)

Both `FrameCheckoutView` and `FrameCartView` accept a theme via `setTheme()`:

```kotlin
val cart = findViewById<FrameCartView>(R.id.cart)
cart.setTheme(brandTheme)
cart.configure(/* ... */)
```

### Reading the theme in custom UI

Inside your own Composables hosted under `FrameTheme { ... }`, read the active theme via `LocalFrameTheme.current`:

```kotlin
@Composable
fun MyHeader() {
    val theme = LocalFrameTheme.current
    Text(text = "Welcome", color = theme.colors.textPrimary, style = theme.fonts.heading)
}
```

A working example with a custom theme is in [`PlaygroundScreen.kt`](app/src/main/java/com/framepayments/frame/PlaygroundScreen.kt).

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
