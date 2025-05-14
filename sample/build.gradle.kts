import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val releaseArtifact: String by project

val jdkVersion = JavaLanguageVersion.of(libs.versions.jdk.get())
val jreVersion = JavaLanguageVersion.of(libs.versions.jre.get())

plugins {
    kotlin("android") version libs.versions.kotlin
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktlint)
}

kotlin.jvmToolchain(jdkVersion.asInt())

ktlint.version.set(libs.versions.ktlint.get())

android {
    namespace = "com.example"
    defaultConfig {
        minSdk = 23
        applicationId = "com.example"
        multiDexEnabled = true
    }
}

// hotfix: duplicate class androidx.lifecycle.viewmodel
// https://stackoverflow.com/questions/69817925/problem-duplicate-class-androidx-lifecycle-viewmodel-found-in-modules
configurations.all {
    exclude("androidx.lifecycle", "lifecycle-viewmodel-ktx")
}

dependencies {
    implementation(project(":$releaseArtifact"))
    implementation(libs.material)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.roundedimageview)
    implementation(libs.process.phoenix)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions
        .jvmTarget
        .set(JvmTarget.fromTarget(JavaVersion.toVersion(jreVersion).toString()))
}
