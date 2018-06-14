import org.gradle.kotlin.dsl.kotlin
import org.gradle.language.base.plugins.LifecycleBasePlugin.*
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    `android-library`
    kotlin("android")
    dokka
    `git-publish`
    `bintray-release`
}

android {
    compileSdkVersion(SDK_TARGET)
    buildToolsVersion(BUILD_TOOLS)
    defaultConfig {
        minSdkVersion(SDK_MIN)
        targetSdkVersion(SDK_TARGET)
        versionName = VERSION_SUPPORT
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src")
            res.srcDir("res")
            resources.srcDir("src")
        }
        getByName("androidTest") {
            setRoot("tests")
            manifest.srcFile("tests/AndroidManifest.xml")
            java.srcDir("tests/src")
            res.srcDir("tests/res")
            resources.srcDir("tests/src")
        }
    }
    libraryVariants.all {
        generateBuildConfig?.enabled = false
    }
}

val ktlint by configurations.creating

dependencies {
    api(kotlin("stdlib", VERSION_KOTLIN))
    implementation(support("design", VERSION_SUPPORT))

    testImplementation(junit())
    testImplementation(truth())
    androidTestImplementation(truth())
    androidTestImplementation(hendraanggrian("errorbar", VERSION_SUPPORT))
    androidTestImplementation(support("espresso-core", VERSION_ESPRESSO, "test", "espresso"))
    androidTestImplementation(support("runner", VERSION_RUNNER, "test"))
    androidTestImplementation(support("rules", VERSION_RULES, "test"))

    ktlint(ktlint())
}

tasks {
    "ktlint"(JavaExec::class) {
        get("check").dependsOn(this)
        group = VERIFICATION_GROUP
        inputs.dir("src")
        outputs.dir("src")
        description = "Check Kotlin code style."
        classpath = ktlint
        main = "com.github.shyiko.ktlint.Main"
        args("--android", "src/**/*.kt")
    }
    "ktlintFormat"(JavaExec::class) {
        group = "formatting"
        inputs.dir("src")
        outputs.dir("src")
        description = "Fix Kotlin code style deviations."
        classpath = ktlint
        main = "com.github.shyiko.ktlint.Main"
        args("--android", "-F", "src/**/*.kt")
    }

    val dokka by getting(DokkaTask::class) {
        outputDirectory = "$buildDir/docs"
        doFirst { file(outputDirectory).deleteRecursively() }
    }
    gitPublish {
        repoUri = RELEASE_WEBSITE
        branch = "gh-pages"
        contents.from(dokka.outputDirectory)
    }
    get("gitPublishCopy").dependsOn(dokka)
}

publish {
    userOrg = RELEASE_USER
    groupId = RELEASE_GROUP
    artifactId = RELEASE_ARTIFACT
    publishVersion = VERSION_SUPPORT
    desc = RELEASE_DESC
    website = RELEASE_WEBSITE
}
