import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.plugin.use.PluginDependenciesSpec

fun DependencyHandler.android() = "com.android.tools.build:gradle:$VERSION_ANDROID_PLUGIN"
inline val PluginDependenciesSpec.`android-library` get() = id("com.android.library")
inline val PluginDependenciesSpec.`android-application` get() = id("com.android.application")

fun androidx(
    repository: String,
    module: String = repository,
    version: String = VERSION_ANDROIDX
): String = "androidx.$repository:$module:$version"

fun hendraanggrian(
    repository: String,
    module: String? = null,
    version: String = VERSION_ANDROIDX
): String = "com.hendraanggrian.$repository:${module?.let { "$repository-$it" }
    ?: repository}:$version"

fun material() = "com.google.android.material:material:$VERSION_ANDROIDX"

fun DependencyHandler.bottomsheet(module: String) = "com.flipboard:bottomsheet-$module:$VERSION_BOTTOMSHEET"

fun DependencyHandler.materialDialogs(module: String) = "com.afollestad.material-dialogs:$module:$VERSION_MATERIAL_DIALOGS"

fun DependencyHandler.truth() = "com.google.truth:truth:$VERSION_TRUTH"

fun DependencyHandler.junit() = "junit:junit:$VERSION_JUNIT"

fun DependencyHandler.ktlint() = "com.github.shyiko:ktlint:$VERSION_KTLINT"

fun DependencyHandler.anko(module: String? = null) = "org.jetbrains.anko:${module?.let { "anko-$it" }
    ?: "anko"}:$VERSION_ANKO"

fun DependencyHandler.dokka() = "org.jetbrains.dokka:dokka-android-gradle-plugin:$VERSION_DOKKA"
inline val PluginDependenciesSpec.dokka get() = id("org.jetbrains.dokka-android")

fun DependencyHandler.gitPublish() = "org.ajoberstar:gradle-git-publish:$VERSION_GIT_PUBLISH"
inline val PluginDependenciesSpec.`git-publish` get() = id("org.ajoberstar.git-publish")

fun DependencyHandler.bintrayRelease() = "com.novoda:bintray-release:$VERSION_BINTRAY_RELEASE"
inline val PluginDependenciesSpec.`bintray-release` get() = id("com.novoda.bintray-release")