import com.vanniktech.maven.publish.AndroidLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    alias(plugs.plugins.spotless)
    alias(plugs.plugins.mvn.publish)
}

android {
    buildTypes {
        debug {
            isTestCoverageEnabled = true
        }
    }
    buildFeatures {
        buildConfig = false
    }
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
    androidTestImplementation(testLibs.bundles.androidx)
}
