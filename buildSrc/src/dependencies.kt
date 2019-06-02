import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.plugin.use.PluginDependenciesSpec

fun DependencyHandler.android() = "com.android.tools.build:gradle:3.5.0-beta03"
fun PluginDependenciesSpec.android(submodule: String) = id("com.android.$submodule")

fun DependencyHandler.androidx(
    repository: String,
    module: String = repository,
    version: String = VERSION_ANDROIDX
) = "androidx.$repository:$module:$version"

fun DependencyHandler.material(
    version: String = VERSION_ANDROIDX
) = "com.google.android.material:material:$version"

fun DependencyHandler.hendraanggrian(
    repository: String,
    module: String = repository,
    version: String
) = "com.hendraanggrian.$repository:$module:$version"

fun DependencyHandler.jakeWharton(
    module: String,
    version: String
) = "com.jakewharton:$module:$version"

fun DependencyHandler.truth() = "com.google.truth:truth:0.44"

fun DependencyHandler.bintray() = "com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4"
inline val PluginDependenciesSpec.bintray get() = id("com.jfrog.bintray")

fun DependencyHandler.bintrayRelease() = "com.novoda:bintray-release:0.9.1"
inline val PluginDependenciesSpec.`bintray-release` get() = id("com.novoda.bintray-release")
