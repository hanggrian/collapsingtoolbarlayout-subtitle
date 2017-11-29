import org.gradle.api.artifacts.dsl.DependencyHandler

const val bintrayUser = "hendraanggrian"
const val bintrayGroup = "com.hendraanggrian"
const val bintrayArtifact = "collapsingtoolbarlayout-subtitle"
const val bintrayDesc = "Standard CollapsingToolbarLayout with subtitle support"
const val bintrayWeb = "https://github.com/hendraanggrian/collapsingtoolbarlayout-subtitle"

const val minSdk = 14
const val targetSdk = 27
const val buildTools = "27.0.1"

const val kotlinVersion = "1.1.60"
const val kotaVersion = "0.19"
const val supportVersion = "27.0.1"
const val errorbarVersion = "0.3.0"

const val junitVersion = "4.12"
const val truthVersion = "0.36"
const val runnerVersion = "1.0.1"
const val espressoVersion = "3.0.1"

fun DependencyHandler.support(module: Any, version: Any, vararg groupSuffixes: Any) = "${StringBuilder("com.android.support").apply { groupSuffixes.forEach { append(".$it") } }}:$module:$version"
fun DependencyHandler.hendraanggrian(module: Any, version: Any) = "com.hendraanggrian:$module:$version"
fun DependencyHandler.junit(version: Any) = "junit:junit:$version"
fun DependencyHandler.google(module: Any, version: Any) = "com.google.$module:$module:$version"