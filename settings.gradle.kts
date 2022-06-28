include("collapsingtoolbarlayout-subtitle")
include("sample")
include("website")

dependencyResolutionManagement {
    versionCatalogs {
        val kotlinVersion = "1.6.21"
        val androidxVersion = "1.3.0"
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
            plugin("mvn-publish", "com.vanniktech.maven.publish.base").version("0.20.0")
            plugin("jacoco", "com.vanniktech.android.junit.jacoco").version("0.16.0")
            plugin("git-publish", "org.ajoberstar.git-publish").version("3.0.1")
            library("pages", "com.hendraanggrian:pages-gradle-plugin:0.1")
        }
        register("libs") {
            library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
            library("material", "com.google.android.material:material:$androidxVersion")
            library("androidx-appcompat", "androidx.appcompat:appcompat:$androidxVersion")
            library("androidx-core-ktx", "androidx.core:core-ktx:$androidxVersion")
            library("androidx-multidex", "androidx.multidex:multidex:2.0.1")
            library("androidx-coordinatorlayout", "androidx.multidex:multidex:1.1.0")
            library("androidx-preference", "androidx.multidex:multidex:1.1.0")
            library("auto-prefs-android", "com.hendraanggrian.auto:prefs-android:0.1-SNAPSHOT")
            library("auto-prefs-compiler", "com.hendraanggrian.auto:prefs-compiler:0.1-SNAPSHOT")
            library("picasso-ktx", "com.hendraanggrian.appcompat:picasso-ktx:0.1-SNAPSHOT")
            library("process-phoenix", "com.jakewharton:process-phoenix:2.1.2")
            val colorPreferenceVersion = "1.1.0"
            library("color-preference-core", "com.github.kizitonwose.colorpreference:core:$colorPreferenceVersion")
            library(
                "color-preference-support",
                "com.github.kizitonwose.colorpreference:support:$colorPreferenceVersion"
            )
            bundle("color-preference", listOf("color-preference-core", "color-preference-support"))
        }
        register("testLibs") {
            library("junit", "androidx.test.ext:junit:1.1.3")
            library("androidx-core", "androidx.test:core:$androidxVersion")
            library("androidx-runner", "androidx.test:runner:$androidxVersion")
            library("robolectric", "org.robolectric:robolectric:4.8.1")
            library("truth", "com.google.truth:truth:1.1.3")
        }
    }
}
