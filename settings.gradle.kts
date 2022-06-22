include("collapsingtoolbarlayout-subtitle")
include("sample")
include("website")

dependencyResolutionManagement {
    versionCatalogs {
        val kotlinVersion = "1.6.21"
        val androidxVersion = "1.2.0"
        register("sdk") {
            version("jdk", "11")
            version("androidMin", "14")
            version("androidTarget", "32")
        }
        register("plugs") {
            library("android", "com.android.tools.build:gradle:7.2.1")
            library("kotlin", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
            library("dokka", "org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
            library("spotless", "com.diffplug.spotless:spotless-plugin-gradle:6.7.2")
            library("maven-publish", "com.vanniktech:gradle-maven-publish-plugin:0.20.0")
            library("pages", "com.hendraanggrian:pages-gradle-plugin:0.1")
            library("git-publish", "org.ajoberstar.git-publish:gradle-git-publish:3.0.1")
        }
        register("libs") {
            library("kotlinx-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
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
            library("color-preference-support", "com.github.kizitonwose.colorpreference:support:$colorPreferenceVersion")
            bundle("color-preference", listOf("color-preference-core", "color-preference-support"))
        }
        register("testLibs") {
            library("kotlin-junit", "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
            library("androidx-core-ktx", "androidx.test:core-ktx:$androidxVersion")
            library("androidx-runner", "androidx.test:runner:$androidxVersion")
            library("androidx-rules", "androidx.test:rules:$androidxVersion")
            library("androidx-junit-ktx", "androidx.test.ext:junit-ktx:1.1.3")
            library("androidx-truth", "androidx.test.ext:truth:1.4.0")
            library("androidx-espresso-core", "androidx.test.espresso:espresso-core:3.4.0")
            bundle(
                "androidx",
                listOf(
                    "androidx-core-ktx", "androidx-runner", "androidx-rules",
                    "androidx-junit-ktx", "androidx-truth", "androidx-espresso-core"
                )
            )
        }
    }
}
