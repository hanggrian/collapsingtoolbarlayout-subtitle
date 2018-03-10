import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.plugin.use.PluginDependenciesSpec

const val releaseUser = "hendraanggrian"
const val releaseGroup = "com.$releaseUser"
const val releaseArtifact = "collapsingtoolbarlayout-subtitle"
const val releaseDesc = "Standard CollapsingToolbarLayout with subtitle support"
const val releaseWeb = "https://github.com/$releaseUser/$releaseArtifact"

const val minSdk = 14
const val targetSdk = 27
const val buildTools = "27.0.3"

const val kotlinVersion = "1.2.30"
const val kotaVersion = "0.19"
const val supportVersion = "27.1.0"
const val errorbarVersion = "0.3.0"

const val runnerVersion = "1.0.1"
const val espressoVersion = "3.0.1"

fun DependencyHandler.android() = "com.android.tools.build:gradle:3.0.1"
inline val PluginDependenciesSpec.`android-library` get() = id("com.android.library")
inline val PluginDependenciesSpec.`android-application` get() = id("com.android.application")

fun DependencyHandler.dokka() = "org.jetbrains.dokka:dokka-android-gradle-plugin:0.9.16"
inline val PluginDependenciesSpec.dokka get() = id("org.jetbrains.dokka-android")

fun DependencyHandler.gitPublish() = "org.ajoberstar:gradle-git-publish:0.3.3"
inline val PluginDependenciesSpec.`git-publish` get() = id("org.ajoberstar.git-publish")

fun DependencyHandler.bintrayRelease() = "com.novoda:bintray-release:0.8.0"
inline val PluginDependenciesSpec.`bintray-release` get() = id("com.novoda.bintray-release")

fun DependencyHandler.ktlint() = "com.github.shyiko:ktlint:0.19.0"

fun DependencyHandler.support(module: String, version: String, vararg groupSuffixes: String) =
    "${StringBuilder("com.android.support").apply {
        groupSuffixes.forEach { append(".$it") }
    }}:$module:$version"

fun DependencyHandler.hendraanggrian(module: String, version: String) = "com.hendraanggrian:$module:$version"

fun DependencyHandler.junit() = "junit:junit:4.12"

fun DependencyHandler.truth() = "com.google.truth:truth:0.39"
