const val SDK_MIN = 14
const val SDK_TARGET = 30

const val RELEASE_GROUP = "com.hendraanggrian.material"
const val RELEASE_ARTIFACT = "collapsingtoolbarlayout-subtitle"
const val RELEASE_VERSION = "$VERSION_ANDROIDX-SNAPSHOT"
const val RELEASE_DESCRIPTION = "Standard CollapsingToolbarLayout with subtitle support"
const val RELEASE_URL = "https://github.com/hendraanggrian/$RELEASE_ARTIFACT"

fun getGithubRemoteUrl(artifact: String = RELEASE_ARTIFACT) =
    `java.net`.URL("$RELEASE_URL/tree/main/$artifact/src")
