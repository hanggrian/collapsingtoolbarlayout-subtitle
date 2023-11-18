pluginManagement.repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}
dependencyResolutionManagement.repositories {
    mavenCentral()
    google()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io/")
}

rootProject.name = "collapsingtoolbarlayout-subtitle"

include("collapsingtoolbarlayout-subtitle")
include("sample")
include("website")
