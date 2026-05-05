plugins {
    alias(libs.plugins.android.library)
    kotlin("android") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.framepayments.frameonboarding"
    compileSdk {
        version = release(36)
    }
    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables.useSupportLibrary = true
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
    buildFeatures {
        compose = true
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
    implementation(libs.evervault.inputs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    // Activity Compose for permission launcher
    implementation(libs.androidx.activity.compose)
    
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // Coil for image loading
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Plaid Link SDK
    implementation(libs.plaid.link)

    // Prove Auth SDK
    implementation(libs.proveauth)

    // Coroutines for ProveAuthService suspend API
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // libphonenumber for E.164 validation and AsYouTypeFormatter
    implementation(libs.libphonenumber)
}

mavenPublishing {
    coordinates("com.framepayments", "framesdk_onboarding", "2.0.2")

    pom {
        name.set("FrameSDK-Onboarding")
        description.set("Android Onboarding SDK for Frame Payments.")
        url.set("https://github.com/Frame-Payments/frame-android")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("frame")
                name.set("Frame Payments")
                email.set("engineering@framepayments.com")
            }
        }

        scm {
            url.set("https://github.com/Frame-Payments/frame-android")
            connection.set("scm:git:git://github.com/Frame-Payments/frame-android.git")
            developerConnection.set("scm:git:ssh://git@github.com/Frame-Payments/frame-android.git")
        }
    }

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}