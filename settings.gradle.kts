pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()          // necesario para kotlin.plugin.compose
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

rootProject.name = "TibiBalance"
include(":app")
include(":core")
include(":data")
include(":domain")
