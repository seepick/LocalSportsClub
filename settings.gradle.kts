pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
@Suppress("UnstableApiUsage") dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        maven("https://jitpack.io")
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "LocalSportsClub"

include(
    "repo",
    "domain", // TODO delete me
    "domain-model",
    "domain-repo",
    "domain-logic",
    "view",
    // "view-common" (domain independent)
    // "view-presentation" (@Component)
    // "view-logic" (view-models, controllers)
)
