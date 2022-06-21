import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(plugs.android)
        classpath(plugs.kotlin)
        classpath(plugs.dokka)
        classpath(plugs.spotless)
        classpath(plugs.maven.publish)
        classpath(plugs.pages) { features("pages-minimal") }
        classpath(plugs.git.publish)
    }
}

allprojects {
    group = RELEASE_GROUP
    version = RELEASE_VERSION
    repositories {
        mavenCentral()
        google()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://jitpack.io")
    }
}

subprojects {
    afterEvaluate {
        val configureAndroid: BaseExtension.() -> Unit = {
            setCompileSdkVersion(sdk.versions.androidTarget.getInt())
            defaultConfig {
                minSdk = sdk.versions.androidMin.getInt()
                targetSdk = sdk.versions.androidTarget.getInt()
                version = RELEASE_VERSION
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                targetCompatibility = JavaVersion.VERSION_1_8
                sourceCompatibility = JavaVersion.VERSION_1_8
            }
            (this as ExtensionAware).extensions.find<KotlinJvmOptions>("kotlinOptions") {
                jvmTarget = JavaVersion.VERSION_1_8.toString()
            }
        }
        extensions.find<LibraryExtension> { configureAndroid() }
        extensions.find<BaseAppModuleExtension> { configureAndroid() }
        extensions.find<KotlinProjectExtension>()?.jvmToolchain {
            (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(sdk.versions.jdk.get()))
        }
    }
}
