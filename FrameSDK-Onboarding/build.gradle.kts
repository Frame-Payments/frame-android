import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    alias(libs.plugins.android.library)
    kotlin("android") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.vanniktech.maven.publish")
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
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

    // Coil for image loading
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Plaid Link SDK
    implementation(libs.plaid.link)

    // Persona Inquiry SDK (government-ID identity verification for the no-SSN path)
    implementation(libs.persona.inquiry)

    // Prove Auth SDK
    implementation(libs.proveauth)

    // Coroutines for ProveAuthService suspend API
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // libphonenumber for E.164 validation and AsYouTypeFormatter
    implementation(libs.libphonenumber)
}

mavenPublishing {
    // AGP's own javaDocReleaseGeneration task bundles a pre-ASM9 Dokka that cannot read the
    // PermittedSubclasses attribute emitted for the Persona SDK's sealed classes. Skip it and
    // attach the Dokka plugin's javadoc jar (see dokkaJavadocJar below) instead.
    configure(AndroidSingleVariantLibrary(variant = "release", publishJavadocJar = false))

    coordinates("com.framepayments", "framesdk_onboarding", project.findProperty("SDK_VERSION") as String? ?: "unspecified")

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
detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    source.setFrom("src/main/java")
    autoCorrect = false
}

tasks.named("dokkaHtml") {
    outputs.upToDateWhen { false }
}

// Maven Central requires a javadoc jar. AGP's generator can't parse the Persona SDK's sealed
// classes, so build it from Dokka, which can. Paired with publishJavadocJar = false above.
val dokkaJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

afterEvaluate {
    publishing.publications.named<MavenPublication>("maven") {
        artifact(dokkaJavadocJar)
    }
}

// CI: print per-test pass/fail names instead of the silent default. Helps surface
// which API tests actually ran when reviewing the Android CI workflow logs.
tasks.withType<Test> {
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}
