val releaseGroup: String by project
val releaseArtifact: String by project

plugins {
    alias(libs.plugins.android.library)
    checkstyle
    jacoco
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "$releaseGroup.${releaseArtifact.replace("-", "")}"
    testNamespace = "$namespace.test"
    buildFeatures.buildConfig = false
    testOptions.unitTests.isIncludeAndroidResources = true

    tasks.register<Javadoc>("javadocAndroid") {
        source = sourceSets["main"].java.getSourceFiles()
        classpath += files(bootClasspath)
        classpath +=
            libraryVariants
                .find { it.name == "release" }!!
                .javaCompileProvider
                .get()
                .classpath
        setDestinationDir(layout.buildDirectory.dir("docs/${project.name}").get().asFile)
    }
}

dependencies {
    checkstyle(libs.rulebook.checkstyle)

    implementation(libs.material)

    testImplementation(libs.bundles.androidx.test)
}

tasks {
    val checkstyleAndroid by registering(Checkstyle::class) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Generate Android lint report"

        source("src")
        include("**/*.java")
        exclude("**/gen/**", "**/R.java")
        classpath = files()
    }
    named("check") {
        dependsOn(checkstyleAndroid)
    }

    withType<Test>().configureEach {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
    register<JacocoReport>("jacocoAndroid") {
        group = "Reporting"
        description = "Generate Android test coverage"

        dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")
        mustRunAfter("test")
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        sourceDirectories.setFrom(layout.projectDirectory.dir("src/main/java"))
        classDirectories.setFrom(
            files(
                fileTree(layout.buildDirectory.dir("intermediates/javac/")) {
                    exclude(
                        "**/R.class",
                        "**/R\$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*",
                        "**/*Args.*",
                        "**/*Directions.*",
                    )
                },
            ),
        )
        executionData.setFrom(
            files(
                fileTree(layout.buildDirectory) {
                    include("**/*.exec", "**/*.ec")
                }
            ),
        )
    }
}
