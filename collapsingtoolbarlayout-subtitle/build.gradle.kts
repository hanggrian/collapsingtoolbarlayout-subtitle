import com.vanniktech.maven.publish.AndroidLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    jacoco
    alias(plugs.plugins.spotless)
    alias(plugs.plugins.maven.publish)
}

android {
    buildFeatures.buildConfig = false
    testOptions.unitTests.isIncludeAndroidResources = true
}

spotless.java {
    target("src/main/java/**/*.java")
    googleJavaFormat()
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    pom {
        name.set(project.name)
        description.set(RELEASE_DESCRIPTION)
        url.set(RELEASE_URL)
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        scm {
            connection.set("scm:git:https://github.com/$DEVELOPER_ID/$RELEASE_ARTIFACT.git")
            developerConnection.set("scm:git:ssh://git@github.com/$DEVELOPER_ID/$RELEASE_ARTIFACT.git")
            url.set(RELEASE_URL)
        }
        developers {
            developer {
                id.set(DEVELOPER_ID)
                name.set(DEVELOPER_NAME)
                url.set(DEVELOPER_URL)
            }
        }
    }
    configure(AndroidLibrary(javadocJar = JavadocJar.Javadoc()))
}

dependencies {
    implementation(libs.material)
    testImplementation(testLibs.androidx.core)
    testImplementation(testLibs.androidx.runner)
    testImplementation(testLibs.androidx.junit)
    testImplementation(testLibs.robolectric)
    testImplementation(testLibs.truth)
}

tasks {
    withType<Test> {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
    register<JacocoReport>("jacocoTestReport") {
        dependsOn("testDebugUnitTest")
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        val fileFilter = listOf(
            "**/R.class", "**/R\$*.class", "**/BuildConfig.*",
            "**/Manifest*.*", "**/*Test*.*", "android/**/*.*"
        )
        val debugTree = fileTree("dir" to "$buildDir/intermediates/javac/debug", "excludes" to fileFilter)
        val mainSrc = "$projectDir/src/main/java"
        sourceDirectories.setFrom(mainSrc)
        classDirectories.setFrom(debugTree)
        executionData.setFrom(
            fileTree(
                "dir" to buildDir,
                "includes" to listOf("jacoco/testDebugUnitTest.exec")
            )
        )
    }
}
