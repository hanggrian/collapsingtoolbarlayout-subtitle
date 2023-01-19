plugins {
    alias(libs.plugins.android.application)
    kotlin("android") version libs.versions.kotlin
    kotlin("android.extensions") version libs.versions.kotlin
    kotlin("kapt") version libs.versions.kotlin
}

android {
    defaultConfig {
        minSdk = 23
        applicationId = "com.example.collapsingtoolbarlayoutsubtitle"
        multiDexEnabled = true
    }
    lint.abortOnError = false
}

// hotfix: duplicate class androidx.lifecycle.viewmodel
// https://stackoverflow.com/questions/69817925/problem-duplicate-class-androidx-lifecycle-viewmodel-found-in-modules
configurations.all {
    exclude("androidx.lifecycle", "lifecycle-viewmodel-ktx")
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
