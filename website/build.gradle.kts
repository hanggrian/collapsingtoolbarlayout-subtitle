plugins {
    alias(plugs.plugins.pages)
    alias(plugs.plugins.git.publish)
}

pages {
    contents.index(rootDir.resolve("README.md"))
    styles.add("https://cdnjs.cloudflare.com/ajax/libs/prism/1.28.0/themes/prism-tomorrow.min.css")
    scripts.addAll(
        "https://cdnjs.cloudflare.com/ajax/libs/prism/1.28.0/prism.min.js",
        "https://cdnjs.cloudflare.com/ajax/libs/prism/1.28.0/components/prism-groovy.min.js"
    )
    cayman {
        darkTheme()
        authorName = DEVELOPER_NAME
        authorUrl = DEVELOPER_URL
        projectName = RELEASE_ARTIFACT
        projectDescription = RELEASE_DESCRIPTION
        projectUrl = RELEASE_URL
    }
}

gitPublish {
    repoUri.set("git@github.com:$DEVELOPER_ID/$RELEASE_ARTIFACT.git")
    branch.set("gh-pages")
    contents.from(pages.outputDirectory)
}

tasks {
    register(LifecycleBasePlugin.CLEAN_TASK_NAME) {
        delete(buildDir)
    }
}
