import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(plugs.android)
    }
}

plugins {
    alias(plugs.plugins.kotlin.android) apply false
    alias(plugs.plugins.kotlin.android.extensions) apply false
    alias(plugs.plugins.kotlin.kapt) apply false
}

allprojects {
    group = RELEASE_GROUP
    version = RELEASE_VERSION
    repositories {
        mavenCentral()
        google()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io/")
    }
}

subprojects {
    withPlugin<LibraryPlugin> {
        configure<LibraryExtension>(::androidConfig)
    }
    withPlugin<AppPlugin> {
        configure<BaseAppModuleExtension>(::androidConfig)
    }
    withPluginEagerly<KotlinAndroidPluginWrapper> {
        kotlinExtension.jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(sdk.versions.jdk.get()))
        }
        (the<BaseExtension>() as ExtensionAware).extensions.getByType<KotlinJvmOptions>()
            .jvmTarget = JavaVersion.toVersion(sdk.versions.androidJdk.get()).toString()
    }
}

fun androidConfig(extension: BaseExtension) {
    extension.setCompileSdkVersion(sdk.versions.androidTarget.get().toInt())
    extension.defaultConfig {
        minSdk = sdk.versions.androidMin.get().toInt()
        targetSdk = sdk.versions.androidTarget.get().toInt()
        version = RELEASE_VERSION
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    extension.compileOptions {
        targetCompatibility = JavaVersion.toVersion(sdk.versions.androidJdk.get())
        sourceCompatibility = JavaVersion.toVersion(sdk.versions.androidJdk.get())
    }
}
