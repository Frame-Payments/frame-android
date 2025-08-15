plugins {
    alias(libs.plugins.android.library)
    kotlin("android") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.framepayments.framesdk_ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        compose = true
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.coil)
    implementation(project(":FrameSDK"))
    implementation(libs.evervault.core)
    implementation(libs.evervault.inputs)
    implementation(libs.evervault.enclaves)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.coil)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    coordinates("io.github.townsendeb", "framesdk_ui", "1.0.1")

    pom {
        name.set("FrameSDK-UI")
        description.set("Android UI SDK for Frame Payments.")
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

    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}