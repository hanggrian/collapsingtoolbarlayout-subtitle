include("collapsingtoolbarlayout-subtitle")
include("sample")
include("website")

dependencyResolutionManagement {
    versionCatalogs {
        val kotlinVersion = "1.7.0"
        val androidxVersion = "1.6.0"
        register("sdk") {
            version("jdk", "11")
            version("androidJdk", "8")
            version("androidMin", "14")
            version("androidTarget", "32")
        }
        register("plugs") {
            library("android", "com.android.tools.build:gradle:7.2.1")
            plugin("kotlin-android", "org.jetbrains.kotlin.android").version(kotlinVersion)
            plugin("kotlin-android-extensions", "org.jetbrains.kotlin.android.extensions").version(kotlinVersion)
            plugin("kotlin-kapt", "org.jetbrains.kotlin.kapt").version(kotlinVersion)
            plugin("dokka", "org.jetbrains.dokka").version(kotlinVersion)
            plugin("spotless", "com.diffplug.spotless").version("6.7.2")
            plugin("maven-publish", "com.vanniktech.maven.publish.base").version("0.20.0")
            plugin("git-publish", "org.ajoberstar.git-publish").version("3.0.1")
            plugin("pages", "com.hendraanggrian.pages").version("0.1")
        }
        register("libs") {
            library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
            library("material", "com.google.android.material:material:$androidxVersion")
            library("androidx-appcompat", "androidx.appcompat:appcompat:$androidxVersion")
            library("androidx-core-ktx", "androidx.core:core-ktx:$androidxVersion")
            library("androidx-multidex", "androidx.multidex:multidex:2.0.1")
            library("process-phoenix", "com.jakewharton:process-phoenix:2.1.2")
            val autoPrefsVersion = "0.1-SNAPSHOT"
            library("auto-prefs-android", "com.hendraanggrian.auto:prefs-android:$autoPrefsVersion")
            library("auto-prefs-compiler", "com.hendraanggrian.auto:prefs-compiler:$autoPrefsVersion")
            val colorPreferenceVersion = "1.1.0"
            library("color-preference-core", "com.github.kizitonwose.colorpreference:core:$colorPreferenceVersion")
            library(
                "color-preference-support",
                "com.github.kizitonwose.colorpreference:support:$colorPreferenceVersion"
            )
            bundle("color-preference", listOf("color-preference-core", "color-preference-support"))
        }
        register("testLibs") {
            val androidxTestVersion = "1.5.0-alpha01"
            library("androidx-core", "androidx.test:core:$androidxTestVersion")
            library("androidx-runner", "androidx.test:runner:$androidxTestVersion")
            library("androidx-junit", "androidx.test.ext:junit:1.1.3")
            library("robolectric", "org.robolectric:robolectric:4.8.1")
            library("truth", "com.google.truth:truth:1.1.3")
        }
    }
}
