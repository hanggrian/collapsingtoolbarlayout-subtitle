plugins {
    alias(libs.plugins.android.application)
    kotlin("android") version libs.versions.kotlin
    kotlin("android.extensions") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
}

android {
    defaultConfig {
        minSdk = 23
        applicationId = "com.example.subtitlecollapsingtoolbarlayout"
        multiDexEnabled = true
    }
    lint.abortOnError = false
}

dependencies {
    implementation(project(":$RELEASE_ARTIFACT"))
    implementation(libs.material)
    implementation(libs.androidx.multidex)
    implementation(libs.process.phoenix)
    implementation(libs.auto.prefs.android)
    kapt(libs.auto.prefs.compiler)
    implementation(libs.bundles.color.preference)
}
