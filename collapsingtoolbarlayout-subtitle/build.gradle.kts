import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.novoda.bintray-release")
}

android {
    compileSdkVersion(targetSdk)
    buildToolsVersion(buildTools)
    defaultConfig {
        minSdkVersion(minSdk)
        targetSdkVersion(targetSdk)
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
}

dependencies {
    compile(kotlin("stdlib", kotlinVersion))
    compile(support("design", supportVersion))

    testImplementation(junit(junitVersion))
    testImplementation(google("truth", truthVersion))

    androidTestImplementation(google("truth", truthVersion))
    androidTestImplementation(hendraanggrian("errorbar", errorbarVersion))
    androidTestImplementation(support("runner", runnerVersion, "test"))
    androidTestImplementation(support("espresso-core", espressoVersion, "test", "espresso"))
}

publish {
    userOrg = bintrayUser
    groupId = bintrayGroup
    artifactId = bintrayArtifact
    publishVersion = supportVersion
    desc = bintrayDesc
    website = bintrayWeb
}