pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.vanniktech.maven.publish") version "0.34.0"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://prove.jfrog.io/artifactory/libs-public-maven/") }
        maven { url = uri("https://maven.fpregistry.io/releases") }
        // Persona SDK is published to Maven Central, but its own repo is the authoritative
        // source and mirrors newer releases first. Required here because
        // RepositoriesMode.FAIL_ON_PROJECT_REPOS forbids per-module repository declarations.
        maven { url = uri("https://sdk.withpersona.com/android/releases") }
    }
}

rootProject.name = "frame"
include(":app")
include(":FrameSDK")
include(":FrameSDK-UI")
include(":FrameSDK-Onboarding")
