private const val VERSION_GIT_PUBLISH = "2.1.1"

fun Dependencies.gitPublish() =
    "org.ajoberstar:gradle-git-publish:$VERSION_GIT_PUBLISH"

val Plugins.`git-publish`
    get() = id("org.ajoberstar.git-publish")
