# Rules applied to consuming apps that depend on FrameSDK-Onboarding.
# Keep all public SDK types so host apps can use them even when minification is enabled.

# Public API: config, results, capabilities, and step types
-keep public class com.framepayments.frameonboarding.classes.OnboardingConfig { public *; }
-keep public class com.framepayments.frameonboarding.classes.OnboardingResult { public *; }
-keep public class com.framepayments.frameonboarding.classes.OnboardingResult$* { public *; }
-keep public class com.framepayments.frameonboarding.classes.OnboardingStep { public *; }
-keep public class com.framepayments.frameonboarding.classes.OnboardingStep$* { public *; }
-keep public enum com.framepayments.frameonboarding.classes.Capabilities { *; }
-keep public class com.framepayments.frameonboarding.classes.PaymentMethodDetails { public *; }
-keep public class com.framepayments.frameonboarding.classes.PayoutMethodDetails { public *; }

# Public reusable composables
-keep public class com.framepayments.frameonboarding.reusable.BillingAddressFormKt { public *; }
-keep public class com.framepayments.frameonboarding.reusable.BankAccountFormKt { public *; }
-keep public class com.framepayments.frameonboarding.reusable.PaymentCardFormKt { public *; }
-keep public class com.framepayments.frameonboarding.reusable.CustomerInformationFormKt { public *; }
-keep public class com.framepayments.frameonboarding.reusable.TermsOfServiceViewKt { public *; }

# Public theme tokens
-keep public class com.framepayments.frameonboarding.theme.ColorsKt { public *; }

# Preserve Gson serialization for networking models
-keep class com.framepayments.frameonboarding.networking.** { *; }
-keepclassmembers class com.framepayments.frameonboarding.networking.** {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers enum com.framepayments.frameonboarding.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve line numbers in crash reports
-keepattributes SourceFile,LineNumberTable
