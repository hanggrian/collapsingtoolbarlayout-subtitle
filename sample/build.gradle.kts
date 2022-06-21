plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
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
        named("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":$RELEASE_ARTIFACT"))
    implementation(libs.material)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.preference)
    implementation(libs.auto.prefs.android)
    kapt(libs.auto.prefs.compiler)
    implementation(libs.picasso.ktx)
    implementation(libs.process.phoenix)
    implementation(libs.bundles.color.preference)
}
