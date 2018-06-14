import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.plugin.use.PluginDependenciesSpec

fun DependencyHandler.android() = "com.android.tools.build:gradle:$VERSION_ANDROID_PLUGIN"
inline val PluginDependenciesSpec.`android-library` get() = id("com.android.library")
inline val PluginDependenciesSpec.`android-application` get() = id("com.android.application")

fun DependencyHandler.androidKTX() = "androidx.core:core-ktx:$VERSION_ANDROID_KTX"

fun DependencyHandler.bottomsheet(module: String) = "com.flipboard:bottomsheet-$module:$VERSION_BOTTOMSHEET"

fun DependencyHandler.support(module: String, version: String, vararg suffixes: String) =
    "${StringBuilder("com.android.support").apply {
        suffixes.forEach { append(".$it") }
    }}:$module:$version"

fun DependencyHandler.hendraanggrian(module: String, version: String) = "com.hendraanggrian:$module:$version"

fun DependencyHandler.truth() = "com.google.truth:truth:$VERSION_TRUTH"

fun DependencyHandler.junit() = "junit:junit:$VERSION_JUNIT"

fun DependencyHandler.ktlint() = "com.github.shyiko:ktlint:$VERSION_KTLINT"

fun DependencyHandler.dokkaAndroid() = "org.jetbrains.dokka:dokka-android-gradle-plugin:$VERSION_DOKKA"
inline val PluginDependenciesSpec.`dokka-android` get() = id("org.jetbrains.dokka-android")

fun DependencyHandler.gitPublish() = "org.ajoberstar:gradle-git-publish:$VERSION_GIT_PUBLISH"
inline val PluginDependenciesSpec.`git-publish` get() = id("org.ajoberstar.git-publish")

fun DependencyHandler.bintrayRelease() = "com.novoda:bintray-release:$VERSION_BINTRAY_RELEASE"
inline val PluginDependenciesSpec.`bintray-release` get() = id("com.novoda.bintray-release")