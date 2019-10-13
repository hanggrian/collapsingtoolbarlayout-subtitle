import org.gradle.model.internal.core.ModelNodes.all

plugins {
    android("application")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(SDK_TARGET)
    defaultConfig {
        minSdkVersion(SDK_MIN)
        targetSdkVersion(SDK_TARGET)
        applicationId = "$RELEASE_GROUP.demo"
        versionCode = 1
        versionName = VERSION_ANDROIDX
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src")
            assets.srcDirs("assets")
            res.srcDir("res")
            resources.srcDir("src")
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

    implementation(material("$VERSION_ANDROIDX-beta01"))
    implementation(androidx("core", "core-ktx", VERSION_ANDROIDX))
    implementation(androidx("appcompat", version = VERSION_ANDROIDX))
    implementation(androidx("coordinatorlayout", version = "$VERSION_ANDROIDX-beta01"))
    implementation(androidx("preference", "preference-ktx", VERSION_ANDROIDX))

    implementation(hendraanggrian("material", "errorbar-ktx", "$VERSION_ANDROIDX-beta01"))
    implementation(hendraanggrian("pikasso", "pikasso", version = VERSION_PIKASSO))
    implementation(jakewharton("process-phoenix", VERSION_PROCESSPHOENIX))
}
