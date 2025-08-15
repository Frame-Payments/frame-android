val sdkVersion = "1.0.1"

plugins {
    alias(libs.plugins.android.library)
    kotlin("android") version "2.2.0"
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.framepayments.framesdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "SDK_VERSION", "\"${sdkVersion}\"")
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
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.evervault.core)
    implementation(libs.evervault.inputs)
    implementation(libs.evervault.enclaves)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.gson)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    coordinates("io.github.townsendeb", "framesdk", sdkVersion)

    pom {
        name.set("FrameSDK")
        description.set("Android SDK for Frame Payments.")
        url.set("https://github.com/Frame-Payments/frame-android")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("eric")
                name.set("Eric Townsend")
                email.set("eric@framepayments.com")
            }
        }

        scm {
            url.set("https://github.com/Frame-Payments/frame-android")
            connection.set("scm:git:git://github.com/Frame-Payments/frame-android.git")
            developerConnection.set("scm:git:ssh://git@github.com/Frame-Payments/frame-android.git")
        }
    }

    publishToMavenCentral(automaticRelease = false)
    signAllPublications()
}