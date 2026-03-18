# Keep public SDK entry points
-keep public class com.framepayments.frameonboarding.classes.** { public *; }
-keep public class com.framepayments.frameonboarding.viewmodels.** { public *; }

# Keep data classes used for Gson serialization (have @SerializedName fields)
-keep class com.framepayments.frameonboarding.networking.** { *; }
-keepclassmembers class com.framepayments.frameonboarding.networking.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Gson @SerializedName enum values from being renamed
-keepclassmembers enum com.framepayments.frameonboarding.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Preserve line numbers for stack traces in release builds
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
