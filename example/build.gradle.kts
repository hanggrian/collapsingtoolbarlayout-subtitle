import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(targetSdk)
    buildToolsVersion(buildTools)
    defaultConfig {
        minSdkVersion(minSdk)
        targetSdkVersion(targetSdk)
        applicationId = "com.example.collapsingtoolbarlayoutsubtitle"
        versionCode = 1
        versionName = "1.0"
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src")
            res.srcDir("res")
            resources.srcDir("src")
        }
    }
    buildTypes {
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
    implementation(project(":collapsingtoolbarlayout-subtitle"))
    implementation(kotlin("stdlib", kotlinVersion))

    implementation(support("design", supportVersion))
    implementation(support("appcompat-v7", supportVersion))

    implementation(hendraanggrian("kota-design", kotaVersion))
    implementation(hendraanggrian("kota-appcompat-v7", kotaVersion))

    implementation("com.afollestad.material-dialogs:core:0.9.4.5") {
        exclude(group = "com.android.support")
    }
}