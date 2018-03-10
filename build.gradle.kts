buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(android())
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(dokka())
        classpath(gitPublish())
        classpath(bintrayRelease())
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks {
    "clean"(Delete::class) {
        delete(buildDir)
    }
    "wrapper"(Wrapper::class) {
        gradleVersion = "4.4"
    }
}

/** bintray upload snippet
./gradlew bintrayUpload -PbintrayUser=hendraanggrian -PdryRun=false -PbintrayKey=
 */
