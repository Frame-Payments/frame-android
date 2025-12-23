plugins {
    alias(libs.plugins.android.application)
    kotlin("android") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.framepayments.frame"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.framepayments.frame"
        targetSdk = 35
        minSdk = 26
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":FrameSDK"))
    implementation(project(":FrameSDK-UI"))
    implementation(project(":FrameSDK-Onboarding"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom.v20251201))
    implementation("androidx.compose.material3:material3")
    implementation(libs.ui)
    implementation(libs.androidx.material3)
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
}