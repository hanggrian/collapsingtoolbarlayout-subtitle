plugins {
    android("application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(SDK_TARGET)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(SDK_TARGET)
        applicationId = "com.example.subtitlecollapsingtoolbarlayout"
        versionName = RELEASE_VERSION
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDir("src")
            assets.srcDir("assets")
            res.srcDir("res")
        }
    }
    buildTypes {
        all {
            buildConfigField("String", "RELEASE_WEBSITE", "\"$RELEASE_WEBSITE\"")
            buildConfigField("String", "RELEASE_ARTIFACT", "\"$RELEASE_ARTIFACT\"")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    lintOptions {
        isAbortOnError = false
    }
}

dependencies {
    implementation(kotlin("stdlib", VERSION_KOTLIN))
    implementation(project(":$RELEASE_ARTIFACT"))

    implementation(material())
    implementation(androidx("core", "core-ktx"))
    implementation(androidx("appcompat"))
    implementation(androidx("coordinatorlayout"))
    implementation(androidx("preference", "preference-ktx"))

    implementation(hendraanggrian("prefy", "prefy-android", VERSION_PREFY))
    kapt(hendraanggrian("prefy", "prefy-compiler", VERSION_PREFY))
    implementation(hendraanggrian("material", "bannerbar-ktx", VERSION_ANDROIDX))
    implementation(hendraanggrian("pikasso", "pikasso", version = VERSION_PIKASSO))
    implementation(processPhoenix())
    implementation(colorPreference("core"))
    implementation(colorPreference("support"))
}
