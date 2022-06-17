plugins {
    android("application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdk = SDK_TARGET
    defaultConfig {
        minSdk = SDK_MIN
        targetSdk = SDK_TARGET
        applicationId = "com.example.subtitlecollapsingtoolbarlayout"
        versionName = RELEASE_VERSION
        multiDexEnabled = true
    }
    sourceSets {
        named("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDir("src")
            assets.srcDir("assets")
            res.srcDir("res")
            resources.srcDir("src")
        }
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
        isAbortOnError = false
    }
}

dependencies {
    implementation(kotlin("stdlib", VERSION_KOTLIN))
    implementation(project(":$RELEASE_ARTIFACT"))
    implementation(material())
    implementation(androidx("multidex", version = VERSION_MULTIDEX))
    implementation(androidx("core", "core-ktx"))
    implementation(androidx("appcompat"))
    androidTestImplementation(androidx("coordinatorlayout", version = "1.1.0"))
    implementation(androidx("preference", "preference-ktx", "1.1.1"))
    implementation(hendraanggrian("auto", "prefs-android", VERSION_PREFS))
    kapt(hendraanggrian("auto", "prefs-compiler", VERSION_PREFS))
    implementation(hendraanggrian("material", "bannerbar-ktx", "$VERSION_ANDROIDX-SNAPSHOT"))
    implementation(hendraanggrian("appcompat", "picasso-ktx", VERSION_PICASSOKTX))
    implementation(processPhoenix())
    implementation(colorPreference("core", "1.1.0"))
    implementation(colorPreference("support", "1.1.0"))
}
