plugins {
    id("com.android.application")
    alias(plugs.plugins.kotlin.android)
    alias(plugs.plugins.kotlin.android.extensions)
    alias(plugs.plugins.kotlin.kapt)
}

android {
    defaultConfig {
        applicationId = "com.example.subtitlecollapsingtoolbarlayout"
        multiDexEnabled = true
    }
    buildTypes {
        all {
            buildConfigField("String", "RELEASE_URL", "\"$RELEASE_URL\"")
            buildConfigField("String", "RELEASE_ARTIFACT", "\"$RELEASE_ARTIFACT\"")
        }
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
